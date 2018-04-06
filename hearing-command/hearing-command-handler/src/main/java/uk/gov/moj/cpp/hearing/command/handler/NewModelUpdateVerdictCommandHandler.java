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
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Function;
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

        for (Defendant defendant: hearingUpdateVerdictCommand.getDefendants()){
            for (Offence offence: defendant.getOffences()){

                applyToHearingAggregate(hearingUpdateVerdictCommand.getHearingId(), hearingAggregate ->
                        hearingAggregate.updateVerdict(
                                hearingUpdateVerdictCommand.getHearingId(),
                                hearingUpdateVerdictCommand.getCaseId(),
                                defendant.getId(),
                                offence.getId(),
                                offence.getVerdict()
                        ), command);
            }
        }
    }

    private void applyToHearingAggregate(final UUID streamId, final Function<NewModelHearingAggregate, Stream<Object>> function,
                                         final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final NewModelHearingAggregate aggregate = this.aggregateService.get(eventStream, NewModelHearingAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }
}
