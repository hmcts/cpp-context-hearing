package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import javax.inject.Inject;

@ServiceComponent(EVENT_PROCESSOR)
public class ReleaseProvisionalHearingSlotsProcessor {

    private final ProvisionalBookingService provisionalBookingService;

    @Inject
    public ReleaseProvisionalHearingSlotsProcessor(final ProvisionalBookingService provisionalBookingService) {
        this.provisionalBookingService = provisionalBookingService;
    }

    @Handles("hearing.event.release-provisional-hearing-slots")
    public void handleReleaseProvisionalHearingSlots(final JsonEnvelope event) {
        final String bookingId = event.payloadAsJsonObject().getString("bookingId");
        provisionalBookingService.releaseSlots(bookingId);
    }
}
