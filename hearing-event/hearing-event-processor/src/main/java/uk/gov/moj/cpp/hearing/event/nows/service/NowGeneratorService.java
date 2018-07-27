package uk.gov.moj.cpp.hearing.event.nows.service;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.assembleEnvelopeWithPayloadAndMetaDetails;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.HEARING_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.FileStorer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.NowsNotificationDocumentState;
import uk.gov.moj.cpp.hearing.event.nows.NowsTemplateNameNotFoundException;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.FileUploadException;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.system.documentgenerator.client.DocumentGeneratorClientProducer;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class NowGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowGeneratorService.class);

    private static final String FAILED = "FAILED";
    public static final String HEARING_UPDATE_NOWS_MATERIAL_STATUS = "hearing.command.update-nows-material-status";
    public static final String RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS = "resultinghmps.update-nows-material-status";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private DocumentGeneratorClientProducer documentGeneratorClientProducer;

    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private FileStorer fileStorer;

    private UploadMaterialService uploadMaterialService;

    @Inject
    public NowGeneratorService(final DocumentGeneratorClientProducer documentGeneratorClientProducer,
                               final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                               final FileStorer fileStorer,
                               final UploadMaterialService uploadMaterialService
    ) {
        this.documentGeneratorClientProducer = documentGeneratorClientProducer;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.fileStorer = fileStorer;
        this.uploadMaterialService = uploadMaterialService;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void generateNow(Sender sender, UUID userId, NowsRequested nowsRequested, String hearingId, Map<NowsDocumentOrder, NowsNotificationDocumentState> nowsDocumentOrderToNotificationState, NowsDocumentOrder nowsDocumentOrder) {
        try {
            final NowsNotificationDocumentState nowsNotificationDocumentState = nowsDocumentOrderToNotificationState.get(nowsDocumentOrder);
            final String templateName = getTemplateName(nowsRequested, nowsNotificationDocumentState);
            final byte[] resultOrderAsByteArray = documentGeneratorClientProducer.documentGeneratorClient().generatePdfDocument(objectToJsonObjectConverter.convert(nowsDocumentOrder), templateName, userId);
            final String filename = String.format("%s_%s.pdf", nowsDocumentOrder.getOrderName(), ZonedDateTime.now().format(TIMESTAMP_FORMATTER));
            addDocumentToMaterial(filename, new ByteArrayInputStream(resultOrderAsByteArray),
                    userId, hearingId, fromString(nowsDocumentOrder.getMaterialId()), nowsNotificationDocumentState);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Error while uploading document generation or upload ", e);
            updateStatus(sender, hearingId, nowsDocumentOrder.getMaterialId(), userId.toString(), FAILED, HEARING_UPDATE_NOWS_MATERIAL_STATUS);
            updateStatus(sender, hearingId, nowsDocumentOrder.getMaterialId(), userId.toString(), FAILED, RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS);
        }
    }

    private String getTemplateName(NowsRequested nowsRequested, NowsNotificationDocumentState nowsNotificationDocumentState) {
        final UUID nowsTypeId = nowsNotificationDocumentState.getNowsTypeId();
        return nowsRequested.getHearing().getNowTypes().stream()
                .filter(nt -> nowsTypeId.toString().equals(nt.getId()))
                .findFirst()
                .map(nt -> nt.getTemplateName()).orElseThrow(() -> new NowsTemplateNameNotFoundException(String.format("Could not find templateName for nowsTypeId: %s", nowsTypeId)));
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


    private void updateStatus(Sender sender, String hearingId, String materialId, String userId, String status, String commandName) {
        final JsonObject payload = Json.createObjectBuilder()
                .add(HEARING_ID, hearingId)
                .add(MATERIAL_ID, materialId)
                .add("status", status).build();

        final JsonEnvelope postRequestEnvelope = assembleEnvelopeWithPayloadAndMetaDetails(payload,
                commandName, materialId, userId);

        sender.send(postRequestEnvelope);
    }
}
