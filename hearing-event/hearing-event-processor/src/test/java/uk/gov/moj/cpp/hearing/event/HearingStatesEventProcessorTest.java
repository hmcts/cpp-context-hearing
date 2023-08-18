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
import static uk.gov.moj.cpp.hearing.event.HearingStatesEventProcessor.*;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;


import java.util.UUID;

import javax.json.Json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingStatesEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private HearingStatesEventProcessor processor;

    @Test
    public void processPublicEventApprovalRejected() {

        final String HEARING_ID = UUID.randomUUID().toString();
        final String USER_ID = UUID.randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(HEARING_EVENT_APPROVAL_REJECTED),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID)
                        .add("userId", USER_ID)
                        .build());

        processor.approvalRejected(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName(PUBLIC_HEARING_EVENT_APPROVAL_REJECTED), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID)),
                        withJsonPath("$.userId", is(USER_ID))
          ))));


    }

    @Test
    public void processPublicEventHearingLocked() {

        final String HEARING_ID = UUID.randomUUID().toString();
        final String USER_ID = UUID.randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(HEARING_HEARING_LOCKED),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID)
                        .build());

        processor.hearingLocked(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName(PUBLIC_HEARING_HEARING_LOCKED), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID))
                ))));


    }

    @Test
    public void processPublicEventHearingLockedByOthertUser() {

        final String HEARING_ID = UUID.randomUUID().toString();
        final String USER_ID = UUID.randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(PUBLIC_HEARING_HEARING_LOCKED_BY_OTHER_USER),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID)
                        .build());

        processor.hearingLockedByOtherUser(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName(PUBLIC_HEARING_HEARING_LOCKED_BY_OTHER_USER), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID))
                ))));


    }

    @Test
    public void processPublicHearingUnlockFailed() {

        final String HEARING_ID = UUID.randomUUID().toString();
        final String USER_ID = UUID.randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(HEARING_EVENT_HEARING_UNLOCK_FAILED),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID)
                        .build());

        processor.processHearingUnlockFailed(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName(PUBLIC_HEARING_EVENT_HEARING_UNLOCK_FAILED), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID))
                ))));
    }

    @Test
    public void processPublicHearingUnlocked() {

        final String HEARING_ID = UUID.randomUUID().toString();
        final String USER_ID = UUID.randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(HEARING_EVENT_HEARING_UNLOCKED),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID)
                        .add("userId", USER_ID)
                        .build());

        processor.processHearingUnlocked(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName(PUBLIC_HEARING_EVENT_HEARING_UNLOCKED), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID)),
                        withJsonPath("$.userId", is(USER_ID))
                ))));
    }
}