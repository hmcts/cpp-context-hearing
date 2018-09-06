package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;
import uk.gov.moj.cpp.hearing.repository.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ResultLineRepository;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_COMPLETED_RESULT_LINES = "completedResultLines";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_RESULTS = "results";
    private static final String FIELD_RESULT_LINE_ID = "resultLineId";

    @Inject
    //TODO remove this
    private HearingOutcomeRepository hearingOutcomeRepository;

    @Inject
    private ResultLineRepository resultLineRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private TargetJPAMapper targetJPAMapper;

    @Handles("hearing.draft-result-saved")
    public void draftResultSaved(final JsonEnvelope event) {
        final DraftResultSaved draftResultSaved = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DraftResultSaved.class);
        final uk.gov.justice.json.schemas.core.Target targetIn = draftResultSaved.getTarget();
        final Hearing hearing = this.hearingRepository.findBy(draftResultSaved.getTarget().getHearingId());
        final Target previousTarget = hearing.getTargets().stream().filter(t -> t.getId().equals(targetIn.getTargetId())).findFirst().orElse(null);
        final Target newTarget = targetJPAMapper.toJPA(hearing, targetIn);
        if (previousTarget != null) {
              hearing.getTargets().remove(previousTarget);
        }
        hearing.getTargets().add(newTarget);

        hearingRepository.save(hearing);

    }

    @Handles("hearing.results-shared")
    public void updateDraftResultWithFromSharedResults(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        final List<HearingOutcome> hearingOutcomes = this.hearingOutcomeRepository.findByHearingId(fromString(payload.getString(FIELD_HEARING_ID)));

        final Map<UUID, JsonObject> hearingOutcomeToDraftResultMap = newHashMap();

        payload.getJsonArray(FIELD_COMPLETED_RESULT_LINES).getValuesAs(JsonObject.class).forEach(resultLine -> {
            final String sharedResultLineId = resultLine.getString(FIELD_GENERIC_ID);
            hearingOutcomes.forEach(hearingOutcome -> {

                if (!hearingOutcomeToDraftResultMap.containsKey(hearingOutcome.getId())) {
                    hydrateWithDraftResultJson(hearingOutcomeToDraftResultMap, hearingOutcome);
                }
                final JsonObject draftResultJson = hearingOutcomeToDraftResultMap.get(hearingOutcome.getId());
                final List<String> resultLineIds = getResultLineIdsFromDraftResultJson(draftResultJson);

                if (resultLineIds.contains(sharedResultLineId)) {
                    final JsonObjectBuilder updatedDraftResultJson = updateDraftResult(draftResultJson);
                    hearingOutcomeToDraftResultMap.put(hearingOutcome.getId(), updatedDraftResultJson.build());
                }
            });
        });

        persistModifiedHearingOutcomes(hearingOutcomes, hearingOutcomeToDraftResultMap);
    }

    //TODO what should this do ?
    @Handles("hearing.result-lines-status-updated")
    public void updateSharedResultLineStatus(final JsonEnvelope event) {

        final ResultLinesStatusUpdated resultLinesStatusUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultLinesStatusUpdated.class);

        final Hearing hearing = hearingRepository.findBy(resultLinesStatusUpdated.getHearingId());

        if (hearing != null) {
            resultLinesStatusUpdated.getSharedResultLines().forEach(sharedResultLineId -> {
                final HearingSnapshotKey resultLineKey = new HearingSnapshotKey(sharedResultLineId.getSharedResultLineId(), resultLinesStatusUpdated.getHearingId());
                final ResultLine resultLine = this.resultLineRepository.findBy(resultLineKey);
                //TODO what should this do ?
                /*if (resultLine != null) {
                    resultLine.setLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime());
                } else {
                    resultLine = ResultLine.builder()
                            .withId(new HearingSnapshotKey(sharedResultLineId.getSharedResultLineId(), resultLinesStatusUpdated.getHearingId()))
                            .withLastSharedDateTime(resultLinesStatusUpdated.getLastSharedDateTime())
                            .build();
                }*/
                this.resultLineRepository.save(resultLine);
            });
        }
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

    private JsonObjectBuilder updateDraftResult(final JsonObject draftResultJson) {
        final JsonObjectBuilder updatedDraftResultJson = createObjectBuilder();
        draftResultJson.forEach((key, value) -> {
            if (key.equals(FIELD_RESULTS)) {
                final JsonArray results = (JsonArray) value;
                final JsonArrayBuilder updatedResultsJson = createArrayBuilder();

                results.getValuesAs(JsonObject.class).forEach(result -> {
                    final JsonObjectBuilder updatedResultJson = createObjectBuilder();
                    result.forEach(updatedResultJson::add);
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
