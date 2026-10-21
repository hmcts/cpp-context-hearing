package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.FileUtil.getPayload;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import javax.json.JsonObject;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class
BookProvisionalHearingSlotsProcessorTest {

    /** Relative to this module's base dir, the way ProvisionalBookingSchemaChainTest does it. */
    private static final String FAILURE_EVENT_SCHEMA =
            "src/yaml/json/schema/public.hearing.hearing-slots-provisional-booking-refused.json";

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

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHandleBookProvisionalHearingSlotsForV1() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v1.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), is("public.hearing.hearing-slots-provisionally-booked"));

    }


    @Test
    public void testHandleBookProvisionalHearingSlotsForV2() throws UnsupportedEncodingException {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v2.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);
        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), is("public.hearing.hearing-slots-provisionally-booked"));

    }

    @Test
    public void shouldSendDurationToCourtSchedulerWhenPresent() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-with-duration.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(provisionalBookingService, times(1)).bookSlots(provisionalBookingServiceCaptor.capture());

        final JsonObject sentSlot = provisionalBookingServiceCaptor.getValue().getJsonArray("provisionalSlots").getJsonObject(0);
        assertThat(sentSlot.getInt("duration"), is(90));
    }

    @Test
    public void shouldOmitDurationEntirelyWhenNull() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v2.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(provisionalBookingService, times(1)).bookSlots(provisionalBookingServiceCaptor.capture());

        final JsonObject sentSlot = provisionalBookingServiceCaptor.getValue().getJsonArray("provisionalSlots").getJsonObject(0);
        assertThat(sentSlot.containsKey("duration"), is(false));
    }

    @Test
    public void shouldForwardBookingIdToCourtscheduler() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-with-booking-id.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(provisionalBookingService).bookSlots(provisionalBookingServiceCaptor.capture());
        assertThat(provisionalBookingServiceCaptor.getValue().getString("bookingId"), is("existing-booking-1"));
    }

    @Test
    public void shouldOmitBookingIdWhenAbsent() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v2.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(provisionalBookingService).bookSlots(provisionalBookingServiceCaptor.capture());
        assertThat(provisionalBookingServiceCaptor.getValue().containsKey("bookingId"), is(false));
    }

    @Test
    public void shouldOmitBookingIdWhenBlank() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-with-blank-booking-id.json"));

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(getNormalResponse());

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(provisionalBookingService).bookSlots(provisionalBookingServiceCaptor.capture());
        assertThat(provisionalBookingServiceCaptor.getValue().containsKey("bookingId"), is(false));
    }

    /**
     * The split this family of tests exists for. Success and refusal used to share the booked
     * name, so a consumer that keys off the event name alone - which is what cpp-ui-hearing's
     * commandSync does - saw a refusal as a success and had to re-inspect the payload to find out
     * otherwise. A refusal must now arrive under its OWN name, and the booked name must never
     * carry an error.
     */
    @Test
    public void shouldPublishTheFailureEventRatherThanTheBookedEventWhenCourtSchedulerRefuses() {
        givenCourtSchedulerRefusesWith(422, "no capacity");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(),
                is("public.hearing.hearing-slots-provisional-booking-refused"));
    }

    /**
     * courtscheduler's HTTP status goes out as is. Hearing does not interpret it: no type, no
     * code - deciding what a status means for the clerk is the consumer's call.
     */
    @Test
    public void shouldCarryCourtSchedulersStatusAndTheMessageOnTheFailureEvent() {
        givenCourtSchedulerRefusesWith(422, "courtscheduler returned 422: {\"error\":\"session full\"}");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        final JsonObject error = sentPayload().getJsonObject("error");
        assertThat(error.getInt("status"), is(422));
        assertThat(error.getString("message"), is("courtscheduler returned 422: {\"error\":\"session full\"}"));
        assertThat(error.keySet(), is(Set.of("status", "message")));
    }

    /** No answer from courtscheduler at all (connection refused, timeout): there is no status to send. */
    @Test
    public void shouldOmitStatusFromTheFailureEventWhenCourtSchedulerNeverAnswered() {
        givenCourtSchedulerRefusesWith(null, "Connection refused");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        final JsonObject error = sentPayload().getJsonObject("error");
        assertThat(error.containsKey("status"), is(false));
        assertThat(error.getString("message"), is("Connection refused"));
    }

    /** A refused re-pick carries the booking id it was asked to reuse. */
    @Test
    public void shouldCarryTheRequestsBookingIdOnTheFailureEventWhenItHadOne() {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-with-booking-id.json",
                422, "no capacity");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(sentPayload().getString("bookingId"), is("existing-booking-1"));
    }

    /** A refused first pick has no booking id at all - courtscheduler only mints one on success. */
    @Test
    public void shouldOmitBookingIdFromTheFailureEventOnAFirstPick() {
        givenCourtSchedulerRefusesWith(422, "no capacity");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(sentPayload().containsKey("bookingId"), is(false));
    }

    @Test
    public void shouldOmitBookingIdFromTheFailureEventWhenTheRequestsWasBlank() {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-with-blank-booking-id.json",
                500, "courtscheduler returned 500: {}");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(sentPayload().containsKey("bookingId"), is(false));
    }

    /**
     * What the processor publishes must validate against the published schema.
     *
     * <p>This is the failure this test exists to prevent and it is a silent one: the framework
     * validates a published event against the schema registered for its name and DROPS it if the
     * payload does not conform. The clerk then watches a spinner until commandSync's 30s timeout,
     * and the only evidence is in the WildFly log. Checked for both shapes the event can take:
     * with a status, and without one.
     */
    @Test
    public void shouldPublishAFailureEventThatTheSchemaAccepts() throws Exception {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-with-booking-id.json",
                422, "courtscheduler returned 422: {}");
        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        assertThat(schemaErrors(sentPayload()), is(Set.of()));

        reset(this.sender);
        givenCourtSchedulerRefusesWith(null, "Connection refused");
        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        assertThat(schemaErrors(sentPayload()), is(Set.of()));
    }

    /** The booked event must now be bookingId-only: its schema no longer permits an error. */
    @Test
    public void shouldPublishBookingIdOnlyOnTheBookedEvent() {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload("hearing.event.book-provisional-hearing-slots-v2.json"));
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);
        final String bookingId = UUID.randomUUID().toString();

        when(provisionalBookingService.bookSlots(any())).thenReturn(ProvisionalBookingServiceResponse.normal(bookingId));

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        final JsonObject payload = sentPayload();
        assertThat(payload.getString("bookingId"), is(bookingId));
        assertThat(payload.containsKey("error"), is(false));
    }

    /** The captor is a raw {@code DefaultEnvelope}, whose {@code payload()} is declared Object. */
    private JsonObject sentPayload() {
        return (JsonObject) envelopeArgumentCaptor.getValue().payload();
    }

    private void givenCourtSchedulerRefusesWith(final Integer status, final String message) {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-v2.json", status, message);
    }

    private void givenCourtSchedulerRefusesWith(final String requestFixture, final Integer status, final String message) {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload(requestFixture));
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(ProvisionalBookingServiceResponse.error(status, message));

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);
    }

    private static Set<String> schemaErrors(final JsonObject payload) throws Exception {
        final File schema = new File(FAILURE_EVENT_SCHEMA);
        if (!schema.exists()) {
            throw new IllegalStateException("schema not found: " + schema.getAbsolutePath()
                    + " \u2014 if the module layout moved, update this test rather than deleting it");
        }
        final JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)
                .getSchema(Files.newInputStream(schema.toPath()));
        final ObjectMapper mapper = new ObjectMapper();
        return jsonSchema.validate(mapper.readTree(payload.toString())).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toSet());
    }

    private static ProvisionalBookingServiceResponse getNormalResponse() {
        return ProvisionalBookingServiceResponse.normal(UUID.randomUUID().toString());
    }
}
