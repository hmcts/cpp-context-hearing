package uk.gov.moj.cpp.hearing.command.handler;


import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateTargetCommandHandler extends AbstractCommandHandler {

    private static final String HEARING_ID = "hearingId";
    private static final String TARGET_ID = "targetId";
    private static final String HEARING_DAY = "hearingDay";

    @Handles("hearing.command.patch-application-finalised-on-target")
    public void patchApplicationFinalisedOnTarget(final JsonEnvelope commandEnvelope) throws EventStreamException {

        final JsonObject payload = commandEnvelope.payloadAsJsonObject();
        final UUID hearingId = UUID.fromString(payload.getString(HEARING_ID));

        final UUID targetId = UUID.fromString(payload.getString(TARGET_ID));
        final LocalDate hearingDay = LocalDate.parse(payload.getString(HEARING_DAY));
        aggregate(HearingAggregate.class, hearingId, commandEnvelope,
                aggregate -> aggregate.updateApplicationFinalisedOnTarget(targetId, hearingId, hearingDay, true));
    }
}

