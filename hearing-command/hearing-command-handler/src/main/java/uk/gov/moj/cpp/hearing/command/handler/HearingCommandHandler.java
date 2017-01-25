package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;
import static uk.gov.moj.cpp.hearing.command.handler.converter.JsonToHearingConverter.HEARING_ID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.handler.converter.JsonToHearingConverter;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventsLogAggregate;
import uk.gov.moj.cpp.hearing.domain.command.AddCaseToHearing;
import uk.gov.moj.cpp.hearing.domain.command.AllocateCourt;
import uk.gov.moj.cpp.hearing.domain.command.BookRoom;
import uk.gov.moj.cpp.hearing.domain.command.EndHearing;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.command.StartHearing;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

@ServiceComponent(Component.COMMAND_HANDLER)
public class HearingCommandHandler {

    private static final String FIELD_ID = "id";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LATEST_HEARING_EVENT_ID = "latestHearingEventId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_START_DATE= "startDate";

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private JsonToHearingConverter jsonToHearingConverter;

    @Handles("hearing.initiate-hearing")
    public void initiateHearing(final JsonEnvelope envelope) throws EventStreamException {
        final UUID streamId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final InitiateHearing initiateHearing = jsonToHearingConverter.convertToInitiateHearing(envelope);
        applyToHearingAggregate(streamId, aggregate -> aggregate.initiateHearing(initiateHearing), envelope);
    }

    @Handles("hearing.allocate-court")
    public void allocateCourt(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final AllocateCourt allocateCourt = jsonToHearingConverter.convertToAllocateCourt(envelope);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.allocateCourt(allocateCourt), envelope);
    }

    @Handles("hearing.book-room")
    public void bookRoom(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final BookRoom bookRoom = jsonToHearingConverter.convertToBookRoom(envelope);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.bookRoom(bookRoom), envelope);
    }

    @Handles("hearing.start")
    public void start(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final StartHearing startHearing = jsonToHearingConverter.convertToStartHearing(envelope);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.startHearing(startHearing), envelope);
    }

    @Handles("hearing.adjourn-date")
    public void adjournHearingDate(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final LocalDate startDate = LocalDate.parse(envelope.payloadAsJsonObject().getString(FIELD_START_DATE));
        applyToHearingAggregate(hearingId, aggregate -> aggregate.adjournHearingDate(hearingId, startDate), envelope);
    }

    @Handles("hearing.end")
    public void end(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final EndHearing endHearing = jsonToHearingConverter.convertToEndHearing(envelope);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.endHearing(endHearing), envelope);
    }

    @Handles("hearing.add-case")
    public void addCase(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final AddCaseToHearing addCaseToHearing = jsonToHearingConverter.convertToAddCase(envelope);
        applyToHearingAggregate(hearingId, aggregate -> aggregate.addCaseToHearing(addCaseToHearing), envelope);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        final UUID personId = fromString(payload.getString("personId"));
        final UUID attendeeId = fromString(payload.getString("attendeeId"));
        final String status = payload.getString("status");
        applyToHearingAggregate(hearingId, aggregate -> aggregate.addProsecutionCounsel(hearingId, attendeeId, personId, status), command);
    }

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        final UUID personId = fromString(payload.getString("personId"));
        final UUID attendeeId = fromString(payload.getString("attendeeId"));
        final String status = payload.getString("status");
        final JsonArray jsonArray = payload.getJsonArray("defendantIds");
        final List<UUID> defendantIds = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final String defendantIdString = jsonArray.getJsonObject(i).getString("defendantId");
            defendantIds.add(fromString(defendantIdString));
        }
        applyToHearingAggregate(hearingId, aggregate -> aggregate.addDefenceCounsel(hearingId, attendeeId, personId, defendantIds, status), command);
    }

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) throws EventStreamException {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingEventDefinitionsId = fromString(payload.getString("id"));
        final List<HearingEventDefinition> hearingEventDefinitions = payload
                .getJsonArray("eventDefinitions").getValuesAs(JsonObject.class)
                .stream().map(hearingDefinitionJson -> new HearingEventDefinition(
                        hearingDefinitionJson.getString("actionLabel"),
                        hearingDefinitionJson.getString("recordedLabel"),
                        hearingDefinitionJson.getInt("sequence"),
                        hearingDefinitionJson.containsKey("caseAttribute") ? hearingDefinitionJson.getString("caseAttribute") : null))
                .collect(toList());

        final EventStream eventStream = eventSource.getStreamById(hearingEventDefinitionsId);
        final Stream<Object> events = Stream.of(new HearingEventDefinitionsCreated(hearingEventDefinitionsId, hearingEventDefinitions));
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID defendantId = fromString(payload.getString("defendantId"));
        final UUID targetId = fromString(payload.getString("targetId"));
        final UUID offenceId = fromString(payload.getString("offenceId"));
        final String draftResult = payload.getString("draftResult");
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));

        final Stream<Object> events = streamOf(new DraftResultSaved(targetId, defendantId, offenceId, draftResult, hearingId));
        eventSource.getStreamById(hearingId).append(events.map(enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.log-hearing-event")
    public void logHearingEvent(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime timestamp = fromJsonString(payload.getJsonString(FIELD_TIMESTAMP));

        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingEventsLogAggregate aggregate = aggregateService.get(eventStream, HearingEventsLogAggregate.class);
        final Stream<Object> events = aggregate.logHearingEvent(hearingId, hearingEventId, recordedLabel, timestamp);
        eventStream.append(events.map(enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.correct-hearing-event")
    public void correctEvent(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final UUID hearingEventId = fromString(payload.getString(FIELD_HEARING_EVENT_ID));
        final String recordedLabel = payload.getString(FIELD_RECORDED_LABEL);
        final ZonedDateTime timestamp = fromJsonString(payload.getJsonString(FIELD_TIMESTAMP));
        final UUID latestHearingEventId = fromString(payload.getString(FIELD_LATEST_HEARING_EVENT_ID));

        final EventStream eventStream = eventSource.getStreamById(hearingId);
        final HearingEventsLogAggregate aggregate = aggregateService.get(eventStream, HearingEventsLogAggregate.class);
        final Stream<Object> events = aggregate.correctEvent(hearingId, hearingEventId, recordedLabel, timestamp, latestHearingEventId);
        eventStream.append(events.map(enveloper.withMetadataFrom(command)));
    }

    private void applyToHearingAggregate(final UUID streamId, final Function<HearingAggregate, Stream<Object>> function,
                                         final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = eventSource.getStreamById(streamId);
        final HearingAggregate aggregate = aggregateService.get(eventStream, HearingAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }
}
