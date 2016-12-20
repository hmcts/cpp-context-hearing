package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.moj.cpp.hearing.command.handler.converter.JsonToHearingConverter.HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.AttendeeType.getAttendeeType;

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
import uk.gov.moj.cpp.hearing.domain.AttendeeType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.command.AddCaseToHearing;
import uk.gov.moj.cpp.hearing.domain.command.AllocateCourt;
import uk.gov.moj.cpp.hearing.domain.command.BookRoom;
import uk.gov.moj.cpp.hearing.domain.command.EndHearing;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.command.StartHearing;
import uk.gov.moj.cpp.hearing.domain.command.CreateHearingEventDefinitions;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(Component.COMMAND_HANDLER)
public class HearingCommandHandler {

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject HearingCommandFactory hearingCommandFactory;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private JsonToHearingConverter jsonToHearingConverter;

    @Handles("hearing.initiate-hearing")
    public void initiateHearing(final JsonEnvelope envelope) throws EventStreamException {
        final UUID streamId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final InitiateHearing initiateHearing = jsonToHearingConverter.convertToInitiateHearing(envelope);
        applyToAggregate(streamId, aggregate -> aggregate.initiateHearing(initiateHearing), envelope);
    }

    @Handles("hearing.allocate-court")
    public void allocateCourt(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final AllocateCourt allocateCourt = jsonToHearingConverter.convertToAllocateCourt(envelope);
        applyToAggregate(hearingId, aggregate -> aggregate.allocateCourt(allocateCourt), envelope);
    }

    @Handles("hearing.book-room")
    public void bookRoom(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final BookRoom bookRoom = jsonToHearingConverter.convertToBookRoom(envelope);
        applyToAggregate(hearingId, aggregate -> aggregate.bookRoom(bookRoom), envelope);
    }

    @Handles("hearing.start")
    public void start(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final StartHearing startHearing = jsonToHearingConverter.convertToStartHearing(envelope);
        applyToAggregate(hearingId, aggregate -> aggregate.startHearing(startHearing), envelope);
    }

    @Handles("hearing.end")
    public void end(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final EndHearing endHearing = jsonToHearingConverter.convertToEndHearing(envelope);
        applyToAggregate(hearingId, aggregate -> aggregate.endHearing(endHearing), envelope);
    }

    @Handles("hearing.add-case")
    public void addCase(final JsonEnvelope envelope) throws EventStreamException {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
        final AddCaseToHearing addCaseToHearing = jsonToHearingConverter.convertToAddCase(envelope);
        applyToAggregate(hearingId, aggregate -> aggregate.addCaseToHearing(addCaseToHearing), envelope);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addAttendee(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        final UUID personId = fromString(payload.getString("personId"));
        final UUID attendeeId = fromString(payload.getString("attendeeId"));
        final String status = payload.getString("status");
        applyToAggregate(hearingId, aggregate -> aggregate.addProsecutionCounsel(hearingId, attendeeId, personId, status), command);

    }

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) throws EventStreamException {
        final UUID streamId = UUID.fromString(envelope.payloadAsJsonObject().getString("id"));
        final CreateHearingEventDefinitions createHearingEventDefinitions = hearingCommandFactory.createHearingEventDefinitionsFrom(envelope.payloadAsJsonObject());
        EventStream eventStream = eventSource.getStreamById(streamId);
        Stream<Object> events = Stream.of(new HearingEventDefinitionsCreated(createHearingEventDefinitions.getUUID(), createHearingEventDefinitions.getEventDefinitions()));
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }

    private void applyToAggregate(final UUID streamId, final Function<HearingAggregate, Stream<Object>> function,
                                  final JsonEnvelope envelope) throws EventStreamException {
        EventStream eventStream = eventSource.getStreamById(streamId);
        HearingAggregate aggregate = aggregateService.get(eventStream, HearingAggregate.class);
        Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }
}
