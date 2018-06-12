package uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.task;

import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.assembleEnvelopeWithPayloadAndMetaDetails;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.FILE_SERVICE_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.USER_ID;

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
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(Component.EVENT_PROCESSOR)
@Named
public class MaterialUpload implements JavaDelegate {
    public static final String
            MATERIAL_COMMAND_UPLOAD_FILE = "material.command.upload-file";
    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Handles("material.command.upload-file-dummy")
    public void doesNothing(final JsonEnvelope jsonEnvelope) {
        // required by framework
    }

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        
        final UUID userId = execution.getVariable(USER_ID,UUID.class);
        final UUID fieldId = execution.getVariable(FILE_SERVICE_ID,UUID.class);
        final String materialId = execution.getVariable(MATERIAL_ID, String.class);

        final JsonObject payload = Json.createObjectBuilder().add(MATERIAL_ID, materialId)
                        .add(FILE_SERVICE_ID, fieldId.toString()).build();

        final JsonEnvelope postRequestEnvelope = assembleEnvelopeWithPayloadAndMetaDetails(payload,MATERIAL_COMMAND_UPLOAD_FILE,fieldId.toString(),userId.toString());

        sender.send(postRequestEnvelope);

    }

}
