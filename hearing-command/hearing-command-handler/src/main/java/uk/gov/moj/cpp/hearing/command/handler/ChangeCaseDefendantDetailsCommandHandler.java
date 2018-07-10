package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ChangeCaseDefendantDetailsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ChangeCaseDefendantDetailsCommandHandler.class.getName());

    @Handles("hearing.update-case-defendant-details")
    public void initiateCaseDefendantDetailsChange(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-case-defendant-details event received {}", envelope.toObfuscatedDebugString());
        }
        final CaseDefendantDetails caseDefendantDetails = convertToObject(envelope, CaseDefendantDetails.class);

        for (final Defendant defendant : caseDefendantDetails.getDefendants()) {

            final CaseDefendantDetailsCommand caseDefendantDetailsCommand = CaseDefendantDetailsCommand.builder()
                    .withCaseId(caseDefendantDetails.getCaseId())
                    .withDefendant(Defendant.builder(defendant))
                    .build();

            aggregate(DefendantAggregate.class,
                    defendant.getId(),
                    envelope,
                    defendantAggregate -> defendantAggregate.enrichCaseDefendantDetailsWithHearingIds(caseDefendantDetailsCommand));
        }
    }

    @Handles("hearing.update-case-defendant-details-against-hearing-aggregate")
    public void updateCaseDefendantDetails(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-case-defendant-details-against-hearing-aggregate event received {}", envelope.toObfuscatedDebugString());
        }

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings = convertToObject(envelope, CaseDefendantDetailsWithHearings.class);

        for (final UUID hearingId : caseDefendantDetailsWithHearings.getHearingIds()) {

            final CaseDefendantDetailsWithHearingCommand defendantWithHearingCommand = CaseDefendantDetailsWithHearingCommand.builder()
                    .withCaseId(caseDefendantDetailsWithHearings.getCaseId())
                    .withDefendants(Defendant.builder(caseDefendantDetailsWithHearings.getDefendant()))
                    .withHearingIds(Collections.singletonList(hearingId))
                    .build();

            aggregate(NewModelHearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.updateDefendantDetails(defendantWithHearingCommand));

        }
    }
}
