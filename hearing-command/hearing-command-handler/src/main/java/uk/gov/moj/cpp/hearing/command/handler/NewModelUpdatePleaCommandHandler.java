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
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelUpdatePleaCommandHandler {

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingCommandHandler hearingCommandHandler;

    @Handles("hearing.command.update-plea")
    public void updatePlea(final JsonEnvelope command) throws EventStreamException {

        /*
        Would like to change the input document to be

            [{
				"offenceId": "e7392829-4e80-4486-adab-21f1a23cd18a",
				"originalHearingId": "2b407bdc-e20e-4536-9fd9-d4cd5c4b30bf",
				"plea": {
					"pleaDate": "2017-02-01",
					"value": "GUILTY"
				}
			}]

         */

        final HearingUpdatePleaCommand hearingUpdatePleaCommand = this.jsonObjectToObjectConverter.convert(command.payloadAsJsonObject(), HearingUpdatePleaCommand.class);

        for (Offence offence : forAllOffences(hearingUpdatePleaCommand).collect(Collectors.toList())) {

            applyToOffenceAggregate(offence.getId(), offenceAggregate ->
                    offenceAggregate.updatePlea(
                            hearingUpdatePleaCommand.getHearingId(),
                            offence.getPlea()
                    ), command);
        }

        //TODO - GPE-3032 - cleanup
        hearingCommandHandler.updatePlea(command);
    }

    private static Stream<Offence> forAllOffences(HearingUpdatePleaCommand command) {
        return command.getDefendants().stream().flatMap(d -> d.getOffences().stream());
    }

    private OffenceAggregate applyToOffenceAggregate(final UUID streamId, final Function<OffenceAggregate, Stream<Object>> function,
                                                     final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final OffenceAggregate aggregate = this.aggregateService.get(eventStream, OffenceAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
        return aggregate;
    }
}
