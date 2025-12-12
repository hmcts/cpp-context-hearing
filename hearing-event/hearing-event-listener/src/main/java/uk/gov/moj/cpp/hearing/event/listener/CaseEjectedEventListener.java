package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseEjected;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class CaseEjectedEventListener {
    private static final String CASE_STATUS_EJECTED = "EJECTED";
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseEjectedEventListener.class);
    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Handles("hearing.case-ejected")
    public void caseEjected(final JsonEnvelope event) {
        final CaseEjected caseEjectedEvent = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CaseEjected.class);
        LOGGER.info("Received event {}", event.toObfuscatedDebugString());
        final UUID prosecutionCaseId = caseEjectedEvent.getProsecutionCaseId();
        final List<UUID> hearingIds = caseEjectedEvent.getHearingIds();
        hearingIds.stream().forEach(hearingId -> {
            final Hearing hearingEntity = hearingRepository.findBy(hearingId);
            if (isNull(hearingEntity)) {
                LOGGER.info("Hearing not found for hearing id {} . Case with id {} does not exists ", hearingId, prosecutionCaseId);
            } else {

                hearingEntity.getProsecutionCases().stream().filter(pc -> pc.getId().getId().equals(prosecutionCaseId)).findFirst().ifPresent(persistentCase ->
                        persistentCase.setCaseStatus(CASE_STATUS_EJECTED));
                if(isNotBlank(hearingEntity.getCourtApplicationsJson())){
                    final String courtApplicationsJson = hearingJPAMapper.updateLinkedApplicationStatus(hearingEntity.getCourtApplicationsJson(),
                            prosecutionCaseId, ApplicationStatus.EJECTED);
                    hearingEntity.setCourtApplicationsJson(courtApplicationsJson);
                }
                hearingRepository.save(hearingEntity);
            }
        });

    }

}
