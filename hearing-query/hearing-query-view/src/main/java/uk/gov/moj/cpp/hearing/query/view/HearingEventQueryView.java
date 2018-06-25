package uk.gov.moj.cpp.hearing.query.view;

import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

@SuppressWarnings({"CdiInjectionPointsInspection", "WeakerAccess"})
@ServiceComponent(QUERY_VIEW)
public class HearingEventQueryView {

    private static final String FIELD_COUNSEL_ID = "counselId";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_HEARING_EVENTS = "events";
    private static final String FIELD_HEARING_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_ACTION_LABEL_EXT = "actionLabelExtension";
    private static final String FIELD_GROUP_LABEL = "groupLabel";

    private static final String FIELD_CASE_ATTRIBUTES = "caseAttributes";
    private static final String FIELD_DEFENDANT_NAME = "defendant.name";
    private static final String FIELD_COUNSEL_NAME = "counsel.name";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SEQUENCE_TYPE = "type";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_WITNESS_ID = "witnessId";

    @Inject
    private Enveloper enveloper;

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;


    @Handles("hearing.get-hearing-event-definitions")
    public JsonEnvelope getHearingEventDefinitionsVersionTwo(final JsonEnvelope query) {
        final List<HearingEventDefinition> hearingEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();
        final JsonArrayBuilder eventDefinitionsJsonArrayBuilder = createArrayBuilder();

        hearingEventDefinitions.forEach(eventDefinition -> eventDefinitionsJsonArrayBuilder.add(prepareEventDefinitionJsonObjectVersionTwo(eventDefinition)));

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_DEFINITIONS)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_EVENT_DEFINITIONS, eventDefinitionsJsonArrayBuilder)
                        .build());
    }

    @Handles(RESPONSE_NAME_HEARING_EVENT_DEFINITION)
    public JsonEnvelope getHearingEventDefinition(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final UUID hearingEventDefinitionId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_EVENT_DEFINITION_ID));

        final HearingEventDefinition hearingEventDefinition = hearingEventDefinitionRepository.findBy(hearingEventDefinitionId);

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_DEFINITION)
                .apply(prepareEventDefinitionJsonObject(hearingId, hearingEventDefinition).build());
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        final String hearingId = query.payloadAsJsonObject().getString(FIELD_HEARING_ID);

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(fromString(hearingId));

        final JsonArrayBuilder eventLogJsonArrayBuilder = createArrayBuilder();

        hearingEvents.
                forEach(hearingEvent ->
                        {
                            final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                                    .add(FIELD_HEARING_EVENT_ID, hearingEvent.getId().toString())
                                    .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEvent.getHearingEventDefinitionId().toString())
                                    .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel())
                                    .add(FIELD_EVENT_TIME, ZonedDateTimes.toString(hearingEvent.getEventTime()))
                                    .add(FIELD_LAST_MODIFIED_TIME, ZonedDateTimes.toString(hearingEvent.getLastModifiedTime()))
                                    .add(FIELD_ALTERABLE, hearingEvent.isAlterable());

                            if (hearingEvent.getWitnessId() != null) {
                                jsonObjectBuilder.add(FIELD_WITNESS_ID, hearingEvent.getWitnessId().toString());
                            }
                            if (hearingEvent.getCounselId() != null) {
                                jsonObjectBuilder.add(FIELD_COUNSEL_ID,
                                                hearingEvent.getCounselId().toString());
                            }
                            eventLogJsonArrayBuilder.add(jsonObjectBuilder);
                        }

                );

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_LOG)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId)
                        .add(FIELD_HEARING_EVENTS, eventLogJsonArrayBuilder)
                        .build()
                );
    }

    private JsonArrayBuilder defendantAndDefenceCounselAttributesFor(final UUID hearingId) {

        final Hearing aHearing = hearingRepository.findById(hearingId);

        final JsonArrayBuilder caseAttributesJsonArrayBuilder = createArrayBuilder();

        aHearing.getAttendees().stream()
                .filter(a -> a instanceof DefenceAdvocate)
                .map(DefenceAdvocate.class::cast)
                .forEach(defenceAdvocate ->
                    caseAttributesJsonArrayBuilder.add(
                            createObjectBuilder()
                                    .add(FIELD_COUNSEL_NAME, defenceAdvocate.getPersonId().toString())
                                    .add(FIELD_DEFENDANT_NAME, defenceAdvocate.getDefendants().get(0).getId().getId().toString())
                    )
                );

        return caseAttributesJsonArrayBuilder;
    }

    private boolean requireDefendantAndDefenceCounselDetails(final HearingEventDefinition eventDefinition) {
        return eventDefinition.getCaseAttribute() != null
                && eventDefinition.getCaseAttribute().contains(FIELD_COUNSEL_NAME)
                && eventDefinition.getCaseAttribute().contains(FIELD_DEFENDANT_NAME);
    }

    private JsonObjectBuilder prepareEventDefinitionJsonObject(final UUID hearingId, final HearingEventDefinition eventDefinition) {
        final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

        if (requireDefendantAndDefenceCounselDetails(eventDefinition)) {
            eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTES, defendantAndDefenceCounselAttributesFor(hearingId));
        }

        if (eventDefinition.getGroupLabel() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_LABEL, eventDefinition.getGroupLabel());
        }

        if (eventDefinition.getActionLabelExtension() != null) {
            eventDefinitionBuilder.add(FIELD_ACTION_LABEL_EXT, eventDefinition.getActionLabelExtension());
        }

        if (eventDefinition.getSequenceNumber() != null) {
            eventDefinitionBuilder.add(FIELD_SEQUENCE, createObjectBuilder()
                    .add(FIELD_GENERIC_ID, eventDefinition.getSequenceNumber())
                    .add(FIELD_SEQUENCE_TYPE, eventDefinition.getSequenceType())
            );
        }

        eventDefinitionBuilder
                .add(FIELD_GENERIC_ID, eventDefinition.getId().toString())
                .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                .add(FIELD_ALTERABLE, eventDefinition.isAlterable());
        return eventDefinitionBuilder;
    }

    private JsonObjectBuilder prepareEventDefinitionJsonObjectVersionTwo(final HearingEventDefinition eventDefinition) {
        final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

        if (eventDefinition.getCaseAttribute() != null) {
            final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
            Arrays.asList(eventDefinition.getCaseAttribute().split(",")).forEach(caseAttribute -> jsonArrayBuilder.add(caseAttribute.trim()));
            eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTES, jsonArrayBuilder.build());
        }

        if (eventDefinition.getGroupLabel() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_LABEL, eventDefinition.getGroupLabel());
        }

        if (eventDefinition.getActionLabelExtension() != null) {
            eventDefinitionBuilder.add(FIELD_ACTION_LABEL_EXT, eventDefinition.getActionLabelExtension());
        }

        if (eventDefinition.getSequenceNumber() != null) {
            eventDefinitionBuilder.add(FIELD_SEQUENCE, createObjectBuilder()
                    .add(FIELD_GENERIC_ID, eventDefinition.getSequenceNumber())
                    .add(FIELD_SEQUENCE_TYPE, eventDefinition.getSequenceType())
            );
        }

        eventDefinitionBuilder
                .add(FIELD_GENERIC_ID, eventDefinition.getId().toString())
                .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                .add(FIELD_ALTERABLE, eventDefinition.isAlterable());
        return eventDefinitionBuilder;
    }
}
