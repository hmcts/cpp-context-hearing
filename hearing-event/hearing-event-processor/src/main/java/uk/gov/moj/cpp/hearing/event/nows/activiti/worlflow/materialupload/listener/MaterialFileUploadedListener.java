package uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.listener;

import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.activiti.service.ActivitiService;

@ServiceComponent(Component.EVENT_PROCESSOR)
public class MaterialFileUploadedListener {

    public static final String RECEIVE_MATERIAL_UPLOADED_CONFIRMATION = "receiveMaterialUploadedConfirmation";
    public static final String MATERIAL_EVENTS_FILE_UPLOADED = "material.material-added";

    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialFileUploadedListener.class.getName());

    @Inject
    private ActivitiService activitiService;

    @Handles(MATERIAL_EVENTS_FILE_UPLOADED)
    public void processEvent(final JsonEnvelope jsonEnvelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received material.material-added {}", jsonEnvelope.toObfuscatedDebugString());
        }
        if (jsonEnvelope.payloadAsJsonObject().containsKey(MATERIAL_ID)) {
            final String materialId = jsonEnvelope.payloadAsJsonObject().getString(MATERIAL_ID);
            activitiService.signalProcessByActivitiIdAndFieldName(RECEIVE_MATERIAL_UPLOADED_CONFIRMATION, MATERIAL_ID, materialId);
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Event Received without materialId : metadata {} payload {}", jsonEnvelope.metadata(), jsonEnvelope.toObfuscatedDebugString());
            }
        }
    }

}
