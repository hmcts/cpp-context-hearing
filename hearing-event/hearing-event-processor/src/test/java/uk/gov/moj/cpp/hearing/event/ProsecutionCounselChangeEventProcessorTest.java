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

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProsecutionCounselChangeEventProcessorTest {

    private static final String REASON = "Provided ProsecutionCounsel already exists";
    private static final String PERSON_ID = randomUUID().toString();
    private static final String ATTENDEE_ID = randomUUID().toString();
    private static final String ID = randomUUID().toString();
    private static final String HEARING_ID = randomUUID().toString();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private ProsecutionCounselChangeEventProcessor prosecutionCounselChangeEventProcessor;

    @Test
    public void processProsecutionCounselChangeIgnoredEvent() {

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

    @Test
    public void processProsecutionCounselUpdatedEvent() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.prosecution-counsel-updated"),
                createObjectBuilder()
                        .add("personId", PERSON_ID)
                        .add("attendeeId", ATTENDEE_ID)
                        .build());

        prosecutionCounselChangeEventProcessor.publishPublicProsecutionCounselUpdatedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.hearing.prosecution-counsel-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", is(PERSON_ID)),
                                withJsonPath("$.attendeeId", is(ATTENDEE_ID))
                                )
                        )
                )
        );
    }

    @Test
    public void processProsecutionCounselRemovedEvent() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.prosecution-counsel-removed"),
                Json.createObjectBuilder()
                        .add("id", ID)
                        .add("hearingId", HEARING_ID)
                        .build());

        prosecutionCounselChangeEventProcessor.publishPublicProsecutionCounselRemovedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.hearing.prosecution-counsel-removed"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(ID)),
                                withJsonPath("$.hearingId", is(HEARING_ID))
                                )
                        )
                )
        );
    }
}
