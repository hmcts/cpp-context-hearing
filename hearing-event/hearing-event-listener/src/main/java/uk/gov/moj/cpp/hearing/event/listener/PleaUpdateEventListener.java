package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.IndicatedPleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class PleaUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PleaUpdateEventListener.class);

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private PleaJPAMapper pleaJpaMapper;

    @Inject
    private IndicatedPleaJPAMapper indicatedPleaJPAMapper;

    @Inject
    private AllocationDecisionJPAMapper allocationDecisionJPAMapper;

    @Transactional
    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelope) {

        final PleaUpsert event = convertToObject(envelope);
        LOGGER.debug("hearing.hearing-offence-plea-updated event received for hearingId {} with offenceID {}", event.getHearingId(), event.getPleaModel().getOffenceId());

        if(event.getPleaModel().getApplicationId() != null){
            courtApplicationPleaUpdated(event.getHearingId(), event.getPleaModel().getPlea());
        }else {
            final UUID offenceId = event.getPleaModel().getOffenceId();
            final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceId, event.getHearingId()));

            if (nonNull(offence)) {
                updateOffence(event, offence);
            }else {
                updateOffenceUnderCourtApplication(event);
            }
        }
    }

    @Transactional
    @Handles("hearing.event.indicated-plea-updated")
    public void indicatedPleaUpdated(final JsonEnvelope envelop) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.indicated-plea-updated event received {}", envelop.toObfuscatedDebugString());
        }

        final IndicatedPleaUpdated event = jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), IndicatedPleaUpdated.class);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getIndicatedPlea().getOffenceId(), event.getHearingId()));

        if (nonNull(offence)) {

            final boolean shouldSetPlea = isNull(offence.getIndicatedPlea());

            if (shouldSetPlea) {
                offence.setIndicatedPlea(indicatedPleaJPAMapper.toJPA(event.getIndicatedPlea()));
                offenceRepository.save(offence);
            }
        }
    }

    private void updateOffence(final PleaUpsert event, final Offence offence) {
        if (Objects.nonNull(event.getPleaModel().getPlea())) {
            offence.setPlea(pleaJpaMapper.toJPA(event.getPleaModel().getPlea()));
        } else {
            offence.setPlea(null);
        }
        if (Objects.nonNull(event.getPleaModel().getAllocationDecision())) {
            offence.setAllocationDecision(allocationDecisionJPAMapper.toJPA(event.getPleaModel().getAllocationDecision()));
        } else {
            offence.setAllocationDecision(null);
        }
        if (Objects.nonNull(event.getPleaModel().getIndicatedPlea())) {
            offence.setIndicatedPlea(indicatedPleaJPAMapper.toJPA(event.getPleaModel().getIndicatedPlea()));
        } else {
            offence.setIndicatedPlea(null);
        }
        offenceRepository.save(offence);
    }

    private void updateOffenceUnderCourtApplication(final PleaUpsert event) {
        final Hearing hearingEntity = hearingRepository.findBy(event.getHearingId());
        final String updatedCourtApplicationJson = hearingJPAMapper.updatePleaOnOffencesInCourtApplication(hearingEntity.getCourtApplicationsJson(), event.getPleaModel());
        hearingEntity.setCourtApplicationsJson(updatedCourtApplicationJson);
        hearingRepository.save(hearingEntity);
    }

    private void courtApplicationPleaUpdated(final UUID hearingId, Plea plea) {
        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        final uk.gov.justice.core.courts.Hearing hearing = hearingJPAMapper.fromJPA(hearingEntity);

        final Optional<CourtApplication> courtApplication = hearing.getCourtApplications().stream()
                .filter( ca -> ca.getId().equals(plea.getApplicationId()))
                .findFirst();

        if(courtApplication.isPresent()) {
            courtApplication.get().setPlea(plea);
            final String updatedCourtApplications = hearingJPAMapper.addOrUpdateCourtApplication(hearingEntity.getCourtApplicationsJson(), courtApplication.get());
            hearingEntity.setCourtApplicationsJson(updatedCourtApplications);
            hearingRepository.save(hearingEntity);
        }else{
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("hearing.hearing-court-application-plea-updated event application not found {}", plea.getApplicationId());
            }
        }
    }

    private PleaUpsert convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), PleaUpsert.class);
    }


}
