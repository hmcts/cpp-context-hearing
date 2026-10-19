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
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingFailureCode;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;
import uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.json.JsonObject;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

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
        givenCourtSchedulerRefusesWith(ProvisionalBookingFailureCode.NO_CAPACITY, "no capacity");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue().metadata().name(),
                is("public.hearing.hearing-slots-provisional-booking-refused"));
    }

    @Test
    public void shouldCarryTypeCodeAndReasonOnTheFailureEvent() {
        givenCourtSchedulerRefusesWith(ProvisionalBookingFailureCode.NO_CAPACITY,
                "courtscheduler returned 409: {\"error\":\"session full\"}");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        final JsonObject error = sentPayload().getJsonObject("error");
        assertThat(error.getString("type"), is("BUSINESS"));
        assertThat(error.getString("code"), is("NO_CAPACITY"));
        assertThat(error.getString("reason"), is("courtscheduler returned 409: {\"error\":\"session full\"}"));
    }

    /** A refused re-pick carries the booking id it was asked to reuse. */
    @Test
    public void shouldCarryTheRequestsBookingIdOnTheFailureEventWhenItHadOne() {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-with-booking-id.json",
                ProvisionalBookingFailureCode.NO_CAPACITY, "no capacity");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(sentPayload().getString("bookingId"), is("existing-booking-1"));
    }

    /** A refused first pick has no booking id at all - courtscheduler only mints one on success. */
    @Test
    public void shouldOmitBookingIdFromTheFailureEventOnAFirstPick() {
        givenCourtSchedulerRefusesWith(ProvisionalBookingFailureCode.NO_CAPACITY, "no capacity");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(sentPayload().containsKey("bookingId"), is(false));
    }

    @Test
    public void shouldOmitBookingIdFromTheFailureEventWhenTheRequestsWasBlank() {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-with-blank-booking-id.json",
                ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR, "courtscheduler returned 500: {}");

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        assertThat(sentPayload().containsKey("bookingId"), is(false));
    }

    /**
     * Every code must survive the hop to the event unchanged - the UI branches on it to decide
     * whether the clerk may be told to pick another session, and a code it does not recognise
     * is treated as technical.
     *
     * <p>Driven off {@code values()} rather than a hand-written list so a code added to the enum
     * is covered the moment it exists, and one removed cannot leave a stale assertion behind.
     */
    @Test
    public void shouldPublishEveryFailureCodeVerbatim() {
        for (final ProvisionalBookingFailureCode code : ProvisionalBookingFailureCode.values()) {
            reset(this.sender);
            givenCourtSchedulerRefusesWith(code, "reason for " + code);

            verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

            final JsonObject error = sentPayload().getJsonObject("error");
            assertThat(error.getString("type"), is(code.getType().name()));
            assertThat(error.getString("code"), is(code.name()));
            assertThat(error.getString("reason"), is("reason for " + code));
        }
    }

    /**
     * The enum and the published schema's {@code code} list must agree, in both directions.
     *
     * <p>This is the failure this test exists to prevent and it is a silent one: the framework
     * validates a published event against the schema registered for its name and DROPS it if the
     * payload does not conform. A code added to the enum but not to the schema therefore produces
     * no public event at all - the clerk watches a spinner until commandSync's 30s timeout, and
     * the only evidence is in the WildFly log. The reverse direction matters less but still rots
     * the contract, advertising codes nothing can emit.
     *
     * <p>Compared as sets, by name: ordering in the schema is presentational.
     */
    @Test
    public void everyFailureCodeIsDeclaredInThePublishedSchema() throws Exception {
        final File schema = new File(FAILURE_EVENT_SCHEMA);
        if (!schema.exists()) {
            throw new IllegalStateException("schema not found: " + schema.getAbsolutePath()
                    + " \u2014 if the module layout moved, update this test rather than deleting it");
        }

        final JsonNode codeEnum = new ObjectMapper().readTree(schema)
                .path("properties").path("error").path("properties").path("code").path("enum");
        if (!codeEnum.isArray()) {
            throw new IllegalStateException("schema has no properties.error.properties.code.enum: "
                    + FAILURE_EVENT_SCHEMA);
        }

        final Set<String> declared = new TreeSet<>();
        codeEnum.forEach(node -> declared.add(node.asText()));

        final Set<String> actual = new TreeSet<>();
        for (final ProvisionalBookingFailureCode code : ProvisionalBookingFailureCode.values()) {
            actual.add(code.name());
        }

        assertThat("codes the published schema would REJECT, silently dropping the event",
                declared.containsAll(actual), is(true));
        assertThat("codes the schema advertises that no enum constant can emit",
                actual.containsAll(declared), is(true));
        assertThat(actual, is(declared));
    }

    /** Same silent-drop risk for {@code type}: every type a code can carry must be declared. */
    @Test
    public void everyFailureTypeIsDeclaredInThePublishedSchema() throws Exception {
        final JsonNode typeEnum = new ObjectMapper().readTree(new File(FAILURE_EVENT_SCHEMA))
                .path("properties").path("error").path("properties").path("type").path("enum");
        if (!typeEnum.isArray()) {
            throw new IllegalStateException("schema has no properties.error.properties.type.enum: "
                    + FAILURE_EVENT_SCHEMA);
        }

        final Set<String> declared = new TreeSet<>();
        typeEnum.forEach(node -> declared.add(node.asText()));

        final Set<String> actual = new TreeSet<>();
        for (final ProvisionalBookingFailureCode.Type type : ProvisionalBookingFailureCode.Type.values()) {
            actual.add(type.name());
        }

        assertThat(actual, is(declared));
    }

    /** The split agreed in LPT-2590: three refusals the clerk is told about, one technical failure. */
    @Test
    public void everyFailureCodeHasTheAgreedType() {
        assertThat(ProvisionalBookingFailureCode.NO_CAPACITY.getType(), is(ProvisionalBookingFailureCode.Type.BUSINESS));
        assertThat(ProvisionalBookingFailureCode.CONFIRMED_BOOKING_EXISTS.getType(), is(ProvisionalBookingFailureCode.Type.BUSINESS));
        assertThat(ProvisionalBookingFailureCode.NO_SESSION_AVAILABLE.getType(), is(ProvisionalBookingFailureCode.Type.BUSINESS));
        assertThat(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR.getType(), is(ProvisionalBookingFailureCode.Type.TECHNICAL));
        assertThat(ProvisionalBookingFailureCode.values().length, is(4));
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

    private void givenCourtSchedulerRefusesWith(final ProvisionalBookingFailureCode code, final String reason) {
        givenCourtSchedulerRefusesWith("hearing.event.book-provisional-hearing-slots-v2.json", code, reason);
    }

    private void givenCourtSchedulerRefusesWith(final String requestFixture, final ProvisionalBookingFailureCode code, final String reason) {
        final JsonObject bookProvisionalHearingSlotsJsonObject = new StringToJsonObjectConverter().convert(getPayload(requestFixture));
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.book-unconfirmed-hearing-slots"), bookProvisionalHearingSlotsJsonObject);

        when(provisionalBookingService.bookSlots(any())).thenReturn(ProvisionalBookingServiceResponse.error(code, reason));

        bookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots(event);
    }

    private static ProvisionalBookingServiceResponse getNormalResponse() {
        return ProvisionalBookingServiceResponse.normal(UUID.randomUUID().toString());
    }
}
