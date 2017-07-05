package uk.gov.moj.cpp.hearing.query.view;

import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselRepository;
import uk.gov.moj.cpp.hearing.persist.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.persist.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselToDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinition;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

@SuppressWarnings("CdiInjectionPointsInspection")
@ServiceComponent(Component.QUERY_VIEW)
public class HearingEventQueryView {

    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";

    private static final String FIELD_HEARING_ID = "hearingId";

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
    private static final String FIELD_SEQUENCE_ID = "id";
    private static final String FIELD_SEQUENCE_TYPE = "type";

    @Inject
    private Enveloper enveloper;

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Inject
    private DefenceCounselRepository defenceCounselRepository;

    @Inject
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Handles("hearing.get-hearing-event-definitions")
    public JsonEnvelope getHearingEventDefinitions(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final List<HearingEventDefinition> hearingEventDefinitions = hearingEventDefinitionRepository.findAllOrderBySequenceTypeSequenceNumberAndActionLabel();
        final JsonArrayBuilder eventDefinitionsJsonArrayBuilder = createArrayBuilder();

        hearingEventDefinitions.forEach(eventDefinition -> {
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
                        .add(FIELD_SEQUENCE_ID, eventDefinition.getSequenceNumber())
                        .add(FIELD_SEQUENCE_TYPE, eventDefinition.getSequenceType())
                );
            }
            eventDefinitionsJsonArrayBuilder.add(
                    eventDefinitionBuilder
                            .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                            .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
            );
        });

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_DEFINITIONS)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_EVENT_DEFINITIONS, eventDefinitionsJsonArrayBuilder)
                        .build());
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        final String hearingId = query.payloadAsJsonObject().getString(FIELD_HEARING_ID);

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(fromString(hearingId));

        final JsonArrayBuilder eventLogJsonArrayBuilder = createArrayBuilder();

        hearingEvents.
                forEach(hearingEvent -> eventLogJsonArrayBuilder.add(
                        createObjectBuilder()
                                .add(FIELD_HEARING_EVENT_ID, hearingEvent.getId().toString())
                                .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel())
                                .add(FIELD_EVENT_TIME, ZonedDateTimes.toString(hearingEvent.getEventTime()))
                                .add(FIELD_LAST_MODIFIED_TIME, ZonedDateTimes.toString(hearingEvent.getLastModifiedTime()))
                ));

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_LOG)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId)
                        .add(FIELD_HEARING_EVENTS, eventLogJsonArrayBuilder)
                        .build()
                );
    }

    private JsonArrayBuilder defendantAndDefenceCounselAttributesFor(final UUID hearingId) {
        final List<DefenceCounselToDefendant> defenceCounselDefendants = defenceCounselRepository.findDefenceCounselAndDefendantByHearingId(hearingId);
        final JsonArrayBuilder caseAttributesJsonArrayBuilder = createArrayBuilder();

        defenceCounselDefendants.forEach(defenceCounselDefendant -> caseAttributesJsonArrayBuilder.add(
                createObjectBuilder()
                        .add(FIELD_COUNSEL_NAME, defenceCounselDefendant.getPersonId().toString())
                        .add(FIELD_DEFENDANT_NAME, defenceCounselDefendant.getDefendantId().toString())
        ));

        return caseAttributesJsonArrayBuilder;
    }

    private boolean requireDefendantAndDefenceCounselDetails(final HearingEventDefinition eventDefinition) {
        return eventDefinition.getCaseAttribute() != null
                && eventDefinition.getCaseAttribute().contains(FIELD_COUNSEL_NAME)
                && eventDefinition.getCaseAttribute().contains(FIELD_DEFENDANT_NAME);
    }

}
