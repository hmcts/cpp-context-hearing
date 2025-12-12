package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.LaaReference;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class ApplicationDetailChangeCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ApplicationDetailChangeCommandHandler.class.getName());

    @Handles("hearing.update-court-application")
    public void updateExistingCourtApplication(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-court-application event received {}", envelope.toObfuscatedDebugString());
        }
        final CourtApplication courtApplication = convertToObject(envelope.payloadAsJsonObject().getJsonObject("courtApplication"), CourtApplication.class);
        final List<UUID> hearingsLinkedWithApplication = aggregate(ApplicationAggregate.class, courtApplication.getId()).getHearingIds();
        for (final UUID hearingId : hearingsLinkedWithApplication) {
            aggregate(HearingAggregate.class, hearingId, envelope, a -> a.updateCourtApplication(hearingId, courtApplication));
        }
    }
    @Handles("hearing.update-laareference-for-application")
    public void updateLaaReferenceCourtApplication(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-laareference-for-application event received {}", envelope.toObfuscatedDebugString());
        }
        final UUID applicationId = UUID.fromString(envelope.payloadAsJsonObject().getString("applicationId"));
        final UUID offenceId = envelope.payloadAsJsonObject().containsKey("offenceId") ? UUID.fromString(envelope.payloadAsJsonObject().getString("offenceId")) : null;
        final UUID subjectId = UUID.fromString(envelope.payloadAsJsonObject().getString("subjectId"));

        final LaaReference laaReference = convertToObject(envelope.payloadAsJsonObject().getJsonObject("laaReference"), LaaReference.class);
        final List<UUID> hearingsLinkedWithApplication = aggregate(ApplicationAggregate.class, applicationId).getHearingIds();
        for (final UUID hearingId : hearingsLinkedWithApplication) {
            aggregate(HearingAggregate.class, hearingId, envelope, a -> a.updateLaareferenceForApplication(hearingId, applicationId, subjectId, offenceId, laaReference));
        }
    }

    @Handles("hearing.update-defence-organisation-for-application")
    public void updateDefenceOrganisationCourtApplication(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.update-defence-organisation-for-application event received {}", envelope.toObfuscatedDebugString());
        }
        final UUID applicationId = UUID.fromString(envelope.payloadAsJsonObject().getString("applicationId"));
        final UUID subjectId = UUID.fromString(envelope.payloadAsJsonObject().getString("subjectId"));

        final AssociatedDefenceOrganisation associatedDefenceOrganisation = envelope.payloadAsJsonObject().containsKey("associatedDefenceOrganisation") ? convertToObject(envelope.payloadAsJsonObject().getJsonObject("associatedDefenceOrganisation"), AssociatedDefenceOrganisation.class) : null;
        final List<UUID> hearingsLinkedWithApplication = aggregate(ApplicationAggregate.class, applicationId).getHearingIds();
        for (final UUID hearingId : hearingsLinkedWithApplication) {
            aggregate(HearingAggregate.class, hearingId, envelope, a -> a.updateDefenceOrganisationForApplication(hearingId, applicationId, subjectId, associatedDefenceOrganisation));
        }
    }
}

