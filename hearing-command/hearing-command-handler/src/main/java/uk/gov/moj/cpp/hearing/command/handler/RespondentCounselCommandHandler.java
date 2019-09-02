package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
import uk.gov.justice.hearing.courts.RemoveRespondentCounsel;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class RespondentCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RespondentCounselCommandHandler.class.getName());

    @Handles("hearing.command.remove-respondent-counsel")
    public void removeRespondentCounsel(final Envelope<RemoveRespondentCounsel> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.remove-respondent-counsel event received {}", envelope.payload().getId());
        }
        final UUID id = envelope.payload().getId();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.removeRespondentCounsel(id, hearingId));
    }

    @Handles("hearing.command.update-respondent-counsel")
    public void updateRespondentCounsel(final Envelope<UpdateRespondentCounsel> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-respondent-counsel event received {}", envelope.payload().getRespondentCounsel());
        }
        final RespondentCounsel respondentCounsel = envelope.payload().getRespondentCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.updateRespondentCounsel(respondentCounsel, hearingId));
    }

    @Handles("hearing.command.add-respondent-counsel")
    public void addRespondentCounsel(final Envelope<AddRespondentCounsel> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-respondent-counsel event received {}", envelope.payload().getRespondentCounsel());
        }
        final RespondentCounsel respondentCounsel = envelope.payload().getRespondentCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.addRespondentCounsel(respondentCounsel, hearingId));
    }
}
