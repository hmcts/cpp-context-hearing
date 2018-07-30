package uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.listener;

import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.ORIGINATOR_VALUE;
import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.getOriginatorValueFromJsonMetadata;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.activiti.service.ActivitiService;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(Component.EVENT_PROCESSOR)
@SuppressWarnings({"squid:S2142"})
public class MaterialFileUploadedListener {

    public static final String RECEIVE_MATERIAL_UPLOADED_CONFIRMATION = "receiveMaterialUploadedConfirmation";
    public static final String MATERIAL_EVENTS_FILE_UPLOADED = "material.material-added";

    private static final Logger LOGGER = LoggerFactory.getLogger(MaterialFileUploadedListener.class.getName());
    private static final int MAX_RETRY_COUNT = 50;
    @Inject
    private ActivitiService activitiService;

    @Handles(MATERIAL_EVENTS_FILE_UPLOADED)
    public void processEvent(final JsonEnvelope jsonEnvelope) {
        LOGGER.info("Received material.material-added {}", jsonEnvelope.payloadAsJsonObject());
        final Optional<String> originator =
                        getOriginatorValueFromJsonMetadata(jsonEnvelope.metadata().asJsonObject());
        if (originator.isPresent() && ORIGINATOR_VALUE.equalsIgnoreCase(originator.get())) {
            findAndNudgeActivity(jsonEnvelope);
        }
    }

    private void findAndNudgeActivity(final JsonEnvelope jsonEnvelope) {
        if (jsonEnvelope.payloadAsJsonObject().containsKey(MATERIAL_ID)) {
            final String materialId = jsonEnvelope.payloadAsJsonObject().getString(MATERIAL_ID);
        int count=0;
        while (!activitiService.signalProcessByActivitiIdAndFieldName(RECEIVE_MATERIAL_UPLOADED_CONFIRMATION, MATERIAL_ID, materialId) && count < MAX_RETRY_COUNT ){
            count++;
            LOGGER.error("No process Found after {} retries : Step Name {}, BusinessKey {} executionid [] ",
                            count,RECEIVE_MATERIAL_UPLOADED_CONFIRMATION, materialId);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (final InterruptedException e) {
                LOGGER.error("Interrupted ...",e);
            }
        }

        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Event Received without materialId : metadata {} payload {}",
                                jsonEnvelope.metadata(),
                                jsonEnvelope.toObfuscatedDebugString());
            }
        }
    }

}
