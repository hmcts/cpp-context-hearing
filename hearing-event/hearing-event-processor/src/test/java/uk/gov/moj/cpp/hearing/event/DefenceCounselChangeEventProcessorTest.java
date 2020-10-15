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

import javax.json.Json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefenceCounselChangeEventProcessorTest {

    private static final String REASON = "Provided DefenceCounsel already exists";
    private static final String PERSON_ID = randomUUID().toString();
    private static final String ATTENDEE_ID = randomUUID().toString();
    private static final String ID = randomUUID().toString();
    private static final String HEARING_ID = randomUUID().toString();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private DefenceCounselChangeEventProcessor defenceCounselChangeEventProcessor;

    @Test
    public void processDefenceCounselChangeIgnoredEvent() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.defence-counsel-change-ignored"),
                createObjectBuilder()
                        .add("reason", REASON)
                        .build());

        defenceCounselChangeEventProcessor.publishPublicDefenceCounselChangeIgnoredEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.hearing.defence-counsel-change-ignored"),
                        payloadIsJson(allOf(
                                withJsonPath("$.reason", is(REASON))
                                )
                        )
                )
        );
    }

    @Test
    public void processDefenceCounselUpdatedEvent() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.defence-counsel-updated"),
                createObjectBuilder()
                        .add("personId", PERSON_ID)
                        .add("attendeeId", ATTENDEE_ID)
                        .build());

        defenceCounselChangeEventProcessor.publishPublicDefenceCounselUpdatedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.hearing.defence-counsel-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", is(PERSON_ID)),
                                withJsonPath("$.attendeeId", is(ATTENDEE_ID))
                                )
                        )
                )
        );
    }

    @Test
    public void processDefenceCounselRemovedEvent() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.defence-counsel-removed"),
                Json.createObjectBuilder()
                        .add("id", ID)
                        .add("hearingId", HEARING_ID)
                        .build());

        defenceCounselChangeEventProcessor.publishPublicDefenceCounselRemovedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("public.hearing.defence-counsel-removed"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(ID)),
                                withJsonPath("$.hearingId", is(HEARING_ID))
                                )
                        )
                )
        );
    }
}
