package uk.gov.moj.cpp.hearing.query.view;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"CdiInjectionPointsInspection", "WeakerAccess", "squid:S1172", "squid:CommentedOutCodeLine", "squid:S1481", "squid:S1854"})
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
    private HearingService hearingService;

    public Envelope<JsonObject> getHearingEventDefinitions(final JsonEnvelope query) {
        final List<HearingEventDefinition> hearingEventDefinitions = hearingService.getHearingEventDefinitions();
        final JsonArrayBuilder eventDefinitionsJsonArrayBuilder = createArrayBuilder();
        final JsonObjectBuilder objectBuilder = createObjectBuilder();

        hearingEventDefinitions.forEach(eventDefinition -> eventDefinitionsJsonArrayBuilder.add(prepareEventDefinitionJsonObjectVersionTwo(eventDefinition)));
        objectBuilder.add(FIELD_HEARING_EVENT_DEFINITIONS, eventDefinitionsJsonArrayBuilder);

        return envelop(objectBuilder.build())
                .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS)
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getHearingEventDefinition(final JsonEnvelope query) {
        final UUID hearingEventDefinitionId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_EVENT_DEFINITION_ID));
        final Optional<HearingEventDefinition> optionalHearingEventDefinition = hearingService.getHearingEventDefinition(hearingEventDefinitionId);

        if (optionalHearingEventDefinition.isPresent()) {
            final JsonObject jsonObject = prepareEventDefinitionJsonObject(optionalHearingEventDefinition.get()).build();
            return envelop(jsonObject)
                    .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITION)
                    .withMetadataFrom(query);
        }

        return envelop((JsonObject)null)
                .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITION)
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getHearingEventLog(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final LocalDate date = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_DATE));
        final List<HearingEvent> hearingEvents = hearingService.getHearingEvents(hearingId, date);
        final List<UUID> hearingIds = getActiveHearingsForCourtRoom(hearingId, date);
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

                            if (nonNull(hearingEvent.getDefenceCounselId())) {
                                jsonObjectBuilder.add(FIELD_DEFENCE_COUNSEL_ID,
                                        hearingEvent.getDefenceCounselId().toString());
                            }

                            eventLogJsonArrayBuilder.add(jsonObjectBuilder);
                        }
                );

        return envelop(createObjectBuilder()
                .add(FIELD_HEARING_ID, hearingId.toString())
                .add(FIELD_HAS_ACTIVE_HEARING, isNotEmpty(hearingIds) ? TRUE : FALSE)
                .add(FIELD_HEARING_EVENTS, eventLogJsonArrayBuilder)
                .build())
                .withName(RESPONSE_NAME_HEARING_EVENT_LOG)
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getActiveHearingsForCourtRoom(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final LocalDate eventDate = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_EVENT_DATE));
        final List<UUID> hearingIds = getActiveHearingsForCourtRoom(hearingId, eventDate);
        final JsonObjectBuilder objectBuilder = createObjectBuilder();
        final JsonArrayBuilder activeHearingIds = createArrayBuilder();

        hearingIds.forEach(id -> activeHearingIds.add(id.toString()));
        objectBuilder.add(FIELD_ACTIVE_HEARINGS, activeHearingIds);

        return envelop(objectBuilder.build())
                .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM)
                .withMetadataFrom(query);
    }

    private List<UUID> getActiveHearingsForCourtRoom(final UUID hearingId, final LocalDate date) {
        final Optional<CourtCentre> optionalCourtCentre = hearingService.getCourtCenterByHearingId(hearingId);
        if (!optionalCourtCentre.isPresent()) {
            return Collections.emptyList();
        }

        final CourtCentre courtCentre = optionalCourtCentre.get();
        final List<HearingEvent> hearingEvents =
                hearingService
                        .getHearingEvents(
                                courtCentre.getId(),
                                courtCentre.getRoomId(),
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

    private boolean requireDefendantAndDefenceCounselDetails(final HearingEventDefinition eventDefinition) {
        return eventDefinition.getCaseAttribute() != null
                && eventDefinition.getCaseAttribute().contains(FIELD_COUNSEL_NAME)
                && eventDefinition.getCaseAttribute().contains(FIELD_DEFENDANT_NAME);
    }

    private JsonObjectBuilder prepareEventDefinitionJsonObject(final HearingEventDefinition eventDefinition) {
        final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

        if (requireDefendantAndDefenceCounselDetails(eventDefinition)) {
            eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTES, createArrayBuilder());
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
