package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import java.io.IOException;
import java.util.Set;

import javax.json.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ConvictionDateEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @InjectMocks
    private ConvictionDateEventProcessor convictionDateEventProcessor;
    @Mock
    private Sender sender;
    @Mock
    private Requester requester;
    @Mock
    private JsonEnvelope responseEnvelope;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void publishOffenceConvictionDateChangedPublicEvent() {

        ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setHearingId(randomUUID())
                .setCaseId(randomUUID())
                .setOffenceId(randomUUID()).setConvictionDate(PAST_LOCAL_DATE.next());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded));

        this.convictionDateEventProcessor.publishOffenceConvictionDateChangedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-changed"), payloadIsJson(allOf(
                        withJsonPath("$.offenceId", is(convictionDateAdded.getOffenceId().toString())),
                        withJsonPath("$.convictionDate", is(convictionDateAdded.getConvictionDate().toString()))))));
    }

    @Test
    public void publishOffenceConvictionDateChangedPublicEventForOffenceUnderApplication() {

        ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setHearingId(randomUUID())
                .setCourtApplicationId(randomUUID())
                .setOffenceId(randomUUID())
                .setConvictionDate(PAST_LOCAL_DATE.next());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded));

        this.convictionDateEventProcessor.publishOffenceConvictionDateChangedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-changed"), payloadIsJson(allOf(
                        withJsonPath("$.courtApplicationId", is(convictionDateAdded.getCourtApplicationId().toString())),
                        withJsonPath("$.offenceId", is(convictionDateAdded.getOffenceId().toString())),
                        withJsonPath("$.convictionDate", is(convictionDateAdded.getConvictionDate().toString()))))));
    }

    @Test
    public void publishOffenceConvictionDateChangedPublicEventForApplication() {

        ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setHearingId(randomUUID())
                .setCourtApplicationId(randomUUID())
                .setConvictionDate(PAST_LOCAL_DATE.next());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded));

        this.convictionDateEventProcessor.publishOffenceConvictionDateChangedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-changed"), payloadIsJson(allOf(
                        withJsonPath("$.courtApplicationId", is(convictionDateAdded.getCourtApplicationId().toString())),
                        hasNoJsonPath("$.offenceId"),
                        withJsonPath("$.convictionDate", is(convictionDateAdded.getConvictionDate().toString()))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void publishOffenceConvictionDateRemovedPublicEvent() {

        ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setHearingId(randomUUID())
                .setCaseId(randomUUID())
                .setOffenceId(randomUUID());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved));

        this.convictionDateEventProcessor.publishOffenceConvictionDateRemovedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-removed"), payloadIsJson(
                        allOf(withJsonPath("$.offenceId", is(convictionDateRemoved.getOffenceId().toString()))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void publishOffenceConvictionDateRemovedPublicEventForOffenceUnderApplication() {

        ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setHearingId(randomUUID())
                .setOffenceId(randomUUID())
                .setCourtApplicationId(randomUUID());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved));

        this.convictionDateEventProcessor.publishOffenceConvictionDateRemovedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-removed"), payloadIsJson(
                        allOf(withJsonPath("$.offenceId", is(convictionDateRemoved.getOffenceId().toString())),
                                withJsonPath("$.courtApplicationId", is(convictionDateRemoved.getCourtApplicationId().toString()))))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void publishOffenceConvictionDateRemovedPublicEventForApplication() {

        ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setHearingId(randomUUID())
                .setCourtApplicationId(randomUUID());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved));

        this.convictionDateEventProcessor.publishOffenceConvictionDateRemovedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-removed"), payloadIsJson(
                        allOf(hasNoJsonPath("$.offenceId"),
                                withJsonPath("$.courtApplicationId", is(convictionDateRemoved.getCourtApplicationId().toString()))))));
    }

    @Test
    public void shouldPassSchemaValidationForValidPayloadOfConvictionDateAdded() {
        //given
        JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                Json.createObjectBuilder()
                        .add("caseId", "30dd24a6-e383-48f6-afa0-e4b174ecb89c")
                        .add("hearingId", "c76ead4b-5ac8-48e0-b744-f4ade56c8198")
                        .add("offenceId", "0683dfed-f9a4-4661-aaa9-d43fda9ef93d")
                        .add("courtApplicationId", "a806495a-75c1-455a-9788-f069be3124d9")
                        .add("convictionDate", "2022-11-17")
                        .build());

        //then
        assertThat(envelope, jsonEnvelope().thatMatchesSchema());
    }

    @Test
    public void shouldPassSchemaValidationForValidPayloadOfConvictionDateRemoved() {
        //given
        JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                Json.createObjectBuilder()
                        .add("caseId", "30dd24a6-e383-48f6-afa0-e4b174ecb89c")
                        .add("hearingId", "c76ead4b-5ac8-48e0-b744-f4ade56c8198")
                        .add("offenceId", "0683dfed-f9a4-4661-aaa9-d43fda9ef93d")
                        .add("courtApplicationId", "a806495a-75c1-455a-9788-f069be3124d9")
                        .add("convictionDate", "2022-11-17")
                        .build());

        //then
        assertThat(envelope, jsonEnvelope().thatMatchesSchema());
    }


    @Test
    public void shouldFailSchemaValidationForInValidPayloadOfConvictionDateAdded() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema jsonSchema = factory.getSchema(
                ConvictionDateEventProcessorTest.class.getResourceAsStream("/yaml/json/schema/hearing.conviction-date-added.json"));
        JsonNode jsonNode = mapper.readTree(
                ConvictionDateEventProcessorTest.class.getResourceAsStream("/hearing.conviction-date-added-invalid.json"));
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        assertThat(errors.size(), is(1));
        assertThat(errors.iterator().next().getMessage(), is("$.convictionDate: is missing but it is required"));

    }

    @Test
    public void shouldFailSchemaValidationForInValidPayloadOfConvictionDateRemoved() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema jsonSchema = factory.getSchema(
                ConvictionDateEventProcessorTest.class.getResourceAsStream("/yaml/json/schema/hearing.conviction-date-removed.json"));
        JsonNode jsonNode = mapper.readTree(
                ConvictionDateEventProcessorTest.class.getResourceAsStream("/hearing.conviction-date-removed-invalid.json"));
        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        assertThat(errors.size(), is(2));
        assertThat(errors.iterator().next().getMessage(), is("$.offenceId: is missing but it is required"));

    }

}