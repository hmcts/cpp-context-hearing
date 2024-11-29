package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.hearing.courts.AddApplicantCounsel;
import uk.gov.justice.hearing.courts.RemoveApplicantCounsel;
import uk.gov.justice.hearing.courts.UpdateApplicantCounsel;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ApplicantCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ApplicantCounselCommandHandler.class.getName());

    @Handles("hearing.command.add-applicant-counsel")
    public void addApplicantCounsel(final Envelope<AddApplicantCounsel> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-applicant-counsel event received. ApplicantCounsel Id: {}", envelope.payload().getApplicantCounsel().getId());
        }
        final ApplicantCounsel applicantCounsel = envelope.payload().getApplicantCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.addApplicantCounsel(applicantCounsel, hearingId));
    }

    @Handles("hearing.command.remove-applicant-counsel")
    public void removeApplicantCounsel(final Envelope<RemoveApplicantCounsel> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.remove-applicant-counsel event received {}", envelope.payload().getId());
        }
        final UUID id = envelope.payload().getId();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.removeApplicantCounsel(id, hearingId));
    }

    @Handles("hearing.command.update-applicant-counsel")
    public void updateApplicantCounsel(final Envelope<UpdateApplicantCounsel> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-applicant-counsel event received. ApplicantCounsel Id: {}", envelope.payload().getApplicantCounsel().getId());
        }
        final ApplicantCounsel applicantCounsel = envelope.payload().getApplicantCounsel();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.updateApplicantCounsel(applicantCounsel, hearingId));
    }
}
