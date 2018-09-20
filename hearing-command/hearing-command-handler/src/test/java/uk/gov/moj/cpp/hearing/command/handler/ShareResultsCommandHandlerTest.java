package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Prompt;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"serial", "unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class ShareResultsCommandHandlerTest {

    final static String DRAFT_RESULT_SAVED_EVENT_NAME = "hearing.draft-result-saved";
    public static final String HEARING_RESULTS_SHARED_EVENT_NAME = "hearing.results-shared";

    @InjectMocks
    private ShareResultsCommandHandler shareResultsCommandHandler;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private Clock clock;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(DraftResultSaved.class, ResultsShared.class);

    private static InitiateHearingCommand initiateHearingCommand;
    private static ProsecutionCounselUpsert prosecutionCounselUpsert;
    private static DefenceCounselUpsert defenceCounselUpsert;
    private static uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent nowsVariantsSavedEvent;
    private static UUID metadataId;
    private static ZonedDateTime sharedTime;

    @BeforeClass
    public static void init() {
        initiateHearingCommand = standardInitiateHearingTemplate();
        metadataId = UUID.randomUUID();
        sharedTime = new UtcClock().now();
        prosecutionCounselUpsert = ProsecutionCounselUpsert.builder()
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withPersonId(randomUUID())
                .withAttendeeId(randomUUID())
                .withTitle(STRING.next())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withStatus(STRING.next())
                .build();
        defenceCounselUpsert = DefenceCounselUpsert.builder()
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withPersonId(randomUUID())
                .withAttendeeId(randomUUID())
                .withTitle(STRING.next())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withStatus(STRING.next())
//TODO:GPE-5480                .withDefendantIds(singletonList(initiateHearingCommand.getHearing().getDefendants().get(0).getId()))
                .build();
        nowsVariantsSavedEvent = NowsVariantsSavedEvent.nowsVariantsSavedEvent()
                .setHearingId(initiateHearingCommand.getHearing().getId())
                .setVariants(singletonList(Variant.variant()
                        .setKey(VariantKey.variantKey().setDefendantId(UUID.randomUUID()))
                        .setValue(VariantValue.variantValue())
                ));
    }

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        when(this.eventSource.getStreamById(initiateHearingCommand.getHearing().getId())).thenReturn(this.hearingEventStream);
        when(this.clock.now()).thenReturn(sharedTime);
    }

    @Test
    public void shouldRaiseDraftResultSaved() throws Exception {

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselUpsert));
            apply(Stream.of(defenceCounselUpsert));
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now());

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.save-draft-result"), objectToJsonObjectConverter.convert(saveDraftResultCommand));

        final Target targetIn = saveDraftResultCommand.getTarget();
        final ResultLine resultLineIn = targetIn.getResultLines().get(0);
        final Prompt promptIn = resultLineIn.getPrompts().get(0);
        this.shareResultsCommandHandler.saveDraftResult(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> DRAFT_RESULT_SAVED_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + DRAFT_RESULT_SAVED_EVENT_NAME, efound.get(), IsNull.notNullValue());

        assertThat(asPojo(efound.get(), DraftResultSaved.class), isBean(DraftResultSaved.class)
                .with(DraftResultSaved::getTarget, isBean(Target.class)
                        .with(Target::getTargetId, is(targetIn.getTargetId()))
                        .with(Target::getDefendantId, is(targetIn.getDefendantId()))
                        .with(Target::getDraftResult, is(targetIn.getDraftResult()))
                        .with(Target::getHearingId, is(targetIn.getHearingId()))
                        .with(Target::getOffenceId, is(targetIn.getOffenceId()))
                        .with(t -> t.getResultLines().size(), is(targetIn.getResultLines().size()))
                        .with(Target::getResultLines, first(isBean(ResultLine.class)
                                //TODO 5480 add more fields
                                .with(ResultLine::getResultLineId, is(resultLineIn.getResultLineId()))
                                .with(ResultLine::getOrderedDate, is(resultLineIn.getOrderedDate()))
                                .with(r -> r.getPrompts().size(), is(resultLineIn.getPrompts().size()))
                                .with(ResultLine::getPrompts, first(isBean(Prompt.class)
                                        //TODO 5480 add more fields
                                        .with(Prompt::getId, is(promptIn.getId()))
                                        .with(Prompt::getLabel, is(promptIn.getLabel()))
                                ))
                        ))
                )
        );

    }

    @Test
    public void shouldRaiseResultsSharedEvent() throws Exception {

        final SaveDraftResultCommand saveDraftResultCommand = TestTemplates.saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now());
        final Target targetIn = saveDraftResultCommand.getTarget();
        final ResultLine resultLineIn = targetIn.getResultLines().get(0);
        final Prompt promptIn = resultLineIn.getPrompts().get(0);
        final DraftResultSaved draftResultSavedEvent = (new DraftResultSaved(targetIn));

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselUpsert));
            apply(Stream.of(defenceCounselUpsert));
            apply(Stream.of(nowsVariantsSavedEvent));
            apply(draftResultSavedEvent);
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final ShareResultsCommand shareResultsCommand =
                TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate(initiateHearingCommand.getHearing().getId());

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.share-results"), objectToJsonObjectConverter.convert(shareResultsCommand));

        this.shareResultsCommandHandler.shareResult(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> HEARING_RESULTS_SHARED_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + HEARING_RESULTS_SHARED_EVENT_NAME, efound.get(), IsNull.notNullValue());

        assertThat(asPojo(efound.get(), ResultsShared.class), isBean(ResultsShared.class)
                .with(ResultsShared::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(targetIn.getHearingId()))
                        .with(h -> h.getTargets().size(), is(1))
                        .with(Hearing::getTargets, first(isBean(Target.class)
                                .with(Target::getTargetId, is(targetIn.getTargetId()))
                                .with(t -> t.getResultLines().size(), is(targetIn.getResultLines().size()))
                                .with(Target::getResultLines, first(isBean(ResultLine.class)
                                        .with(ResultLine::getResultLineId, is(resultLineIn.getResultLineId()))
                                        .with(rl -> rl.getPrompts().size(), is(resultLineIn.getPrompts().size()))
                                        .with(ResultLine::getPrompts, first(isBean(Prompt.class)
                                                .with(Prompt::getId, is(promptIn.getId()))
                                        ))
                                ))
                        ))
                )
        );
    }
}