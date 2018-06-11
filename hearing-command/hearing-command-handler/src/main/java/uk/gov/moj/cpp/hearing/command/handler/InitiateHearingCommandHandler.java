package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.LookupPleaOnOffenceForHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.LookupWitnessesOnDefendantForHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class InitiateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitiateHearingCommandHandler.class.getName());

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.initiate event received {}", envelope.payloadAsJsonObject());

        final InitiateHearingCommand command = convertToObject(envelope, InitiateHearingCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearing().getId(), envelope, a -> a.initiate(command));
    }

    @Handles("hearing.command.lookup-plea-on-offence-for-hearing")
    public void initiateHearingOffence(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.lookup-plea-on-offence-for-hearing event received {}", envelope.payloadAsJsonObject());
        final LookupPleaOnOffenceForHearingCommand command = convertToObject(envelope, LookupPleaOnOffenceForHearingCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelope, a -> a.lookupPleaForHearing(command));
    }

    @Handles("hearing.command.update-hearing-with-inherited-plea")
    public void initiateHearingOffencePlea(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.update-hearing-with-inherited-plea event received {}", envelope.payloadAsJsonObject());
        final UpdateHearingWithInheritedPleaCommand command = convertToObject(envelope, UpdateHearingWithInheritedPleaCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelope, a -> a.inheritPlea(command));
    }

    @Handles("hearing.command.lookup-witnesses-on-defendant-for-hearing")
    public void initiateHearingDefenceWitness(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.lookup-witnesses-on-defendant-for-hearing event received {}", envelope.payloadAsJsonObject());
        final LookupWitnessesOnDefendantForHearingCommand command = convertToObject(envelope, LookupWitnessesOnDefendantForHearingCommand.class);
        aggregate(DefendantAggregate.class, command.getDefendantId(), envelope, a -> a.lookupWitnessesForHearing(command));
    }

    @Handles("hearing.command.register-hearing-against-defendant")
    public void recordHearingDefendant(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.register-hearing-against-defendant event received {}", envelope.payloadAsJsonObject());
        final RegisterHearingAgainstDefendantCommand command = convertToObject(envelope, RegisterHearingAgainstDefendantCommand.class);
        aggregate(DefendantAggregate.class, command.getDefendantId(), envelope, defendantAggregate -> defendantAggregate.registerHearing(command));
    }

    @Handles("hearing.command.register-hearing-against-case")
    public void registerHearingAgainstCase(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.register-hearing-against-case event received {}", envelope.payloadAsJsonObject());
        final RegisterHearingAgainstCaseCommand command = convertToObject(envelope, RegisterHearingAgainstCaseCommand.class);
        aggregate(CaseAggregate.class, command.getCaseId(), envelope, caseAggregate -> caseAggregate.registerHearingId(command));
    }
}
