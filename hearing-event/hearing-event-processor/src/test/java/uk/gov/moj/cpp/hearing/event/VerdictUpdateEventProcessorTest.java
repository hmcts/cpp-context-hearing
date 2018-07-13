package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

public class VerdictUpdateEventProcessorTest {

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @InjectMocks
    private VerdictUpdateEventProcessor verdictUpdateEventProcessor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void offencePleaUpdate() {

        VerdictUpsert verdictUpsert = VerdictUpsert.builder()
                .withHearingId(randomUUID())
                .withOffenceId(randomUUID())
                .withCaseId(randomUUID())
                .withVerdictId(randomUUID())
                .withVerdictValueId(randomUUID())
                .withCategory(STRING.next())
                .withCode(STRING.next())
                .withDescription(STRING.next())
                .withNumberOfJurors(INTEGER.next())
                .withNumberOfSplitJurors(INTEGER.next())
                .withUnanimous(BOOLEAN.next())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert));

        this.verdictUpdateEventProcessor.verdictUpdate(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("public.hearing.verdict-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(verdictUpsert.getHearingId().toString()))

                                )
                        )
                )
        );
    }
}