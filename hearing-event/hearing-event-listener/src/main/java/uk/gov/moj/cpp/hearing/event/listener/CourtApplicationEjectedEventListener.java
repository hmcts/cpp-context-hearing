package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationEjected;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class CourtApplicationEjectedEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CourtApplicationEjectedEventListener.class);
    @Inject
    private HearingRepository hearingRepository;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Handles("hearing.court-application-ejected")
    public void courtApplicationEjected(final JsonEnvelope event){
        final CourtApplicationEjected courtApplicationEjected = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CourtApplicationEjected.class);
        LOGGER.info("Received event {}", event.toObfuscatedDebugString());
        final UUID applicationId = courtApplicationEjected.getApplicationId();
        final List<UUID> hearingIds = courtApplicationEjected.getHearingIds();
        hearingIds.stream().forEach(hearingId -> {
            final Hearing hearingEntity = hearingRepository.findBy(hearingId);
            if (isNull(hearingEntity)) {
                LOGGER.info("Hearing not found for hearing id {}. Application with id {} does not exists", hearingId, applicationId);
            } else {
                if(isNotBlank(hearingEntity.getCourtApplicationsJson())){
                    final String courtApplicationsJson = hearingJPAMapper
                            .updateStandaloneApplicationStatus(hearingEntity.getCourtApplicationsJson(),
                                    applicationId, ApplicationStatus.EJECTED);
                    hearingEntity.setCourtApplicationsJson(courtApplicationsJson);
                    hearingRepository.save(hearingEntity);
                }
            }
        });

    }

}
