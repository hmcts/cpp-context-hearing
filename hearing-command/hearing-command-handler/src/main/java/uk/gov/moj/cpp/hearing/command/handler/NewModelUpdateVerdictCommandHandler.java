package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelUpdateVerdictCommandHandler extends AbstractCommandHandler {

    @Inject
    public NewModelUpdateVerdictCommandHandler(EventSource eventSource, Enveloper enveloper, AggregateService aggregateService, JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.command.update-verdict")
    public void updateVerdict(final JsonEnvelope command) throws EventStreamException {

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = this.jsonObjectToObjectConverter.convert(command.payloadAsJsonObject(), HearingUpdateVerdictCommand.class);

        for (Defendant defendant: hearingUpdateVerdictCommand.getDefendants()){
            for (Offence offence: defendant.getOffences()){

                aggregate(NewModelHearingAggregate.class, hearingUpdateVerdictCommand.getHearingId(), command,
                        hearingAggregate ->
                                hearingAggregate.updateVerdict(
                                        hearingUpdateVerdictCommand.getHearingId(),
                                        hearingUpdateVerdictCommand.getCaseId(),
                                        offence.getId(),
                                        offence.getVerdict()
                                )
                        );
            }
        }
    }
}
