package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import java.util.stream.Stream;

import javax.inject.Inject;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelShareResultsCommandHandler extends AbstractCommandHandler {

    private final Clock clock;

    @Inject
    public NewModelShareResultsCommandHandler(final EventSource eventSource, final Enveloper enveloper,
            final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter,
            final Clock clock) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
        this.clock = clock;
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) throws EventStreamException {
        final SaveDraftResultCommand command = convertToObject(envelope, SaveDraftResultCommand.class);
        final Stream<Object> events = Stream.of(DraftResultSaved.builder()
                .withHearingId(command.getHearingId())
                .withTargetId(command.getTargetId())
                .withDefendantId(command.getDefendantId())
                .withOffenceId(command.getOffenceId())
                .withDraftResult(command.getDraftResult())
                .build());
        this.eventSource.getStreamById(command.getHearingId()).append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    @Handles("hearing.command.share-results")
    public void shareResult(final JsonEnvelope envelope) throws EventStreamException {
        final ShareResultsCommand command = convertToObject(envelope, ShareResultsCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelope,
                aggregate -> aggregate.shareResults(command, clock.now()));
    }
}