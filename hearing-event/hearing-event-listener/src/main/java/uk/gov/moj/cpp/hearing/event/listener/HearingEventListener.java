package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.mapping.ApplicationDraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private TargetJPAMapper targetJPAMapper;

    @Inject
    private ApplicationDraftResultJPAMapper applicationDraftResultJPAMapper;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Handles("hearing.draft-result-saved")
    public void draftResultSaved(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-saved event received {}", event.toObfuscatedDebugString());
        }
        final DraftResultSaved draftResultSaved = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSaved.class);

        final Target targetIn = draftResultSaved.getTarget();

        final Hearing hearing = this.hearingRepository.findBy(draftResultSaved.getTarget().getHearingId());
        if (hearing.getHasSharedResults()) {
            hearing.setHasSharedResults(false);
        }
        hearing.getTargets().stream()
                .filter(t -> t.getId().equals(targetIn.getTargetId()))
                .findFirst()
                .ifPresent(previousTarget -> hearing.getTargets().remove(previousTarget));

        hearing.getTargets().add(targetJPAMapper.toJPA(hearing, targetIn));

        hearingRepository.save(hearing);
    }

    @Handles("hearing.application-draft-resulted")
    public void applicationDraftResulted(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.application-draft-resulted event received {}", event.toObfuscatedDebugString());
        }
        final ApplicationDraftResulted applicationDraftResulted = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ApplicationDraftResulted.class);

        final Hearing hearing = this.hearingRepository.findBy(applicationDraftResulted.getHearingId());
        // if share result of application and hearing are same then need to set  hearing.setHasSharedResults(false);
        hearing.getApplicationDraftResults().stream()
                .filter(adr -> adr.getId().equals(applicationDraftResulted.getTargetId()))
                .findFirst()
                .ifPresent(previousADR -> hearing.getApplicationDraftResults().remove(previousADR));

        hearing.getApplicationDraftResults().add(applicationDraftResultJPAMapper.toJPA(hearing,
                applicationDraftResulted.getTargetId(),
                applicationDraftResulted.getApplicationId(),
                applicationDraftResulted.getDraftResult()));

        if (applicationDraftResulted.getApplicationOutcomeType() != null) {
            final String courtApplicationsJson = hearingJPAMapper.saveApplicationOutcome(hearing.getCourtApplicationsJson(), getApplicationOutcome(applicationDraftResulted));
            hearing.setCourtApplicationsJson(courtApplicationsJson);
        }

        hearingRepository.save(hearing);
    }

    private CourtApplicationOutcome getApplicationOutcome(final ApplicationDraftResulted applicationDraftResulted) {
        return CourtApplicationOutcome.courtApplicationOutcome()
                .withApplicationId(applicationDraftResulted.getApplicationId())
                .withOriginatingHearingId(applicationDraftResulted.getHearingId())
                .withApplicationOutcomeType(applicationDraftResulted.getApplicationOutcomeType())
                .withApplicationOutcomeDate(applicationDraftResulted.getApplicationOutcomeDate())
                .build();
    }

    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.results-shared event received {}", event.toObfuscatedDebugString());
        }

        final ResultsShared resultsShared = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        final Hearing hearing = hearingRepository.findBy(resultsShared.getHearing().getId());
        if (hearing == null) {
            LOGGER.error("Hearing not found");
        } else {
            if (resultsShared.getHearing().getHasSharedResults()) {
                hearing.setHasSharedResults(true);
                hearingRepository.save(hearing);
            }

        }

    }
}
