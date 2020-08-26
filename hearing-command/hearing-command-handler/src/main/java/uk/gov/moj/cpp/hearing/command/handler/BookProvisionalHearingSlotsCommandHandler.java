package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class BookProvisionalHearingSlotsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BookProvisionalHearingSlotsCommandHandler.class.getName());

    @Handles("hearing.command.book-provisional-hearing-slots")
    public void bookProvisionalHearingSlots(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.book-provisional-hearing-slots event received {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final List<ProvisionalHearingSlotInfo> slots = new ArrayList<>();
        final JsonArray slotsArray = envelope.payloadAsJsonObject().getJsonArray("slots");
        for (int i = 0; i < slotsArray.size(); i++) {
            final ProvisionalHearingSlotInfo provisionalHearingSlotInfo = convertToObject(slotsArray.getJsonObject(i), ProvisionalHearingSlotInfo.class);
            slots.add(provisionalHearingSlotInfo);
        }

        aggregate(HearingAggregate.class, hearingId, envelope, hearingAggregate -> hearingAggregate.bookProvisionalHearingSlots(hearingId, slots));
    }
}

