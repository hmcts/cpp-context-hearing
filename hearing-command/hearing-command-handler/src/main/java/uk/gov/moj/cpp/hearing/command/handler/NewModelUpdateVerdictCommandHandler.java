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
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelUpdateVerdictCommandHandler {

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


    @Handles("hearing.command.update-verdict")
    public void updateVerdict(final JsonEnvelope command) throws EventStreamException {

        /*
            This is the perfect update verdict packet.
                [{
                    "offenceId": "a8a87b20-b8d7-4f04-9a5e-08b366b85723"
                    "originHearingId": "518751a6-05b3-4a51-8733-b8df4d916799"
            		"verdictDate": "2018-03-03",
					"unanimous": true,
					"numberOfSplitJurors": 2,
					"numberOfJurors": 11
					"value": {
						"code": "A1",
						"description": "Guilty By Jury On Judges Direction",
						"category": "GUILTY"
					},
                }]
         */

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = this.jsonObjectToObjectConverter.convert(command.payloadAsJsonObject(), HearingUpdateVerdictCommand.class);

        for (Offence offence : forAllOffences(hearingUpdateVerdictCommand).collect(Collectors.toList())) {

            applyToOffenceAggregate(offence.getId(), offenceAggregate ->
                    offenceAggregate.updateVerdict(
                            hearingUpdateVerdictCommand.getHearingId(),
                            hearingUpdateVerdictCommand.getCaseId(),
                            offence.getId(),
                            offence.getVerdict()
                    ), command);
        }

        //TODO - GPE-3032 - cleanup
        hearingCommandHandler.updateVerdict(command);
    }

    private static Stream<Offence> forAllOffences(HearingUpdateVerdictCommand command) {
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
