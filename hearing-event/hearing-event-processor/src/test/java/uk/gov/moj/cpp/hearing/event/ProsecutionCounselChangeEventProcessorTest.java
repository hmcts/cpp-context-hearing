package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCounselChangeEventProcessorTest {

    private static final String REASON = "Provided ProsecutionCounsel already exists";

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private ProsecutionCounselChangeEventProcessor prosecutionCounselChangeEventProcessor;

    @Test
    public void processPublicProsecutionCounselChangeIgnoredEvent() {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.prosecution-counsel-change-ignored"),
                Json.createObjectBuilder()
                        .add("reason", REASON)
                        .build());

        prosecutionCounselChangeEventProcessor.publishPublicProsecutionCounselChangeIgnoredEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.hearing.prosecution-counsel-change-ignored"),
                        payloadIsJson(allOf(
                                withJsonPath("$.reason", is(REASON))
                                )
                        )
                )
        );
    }
}
