package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.parse;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.hamcrest.MatcherAssert;

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
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestRejected;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequested;

import java.io.IOException;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancellationFailed;

@RunWith(MockitoJUnitRunner.class)
public class RequestApprovalCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(ApprovalRequested.class, ApprovalRequestRejected.class, ResultAmendmentsCancellationFailed.class);

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private RequestApprovalCommandHandler requestApprovalCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void requestApproval() throws EventStreamException, IOException {

        //Given
        final UUID hearingId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        setupMockedEventStream(hearingId, this.hearingEventStream, new HearingAggregate());
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("userId", userId.toString())
                .build();

//        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.approval-requested"), objectToJsonObjectConverter.convert(requestApproval));

        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.approval-requested").withUserId(UUID.randomUUID().toString()), objectToJsonObjectConverter.convert(requestApproval));

        requestApprovalCommandHandler.requestApproval(commandEnvelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(commandEnvelope).withName("hearing.event.approval-rejected"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())))))
        ));
    }

    @Test
    public void shouldEmitCancelAmendmentsFailedEvent() throws EventStreamException {
        //Given
        final UUID hearingId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        setupMockedEventStream(hearingId, this.hearingEventStream, new HearingAggregate());
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);

        final JsonObject requestApproval = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build();
        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.change-cancel-amendments").withUserId(userId.toString()), objectToJsonObjectConverter.convert(requestApproval));
        requestApprovalCommandHandler.cancelAmendments(commandEnvelope);
        MatcherAssert.assertThat("Event is not cancellationFailed", ((DefaultJsonEnvelope)verifyAppendAndGetArgumentFrom(hearingEventStream).findFirst().get()).metadata().name().equals("hearing.events.results-amendments-cancellation-failed"));
    }
    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(final UUID id, final EventStream eventStream, final T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        final Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}