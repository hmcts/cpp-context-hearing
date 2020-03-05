package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.HEAD;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDefendantLegalAidStatusCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefendantLegalAidStatusUpdated.class,
            DefendantLegalAidStatusUpdatedForHearing.class);

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private UpdateDefendantLegalAidStatusCommandHandler updateDefendantLegalAidStatusCommandHandler;

    private DefendantAggregate defendantAggregate;

    private HearingAggregate hearingAggregate;

    private static InitiateHearingCommand initiateHearingCommand;

    @Before
    public void setup() {
        defendantAggregate = new DefendantAggregate();
        hearingAggregate = new HearingAggregate();
        initiateHearingCommand = standardInitiateHearingTemplate();

    }

    @Test
    public void testUpdateDefendantLegalAidStatus () throws EventStreamException {

        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();

        setupMockedEventStream(defendantId, this.eventStream, defendantAggregate);


        defendantAggregate.registerHearing(defendantId, hearingId);

        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("defendantId", defendantId.toString())
                .add("legalAidStatus", "Granted")
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()).build())
                .build();


        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-defendant-legalaid-status"), commandPayload);

        updateDefendantLegalAidStatusCommandHandler.updateDefendantLegalAidStatus(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-legalaid-status-updated"),
                        payloadIsJson(allOf(withJsonPath("$.defendantId", is(defendantId.toString()))))
                )));
    }

    @Test
    public void testUpdateDefendantLegalAidStatusWhenHearingNotRegisteredAgainstDefendant () throws EventStreamException {

        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();

        setupMockedEventStream(defendantId, this.eventStream, defendantAggregate);



        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("defendantId", defendantId.toString())
                .add("legalAidStatus", "Granted")
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()).build())
                .build();


        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-defendant-legalaid-status"), commandPayload);

        updateDefendantLegalAidStatusCommandHandler.updateDefendantLegalAidStatus(envelope);

        assertThat(this.eventStream.size(), is(0L));
    }

    @Test
    public void testUpdateDefendantLegalAidStatusForHearing () throws EventStreamException {

        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        setupMockedEventStream(hearingId, eventStream,  hearingAggregate);
        hearingAggregate.initiate(initiateHearingCommand.getHearing());


        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("defendantId", defendantId.toString())
                .add("legalAidStatus", "Granted")
                .build();


        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-defendant-legalaid-status-for-hearing"), commandPayload);

        updateDefendantLegalAidStatusCommandHandler.updateDefendantLegalAidStatusForHearing(envelope);
        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-legalaid-status-updated-for-hearing"),
                        payloadIsJson(allOf(withJsonPath("$.defendantId", is(defendantId.toString()))))
                )));

    }
    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}
