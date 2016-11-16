package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@ServiceComponent(Component.COMMAND_HANDLER)
public class HearingCommandHandler {

    private static final String FIELD_STREAM_ID = "hearingId";

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private HearingCommandFactory hearingCommandFactory;

    @Handles("hearing.command.list-hearing")
    public void listHearing(final JsonEnvelope envelope) throws EventStreamException {
        final UUID streamId = UUID.fromString(envelope.payloadAsJsonObject().getString(FIELD_STREAM_ID));
        final ListHearing listHearing = hearingCommandFactory.getListHearing(envelope);
        applyToAggregate(streamId, aggregate -> aggregate.listHearing(listHearing), envelope);
    }

    @Handles("hearing.command.vacate-hearing")
    public void vacateHearing(final JsonEnvelope envelope) throws EventStreamException {
        final UUID streamId = UUID.fromString(envelope.payloadAsJsonObject().getString(FIELD_STREAM_ID));
        final VacateHearing hearingVacatedEvent = hearingCommandFactory.getVacateHearing(envelope);
        applyToAggregate(streamId, aggregate -> aggregate.vacateHearing(hearingVacatedEvent), envelope);
    }

    private void applyToAggregate(final UUID streamId, final Function<HearingAggregate, Stream<Object>> function,
                                  final JsonEnvelope envelope) throws EventStreamException {
        EventStream eventStream = eventSource.getStreamById(streamId);
        HearingAggregate aggregate = aggregateService.get(eventStream, HearingAggregate.class);
        Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(enveloper.withMetadataFrom(envelope)));
    }
}
