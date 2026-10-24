package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ReleaseProvisionalHearingSlotsProcessorTest {

    @Mock
    private ProvisionalBookingService provisionalBookingService;

    @InjectMocks
    private ReleaseProvisionalHearingSlotsProcessor processor;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReleaseEveryBookingInOneCallToCourtScheduler() {
        final UUID hearingId = randomUUID();
        final String bookingA = randomUUID().toString();
        final String bookingB = randomUUID().toString();

        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("bookingIds", createArrayBuilder().add(bookingA).add(bookingB))
                .add("reason", "DROPPED_FROM_DRAFT")
                .build();
        final JsonEnvelope event = envelopeFrom(
                metadataWithRandomUUID("hearing.event.release-provisional-hearing-slots"), payload);

        processor.handleReleaseProvisionalHearingSlots(event);

        verify(provisionalBookingService, times(1)).releaseUnconfirmedBookings(List.of(bookingA, bookingB));
        verifyNoMoreInteractions(provisionalBookingService);
    }
}
