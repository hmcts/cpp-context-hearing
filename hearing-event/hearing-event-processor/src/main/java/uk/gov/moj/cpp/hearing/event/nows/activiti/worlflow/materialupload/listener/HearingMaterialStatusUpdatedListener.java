package uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.listener;

import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.activiti.service.ActivitiService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(Component.EVENT_PROCESSOR)
public class HearingMaterialStatusUpdatedListener {

    public static final String PUBLIC_HEARING_EVENTS_NOWS_MATERIAL_STATUS_UPDATED = "public.hearing.events.nows-material-status-updated";
    public static final String RECEIVE_STATUS_UPDATE_CONFIRMATION_HEARING = "receiveStatusUpdateConfirmationHearing";

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingMaterialStatusUpdatedListener.class.getName());

    @Inject
    private ActivitiService activitiService;

    @Handles(PUBLIC_HEARING_EVENTS_NOWS_MATERIAL_STATUS_UPDATED)
    public void processEvent(final JsonEnvelope jsonEnvelope) {
        LOGGER.info("Received public.hearing.events.nows-material-status-updated {}",
                jsonEnvelope.payloadAsJsonObject());
        if (jsonEnvelope.payloadAsJsonObject().containsKey(MATERIAL_ID)) {
            final String materialId = jsonEnvelope.payloadAsJsonObject().getString(MATERIAL_ID);
            activitiService.signalProcessByActivitiIdAndFieldName(RECEIVE_STATUS_UPDATE_CONFIRMATION_HEARING, MATERIAL_ID, materialId);
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Event Received without materialId : metadata {} payload {}", jsonEnvelope.metadata(), jsonEnvelope.toObfuscatedDebugString());
            }
        }
    }
}
