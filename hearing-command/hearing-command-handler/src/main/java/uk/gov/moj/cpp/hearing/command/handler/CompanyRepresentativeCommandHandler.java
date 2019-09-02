package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.hearing.courts.AddCompanyRepresentative;
import uk.gov.justice.hearing.courts.RemoveCompanyRepresentative;
import uk.gov.justice.hearing.courts.UpdateCompanyRepresentative;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class CompanyRepresentativeCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CompanyRepresentativeCommandHandler.class.getName());

    @Handles("hearing.command.add-company-representative")
    public void addCompanyRepresentative(final Envelope<AddCompanyRepresentative> envelope) throws EventStreamException {

    if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-company-representative event received {}", envelope.payload().getCompanyRepresentative());
        }
        final CompanyRepresentative companyRepresentative = envelope.payload().getCompanyRepresentative();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.addCompanyRepresentative(companyRepresentative, hearingId));
    }

    @Handles("hearing.command.update-company-representative")
    public void updateCompanyRepresentative(final Envelope<UpdateCompanyRepresentative> envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-company-representative event received {}", envelope.payload().getCompanyRepresentative());
        }
        final CompanyRepresentative companyRepresentative = envelope.payload().getCompanyRepresentative();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.updateCompanyRepresentative(companyRepresentative, hearingId));
    }

    @Handles("hearing.command.remove-company-representative")
    public void removeCompanyRepresentative(final Envelope<RemoveCompanyRepresentative> envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.remove-company-representative event received {}", envelope.payload().getId());
        }
        final UUID id = envelope.payload().getId();
        final UUID hearingId = envelope.payload().getHearingId();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.class,
                hearingId,
                envelope,
                aggregate -> aggregate.removeCompanyRepresentative(id, hearingId));
    }


}
