package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.listing.common.azure.ProvisionalBookingService;

import java.util.Arrays;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class BookProvisionalHearingSlotsProcessorTest {

    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeArgumentCaptor;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private BookProvisionalHearingSlotsProcessor bookProvisionalHearingSlotsProcessor;
    @Mock
    private ProvisionalBookingService provisionalBookingService;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHandleBookProvisionalHearingSlots() {
        final UUID hearingId = randomUUID();
        final UUID courtScheduleId1 = randomUUID();
        final UUID courtScheduleId2 = randomUUID();

        final BookProvisionalHearingSlots bookProvisionalHearingSlots = BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(hearingId)
                .withSlots(Arrays.asList(courtScheduleId1, courtScheduleId2))
                .build();

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-provisional-hearing-slots"),
                objectToJsonObjectConverter.convert(bookProvisionalHearingSlots));

        final JsonObject bookingReference = Json.createObjectBuilder().add("bookingId", randomUUID().toString()).build();
        final Response bookingReferenceResponse = Response.status(Response.Status.OK).entity(bookingReference).build();
        when(provisionalBookingService.bookSlots(any())).thenReturn(bookingReferenceResponse);

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), is("public.hearing.hearing-slots-provisionally-booked"));

    }
}
