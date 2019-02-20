package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class InitiateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitiateHearingCommandHandler.class.getName());

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.initiate event received {}", envelope.toObfuscatedDebugString());
        }
        final InitiateHearingCommand command = convertToObject(envelope, InitiateHearingCommand.class);
        // initiate Hearing command must not contain verdict and plea
        command.getHearing().getProsecutionCases().stream()
                .flatMap(p -> p.getDefendants().stream())
                .collect(Collectors.toList())
                .stream()
                .flatMap(d -> d.getOffences().stream())
                .forEach(o -> {
                    o.setVerdict(null);
                    o.setPlea(null);
                });
        aggregate(HearingAggregate.class, command.getHearing().getId(), envelope, a -> a.initiate(command.getHearing()));
    }

    @Handles("hearing.command.register-hearing-against-offence")
    public void initiateHearingOffence(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-offence event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstOffenceCommand command = convertToObject(envelope, RegisterHearingAgainstOffenceCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelope, a -> a.lookupOffenceForHearing(command.getHearingId(), command.getOffenceId()));
    }

    @Handles("hearing.command.update-hearing-with-inherited-plea")
    public void initiateHearingOffencePlea(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-hearing-with-inherited-plea event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateHearingWithInheritedPleaCommand command = convertToObject(envelope, UpdateHearingWithInheritedPleaCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope, a -> a.inheritPlea(command.getHearingId(), command.getPlea()));
    }

    @Handles("hearing.command.update-hearing-with-inherited-verdict")
    public void initiateHearingOffenceVerdict(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-hearing-with-inherited-verdict event received {}", envelope.toObfuscatedDebugString());
        }
        final UpdateHearingWithInheritedVerdictCommand command = convertToObject(envelope, UpdateHearingWithInheritedVerdictCommand.class);
        aggregate(HearingAggregate.class, command.getHearingId(), envelope, a -> a.inheritVerdict(command.getHearingId(), command.getVerdict()));
    }

    @Handles("hearing.command.register-hearing-against-defendant")
    public void recordHearingDefendant(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-defendant event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstDefendantCommand command = convertToObject(envelope, RegisterHearingAgainstDefendantCommand.class);
        aggregate(DefendantAggregate.class, command.getDefendantId(), envelope, defendantAggregate -> defendantAggregate.registerHearing(command.getDefendantId(), command.getHearingId()));
    }

    @Handles("hearing.command.register-hearing-against-case")
    public void registerHearingAgainstCase(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-case event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstCaseCommand command = convertToObject(envelope, RegisterHearingAgainstCaseCommand.class);
        aggregate(CaseAggregate.class, command.getCaseId(), envelope, caseAggregate -> caseAggregate.registerHearingId(command.getCaseId(), command.getHearingId()));
    }
}
