package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelInitiateHearingCommandHandler {

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope command) throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(
                command.payloadAsJsonObject(), InitiateHearingCommand.class);

        applyToHearingAggregate(initiateHearingCommand.getHearing().getId(),
                a -> a.initiate(initiateHearingCommand),
                command);
    }

    @Handles("hearing.command.initiate-hearing-offence")
    public void initiateHearingOffence(final JsonEnvelope command) throws EventStreamException {

        final InitiateHearingOffenceCommand initiateHearingOffenceCommand = this.jsonObjectToObjectConverter.convert(
                command.payloadAsJsonObject(), InitiateHearingOffenceCommand.class);

        applyToOffenceAggregate(initiateHearingOffenceCommand.getOffenceId(),
                a -> a.initiateHearingOffence(initiateHearingOffenceCommand),
                command);
    }

    @Handles("hearing.command.initiate-hearing-offence-plea")
    public void initiateHearingOffencePlea(final JsonEnvelope command) throws EventStreamException {

        InitiateHearingOffencePleaCommand initiateHearingOffencePleaCommand = this.jsonObjectToObjectConverter.convert(
                command.payloadAsJsonObject(), InitiateHearingOffencePleaCommand.class);

        applyToHearingAggregate(initiateHearingOffencePleaCommand.getHearingId(),
                a -> a.initiateHearingOffencePlea(initiateHearingOffencePleaCommand),
                command);
    }

    private void applyToHearingAggregate(final UUID streamId, final Function<NewModelHearingAggregate, Stream<Object>> function,
                                         final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final NewModelHearingAggregate aggregate = this.aggregateService.get(eventStream, NewModelHearingAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    private OffenceAggregate applyToOffenceAggregate(final UUID streamId, final Function<OffenceAggregate, Stream<Object>> function,
                                                     final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final OffenceAggregate aggregate = this.aggregateService.get(eventStream, OffenceAggregate.class);
        eventStream.append(function.apply(aggregate).map(this.enveloper.withMetadataFrom(envelope)));
        return aggregate;
    }
}
