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
    
    public AbstractCommandHandler(final EventSource eventSource, final Enveloper enveloper, final AggregateService aggregateService,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        assert null != eventSource && null != enveloper && null != aggregateService && null != jsonObjectToObjectConverter;
        this.eventSource = eventSource;
        this.enveloper = enveloper;
        this.aggregateService = aggregateService;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    protected <A extends Aggregate> A aggregate(final Class<A> clazz, final UUID streamId,
            final JsonEnvelope envelope, final Function<A, Stream<Object>> function) throws EventStreamException {
        final EventStream eventStream = eventSource.getStreamById(streamId);
        final A aggregate = aggregateService.get(eventStream, clazz);
        eventStream.append(function.apply(aggregate).map(enveloper.withMetadataFrom(envelope)));
        return aggregate;
    }
    
    protected <T> T convertToObject(final JsonEnvelope envelope, Class<T> clazz) {
        return this.jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), clazz);
    }
}