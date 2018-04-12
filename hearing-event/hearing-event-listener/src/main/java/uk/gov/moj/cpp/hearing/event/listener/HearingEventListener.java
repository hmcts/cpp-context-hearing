package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
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
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.transaction.Transactional;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingEventListener.class.getName());

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
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
    @Handles("hearing.prosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID personId = fromString(payload.getString(FIELD_PERSON_ID));
        final UUID attendeeId = fromString(payload.getString(FIELD_ATTENDEE_ID));
        final String status = payload.getString(FIELD_STATUS);

        this.prosecutionCounselRepository.save(new ProsecutionCounsel(attendeeId, hearingId, personId, status));
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
       // (new NewHearingEventListener()).defenceCounselAdded(event);
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
