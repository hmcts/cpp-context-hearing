package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import java.util.List;
import java.util.UUID;

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

        aggregate(HearingAggregate.class, command.getHearing().getId(), envelope, a -> a.initiate(command.getHearing()));

        final Hearing hearing = command.getHearing();
        final List<CourtApplication> courtApplications = hearing.getCourtApplications();
        if (courtApplications != null) {
            for (final CourtApplication courtApplication : courtApplications) {
                aggregate(ApplicationAggregate.class, courtApplication.getId(), envelope, a -> a.registerHearingId(courtApplication.getId(), hearing.getId()));
            }
        }
    }

    @Handles("hearing.command.extend-hearing")
    public void extendHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.extend-hearing received {}", envelope.toObfuscatedDebugString());
        }

        final ExtendHearingCommand command = convertToObject(envelope, ExtendHearingCommand.class);
        final UUID hearingId = command.getHearingId();
        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.extend(hearingId, command.getCourtApplication(), command.getProsecutionCases(), command.getShadowListedOffences()));

        if(nonNull(command.getCourtApplication())){
            final UUID applicationId = command.getCourtApplication().getId();
            aggregate(ApplicationAggregate.class, applicationId, envelope, a -> a.registerHearingId(applicationId, hearingId));
        }

    }


    @Handles("hearing.command.register-hearing-against-offence")
    public void initiateHearingOffence(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-offence event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstOffenceCommand command = convertToObject(envelope, RegisterHearingAgainstOffenceCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelope, a -> a.lookupOffenceForHearing(command.getHearingId(), command.getOffenceId()));
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
