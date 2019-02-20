package uk.gov.moj.cpp.hearing.event;

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

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
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
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
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

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-changed"), payloadIsJson(allOf(
                        withJsonPath("$.offenceId", is(convictionDateAdded.getOffenceId().toString())),
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

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("public.hearing.offence-conviction-date-removed"), payloadIsJson(
                        allOf(withJsonPath("$.offenceId", is(convictionDateRemoved.getOffenceId().toString()))))));
    }

}