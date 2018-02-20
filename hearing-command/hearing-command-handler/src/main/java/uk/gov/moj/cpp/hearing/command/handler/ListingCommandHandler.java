package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.listing.Hearing;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingsPleaAggregate;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ListingCommandHandler {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ListingCommandHandler.class.getName());

    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_CASE_URN = "urn";
    private static final String FIELD_HEARING = "hearing";


    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Handles("hearing.record-confirmed-hearing")
    public void recordHearingConfirmed(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("Processing hearing.record-confirmed-hearing event");
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID caseId = fromString(payload.getString(FIELD_CASE_ID));
        final String urn = payload.getString(FIELD_CASE_URN);
        final Hearing hearing = jsonObjectToObjectConverter.convert(payload.getJsonObject(FIELD_HEARING), Hearing.class);
        applyToCaseHearingAggregate(caseId, aggregate -> aggregate.recordHearingConfirmed(caseId, urn, hearing), command);

    }

    private void applyToCaseHearingAggregate(final UUID streamId, final Function<HearingsPleaAggregate, Stream<Object>> function,
                                             final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final HearingsPleaAggregate hearingsPleaAggregate = this.aggregateService.get(eventStream, HearingsPleaAggregate.class);
        final Stream<Object> events = function.apply(hearingsPleaAggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

}
