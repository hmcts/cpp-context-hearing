package uk.gov.moj.cpp.hearing.command.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonArray;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

abstract class AbstractCommandHandler {

    @Inject
    protected EventSource eventSource;
    @Inject
    protected Enveloper enveloper;
    @Inject
    protected AggregateService aggregateService;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    protected <A extends Aggregate> A aggregate(final Class<A> clazz, final UUID streamId,
                                                final JsonEnvelope envelope, final Function<A, Stream<Object>> function) throws EventStreamException {
        final EventStream eventStream = eventSource.getStreamById(streamId);
        final A aggregate = aggregateService.get(eventStream, clazz);
        eventStream.append(function.apply(aggregate).map(enveloper.withMetadataFrom(envelope)));
        return aggregate;
    }

    protected <T> T convertToObject(final JsonEnvelope envelope, final Class<T> clazz) {
        return this.jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), clazz);
    }

    protected <T> List<T> convertToList(final JsonArray jsonArray, final Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            list.add(this.jsonObjectToObjectConverter.convert(jsonArray.getJsonObject(i), clazz));
        }
        return list;
    }
}