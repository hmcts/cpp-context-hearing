package uk.gov.moj.cpp.hearing.event.listener;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_LISTENER)
public class HearingLogEventListener {

    private static final String FIELD_COUNSEL_ID = "counselId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_WITNESS_ID = "witnessId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DEFINITION_ID = "id";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";
    private static final String FIELD_ALTERABLE = "alterable";
	private static final String HEARING_EVENTS = "hearingEvents";
    @Inject
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Handles("hearing.hearing-event-definitions-created")
    public void hearingEventDefinitionsCreated(final JsonEnvelope event) {
        final List<HearingEventDefinition> entities = new ArrayList<>();
        event.payloadAsJsonObject().getJsonArray(FIELD_EVENT_DEFINITIONS).forEach(item -> {
            final JsonObject jsonObject = (JsonObject) item;
            entities.add(new HearingEventDefinition(
                    fromString(jsonObject.getString(FIELD_DEFINITION_ID)),
                    jsonObject.getString(FIELD_RECORDED_LABEL),
                    jsonObject.getString(FIELD_ACTION_LABEL),
                    jsonObject.containsKey(FIELD_SEQUENCE) ? jsonObject.getInt(FIELD_SEQUENCE) : null,
                    jsonObject.getString(FIELD_SEQUENCE_TYPE, null),
                    jsonObject.getString(FIELD_CASE_ATTRIBUTE, null),
                    jsonObject.getString(FIELD_GROUP_LABEL, null),
                    jsonObject.getString(FIELD_ACTION_LABEL_EXTENSION, null),
                    jsonObject.getBoolean(FIELD_ALTERABLE)));
        });

        entities.forEach(hearingEventDefinitionRepository::save);
    }

    @SuppressWarnings("unused")
    @Handles("hearing.hearing-event-definitions-deleted")
    public void hearingEventDefinitionsDeleted(final JsonEnvelope event) {
        final List<HearingEventDefinition> activeEventDefinitions = hearingEventDefinitionRepository.findAllActive();

        activeEventDefinitions.stream()
                .map(eventDefinition -> eventDefinition.builder().delete().build())
                .forEach(hearingEventDefinitionRepository::save);
    }

    @Handles("hearing.hearing-event-logged")
    public void hearingEventLogged(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final UUID hearingEventDefinitionId = fromString(payload.getString(FIELD_HEARING_EVENT_DEFINITION_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime eventTime = fromJsonString(payload.getJsonString(FIELD_EVENT_TIME));
        final ZonedDateTime lastModifiedTime = fromJsonString(payload.getJsonString(FIELD_LAST_MODIFIED_TIME));
        final boolean alterable = payload.getBoolean(FIELD_ALTERABLE);
        final UUID witnessId = payload.containsKey(FIELD_WITNESS_ID) ? fromString(payload.getString(FIELD_WITNESS_ID)) : null;
        final UUID counselId = payload.containsKey(FIELD_COUNSEL_ID) ? fromString(payload.getString(FIELD_COUNSEL_ID)) : null;

        hearingEventRepository.save(
                HearingEvent.hearingEvent()
                        .setId(hearingEventId)
                        .setHearingId(hearingId)
                        .setHearingEventDefinitionId(hearingEventDefinitionId)
                        .setRecordedLabel(recordedLabel)
                        .setEventTime(eventTime)
                        .setLastModifiedTime(lastModifiedTime)
                        .setAlterable(alterable)
                        .setWitnessId(witnessId)
                                        .setCounselId(counselId)
        );
    }

    @Handles("hearing.hearing-event-deleted")
    public void hearingEventDeleted(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));

        final Optional<HearingEvent> optionalHearingEvent = hearingEventRepository.findOptionalById(hearingEventId);
        optionalHearingEvent.ifPresent(hearingEvent -> hearingEventRepository.save(hearingEvent.setDeleted(true)));
    }
@Handles("hearing.hearing-events-updated")
    public void hearingEventsUpdated(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final JsonArray hearingEvents = payload.getJsonArray(HEARING_EVENTS);

        final Map<UUID, HearingEvent> hearingEventIdToHEaringEvent = hearingEventRepository
                        .findByHearingIdOrderByEventTimeAsc(hearingId).stream()
                        .collect(Collectors.toMap(HearingEvent::getId,
                                        hearingEvent -> hearingEvent));
        hearingEvents.getValuesAs(JsonObject.class).stream().forEach(hearingEvent -> {
            final UUID hearingEventId = fromString(hearingEvent.getString(FIELD_HEARING_EVENT_ID));
            final HearingEvent repositoryEvent = hearingEventIdToHEaringEvent.get(hearingEventId);

            if (repositoryEvent != null) {
                repositoryEvent.setHearingEventDefinitionId(
                                fromString(hearingEvent
                                                .getString(FIELD_HEARING_EVENT_DEFINITION_ID)));
                repositoryEvent.setRecordedLabel(hearingEvent.getString(FIELD_RECORDED_LABEL));
                repositoryEvent.setEventTime(ZonedDateTimes
                                .fromString(hearingEvent.getString(FIELD_EVENT_TIME)));
                repositoryEvent.setLastModifiedTime(ZonedDateTimes
                                .fromString(hearingEvent.getString(FIELD_LAST_MODIFIED_TIME)));
                if (hearingEvent.containsKey(FIELD_WITNESS_ID)) {
                    repositoryEvent.setWitnessId(
                                    fromString(hearingEvent.getString(FIELD_WITNESS_ID)));
                }
                hearingEventRepository.save(repositoryEvent);
            }
        });

    }	
	
}
