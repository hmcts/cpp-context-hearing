package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@RunWith(MockitoJUnitRunner.class)
public class CreateProsecutionCaseEventProcessorTest {

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CreateProsecutionCaseEventProcessor createProsecutionCaseEventProcessor;

    @Test
    public void handleHearingAgainstCaseRegisteredEvent() {

        final String caseId = randomUUID().toString();
        final String hearingId = randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.registered-hearing-against-case"),
                createObjectBuilder()
                        .add("caseId", caseId)
                        .add("hearingId", hearingId)
                        .build());

        createProsecutionCaseEventProcessor.handleHearingAgainstCaseRegisteredEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.events.hearing.prosecution-case-created-in-hearing"),
                        payloadIsJson(allOf(
                                withJsonPath("$.prosecutionCaseId", is(caseId))
                                )
                        )
                )
        );
    }


}
