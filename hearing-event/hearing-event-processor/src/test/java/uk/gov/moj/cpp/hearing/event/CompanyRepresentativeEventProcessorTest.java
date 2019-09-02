package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import javax.json.Json;

@RunWith(MockitoJUnitRunner.class)
public class CompanyRepresentativeEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CompanyRepresentativeEventProcessor companyRepresentativeEventProcessor;

    @Test
    public void processPublicHearingCompanyRepresentativeChangeIgnored() {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.company-representative-change-ignored"),
                Json.createObjectBuilder()
                        .add("reason", "company representative already added")
                        .build());

        companyRepresentativeEventProcessor.publishPublicCompanyRepresentativeChangeIgnoredEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("public.hearing.company-representative-change-ignored"),
                        payloadIsJson(allOf(
                                withJsonPath("$.reason", is("company representative already added"))
                                )
                        )
                )
        );
    }
}
