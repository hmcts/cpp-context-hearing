package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.moj.cpp.hearing.command.initiate.RegisterCaseWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterDefendantWithHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class InitiateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitiateHearingCommandHandler.class.getName());

    @Inject
    public InitiateHearingCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                         final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.initiate event received {}", envelope.payloadAsJsonObject());

        final InitiateHearingCommand command = convertToObject(envelope, InitiateHearingCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearing().getId(), envelope, a -> a.initiate(command));
    }

    @Handles("hearing.command.initiate-hearing-offence")
    public void initiateHearingOffence(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.initiate-hearing-offence event received {}", envelope.payloadAsJsonObject());
        final InitiateHearingOffenceCommand command = convertToObject(envelope, InitiateHearingOffenceCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelope, a -> a.initiateHearingOffence(command));
    }

    @Handles("hearing.command.initiate-hearing-offence-plea")
    public void initiateHearingOffencePlea(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.initiate-hearing-offence-plea event received {}", envelope.payloadAsJsonObject());
        final InitiateHearingOffencePleaCommand command = convertToObject(envelope, InitiateHearingOffencePleaCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelope, a -> a.initiateHearingOffencePlea(command));
    }

    @Handles("hearing.command.initiate-hearing-defence-witness-enrich")
    public void initiateHearingDefenceWitness(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.initiate-hearing-defence-witness-enrich event received {}", envelope.payloadAsJsonObject());
        final JsonObject payload = envelope.payloadAsJsonObject();
        aggregate(DefendantAggregate.class, UUID.fromString(payload.getString("defendantId")),
                envelope,
                a -> a.initiateHearingDefenceWitness(payload));
    }

    @Handles("hearing.command.register-defendant-with-hearing")
    public void recordHearingDefendant(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.register-defendant-with-hearing event received {}", envelope.payloadAsJsonObject());
        final RegisterDefendantWithHearingCommand command = convertToObject(envelope, RegisterDefendantWithHearingCommand.class);
        aggregate(DefendantAggregate.class, command.getDefendantId(), envelope, defendantAggregate -> defendantAggregate.registerHearingId(command));
    }

    @Handles("hearing.command.register-case-with-hearing")
    public void recordHearingCase(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.register-case-with-hearing event received {}", envelope.payloadAsJsonObject());
        final RegisterCaseWithHearingCommand command = convertToObject(envelope, RegisterCaseWithHearingCommand.class);
        aggregate(CaseAggregate.class, command.getCaseId(), envelope, caseAggregate -> caseAggregate.registerHearingId(command));
    }

}
