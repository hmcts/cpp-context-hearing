package uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.task;

import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.assembleEnvelopeWithPayloadAndMetaDetails;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.HEARING_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant;

@ServiceComponent(Component.EVENT_PROCESSOR)
@Named
public class NowsMaterialStatusUpdateHearing implements JavaDelegate {
    public static final String HEARING_UPDATE_NOWS_MATERIAL_STATUS =
                    "hearing.command.update-nows-material-status";
    public static final String GENERATED = "GENERATED";
    @Inject
    private Sender sender;

    @Handles("hearing.update-nows-material-status-dummy")
    public void doesNothing(final JsonEnvelope jsonEnvelope) {
        // required by framework
    }

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        
        final UUID userId = execution.getVariable(ProcessMapConstant.USER_ID,UUID.class);
        final UUID hearingId = execution.getVariable(HEARING_ID,UUID.class);
        final String materialId = execution.getVariable(MATERIAL_ID, String.class);

        final JsonObject payload= Json.createObjectBuilder()
                .add(HEARING_ID,hearingId.toString())
                        .add(MATERIAL_ID, materialId)
                .add("status", GENERATED).build();

        final JsonEnvelope postRequestEnvelope = assembleEnvelopeWithPayloadAndMetaDetails(payload,
                        HEARING_UPDATE_NOWS_MATERIAL_STATUS, materialId, userId.toString());

        sender.send(postRequestEnvelope);

    }

}
