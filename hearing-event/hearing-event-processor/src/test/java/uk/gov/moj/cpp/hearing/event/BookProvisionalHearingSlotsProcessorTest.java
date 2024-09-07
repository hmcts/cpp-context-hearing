package uk.gov.moj.cpp.hearing.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import javax.json.JsonObject;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.FileUtil.getPayload;

public class
BookProvisionalHearingSlotsProcessorTest {

    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeArgumentCaptor;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @InjectMocks
    private BookProvisionalHearingSlotsProcessor bookProvisionalHearingSlotsProcessor;
    @Mock
    private ProvisionalBookingService provisionalBookingService;
    @Captor
    private ArgumentCaptor<JsonObject> provisionalBookingServiceCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHandleBookProvisionalHearingSlotsForV1() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v1.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-provisional-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), is("public.hearing.hearing-slots-provisionally-booked"));

    }


    @Test
    public void testHandleBookProvisionalHearingSlotsForV2() throws UnsupportedEncodingException {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v2.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-provisional-hearing-slots"), bookProvisionalHearingSlotsJsonObject);
        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), is("public.hearing.hearing-slots-provisionally-booked"));

    }

    private static ProvisionalBookingServiceResponse getNormalResponse() {
        return ProvisionalBookingServiceResponse.normal(UUID.randomUUID().toString());
    }
}
