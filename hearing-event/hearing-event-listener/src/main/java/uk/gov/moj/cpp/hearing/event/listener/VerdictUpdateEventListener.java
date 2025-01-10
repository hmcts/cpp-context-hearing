package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S00112")
@ServiceComponent(EVENT_LISTENER)
public class VerdictUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerdictUpdateEventListener.class);

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private VerdictJPAMapper verdictJPAMapper;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Transactional
    @Handles("hearing.hearing-offence-verdict-updated")
    public void verdictUpdate(final JsonEnvelope event) {

        final VerdictUpsert verdictUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), VerdictUpsert.class);

        if(verdictUpdated.getVerdict().getApplicationId() != null){
            aplicationVerdictUpdate(verdictUpdated.getHearingId(), verdictUpdated.getVerdict());
        }else {
            final Hearing hearing = hearingRepository.findBy(verdictUpdated.getHearingId());

            final uk.gov.justice.core.courts.Verdict verdictPojo = verdictUpdated.getVerdict();

            final Offence offence = ofNullable(hearing.getProsecutionCases()).orElse(emptySet()).stream()
                    .flatMap(lc -> lc.getDefendants().stream())
                    .flatMap(d -> d.getOffences().stream())
                    .filter(o -> o.getId().getId().equals(verdictPojo.getOffenceId()))
                    .findFirst()
                    .orElse(null);

            if(nonNull(offence)){
                if(nonNull(verdictUpdated.getVerdict().getIsDeleted()) && verdictUpdated.getVerdict().getIsDeleted()) {
                    offence.setVerdict(null);
                } else{
                    final Verdict verdict = verdictJPAMapper.toJPA(verdictPojo);
                    offence.setVerdict(verdict);
                }
                hearingRepository.save(hearing);
            }else{
                final Hearing hearingEntity = hearingRepository.findBy(verdictUpdated.getHearingId());
                final String updatedCourtApplicationJson = hearingJPAMapper.updateVerdictOnOffencesInCourtApplication(hearingEntity.getCourtApplicationsJson(), verdictUpdated.getVerdict());
                if(updatedCourtApplicationJson == null){
                    throw new RuntimeException("Invalid offence id. Offence id is not found on hearing: " + verdictPojo.getOffenceId())  ;
                }
                hearingEntity.setCourtApplicationsJson(updatedCourtApplicationJson);
                hearingRepository.save(hearingEntity);
            }
        }
    }

    private void aplicationVerdictUpdate(final UUID hearingId, final uk.gov.justice.core.courts.Verdict verdict) {
        final Hearing hearingEntity = hearingRepository.findBy(hearingId);

        final uk.gov.justice.core.courts.Hearing hearing = hearingJPAMapper.fromJPA(hearingEntity);

        final Optional<CourtApplication> courtApplication = hearing.getCourtApplications().stream()
                .filter( ca -> ca.getId().equals(verdict.getApplicationId()))
                .findFirst();

        if(courtApplication.isPresent()) {
            if(nonNull(verdict.getIsDeleted()) && verdict.getIsDeleted()) {
                courtApplication.get().setVerdict(null);
            } else {
                courtApplication.get().setVerdict(verdict);
            }

            final String updatedCourtApplications = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), courtApplication.get());
            hearingEntity.setCourtApplicationsJson(updatedCourtApplications);
            hearingRepository.save(hearingEntity);
        }else{
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("hearing.hearing-court-application-verdict-updated event application not found {}", verdict.getApplicationId());
            }
        }
    }
}
