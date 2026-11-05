package uk.gov.moj.cpp.hearing.event;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import java.util.List;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class ReleaseProvisionalHearingSlotsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseProvisionalHearingSlotsProcessor.class);

    private final ProvisionalBookingService provisionalBookingService;

    @Inject
    public ReleaseProvisionalHearingSlotsProcessor(final ProvisionalBookingService provisionalBookingService) {
        this.provisionalBookingService = provisionalBookingService;
    }

    @Handles("hearing.event.release-provisional-hearing-slots")
    public void handleReleaseProvisionalHearingSlots(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();
        final List<String> bookingIds = payload.getJsonArray("bookingIds").getValuesAs(JsonString.class).stream()
                .map(JsonString::getString)
                .collect(toList());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("releasing unconfirmed bookings {} of hearing {}: {}",
                    bookingIds, payload.getString("hearingId"), payload.getString("reason"));
        }
        provisionalBookingService.releaseUnconfirmedBookings(bookingIds);
    }
}
