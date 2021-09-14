package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.base.Predicates.not;
import static java.lang.Boolean.FALSE;
import static java.time.Instant.now;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateChanged;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancelled;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancelledV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsRejected;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsRejectedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.mapping.ApplicationDraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.exception.UnmatchedSittingDayException;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.repository.ApprovalRequestedRepository;
import uk.gov.moj.cpp.hearing.repository.DraftResultRepository;
import uk.gov.moj.cpp.hearing.repository.HearingApplicationRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CommentedOutCodeLine", "squid:S1166", "squid:S134"})
@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventListener.class.getName());
    private static final String LAST_SHARED_DATE = "lastSharedDate";
    private static final String DIRTY = "dirty";
    private static final String REQUEST_APPROVAL = "requestApproval";
    private static final String LAST_UPDATED_AT = "lastUpdatedAt";
    private static final String RESULTS = "results";
    private static final String CHILD_RESULT_LINES = "childResultLines";
    public static final String HEARING_NOT_FOUND = "Hearing not found";

    @Inject
    private BailStatusProducer bailStatusProducer;

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private ApprovalRequestedRepository approvalRequestedRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;

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

    @Inject
    private DraftResultRepository draftResultRepository;

    @Inject
    private ObjectMapper objectMapper;

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
        hearing.setHearingState(draftResultSaved.getHearingState());
        hearing.setAmendedByUserId(draftResultSaved.getAmendedByUserId());

        saveBailStatusForCTLRemandStatus(targetIn);

        hearingRepository.save(hearing);
    }

    private void saveBailStatusForCTLRemandStatus(final Target targetIn) {

        final String draftResultTarget = targetIn.getDraftResult();
        if (nonNull(draftResultTarget)) {
            final JsonObject parsedTarget = stringToJsonObjectConverter.convert(draftResultTarget);
            if (parsedTarget.containsKey(RESULTS)) {

                final JsonArray results = parsedTarget.getJsonArray(RESULTS);
                if (isAllDeleted(results)) {
                    final Offence existingOffence = getOffence(targetIn);
                    if (existingOffence != null) {
                        unsetBailStatus(existingOffence);
                    }
                    return;
                }

                for (int i = 0; i < results.size(); i++) {
                    extractBailStatus(targetIn, results.getJsonObject(i));
                }
            }
        }
    }

    private boolean isAllDeleted(final JsonArray results) {
        boolean deleted = !results.isEmpty();
        for (int i = 0; i < results.size(); i++) {
            deleted = results.getJsonObject(i).getBoolean("isDeleted",false);
            if (!deleted) {
                break;
            }
        }
        return deleted;
    }

    private void extractBailStatus(final Target targetIn, final JsonObject result) {

        if (nonNull(result) && !result.getBoolean("isDeleted", false)) {

            final String resultCode = result.getString("resultCode", null);
            if (nonNull(resultCode)) {
                final Optional<BailStatus> bailStatusOpt = bailStatusProducer.getBailStatus(fromString(resultCode));

                saveOffenceBailStatus(targetIn, bailStatusOpt);
            }
        }
    }

    private void saveOffenceBailStatus(final Target targetIn, final Optional<BailStatus> bailStatusOpt) {
        if (bailStatusOpt.isPresent()) {

            final Offence offence = getOffence(targetIn);
            if (nonNull(offence)) {
                final BailStatus bailStatus = bailStatusOpt.get();
                if(nonNull(bailStatus)) {
                    offence.setBailStatusId(bailStatus.getId());
                    offence.setBailStatusCode(bailStatus.getCode());
                    offence.setBailStatusDescription(bailStatus.getDescription());
                    offenceRepository.save(offence);
                }
            }
        }
    }

    private Offence getOffence(final Target targetIn) {
        final HearingSnapshotKey hearingSnapshotKey = new HearingSnapshotKey(targetIn.getOffenceId(), targetIn.getHearingId());
        return offenceRepository.findBy(hearingSnapshotKey);
    }

    private void unsetBailStatus(final Offence offence) {
        offence.setBailStatusId(null);
        offence.setBailStatusCode(null);
        offence.setBailStatusDescription(null);
        offenceRepository.save(offence);
    }

    @Handles("hearing.draft-result-saved-v2")
    public void draftResultSavedV2(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-saved-v2 event received {}", event.toObfuscatedDebugString());
        }
        final DraftResultSavedV2 draftResultSavedV2 = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSavedV2.class);

        final String draftResultPK = draftResultSavedV2.getHearingId().toString()+draftResultSavedV2.getHearingDay().toString();
        DraftResult draftResult = draftResultRepository.findBy(draftResultPK);
        if (draftResult == null) {
            draftResult = new DraftResult();
        }
        draftResult.setDraftResultId(draftResultPK);
        draftResult.setHearingId(draftResultSavedV2.getHearingId());
        draftResult.setHearingDay(draftResultSavedV2.getHearingDay().toString());
        draftResult.setDraftResultPayload(objectMapper.valueToTree(draftResultSavedV2.getDraftResult()));
        draftResult.setAmendedByUserId(draftResultSavedV2.getAmendedByUserId());
        draftResultRepository.save(draftResult);
    }

    @Handles("hearing.draft-result-deleted-v2")
    public void draftResultDeletedV2(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.draft-result-deleted-v2 event received {}", event.toObfuscatedDebugString());
        }
        final DraftResultDeletedV2 draftResultSavedV2 = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultDeletedV2.class);
        final String draftResultPK = draftResultSavedV2.getHearingId()+draftResultSavedV2.getHearingDay().toString();

        final DraftResult draftResult = draftResultRepository.findBy(draftResultPK);
        if (draftResult != null) {
            draftResultRepository.removeAndFlush(draftResult);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("hearing.draft-result-deleted-v2 event deleted draft result for hearingId:{}", draftResult.getHearingId());
            }
        }
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


    @Handles("hearing.results-shared")
    public void resultsShared(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.results-shared event received {}", event.toObfuscatedDebugString());
        }

        final ResultsShared resultsShared = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        final Hearing hearing = hearingRepository.findBy(resultsShared.getHearing().getId());

        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            if (resultsShared.getHearing().getHasSharedResults()) {
                final List<uk.gov.moj.cpp.hearing.persist.entity.ha.Target> listOfTargets = hearingRepository.findTargetsByHearingId(hearing.getId());
                final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearing.getId());
                final List<Target> targets = targetJPAMapper.fromJPA(Sets.newHashSet(listOfTargets), Sets.newHashSet(listOfProsecutionCases));
                hearing.setHasSharedResults(true);
                hearing.getTargets().clear();
                targets.forEach(targetIn -> updateDraftResult(hearing, targetIn, resultsShared.getSharedTime()));
                hearing.setHearingState(HearingState.SHARED);
                hearingRepository.save(hearing);
                if (resultsShared.getHearing().getYouthCourt() != null) {
                    saveHearingYouthCourtDetails(resultsShared.getHearing().getYouthCourt(), hearing);
                }
                approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
            }
        }
    }

    @Handles("hearing.events.results-shared-v2")
    public void resultsSharedV2(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.results-shared-v2 event received {}", event.toObfuscatedDebugString());
        }

        final ResultsSharedV2 resultsShared = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsSharedV2.class);

        final Hearing hearing = hearingRepository.findBy(resultsShared.getHearing().getId());
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final LocalDate hearingDay = resultsShared.getHearingDay();
            if (resultsShared.getHearing().getHasSharedResults()) {
                final List<uk.gov.moj.cpp.hearing.persist.entity.ha.Target> listOfTargets = hearingRepository.findTargetsByHearingId(hearing.getId());
                final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearing.getId());
                final List<Target> targets = targetJPAMapper.fromJPA(Sets.newHashSet(listOfTargets), Sets.newHashSet(listOfProsecutionCases));
                hearing.setHasSharedResults(true);
                hearing.getHearingDays().stream().filter(hd -> hearingDay.equals(hd.getDate())).forEach(hd -> hd.setHasSharedResults(true));
                hearing.getTargets().clear();
                targets.forEach(targetIn -> updateDraftResult(hearing, targetIn, resultsShared.getSharedTime(), hearingDay));
                hearing.setHearingState(HearingState.SHARED);
                hearingRepository.save(hearing);
                if (resultsShared.getHearing().getYouthCourt() != null) {
                    saveHearingYouthCourtDetails(resultsShared.getHearing().getYouthCourt(), hearing);
                }
                approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
            }
        }
    }

    @Handles("hearing.events.results-shared-v3")
    public void resultsSharedV3(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.results-shared-v3 event received {}", event.toObfuscatedDebugString());
        }

        final ResultsSharedV3 resultsShared = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        saveSharedResults(resultsShared);
        updateDraftResultV2(resultsShared);
    }

    /**
     * This method is used to update DraftResultV2 object with
     * sharedDateTime informationinside __metadata__ object when Results are shared to keep
     * DraftResult and SharedResults in sync.
     *
     * @param resultsShared
     */
    private void updateDraftResultV2(final ResultsSharedV3 resultsShared) {
        final String draftResultPK = resultsShared.getHearingId().toString()+resultsShared.getHearingDay().toString();
        final DraftResult draftResult = draftResultRepository.findBy(draftResultPK);
        if (nonNull(draftResult) && nonNull(draftResult.getDraftResultPayload())) {
            final JsonNode metadataNode = draftResult.getDraftResultPayload().get("__metadata__");
            ((ObjectNode) metadataNode).put("lastSharedTime", ZonedDateTimes.toString(resultsShared.getSharedTime()));
            draftResultRepository.save(draftResult);
        }
    }

    private void saveSharedResults(final ResultsSharedV3 resultsShared) {
        final Hearing hearing = hearingRepository.findBy(resultsShared.getHearing().getId());
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final LocalDate hearingDay = resultsShared.getHearingDay();
            if (resultsShared.getHearing().getHasSharedResults()) {
                final List<Target2> listOfTargets = resultsShared.getTargets();
                final List<uk.gov.moj.cpp.hearing.persist.entity.ha.Target> legacyTargets = hearing.getTargets().stream().filter(t-> !"{}".equals(t.getDraftResult())).collect(Collectors.toList());
                legacyTargets.forEach(legacyTarget->hearing.getTargets().remove(legacyTarget));
                hearing.setHasSharedResults(true);
                hearing.getHearingDays().stream().filter(hd -> hearingDay.equals(hd.getDate())).forEach(hd -> hd.setHasSharedResults(true));
                listOfTargets.forEach(targetIn -> updateDraftResultV2(hearing, targetIn));
                hearing.setHearingState(HearingState.SHARED);
                hearingRepository.save(hearing);
                LOGGER.info("Saved {} result(s) for hearing : {}", hearing.getTargets().size(), hearing.getId());
                if (resultsShared.getHearing().getYouthCourt() != null) {
                    saveHearingYouthCourtDetails(resultsShared.getHearing().getYouthCourt(), hearing);
                }
                approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
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

    @Handles("hearing.events.earliest-next-hearing-date-changed")
    public void changeEarliestNextHearingDate(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.earliest-next-hearing-date-changed {}", event.toObfuscatedDebugString());
        }

        final EarliestNextHearingDateChanged earliestNextHearingDateChanged = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), EarliestNextHearingDateChanged.class);

        final Hearing hearing = hearingRepository.findBy(earliestNextHearingDateChanged.getSeedingHearingId());
        hearing.setEarliestNextHearingDate(earliestNextHearingDateChanged.getEarliestNextHearingDate());
        hearingRepository.save(hearing);

    }

    @Handles("hearing.events.earliest-next-hearing-date-cleared")
    public void removeNextHearingsStartDate(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.earliest-next-hearing-date-cleared {}", event.toObfuscatedDebugString());
        }

        final EarliestNextHearingDateCleared earliestNextHearingDateCleared = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), EarliestNextHearingDateCleared.class);

        final Hearing hearing = hearingRepository.findBy(earliestNextHearingDateCleared.getHearingId());
        hearing.setEarliestNextHearingDate(null);
        hearingRepository.save(hearing);

    }

    @Handles("hearing.event.amended")
    public void amendHearing(final JsonEnvelope event) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.amended {}", event.toObfuscatedDebugString());
        }

        final HearingAmended hearingAmendedEvent = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingAmended.class);

        final Hearing hearing = hearingRepository.findBy(hearingAmendedEvent.getHearingId());
        hearing.setHearingState(hearingAmendedEvent.getNewHearingState());
        hearing.setAmendedByUserId(hearingAmendedEvent.getUserId());
        hearing.setEarliestNextHearingDate(null);
        hearingRepository.save(hearing);
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
            ((ObjectNode) node).put(DIRTY, FALSE);
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

            objectnode.put(REQUEST_APPROVAL, FALSE);
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

    private void saveHearingYouthCourtDetails(final YouthCourt youthCourt, final Hearing hearing) {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.YouthCourt youthCourtEntity = hearing.getYouthCourt() == null
                ? new uk.gov.moj.cpp.hearing.persist.entity.ha.YouthCourt()
                : hearing.getYouthCourt();

        youthCourtEntity.setCourtCode(youthCourt.getCourtCode());
        youthCourtEntity.setId(youthCourt.getYouthCourtId());
        youthCourtEntity.setName(youthCourt.getName());
        youthCourtEntity.setWelshName(youthCourt.getWelshName());
        hearing.setYouthCourt(youthCourtEntity);
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

    /**
     * Updates the draft results with shared time etc. Only updates draft results for the given
     * hearingDay.
     *
     * @param hearing        - the hearing to update
     * @param targetIn       - the target to add
     * @param sharedDateTime - the share time
     * @param hearingDay     - the day of the hearing being shared, if target's hearingDay doesn't
     *                       match it won't be updated.
     */
    private void updateDraftResult(Hearing hearing, Target targetIn, ZonedDateTime sharedDateTime, final LocalDate hearingDay) {
        final String draftResult = targetIn.getDraftResult();
        if (isNotBlank(draftResult)) {

            if (hearingDay != null && hearingDay.equals(targetIn.getHearingDay())) {
                final String updatedDraftResult = enrichDraftResult(draftResult, sharedDateTime);
                if (isNotBlank(updatedDraftResult)) {
                    targetIn.setDraftResult(updatedDraftResult);
                    hearing.getTargets().add(targetJPAMapper.toJPA(hearing, targetIn));
                }
            } else {
                hearing.getTargets().add(targetJPAMapper.toJPA(hearing, targetIn));
            }
        }
    }

    /**
     * Updates the draft results with shared time etc. Only updates draft results for the given
     * hearingDay.
     *
     * @param hearing        - the hearing to update
     * @param targetIn       - the target to add
     *                       match it won't be updated.
     */
    private void updateDraftResultV2(Hearing hearing, Target2 targetIn) {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Target targetReq = targetJPAMapper.toJPA2(hearing, targetIn);

        hearing.getTargets().stream().filter(t->t.getId().equals(targetIn.getTargetId())).findFirst().ifPresent(t->t.getResultLines().removeIf(not(ResultLine::getDeleted)));


        if ( hearing.getTargets().isEmpty() || !hearing.getTargets().contains(targetReq)) {
            hearing.getTargets().add(targetReq);
            return;
        }

        hearing.getTargets().stream()
                .filter(t->t.equals(targetReq))
                .findFirst()
                .ifPresent(t->{
                    t.setCaseId(targetReq.getCaseId());
                    t.setDefendantId(targetReq.getDefendantId());
                    t.setHearingDay(targetReq.getHearingDay());
                    t.setOffenceId(targetReq.getOffenceId());
                    t.setMasterDefendantId(targetReq.getMasterDefendantId());
                    t.setApplicationId(targetReq.getApplicationId());
                    t.setShadowListed(targetReq.getShadowListed());
                    addOrReplaceResultLine(targetReq, t);
                });
    }

    private void addOrReplaceResultLine(final uk.gov.moj.cpp.hearing.persist.entity.ha.Target targetReq, final uk.gov.moj.cpp.hearing.persist.entity.ha.Target t) {

        targetReq.getResultLines().forEach(rl->{
            if (t.getResultLines().contains(rl)){
                t.getResultLines().remove(rl);
                t.getResultLines().add(rl);

            } else {
                t.getResultLines().add(rl);
            }
        });
    }


    private void updateDraftResult(Hearing hearing, Target targetIn, ZonedDateTime sharedDateTime) {
        final String draftResult = targetIn.getDraftResult();
        if (isNotBlank(draftResult)) {
            final String updatedDraftResult = enrichDraftResult(draftResult, sharedDateTime);
            if (isNotBlank(updatedDraftResult)) {
                targetIn.setDraftResult(updatedDraftResult);
                hearing.getTargets().add(targetJPAMapper.toJPA(hearing, targetIn));
            }
        }
    }

    @Handles("hearing.events.result-amendments-cancelled")
    public void sharedResultsAmendmentsCancelled(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.result-amendments-cancelled {}", event.toObfuscatedDebugString());
        }
        final ResultAmendmentsCancelled resultAmendmentsCancelled = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultAmendmentsCancelled.class);
        final Hearing hearing = hearingRepository.findBy(resultAmendmentsCancelled.getHearingId());
        hearing.setHearingState(HearingState.SHARED);

        final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearing.getId());
        resultAmendmentsCancelled.getLatestSharedTargets().forEach(t -> t.setMasterDefendantId(targetJPAMapper.getMasterDefendantId(t.getDefendantId(), Sets.newHashSet(listOfProsecutionCases))));
        hearing.setHasSharedResults(true);
        hearing.setAmendedByUserId(null);
        hearing.getTargets().clear();
        hearing.setHearingState(SHARED);
        resultAmendmentsCancelled.getLatestSharedTargets().forEach(targetIn -> updateDraftResult(hearing, targetIn, resultAmendmentsCancelled.getLastSharedDateTime()));
        hearingRepository.save(hearing);
        approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
    }

    @Handles("hearing.events.result-amendments-cancelled-v2")
    public void sharedResultsAmendmentsCancelledV2(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.result-amendments-cancelled-v2 {}", event.toObfuscatedDebugString());
        }
        final ResultAmendmentsCancelledV2 resultAmendmentsCancelled = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultAmendmentsCancelledV2.class);
        final Hearing hearing = hearingRepository.findBy(resultAmendmentsCancelled.getHearingId());

        hearing.setHasSharedResults(true);
        hearing.setAmendedByUserId(null);
        hearing.setHearingState(SHARED);
        hearingRepository.save(hearing);
        approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
    }

    @Handles("hearing.event.result-amendments-rejected")
    public void resetDraftResultsToLasteSared(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.result-amendments-rejected {}", event.toObfuscatedDebugString());
        }
        final ResultAmendmentsRejected resultAmendmentsRejected = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultAmendmentsRejected.class);
        final Hearing hearing = hearingRepository.findBy(resultAmendmentsRejected.getHearingId());
        final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearing.getId());
        resultAmendmentsRejected.getLatestSharedTargets().forEach(t -> t.setMasterDefendantId(targetJPAMapper.getMasterDefendantId(t.getDefendantId(), Sets.newHashSet(listOfProsecutionCases))));
        hearing.setHasSharedResults(true);
        hearing.getTargets().clear();
        hearing.setHearingState(SHARED);
        resultAmendmentsRejected.getLatestSharedTargets().forEach(targetIn -> updateDraftResult(hearing, targetIn, resultAmendmentsRejected.getLastSharedDateTime()));
        hearingRepository.save(hearing);
        approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
    }

    @Handles("hearing.event.result-amendments-rejected-v2")
    public void resetDraftResultsToLasteSaredV2(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.result-amendments-rejected-v2 {}", event.toObfuscatedDebugString());
        }
        final ResultAmendmentsRejectedV2 resultAmendmentsRejected = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultAmendmentsRejectedV2.class);
        final Hearing hearing = hearingRepository.findBy(resultAmendmentsRejected.getHearingId());
        final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearing.getId());
        resultAmendmentsRejected.getLatestSharedTargets().forEach(t -> t.setMasterDefendantId(targetJPAMapper.getMasterDefendantId(t.getDefendantId(), Sets.newHashSet(listOfProsecutionCases))));
        hearing.setHasSharedResults(true);
        hearing.setAmendedByUserId(null);
        hearing.setHearingState(SHARED);
        hearingRepository.save(hearing);
        approvalRequestedRepository.removeAllRequestApprovals(hearing.getId());
    }


    @Handles("hearing.events.marked-as-duplicate")
    public void handleHearingMarkedAsDuplicate(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.marked-as-duplicate event received {}", event.toObfuscatedDebugString());
        }

        final UUID hearingId = fromString(event.payloadAsJsonObject().getString("hearingId"));
        final Hearing hearing = hearingRepository.findBy(hearingId);

        if (hearing != null) {
            hearingRepository.remove(hearing);
        }
    }
}
