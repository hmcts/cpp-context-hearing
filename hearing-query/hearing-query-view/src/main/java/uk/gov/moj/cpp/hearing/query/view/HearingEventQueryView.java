package uk.gov.moj.cpp.hearing.query.view;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"CdiInjectionPointsInspection", "WeakerAccess", "squid:S1172", "squid:CommentedOutCodeLine", "squid:S1481", "squid:S1854"})
@ServiceComponent(QUERY_VIEW)
public class HearingEventQueryView {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingEventQueryView.class.getName());

    private static final String FIELD_DEFENCE_COUNSEL_ID = "defenceCounselId";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM = "hearing.get-active-hearings-for-court-room";

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
    private static final String FIELD_ACTION_SEQUENCE = "actionSequence";
    private static final String FIELD_GROUP_SEQUENCE = "groupSequence";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTIVE_HEARINGS = "activeHearings";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_EVENT_DATE = "eventDate";

    private static final String FIELD_CASE_ATTRIBUTES = "caseAttributes";
    private static final String FIELD_DEFENDANT_NAME = "defendant.name";
    private static final String FIELD_COUNSEL_NAME = "counsel.name";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_HAS_ACTIVE_HEARING = "hasActiveHearing";
    private static final String RESUME_HEARING_EVENT_DEFINITION_ID = "64476e43-2138-46d5-b58b-848582cf9b07";
    private static final String PAUSE_HEARING_EVENT_DEFINITION_ID = "160ecb51-29ee-4954-bbbf-daab18a24fbb";
    private static final String END_HEARING_EVENT_DEFINITION_ID = "0df93f18-0a21-40f5-9fb3-da4749cd70fe";

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
        final LocalDate date = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_DATE));

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(fromString(hearingId), date);

        final List<UUID> hearingIds = getActiveHearingsForCourtRoom(fromString(hearingId), date);

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

                            if (hearingEvent.getDefenceCounselId() != null) {
                                jsonObjectBuilder.add(FIELD_DEFENCE_COUNSEL_ID,
                                        hearingEvent.getDefenceCounselId().toString());
                            }

                            eventLogJsonArrayBuilder.add(jsonObjectBuilder);
                        }
                );

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_LOG)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId)
                        .add(FIELD_HAS_ACTIVE_HEARING, isNotEmpty(hearingIds) ? TRUE : FALSE)
                        .add(FIELD_HEARING_EVENTS, eventLogJsonArrayBuilder)
                        .build()
                );
    }

    @Handles("hearing.get-active-hearings-for-court-room")
    public JsonEnvelope getActiveHearingsForCourtRoom(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final LocalDate eventDate = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_EVENT_DATE));

        final List<UUID> hearingIds = getActiveHearingsForCourtRoom(hearingId, eventDate);

        final JsonArrayBuilder activeHearingIds = createArrayBuilder();
        hearingIds.forEach(id -> activeHearingIds.add(id.toString()));

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM)
                .apply(createObjectBuilder()
                        .add(FIELD_ACTIVE_HEARINGS, activeHearingIds)
                        .build()
                );
    }

    private List<UUID> getActiveHearingsForCourtRoom(final UUID hearingId, final LocalDate date) {
        final Hearing hearing = hearingRepository.findBy(hearingId);

        final List<HearingEvent> hearingEvents =
                hearingEventRepository
                        .findHearingEvents(
                                hearing.getCourtCentre().getId(),
                                hearing.getCourtCentre().getRoomId(),
                                date);

        return getActiveHearingIdsByHearingEvents(hearingEvents);
    }

    private List<UUID> getActiveHearingIdsByHearingEvents(final List<HearingEvent> hearingEvents) {

        final List<UUID> activeHearings = new ArrayList<>();

        final Map<UUID, Map<UUID, Long>> hearingIdsGroupByEventDefinitionId = hearingEvents.stream().collect(
                Collectors.groupingBy(HearingEvent::getHearingId, Collectors.groupingBy(HearingEvent::getHearingEventDefinitionId, Collectors.counting())));

        final List<UUID> hearingsWithStartEvents = hearingIdsGroupByEventDefinitionId.entrySet().stream()
                .filter(h -> !h.getValue().containsKey(fromString(PAUSE_HEARING_EVENT_DEFINITION_ID)))
                .filter(h -> !h.getValue().containsKey(fromString(RESUME_HEARING_EVENT_DEFINITION_ID)))
                .filter(h -> !h.getValue().containsKey(fromString(END_HEARING_EVENT_DEFINITION_ID)))
                .filter(h -> h.getValue().containsValue(1L))
                .map(Map.Entry::getKey).distinct().collect(Collectors.toList());

        final Map<UUID, Long> hearingsWithPauseEvents = getEventsByHearingDefinition(hearingIdsGroupByEventDefinitionId, PAUSE_HEARING_EVENT_DEFINITION_ID);

        final Map<UUID, Long> hearingsWithResumeEvents = getEventsByHearingDefinition(hearingIdsGroupByEventDefinitionId, RESUME_HEARING_EVENT_DEFINITION_ID);

        hearingsWithPauseEvents.forEach((hearingId, pauseCount) -> {
            final Long resumeCount = hearingsWithResumeEvents.getOrDefault(hearingId, 0L);
            if (pauseCount.equals(resumeCount)) {
                activeHearings.add(hearingId);
            }
        });

        activeHearings.addAll(hearingsWithStartEvents);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Active hearings received from same court room {}", activeHearings);
        }

        return activeHearings;
    }

    private Map<UUID, Long> getEventsByHearingDefinition(Map<UUID, Map<UUID, Long>> hearingIdsGroupByEventDefinitionId, String hearingEventDefinitionId) {
        final Map<UUID, Long> hearingEvents = new HashMap<>();
        hearingIdsGroupByEventDefinitionId.forEach((hearingId, hearingEventDefinition) -> {
            if (hearingEventDefinition.containsKey(fromString(hearingEventDefinitionId)) && !hearingEventDefinition.containsKey(fromString(END_HEARING_EVENT_DEFINITION_ID))) {
                hearingEvents.put(hearingId, hearingEventDefinition.get(fromString(hearingEventDefinitionId)));
            }
        });
        return hearingEvents;
    }

    private JsonArrayBuilder defendantAndDefenceCounselAttributesFor(final UUID hearingId) {

        final Hearing aHearing = hearingRepository.findBy(hearingId);

        final JsonArrayBuilder caseAttributesJsonArrayBuilder = createArrayBuilder();

//        aHearing.getAttendees().stream()
//                .filter(a -> a instanceof DefenceAdvocate)
//                .map(DefenceAdvocate.class::cast)
//                .forEach(defenceAdvocate ->
//                    caseAttributesJsonArrayBuilder.add(
//                            createObjectBuilder()
//                                    .add(FIELD_COUNSEL_NAME, defenceAdvocate.getId().toString())
//                                    .add(FIELD_DEFENDANT_NAME, defenceAdvocate.getDefendants().get(0).getId().toString())
//                    )
//                );

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

        if (eventDefinition.getActionSequence() != null) {
            eventDefinitionBuilder.add(FIELD_ACTION_SEQUENCE, eventDefinition.getActionSequence());
        }

        if (eventDefinition.getGroupSequence() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_SEQUENCE, eventDefinition.getActionSequence());
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

        if (eventDefinition.getActionSequence() != null) {
            eventDefinitionBuilder.add(FIELD_ACTION_SEQUENCE, eventDefinition.getActionSequence());
        }

        if (eventDefinition.getGroupSequence() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_SEQUENCE, eventDefinition.getGroupSequence());
        }

        eventDefinitionBuilder
                .add(FIELD_GENERIC_ID, eventDefinition.getId().toString())
                .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                .add(FIELD_ALTERABLE, eventDefinition.isAlterable());
        return eventDefinitionBuilder;
    }
}
