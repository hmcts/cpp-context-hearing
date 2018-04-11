package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.persist.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinition;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_LISTENER)
public class HearingLogEventListener {

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
        final UUID witnessId = payload.containsKey(FIELD_WITNESS_ID) == true ? fromString(payload.getString(FIELD_WITNESS_ID) ): null;

        hearingEventRepository.save(new HearingEvent(hearingEventId, hearingEventDefinitionId, hearingId, recordedLabel, eventTime, lastModifiedTime, alterable,witnessId));
    }

    @Handles("hearing.hearing-event-deleted")
    public void hearingEventDeleted(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));

        final Optional<HearingEvent> optionalHearingEvent = hearingEventRepository.findOptionalById(hearingEventId);
        optionalHearingEvent.ifPresent(hearingEvent -> hearingEventRepository.save(hearingEvent.builder().delete().build()));
    }

}
