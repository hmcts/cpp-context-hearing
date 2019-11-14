package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.*;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;

//TODO: Extend this class as part of SCSL-132
@ServiceComponent(EVENT_PROCESSOR)
public class CourtListEventProcessor {

    @Inject
    private PublishCourtListCommandSender publishCourtListCommandSender;

    private static final UUID courtListFileId = randomUUID();

    @Handles("hearing.event.publish-court-list-requested")
    public void handlePublishCourtListRequested(final JsonEnvelope envelope) {
        final String courtCentreId = envelope.payloadAsJsonObject().getString("courtCentreId");
        publishCourtListCommandSender.recordCourtListProduced(fromString(courtCentreId), courtListFileId,"TEST");
    }

    @Handles("hearing.event.publish-court-list-produced")
    public void handleProducedCourtList(final JsonEnvelope envelope) {
        final String courtCentreId = envelope.payloadAsJsonObject().getString("courtCentreId");
        publishCourtListCommandSender.recordCourtListExportSuccessful(fromString(courtCentreId), courtListFileId, "TEST");
    }
}
