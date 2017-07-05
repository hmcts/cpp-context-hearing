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

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselDefendantRepository;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselRepository;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.transaction.Transactional;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_ROOM_NAME = "roomName";
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

    @Transactional
    @Handles("hearing.hearing-initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String hearingType = payload.getString(FIELD_HEARING_TYPE);
        final Integer duration = payload.getInt(FIELD_DURATION);
        final ZonedDateTime startDateTime = fromJsonString(payload.getJsonString(FIELD_START_DATE_TIME));

        final Optional<Hearing> existingHearing = hearingRepository.getByHearingId(hearingId);
        final Hearing hearing = existingHearing.map(item ->
                item.builder()
                        .withStartDate(startDateTime.toLocalDate())
                        .withStartTime(startDateTime.toLocalTime())
                        .withDuration(duration)
                        .withHearingType(hearingType)
                        .build())
                .orElseGet(() ->
                        new Hearing(hearingId, startDateTime.toLocalDate(), startDateTime.toLocalTime(),
                                duration, null, hearingType, null));

        hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.court-assigned")
    public void courtAssigned(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String courtCentreName = payload.getString(FIELD_COURT_CENTRE_NAME);

        final Optional<Hearing> existingHearing = hearingRepository.getByHearingId(hearingId);

        final Hearing hearing = existingHearing.map(item ->
                item.builder().withCourtCentreName(courtCentreName).build())
                .orElseGet(() ->
                        new Hearing(hearingId, null, null, null, null, null,
                                courtCentreName));

        hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.room-booked")
    public void roomBooked(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String roomName = payload.getString(FIELD_ROOM_NAME);

        final Optional<Hearing> existingHearing = hearingRepository.getByHearingId(hearingId);

        final Hearing hearing = existingHearing.map(item ->
                item.builder().withRoomName(roomName).build())
                .orElseGet(() ->
                        new Hearing(hearingId, null, null, null, roomName, null,
                                null));

        hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.adjourn-date-updated")
    public void hearingAdjournDateUpdated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final LocalDate startDate = LocalDate.parse(payload.getString(FIELD_START_DATE));

        final Optional<Hearing> existingHearing = hearingRepository.getByHearingId(hearingId);

        final Hearing hearing = existingHearing.map(item ->
                item.builder().withStartDate(startDate).build())
                .orElseGet(() ->
                        new Hearing(hearingId, startDate, null, null, null,
                                null, null));

        hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.prosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);

        prosecutionCounselRepository.save(new ProsecutionCounsel(attendeeId, hearingId, personId, status));
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

        defenceCounselRepository.save(new DefenceCounsel(attendeeId, hearingId, personId, status));

        final List<DefenceCounselDefendant> existingDefendants =
                defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(attendeeId);

        existingDefendants.stream()
                .filter(defendant -> !defendantIds.contains(defendant.getDefendantId()))
                .forEach(defenceCounselDefendantRepository::remove);

        defendantIds.forEach(defendantId ->
                defenceCounselDefendantRepository.save(new DefenceCounselDefendant(attendeeId, defendantId)));
    }

    @Transactional
    @Handles("hearing.case-associated")
    public void caseAssociated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));

        final List<HearingCase> existingHearingCases = hearingCaseRepository.findByHearingId(hearingId);
        if (existingHearingCases.stream().map(HearingCase::getCaseId)
                .noneMatch(id -> id.equals(caseId))) {
            hearingCaseRepository.save(new HearingCase(randomUUID(), hearingId, caseId));
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

        hearingOutcomeRepository.save(new HearingOutcome(offenceId, hearingId, defendantId, targetId, draftResult));
    }

    @Handles("hearing.results-shared")
    public void updateDraftResultWithLastSharedResultIdFromSharedResults(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final List<HearingOutcome> hearingOutcomes = hearingOutcomeRepository.findByHearingId(fromString(payload.getString(FIELD_HEARING_ID)));
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
        final List<HearingOutcome> hearingOutcomes = hearingOutcomeRepository.findByHearingId(fromString(payload.getString(FIELD_HEARING_ID)));
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
                hearingOutcomeRepository.save(new HearingOutcome(hearingOutcome.getOffenceId(),
                        hearingOutcome.getHearingId(), hearingOutcome.getDefendantId(), hearingOutcome.getId(),
                        hearingOutcomeToDraftResult.get(hearingOutcome.getId()).toString()));
            }
        });
    }
}
