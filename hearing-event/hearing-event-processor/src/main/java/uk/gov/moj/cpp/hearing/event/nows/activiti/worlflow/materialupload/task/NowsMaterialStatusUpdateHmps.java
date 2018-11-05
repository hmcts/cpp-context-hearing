package uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.task;

import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.assembleEnvelopeWithPayloadAndMetaDetails;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;

@ServiceComponent(Component.EVENT_PROCESSOR)
@Named
public class NowsMaterialStatusUpdateHmps implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsMaterialStatusUpdateHmps.class);

    public static final String RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS = "resultinghmps.update-nows-material-status";
    public static final String generated = "generated";
    @Inject
    private Sender sender;

    @Handles("resultinghmps.update-nows-material-status-dummy")
    public void doesNothing(final JsonEnvelope jsonEnvelope) {
        // required by framework
    }

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        
        final UUID userId = execution.getVariable(ProcessMapConstant.USER_ID,UUID.class);
        final UUID hearingId = execution.getVariable(ProcessMapConstant.HEARING_ID,UUID.class);
        final String materialId =
                        execution.getVariable(ProcessMapConstant.MATERIAL_ID, String.class);

        LOGGER.info("updating resulting hmps status of material upload {}", materialId);

        final JsonObject payload= Json.createObjectBuilder()
                .add("hearingId",hearingId.toString())
                        .add("materialId", materialId)
                .add("status", generated).build();


        final JsonEnvelope postRequestEnvelope = assembleEnvelopeWithPayloadAndMetaDetails(payload,
                        RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS, materialId, userId.toString());

        sender.send(postRequestEnvelope);
    }

}
