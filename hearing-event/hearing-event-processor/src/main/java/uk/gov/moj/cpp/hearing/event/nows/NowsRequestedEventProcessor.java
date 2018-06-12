package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.event.nows.service.NowsTemplateRegistrationService.TEMPLATE_CONTEXT;
import static uk.gov.moj.cpp.hearing.event.nows.service.NowsTemplateRegistrationService.TEMPLATE_IDENTIFIER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.FileStorer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.domain.NowsOrder;
import uk.gov.moj.cpp.hearing.event.nows.service.DocmosisService;
import uk.gov.moj.cpp.hearing.event.nows.service.UploadMaterialService;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.FileUploadException;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class NowsRequestedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsRequestedEventProcessor.class);


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
        this.uploadMaterialService= uploadMaterialService;
    }



    @Handles("hearing.events.nows-requested")
    public void processNowsRequested(final JsonEnvelope event) throws FileServiceException {
        final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NowsRequested.class);
        final String hearingId = nowsRequested.getHearing().getId();
        LOGGER.info("Nows requested for hearing id {}", hearingId);
        final List<NowsOrder> nowsOrders = NowsRequestedToOrderConvertor.convert(nowsRequested);
        nowsOrders.forEach(nowsOrder -> {
            final byte[] resultOrderAsByteArray = docmosisService.generateDocument(objectToJsonObjectConverter.convert(nowsOrder), TEMPLATE_CONTEXT, TEMPLATE_IDENTIFIER);
            final String filename = String.format("%s_%s.pdf", nowsOrder.getOrderName(), ZonedDateTime.now().format(TIMESTAMP_FORMATTER));
            addDocumentToMaterial(filename, new ByteArrayInputStream(resultOrderAsByteArray),
                            event.metadata().userId(), hearingId, nowsOrder.getMaterialIds());
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
                    final Optional<String> userId, final String hearingId,
                    final List<UUID> materialIds) {
        final JsonObject metadata = createObjectBuilder().add("fileName", filename).build();
        materialIds.stream().forEach(materialId -> {
            try {
                final UUID fileId = fileStorer.store(metadata, fileContent);
                uploadMaterialService.uploadFile(fromString(userId.get()), fromString(hearingId),materialId,fileId);

            } catch (final FileServiceException e) {
                LOGGER.error("Error while uploading file {}", filename);
                throw new FileUploadException(e);
            }
        });
    }

}
