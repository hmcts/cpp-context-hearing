package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nows.NowVariantUtil;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;

import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.io.InputStream;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class GenerateNowsCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            NowsRequested.class,
            NowsMaterialStatusUpdated.class,
            NowsVariantsSavedEvent.class
    );
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
    private GenerateNowsCommandHandler generateNowsCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void generateNowsTest() throws Throwable {

        final InputStream is = GenerateNowsCommandHandlerTest.class.getResourceAsStream("/hearing.command.generate-nows.json");
        final NowsRequested nowsRequestedCommand = new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);

        final UUID hearingId = fromString(nowsRequestedCommand.getHearing().getId());

        setupMockedEventStream(hearingId, this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.command.generate-nows"), objectToJsonObjectConverter.convert(nowsRequestedCommand));

        this.generateNowsCommandHandler.genarateNows(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.nows-requested"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearing.id", equalTo(hearingId.toString())),
                                withJsonPath("$.hearing.defendants.[0].defenceOrganisation", is(nowsRequestedCommand.getHearing().getDefendants().get(0).getDefenceOrganisation()))
                        )))
        ));
    }

    @Test
    public void saveVariantsTest() throws Throwable {

        final SaveNowsVariantsCommand command = (new NowVariantUtil()).createSampleNowsVariantsCommand();

        setupMockedEventStream(command.getHearingId(), this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope commandEnvelope = envelopeFrom(metadataWithRandomUUID("hearing.command.save-nows-variants"), objectToJsonObjectConverter.convert(command));

        this.generateNowsCommandHandler.saveNowsVariants(commandEnvelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(commandEnvelope)
                                .withName("hearing.nows-variants-saved"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(command.getHearingId().toString())),
                                withJsonPath("$.variants.[0].key.defendantId", is(command.getVariants().get(0).getKey().getDefendantId().toString()))
                        )))
        ));

    }


    @Test
    public void nowsGeneratedTest() throws Throwable {

        final NowsMaterialStatusUpdated nowsMaterialStatusUpdated = new NowsMaterialStatusUpdated(UUID.randomUUID(), UUID.randomUUID(), "generated");

        setupMockedEventStream(nowsMaterialStatusUpdated.getHearingId(), this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-nows-material-status"), objectToJsonObjectConverter.convert(nowsMaterialStatusUpdated));

        this.generateNowsCommandHandler.nowsGenerated(jsonEnvelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelope)
                                .withName("hearing.events.nows-material-status-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(nowsMaterialStatusUpdated.getHearingId().toString())),
                                withJsonPath("$.materialId", equalTo(nowsMaterialStatusUpdated.getMaterialId().toString())),
                                withJsonPath("$.status", equalTo(nowsMaterialStatusUpdated.getStatus()))
                        )))
        ));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}