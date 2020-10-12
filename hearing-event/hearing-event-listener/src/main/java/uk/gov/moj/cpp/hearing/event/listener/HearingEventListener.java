package uk.gov.moj.cpp.hearing.event.listener;

import static java.time.Instant.now;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.mapping.ApplicationDraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.exception.UnmatchedSittingDayException;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingApplicationRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CommentedOutCodeLine", "squid:S1166"})
@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventListener.class.getName());
    private static final String LAST_SHARED_DATE = "lastSharedDate";
    private static final String DIRTY = "dirty";
    private static final String REQUEST_APPROVAL = "requestApproval";
    private static final String LAST_UPDATED_AT = "lastUpdatedAt";
    private static final String RESULTS = "results";
    private static final String CHILD_RESULT_LINES = "childResultLines";

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

    @Inject
    private HearingApplicationRepository hearingApplicationRepository;

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

    @Handles("hearing.target-removed")
    public void targetRemoved(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.target-removed event received {}", event.toObfuscatedDebugString());
        }

        final TargetRemoved targetRemoved = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), TargetRemoved.class);
        final Hearing hearing = this.hearingRepository.findBy(targetRemoved.getHearingId());

        hearing.getTargets().stream()
                .filter(t -> t.getId().equals(targetRemoved.getTargetId()))
                .findFirst()
                .ifPresent(targetToRemove -> {
                            hearing.getTargets().remove(targetToRemove);
                            hearingRepository.save(hearing);
                        }
                );


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
                final List<uk.gov.moj.cpp.hearing.persist.entity.ha.Target> listOfTargets = hearingRepository.findTargetsByHearingId(hearing.getId());
                final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearing.getId());
                final List<Target> targets = targetJPAMapper.fromJPA(Sets.newHashSet(listOfTargets), Sets.newHashSet(listOfProsecutionCases));
                hearing.setHasSharedResults(true);
                hearing.getTargets().clear();
                targets.forEach(targetIn -> updateDraftResult(hearing, targetIn, resultsShared.getSharedTime()));
                hearingRepository.save(hearing);
            }
        }
    }

    @Handles("hearing.hearing-trial-type-set")
    public void setHearingTrialType(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-trial-type-set event received {}", event.toObfuscatedDebugString());
        }

        final HearingTrialType hearingTrialType = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingTrialType.class);

        final Hearing hearing = hearingRepository.findBy(hearingTrialType.getHearingId());
        if (nonNull(hearing)) {
            hearing.setTrialTypeId(hearingTrialType.getTrialTypeId());
            hearing.setIsEffectiveTrial(null);
            hearing.setIsVacatedTrial(false);
            hearing.setvacatedTrialReasonId(null);
            hearingRepository.save(hearing);
        }
    }

    @Handles("hearing.trial-vacated")
    public void setHearingVacateTrialType(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.trial-vacated event received {}", event.toObfuscatedDebugString());
        }

        final HearingTrialVacated hearingTrialType = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingTrialVacated.class);

        final Hearing hearing = hearingRepository.findBy(hearingTrialType.getHearingId());
        if (nonNull(hearing)) {
            hearing.setvacatedTrialReasonId(hearingTrialType.getVacatedTrialReasonId());
            hearing.setIsVacatedTrial(nonNull(hearingTrialType.getVacatedTrialReasonId()));
            hearing.setIsEffectiveTrial(null);
            hearing.setTrialTypeId(null);
            hearingRepository.save(hearing);
        }
    }

    @Handles("hearing.hearing-effective-trial-set")
    public void setHearingEffectiveTrial(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-effective-trial-set event received {}", event.toObfuscatedDebugString());
        }

        final HearingEffectiveTrial hearingEffectiveTrial = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingEffectiveTrial.class);

        final Hearing hearing = hearingRepository.findBy(hearingEffectiveTrial.getHearingId());
        if (nonNull(hearing)) {
            hearing.setIsEffectiveTrial(true);
            hearing.setTrialTypeId(null);
            hearing.setIsVacatedTrial(false);
            hearing.setvacatedTrialReasonId(null);
            hearingRepository.save(hearing);
        }
    }

    @Handles("hearing.events.registered-hearing-against-application")
    public void registerHearingAgainstApplication(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.registered-hearing-against-application event received {}", event.toObfuscatedDebugString());
        }

        final RegisteredHearingAgainstApplication registeredHearingAgainstApplication = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), RegisteredHearingAgainstApplication.class);

        final HearingApplication hearingApplication = new HearingApplication();
        hearingApplication.setId(new HearingApplicationKey(registeredHearingAgainstApplication.getApplicationId(), registeredHearingAgainstApplication.getHearingId()));
        hearingApplicationRepository.save(hearingApplication);
    }

    @Handles("hearing.hearing-days-cancelled")
    public void cancelHearingDays(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-days-cancelled event received {}", event.toObfuscatedDebugString());
        }

        final HearingDaysCancelled hearingDaysCancelled = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), HearingDaysCancelled.class);
        final Hearing hearing = hearingRepository.findBy(hearingDaysCancelled.getHearingId());

        if (nonNull(hearing)) {
            final List<HearingDay> cancelledDayList = hearingDaysCancelled.getHearingDays();
            final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay> existingDaySet = hearing.getHearingDays();

            existingDaySet.forEach(existingDay -> {
                final uk.gov.justice.core.courts.HearingDay cancelledDay =
                        cancelledDayList.stream().filter(source -> source.getSittingDay().toLocalDateTime().equals(existingDay.getSittingDay().toLocalDateTime())).
                                findFirst().orElseThrow(() -> new UnmatchedSittingDayException("No match found for sitting day: " + existingDay.getSittingDay()));
                existingDay.setIsCancelled(cancelledDay.getIsCancelled());
            });
            hearing.setHearingDays(existingDaySet);
            hearingRepository.save(hearing);
        }
    }

    public String enrichDraftResult(final String draftResult, final ZonedDateTime sharedTime) {
        final BiConsumer<JsonNode, ZonedDateTime> consumer = (node, sharedDateTime) -> {
            final ObjectNode child = (ObjectNode) node;
            if (node.has(LAST_SHARED_DATE)) {
                child.remove(LAST_SHARED_DATE);
            }

            if (node.has(DIRTY)) {
                child.remove(DIRTY);
            }

            ((ObjectNode) node).put(LAST_SHARED_DATE, sharedDateTime.toLocalDate().toString());
            ((ObjectNode) node).put(DIRTY, Boolean.FALSE);
        };

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonFactory factory = mapper.getFactory();
            final JsonParser parser = factory.createParser(draftResult);
            final JsonNode actualObj = mapper.readTree(parser);
            final ObjectNode objectnode = (ObjectNode) actualObj;

            if (actualObj.has(REQUEST_APPROVAL)) {
                objectnode.remove(REQUEST_APPROVAL);
            }

            if (actualObj.has(LAST_UPDATED_AT)) {
                objectnode.remove(LAST_UPDATED_AT);
            }

            objectnode.put(REQUEST_APPROVAL, Boolean.FALSE);
            objectnode.put(LAST_UPDATED_AT, now().toEpochMilli());

            final JsonNode arrNode = actualObj.get(RESULTS);
            if (nonNull(arrNode) && arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    consumer.accept(objNode, sharedTime);
                    final JsonNode childResultLineNodes = objNode.get(CHILD_RESULT_LINES);
                    enrichAllChildrenNodesWithLastSharedDate(childResultLineNodes, sharedTime, consumer);
                }
            }
            return actualObj.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private void enrichAllChildrenNodesWithLastSharedDate(final JsonNode childResultLineNodes, final ZonedDateTime sharedTime, final BiConsumer<JsonNode, ZonedDateTime> consumer) {
        if (nonNull(childResultLineNodes) && childResultLineNodes.isArray()) {
            for (final JsonNode node : childResultLineNodes) {
                consumer.accept(node, sharedTime);
                final JsonNode childNodes = node.get(CHILD_RESULT_LINES);
                enrichAllChildrenNodesWithLastSharedDate(childNodes, sharedTime, consumer);
            }
        }
    }

    private void updateDraftResult(Hearing hearing, Target targetIn, ZonedDateTime sharedDateTime) {
        final String draftResult = targetIn.getDraftResult();
        if (isNotBlank(draftResult)) {
            final String updatedDraftResult = enrichDraftResult(draftResult, sharedDateTime);
            if(isNotBlank(updatedDraftResult)) {
                targetIn.setDraftResult(updatedDraftResult);
                hearing.getTargets().add(targetJPAMapper.toJPA(hearing, targetIn));
            }
        }
    }
}
