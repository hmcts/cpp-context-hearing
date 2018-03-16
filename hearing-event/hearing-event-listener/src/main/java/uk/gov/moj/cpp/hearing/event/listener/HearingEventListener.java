package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.VerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.VerdictChanged;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselDefendantRepository;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselRepository;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.PleaHearingRepository;
import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictValue;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;

@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingEventListener.class.getName());

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_JUDGE_ID = "id";
    private static final String FIELD_JUDGE_FIRST_NAME = "firstName";
    private static final String FIELD_JUDGE_LAST_NAME = "lastName";
    private static final String FIELD_JUDGE_TITLE = "title";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_ATTENDEE_ID = "attendeeId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_DEFENDANT_IDS = "defendantIds";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_RESULTS = "results";
    private static final String FIELD_RESULT_LINE_ID = "resultLineId";


    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Inject
    private ProsecutionCounselRepository prosecutionCounselRepository;

    @Inject
    private DefenceCounselRepository defenceCounselRepository;

    @Inject
    private DefenceCounselDefendantRepository defenceCounselDefendantRepository;

    @Inject
    private HearingOutcomeRepository hearingOutcomeRepository;

    @Inject
    private PleaHearingRepository pleaHearingRepository;

    @Inject
    private VerdictHearingRepository verdictHearingRepository;

    @Inject
    private HearingJudgeRepository hearingJudgeRepository;

    @Transactional
    @Handles("hearing.hearing-initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String hearingType = payload.getString(FIELD_HEARING_TYPE);
        final Integer duration = payload.getInt(FIELD_DURATION);
        final ZonedDateTime startDateTime = fromJsonString(payload.getJsonString(FIELD_START_DATE_TIME));

        final Optional<uk.gov.moj.cpp.hearing.persist.entity.Hearing> existingHearing = this.hearingRepository.getByHearingId(hearingId);
        final uk.gov.moj.cpp.hearing.persist.entity.Hearing hearing = existingHearing.map(item ->
                item.builder()
                        .withHearingId(item.getHearingId())
                        .withStartDate(startDateTime.toLocalDate())
                        .withStartTime(startDateTime.toLocalTime())
                        .withDuration(duration)
                        .withHearingType(hearingType)
                        .build())
                .orElseGet(() ->
                        new uk.gov.moj.cpp.hearing.persist.entity.Hearing(hearingId, startDateTime.toLocalDate(), startDateTime.toLocalTime(), duration,
                                null, hearingType, null));

        this.hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.court-assigned")
    public void courtAssigned(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String courtCentreName = payload.getString(FIELD_COURT_CENTRE_NAME);
        final UUID courtCentreId = getUUID(payload, FIELD_COURT_CENTRE_ID).orElse(null);
        final Optional<uk.gov.moj.cpp.hearing.persist.entity.Hearing> existingHearing = this.hearingRepository.getByHearingId(hearingId);

        final uk.gov.moj.cpp.hearing.persist.entity.Hearing hearing = existingHearing.map(item ->
                item.builder().withHearingId(item.getHearingId()).withCourtCentreName(courtCentreName)
                        .withCourtCentreId(courtCentreId)
                        .build())
                .orElseGet(() ->
                        new uk.gov.moj.cpp.hearing.persist.entity.Hearing.Builder().withHearingId(hearingId).withCourtCentreId(courtCentreId)
                                .withCourtCentreName(courtCentreName).build());

        this.hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.judge-assigned")
    public void judgeAssigned(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final HearingJudge hearingJudge = new HearingJudge(fromString(payload.getString(FIELD_HEARING_ID)),
                payload.getString(FIELD_JUDGE_ID),
                payload.getString(FIELD_JUDGE_FIRST_NAME),
                payload.getString(FIELD_JUDGE_LAST_NAME),
                payload.getString(FIELD_JUDGE_TITLE));

        this.hearingJudgeRepository.save(hearingJudge);
    }

    @Transactional
    @Handles("hearing.room-booked")
    public void roomBooked(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String roomName = payload.getString(FIELD_ROOM_NAME);
        final UUID roomId = getUUID(payload, FIELD_ROOM_ID).orElse(null);
        final Optional<uk.gov.moj.cpp.hearing.persist.entity.Hearing> existingHearing = this.hearingRepository.getByHearingId(hearingId);

        final uk.gov.moj.cpp.hearing.persist.entity.Hearing hearing = existingHearing.map(item ->
                item.builder().withHearingId(item.getHearingId()).withRoomName(roomName).withRoomId(roomId)
                        .build())
                .orElseGet(() ->
                        new uk.gov.moj.cpp.hearing.persist.entity.Hearing.Builder().withHearingId(hearingId).withRoomId(roomId).withRoomName(roomName)
                                .build());

        this.hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.adjourn-date-updated")
    public void hearingAdjournDateUpdated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final LocalDate startDate = LocalDate.parse(payload.getString(FIELD_START_DATE));

        final Optional<uk.gov.moj.cpp.hearing.persist.entity.Hearing> existingHearing = this.hearingRepository.getByHearingId(hearingId);

        final uk.gov.moj.cpp.hearing.persist.entity.Hearing hearing = existingHearing.map(item ->
                item.builder().withStartDate(startDate).build())
                .orElseGet(() ->
                        new uk.gov.moj.cpp.hearing.persist.entity.Hearing(hearingId, startDate, null, null, null,
                                null, null));

        this.hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.prosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);

        this.prosecutionCounselRepository.save(new ProsecutionCounsel(attendeeId, hearingId, personId, status));
    }

    @Handles("hearing.plea-added")
    public void pleaAdded(final JsonEnvelope event) {
        LOGGER.info("{}", event.payloadAsJsonObject());
        final HearingPleaAdded hearingPleaAdded =
                this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingPleaAdded.class);
        final PleaHearing pleaHearing = new PleaHearing(hearingPleaAdded.getPlea().getId(),
                hearingPleaAdded.getHearingId(),
                hearingPleaAdded.getCaseId(),
                hearingPleaAdded.getDefendantId(),
                hearingPleaAdded.getOffenceId(),
                hearingPleaAdded.getPlea().getPleaDate(),
                hearingPleaAdded.getPlea().getValue(),
                hearingPleaAdded.getPersonId());
        this.pleaHearingRepository.save(pleaHearing);
    }

    @Handles("hearing.plea-changed")
    public void pleaChanged(final JsonEnvelope event) {
        LOGGER.info("{}", event.payloadAsJsonObject());
        final HearingPleaChanged hearingPleaChanged =
                this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingPleaChanged.class);

        final PleaHearing pleaHearing = new PleaHearing(hearingPleaChanged.getPlea().getId(),
                hearingPleaChanged.getHearingId(),
                hearingPleaChanged.getCaseId(),
                hearingPleaChanged.getDefendantId(),
                hearingPleaChanged.getOffenceId(),
                hearingPleaChanged.getPlea().getPleaDate(),
                hearingPleaChanged.getPlea().getValue(),
                hearingPleaChanged.getPersonId());
        this.pleaHearingRepository.save(pleaHearing);

    }

    @Handles("hearing.verdict-added")
    public void verdictAdded(final JsonEnvelope event) {
        LOGGER.info("{}", event.payloadAsJsonObject());
        VerdictAdded verdictAdded =
                this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), VerdictAdded.class);
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictAdded.getVerdict().getValue().getId())
                .withCategory(verdictAdded.getVerdict().getValue().getCategory())
                .withCode(verdictAdded.getVerdict().getValue().getCode())
                .withDescription(verdictAdded.getVerdict().getValue().getDescription()).build();
        final VerdictHearing verdictHearing = new VerdictHearing.Builder()
                .withVerdictId(verdictAdded.getVerdict().getId())
                .withHearingId(verdictAdded.getHearingId())
                .withCaseId(verdictAdded.getCaseId())
                .withPersonId(verdictAdded.getPersonId())
                .withDefendantId(verdictAdded.getDefendantId())
                .withOffenceId(verdictAdded.getOffenceId())
                .withValue(verdictValue)
                .withVerdictDate(verdictAdded.getVerdict().getVerdictDate())
                .withNumberOfSplitJurors(verdictAdded.getVerdict().getNumberOfSplitJurors())
                .withNumberOfJurors(verdictAdded.getVerdict().getNumberOfJurors())
                .withUnanimous(verdictAdded.getVerdict().getUnanimous()).build();
        this.verdictHearingRepository.save(verdictHearing);
    }

    @Handles("hearing.verdict-changed")
    public void verdictChanged(final JsonEnvelope event) {
        LOGGER.info("{}", event.payloadAsJsonObject());
        VerdictChanged verdictChanged =
                this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), VerdictChanged.class);
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictChanged.getVerdict().getValue().getId())
                .withCategory(verdictChanged.getVerdict().getValue().getCategory())
                .withCode(verdictChanged.getVerdict().getValue().getCode())
                .withDescription(verdictChanged.getVerdict().getValue().getDescription()).build();
        final VerdictHearing verdictHearing = new VerdictHearing.Builder()
                .withVerdictId(verdictChanged.getVerdict().getId())
                .withHearingId(verdictChanged.getHearingId())
                .withCaseId(verdictChanged.getCaseId())
                .withPersonId(verdictChanged.getPersonId())
                .withDefendantId(verdictChanged.getDefendantId())
                .withOffenceId(verdictChanged.getOffenceId())
                .withValue(verdictValue)
                .withVerdictDate(verdictChanged.getVerdict().getVerdictDate())
                .withNumberOfSplitJurors(verdictChanged.getVerdict().getNumberOfSplitJurors())
                .withNumberOfJurors(verdictChanged.getVerdict().getNumberOfJurors())
                .withUnanimous(verdictChanged.getVerdict().getUnanimous()).build();
        this.verdictHearingRepository.save(verdictHearing);
    }

    @Transactional
    @Handles("hearing.defence-counsel-added")
    public void defenceCounselAdded(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);

        final List<UUID> defendantIds = JsonObjects.getUUIDs(payload, FIELD_DEFENDANT_IDS);

        this.defenceCounselRepository.save(new DefenceCounsel(attendeeId, hearingId, personId, status));

        final List<DefenceCounselDefendant> existingDefendants =
                this.defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(attendeeId);

        existingDefendants.stream()
                .filter(defendant -> !defendantIds.contains(defendant.getDefendantId()))
                .forEach(this.defenceCounselDefendantRepository::remove);

        defendantIds.forEach(defendantId ->
                this.defenceCounselDefendantRepository.save(new DefenceCounselDefendant(attendeeId, defendantId)));
        (new NewHearingEventListener()).defenceCounselAdded(event);
    }

    @Transactional
    @Handles("hearing.case-associated")
    public void caseAssociated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));

        final List<HearingCase> existingHearingCases = this.hearingCaseRepository.findByHearingId(hearingId);
        if (existingHearingCases.stream().map(HearingCase::getCaseId)
                .noneMatch(id -> id.equals(caseId))) {
            this.hearingCaseRepository.save(new HearingCase(randomUUID(), hearingId, caseId));
        }
    }

    @Handles("hearing.draft-result-saved")
    public void draftResultSaved(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        final UUID targetId = fromString(payload.getString(FIELD_TARGET_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID defendantId = fromString(payload.getString(FIELD_DEFENDANT_ID));
        final UUID offenceId = fromString(payload.getString(FIELD_OFFENCE_ID));
        final String draftResult = payload.getString(FIELD_DRAFT_RESULT);

        this.hearingOutcomeRepository.save(new HearingOutcome(offenceId, hearingId, defendantId, targetId, draftResult));
    }

    @Handles("hearing.results-shared")
    public void updateDraftResultWithLastSharedResultIdFromSharedResults(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final List<HearingOutcome> hearingOutcomes = this.hearingOutcomeRepository.findByHearingId(fromString(payload.getString(FIELD_HEARING_ID)));
        final Map<UUID, JsonObject> hearingOutcomeToDraftResultMap = newHashMap();

        payload.getJsonArray(FIELD_RESULT_LINES).getValuesAs(JsonObject.class).forEach(resultLine -> {
            final String sharedResultLineId = resultLine.getString(FIELD_GENERIC_ID);
            hearingOutcomes.forEach(hearingOutcome -> {

                if (!hearingOutcomeToDraftResultMap.containsKey(hearingOutcome.getId())) {
                    hydrateWithDraftResultJson(hearingOutcomeToDraftResultMap, hearingOutcome);
                }
                final JsonObject draftResultJson = hearingOutcomeToDraftResultMap.get(hearingOutcome.getId());
                final List<String> resultLineIds = getResultLineIdsFromDraftResultJson(draftResultJson);

                if (resultLineIds.contains(sharedResultLineId)) {
                    final JsonObjectBuilder updatedDraftResultJson = updateDraftResultWithLastSharedResultId(draftResultJson, sharedResultLineId);
                    hearingOutcomeToDraftResultMap.put(hearingOutcome.getId(), updatedDraftResultJson.build());
                }
            });
        });

        persistModifiedHearingOutcomes(hearingOutcomes, hearingOutcomeToDraftResultMap);
    }

    @Handles("hearing.result-amended")
    public void updateDraftResultWithLastSharedResultIdFromAmendedResult(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final List<HearingOutcome> hearingOutcomes = this.hearingOutcomeRepository.findByHearingId(fromString(payload.getString(FIELD_HEARING_ID)));
        final Map<UUID, JsonObject> hearingOutcomeToDraftResultMap = newHashMap();

        final String sharedResultLineId = payload.getString(FIELD_GENERIC_ID);

        hearingOutcomes.forEach(hearingOutcome -> {
            final JsonObject draftResultJson = parseDraftResultToJson(hearingOutcome);
            final List<String> resultLineIds = getResultLineIdsFromDraftResultJson(draftResultJson);

            if (resultLineIds.contains(sharedResultLineId)) {
                final JsonObjectBuilder updatedDraftResultJson = updateDraftResultWithLastSharedResultId(draftResultJson, sharedResultLineId);
                hearingOutcomeToDraftResultMap.put(hearingOutcome.getId(), updatedDraftResultJson.build());
            }
        });

        persistModifiedHearingOutcomes(hearingOutcomes, hearingOutcomeToDraftResultMap);
    }

    private void hydrateWithDraftResultJson(final Map<UUID, JsonObject> hearingOutcomeToDraftResultMap, final HearingOutcome hearingOutcome) {
        hearingOutcomeToDraftResultMap.put(hearingOutcome.getId(), parseDraftResultToJson(hearingOutcome));
    }

    private JsonObject parseDraftResultToJson(final HearingOutcome hearingOutcome) {
        try (final JsonReader jsonReader = createReader(new StringReader(hearingOutcome.getDraftResult()))) {
            return jsonReader.readObject();
        }
    }

    private List<String> getResultLineIdsFromDraftResultJson(final JsonObject draftResultJson) {
        return draftResultJson.getJsonArray(FIELD_RESULTS).getValuesAs(JsonObject.class).stream()
                .map(result -> result.getString(FIELD_RESULT_LINE_ID)).collect(toList());
    }

    private JsonObjectBuilder updateDraftResultWithLastSharedResultId(final JsonObject draftResultJson, final String sharedResultLineId) {
        final JsonObjectBuilder updatedDraftResultJson = createObjectBuilder();
        draftResultJson.forEach((key, value) -> {
            if (key.equals(FIELD_RESULTS)) {
                final JsonArray results = (JsonArray) value;
                final JsonArrayBuilder updatedResultsJson = createArrayBuilder();

                results.getValuesAs(JsonObject.class).forEach(result -> {
                    final JsonObjectBuilder updatedResultJson = createObjectBuilder();
                    result.forEach(updatedResultJson::add);
                    if (result.getString(FIELD_RESULT_LINE_ID).equals(sharedResultLineId)) {
                        updatedResultJson.add(FIELD_LAST_SHARED_RESULT_ID, sharedResultLineId);
                    }
                    updatedResultsJson.add(updatedResultJson);
                });
                updatedDraftResultJson.add(key, updatedResultsJson);
            } else {
                updatedDraftResultJson.add(key, value);
            }
        });
        return updatedDraftResultJson;
    }

    private void persistModifiedHearingOutcomes(final List<HearingOutcome> hearingOutcomes, final Map<UUID, JsonObject> hearingOutcomeToDraftResult) {
        hearingOutcomes.forEach(hearingOutcome -> {
            if (hearingOutcomeToDraftResult.containsKey(hearingOutcome.getId())) {
                this.hearingOutcomeRepository.save(new HearingOutcome(hearingOutcome.getOffenceId(),
                        hearingOutcome.getHearingId(), hearingOutcome.getDefendantId(), hearingOutcome.getId(),
                        hearingOutcomeToDraftResult.get(hearingOutcome.getId()).toString()));
            }
        });
    }
}
