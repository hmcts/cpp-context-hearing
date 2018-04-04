package uk.gov.moj.cpp.hearing.command.handler;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

abstract class AbstractCommandHandler {

    protected final EventSource eventSource;
    protected final Enveloper enveloper;
    protected final AggregateService aggregateService;
    protected final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    protected final HearingCommandHandler hearingCommandHandler;
    
    public AbstractCommandHandler(final EventSource eventSource, final Enveloper enveloper, final AggregateService aggregateService,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter, final HearingCommandHandler hearingCommandHandler) {
        this.eventSource = eventSource;
        this.enveloper = enveloper;
        this.aggregateService = aggregateService;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.hearingCommandHandler = hearingCommandHandler;
    }

    protected <A extends Aggregate> A aggregate(final UUID streamId, final JsonEnvelope envelope,
            final Class<A> clazz, final Function<A, Stream<Object>> function) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final A aggregate = this.aggregateService.get(eventStream, clazz);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
        return aggregate;
    }
    
    protected <T> T convertToObject(final JsonEnvelope envelope, Class<T> clazz) {
        return this.jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), clazz);
    }
}