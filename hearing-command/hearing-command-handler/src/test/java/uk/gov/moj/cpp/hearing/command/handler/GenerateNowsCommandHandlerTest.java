package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowsRequestedTemplates.nowsRequestedTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

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

        NowsRequested nowsRequestedCommand = nowsRequestedTemplate();

        final UUID hearingId = nowsRequestedCommand.getHearing().getId();

        setupMockedEventStream(hearingId, this.hearingEventStream, new HearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.command.generate-nows"), objectToJsonObjectConverter.convert(nowsRequestedCommand));

        this.generateNowsCommandHandler.generateNows(command);

        final JsonEnvelope jsonEnvelope = verifyAppendAndGetArgumentFrom(this.hearingEventStream).findAny().get();

        assertThat(asPojo(jsonEnvelope, NowsRequested.class), isBean(NowsRequested.class)
                .with(NowsRequested::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(nowsRequestedCommand.getHearing().getId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(nowsRequestedCommand.getHearing().getProsecutionCases().get(0).getId()))
                        ))
                )
        );
    }

    @Test
    public void saveVariantsTest() throws Throwable {

        final SaveNowsVariantsCommand command = createSampleNowsVariantsCommand();

        setupMockedEventStream(command.getHearingId(), this.hearingEventStream, new HearingAggregate());

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

        setupMockedEventStream(nowsMaterialStatusUpdated.getHearingId(), this.hearingEventStream, new HearingAggregate());

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

    public SaveNowsVariantsCommand createSampleNowsVariantsCommand() {
        final UUID hearingId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final UUID nowsTypeId = UUID.randomUUID();
        return SaveNowsVariantsCommand.saveNowsVariantsCommand()
                .setHearingId(hearingId)
                .setVariants(
                        Arrays.asList(
                                Variant.variant()
                                        .setKey(
                                                VariantKey.variantKey()
                                                        .setHearingId(hearingId)
                                                        .setDefendantId(defendantId)
                                                        .setNowsTypeId(nowsTypeId)
                                                        .setUsergroups(Arrays.asList("Listings Officers", "Court Clerks"))
                                        )
                                        .setValue(
                                                VariantValue.variantValue()
                                                        .setMaterialId(UUID.randomUUID())
                                                        .setStatus(VariantStatus.BUILDING)
                                                        .setResultLines(
                                                                Arrays.asList(
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID()),
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID())
                                                                )
                                                        )
                                        ),
                                Variant.variant()
                                        .setKey(
                                                VariantKey.variantKey()
                                                        .setHearingId(hearingId)
                                                        .setDefendantId(defendantId)
                                                        .setNowsTypeId(nowsTypeId)
                                                        .setUsergroups(Arrays.asList("System Users"))
                                        )
                                        .setValue(
                                                VariantValue.variantValue()
                                                        .setMaterialId(UUID.randomUUID())
                                                        .setStatus(VariantStatus.BUILDING)
                                                        .setResultLines(
                                                                Arrays.asList(
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(null)
                                                                                .setResultLineId(UUID.randomUUID()),
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID())
                                                                )
                                                        )
                                        )

                        )
                );
    }
}