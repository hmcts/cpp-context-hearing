package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.assembleEnvelopeWithPayloadAndMetaDetails;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.HEARING_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;
import static uk.gov.moj.cpp.hearing.event.nows.service.NowsTemplateRegistrationService.TEMPLATE_CONTEXT;
import static uk.gov.moj.cpp.hearing.event.nows.service.NowsTemplateRegistrationService.TEMPLATE_IDENTIFIER;

import uk.gov.justice.services.common.converter.JSONObjectValueObfuscator;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.FileStorer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.service.DocmosisService;
import uk.gov.moj.cpp.hearing.event.nows.service.UploadMaterialService;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.FileUploadException;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class NowsRequestedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsRequestedEventProcessor.class);

    private static final String FAILED = "FAILED";
    public static final String HEARING_UPDATE_NOWS_MATERIAL_STATUS = "hearing.command.update-nows-material-status";
    public static final String RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS = "resultinghmps.update-nows-material-status";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Enveloper enveloper;
    private final Sender sender;
    private final DocmosisService docmosisService;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;
    private final FileStorer fileStorer;
    private final UploadMaterialService uploadMaterialService;

    @Inject
    public NowsRequestedEventProcessor(final Enveloper enveloper, final Sender sender, final DocmosisService docmosisService,
                                       final JsonObjectToObjectConverter jsonObjectToObjectConverter, final ObjectToJsonObjectConverter objectToJsonObjectConverter, final FileStorer fileStorer, final UploadMaterialService uploadMaterialService) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.docmosisService = docmosisService;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.fileStorer = fileStorer;
        this.uploadMaterialService = uploadMaterialService;
    }


    @Handles("hearing.events.nows-requested")
    public void processNowsRequested(final JsonEnvelope event) throws FileServiceException {
        UUID userId = fromString(event.metadata().userId().orElseThrow(() -> new RuntimeException("UserId missing from event.")));

        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NowsRequested.class);
        final String hearingId = nowsRequested.getHearing().getId();
        LOGGER.info("Nows requested for hearing id {}", hearingId);

        final Map<NowsDocumentOrder, NowsNotificationDocumentState> nowsDocumentOrderToNotificationState = NowsRequestedToOrderConvertor.convert(nowsRequested);
        final List<NowsDocumentOrder> nowsDocumentOrdersList = new ArrayList<>(nowsDocumentOrderToNotificationState.keySet());
        nowsDocumentOrdersList.stream().sorted(Comparator.comparing(NowsDocumentOrder::getPriority)).forEach(nowsDocumentOrder -> {
            LOGGER.info("Input for docmosis order {}", JSONObjectValueObfuscator.obfuscated(objectToJsonObjectConverter.convert(nowsDocumentOrder)));

            try {
                final byte[] resultOrderAsByteArray = docmosisService.generateDocument(objectToJsonObjectConverter.convert(nowsDocumentOrder), TEMPLATE_CONTEXT, TEMPLATE_IDENTIFIER);
                final String filename = String.format("%s_%s.pdf", nowsDocumentOrder.getOrderName(), ZonedDateTime.now().format(TIMESTAMP_FORMATTER));
                final NowsNotificationDocumentState nowsNotificationDocumentState = nowsDocumentOrderToNotificationState.get(nowsDocumentOrder);
                addDocumentToMaterial(filename, new ByteArrayInputStream(resultOrderAsByteArray),
                        userId, hearingId, fromString(nowsDocumentOrder.getMaterialId()), nowsNotificationDocumentState);
            } catch (RuntimeException e) {
                LOGGER.error("Error while uploading document generation or upload ", e);
                updateStatus(hearingId, nowsDocumentOrder.getMaterialId(), userId.toString(), FAILED, HEARING_UPDATE_NOWS_MATERIAL_STATUS);
                updateStatus(hearingId, nowsDocumentOrder.getMaterialId(), userId.toString(), FAILED, RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS);
            }
        });

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.events.nows-requested")
                .apply(this.objectToJsonObjectConverter.convert(nowsRequested)));
    }

    @Handles("hearing.events.nows-material-status-updated")
    public void propagateNowsMaterialStatusUpdated(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "public.hearing.events.nows-material-status-updated")
                .apply(createObjectBuilder()
                        .add("materialId", envelope.payloadAsJsonObject().getJsonString("materialId"))
                        .build()
                ));
    }

    private void addDocumentToMaterial(final String filename, final InputStream fileContent,
                                       final UUID userId, final String hearingId,
                                       final UUID materialId, final NowsNotificationDocumentState nowsDocumentOrder) {
        final JsonObject metadata = createObjectBuilder().add("fileName", filename).build();
        try {
            final UUID fileId = fileStorer.store(metadata, fileContent);
            LOGGER.info("Stored material {} in file store {}", materialId, fileId);
            uploadMaterialService.uploadFile(userId, fromString(hearingId), materialId, fileId, nowsDocumentOrder);

        } catch (final FileServiceException e) {
            LOGGER.error("Error while uploading file {}", filename);
            throw new FileUploadException(e);
        }
    }


    private void updateStatus(String hearingId, String materialId, String userId, String status, String commandName) {
        final JsonObject payload = Json.createObjectBuilder()
                .add(HEARING_ID, hearingId)
                .add(MATERIAL_ID, materialId)
                .add("status", status).build();

        final JsonEnvelope postRequestEnvelope = assembleEnvelopeWithPayloadAndMetaDetails(payload,
                commandName, materialId, userId);

        sender.send(postRequestEnvelope);
    }
}

