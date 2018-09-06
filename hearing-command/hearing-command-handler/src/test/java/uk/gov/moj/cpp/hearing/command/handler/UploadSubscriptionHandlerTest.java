package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscription;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.SubscriptionAggregate;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionsUploaded;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UploadSubscriptionHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(SubscriptionsUploaded.class);

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @InjectMocks
    private UploadSubscriptionsCommandHandler uploadSubscriptionsCommandHandler;

    @Test
    public void uploadSubscriptions() throws EventStreamException {

        final UploadSubscriptionsCommand uploadSubscriptionsCommand = buildUploadSubscriptionsCommand();

        setupMockedEventStream(this.eventStream, new SubscriptionAggregate());

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.command.upload-subscriptions"),
                objectToJsonObjectConverter.convert(uploadSubscriptionsCommand));

        uploadSubscriptionsCommandHandler.uploadSubscriptions(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.subscriptions-uploaded"),
                        payloadIsJson(allOf(withJsonPath("$.subscriptions[0].channel",
                                is(uploadSubscriptionsCommand.getSubscriptions().get(0).getChannel())))))));
    }

    private UploadSubscriptionsCommand buildUploadSubscriptionsCommand() {

        final Map<String, String> properties = new HashMap<>();
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());

        final List<UUID> courtCentreIds = Arrays.asList(randomUUID(), randomUUID());

        final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

        final UploadSubscription command = new UploadSubscription();
        command.setChannel(STRING.next());
        command.setChannelProperties(properties);
        command.setDestination(STRING.next());
        command.setUserGroups(asList(STRING.next(), STRING.next()));
        command.setCourtCentreIds(courtCentreIds);
        command.setNowTypeIds(nowTypeIds);

        UploadSubscriptionsCommand uploadSubscriptionsCommand = new UploadSubscriptionsCommand();
        uploadSubscriptionsCommand.setId(randomUUID());
        uploadSubscriptionsCommand.setSubscriptions(asList(command));
        uploadSubscriptionsCommand.setReferenceDate("01012018");

        return uploadSubscriptionsCommand;

    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(any(UUID.class))).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}