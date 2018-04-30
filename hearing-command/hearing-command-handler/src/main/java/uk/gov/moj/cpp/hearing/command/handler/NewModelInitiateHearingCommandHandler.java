package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterDefendantWithHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelInitiateHearingCommandHandler extends AbstractCommandHandler {

    @Inject
    public NewModelInitiateHearingCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                                 final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope envelop) throws EventStreamException {
        final InitiateHearingCommand command = convertToObject(envelop, InitiateHearingCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearing().getId(), envelop, a -> a.initiate(command));
    }

    @Handles("hearing.command.initiate-hearing-offence")
    public void initiateHearingOffence(final JsonEnvelope envelop) throws EventStreamException {
        final InitiateHearingOffenceCommand command = convertToObject(envelop, InitiateHearingOffenceCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelop, a -> a.initiateHearingOffence(command));
    }

    @Handles("hearing.command.initiate-hearing-offence-plea")
    public void initiateHearingOffencePlea(final JsonEnvelope envelop) throws EventStreamException {
        final InitiateHearingOffencePleaCommand command = convertToObject(envelop, InitiateHearingOffencePleaCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelop, a -> a.initiateHearingOffencePlea(command));
    }

    @Handles("hearing.command.initiate-hearing-defence-witness-enrich")
    public void initiateHearingDefenceWitness(final JsonEnvelope event)
            throws EventStreamException {
        final JsonObject payload = event.payloadAsJsonObject();
        aggregate(DefendantAggregate.class, UUID.fromString(payload.getString("defendantId")),
                event,
                a -> a.initiateHearingDefenceWitness(payload));

    }

    @Handles("hearing.command.register-defendant-with-hearing")
    public void recordHearingDefendant(final JsonEnvelope envelope) throws EventStreamException {
        final RegisterDefendantWithHearingCommand command = convertToObject(envelope, RegisterDefendantWithHearingCommand.class);
        aggregate(DefendantAggregate.class, command.getDefendantId(), envelope, defendantAggregate -> defendantAggregate.registerHearingId(command));
    }

}
