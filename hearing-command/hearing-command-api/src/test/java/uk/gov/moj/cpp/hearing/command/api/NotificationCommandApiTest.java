package uk.gov.moj.cpp.hearing.command.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class NotificationCommandApiTest {

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderArgumentCaptor;

    @InjectMocks
    private NotificationCommandApi notificationCommandApi;

    @Test
    public void uploadSubscriptions() {

        final JsonEnvelope envelope = prepareUploadSubscriptionsCommand();

        notificationCommandApi.uploadSubscriptions(envelope);

        verify(sender).send(senderArgumentCaptor.capture());

        assertThat(senderArgumentCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope).withName("hearing.command.upload-subscriptions"),
                payloadIsJson(allOf(
                        withJsonPath("$.subscriptions[0].channel", is("email"))
                )))
        ));
    }

    private JsonEnvelope prepareUploadSubscriptionsCommand() {

        final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder()
                .add(createSubscription())
                .add(createSubscription());

        final JsonObjectBuilder requestPayload = createObjectBuilder()
                .add("subscriptions", jsonArrayBuilder.build());

        return envelopeFrom(metadataWithRandomUUIDAndName(), requestPayload.build());
    }

    private JsonObjectBuilder createSubscription() {
        return createObjectBuilder()
                .add("channel", "email")
                .add("channelProperties",
                        createObjectBuilder()
                                .add(STRING.next(), STRING.next())
                                .add(STRING.next(), STRING.next())
                                .add(STRING.next(), STRING.next()))
                .add("userGroups", createArrayBuilder().add(STRING.next()).add(STRING.next()))
                .add("destination", STRING.next())
                .add("courtCentreIds", createArrayBuilder().add(randomUUID().toString()).add(randomUUID().toString()))
                .add("nowTypeIds", createArrayBuilder().add(randomUUID().toString()).add(randomUUID().toString()));
    }
}