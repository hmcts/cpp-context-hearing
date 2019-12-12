package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.applicationDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.applicationDraftResultWithOutcomeCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.CourtApplicationResponseType;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
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
import uk.gov.moj.cpp.hearing.command.result.ApplicationDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.application.ApplicationResponseSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings({"serial", "unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class ShareResultsCommandHandlerTest {

    public static final String HEARING_RESULTS_SHARED_EVENT_NAME = "hearing.results-shared";
    final static String DRAFT_RESULT_SAVED_EVENT_NAME = "hearing.draft-result-saved";
    final static String APPLICATION_DRAFT_RESULT_EVENT_NAME = "hearing.application-draft-resulted";

    private static InitiateHearingCommand initiateHearingCommand;
    private static ProsecutionCounselAdded prosecutionCounselAdded;
    private static DefenceCounselAdded defenceCounselUpsert;
    private static HearingExtended hearingExtended;
    private static uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent nowsVariantsSavedEvent;
    private static UUID metadataId;
    private static ZonedDateTime sharedTime;
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(DraftResultSaved.class, ResultsShared.class, ApplicationDraftResulted.class);
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

    @BeforeClass
    public static void init() {
        initiateHearingCommand = standardInitiateHearingTemplate();
        metadataId = UUID.randomUUID();
        sharedTime = new UtcClock().now();
        ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next()
        );
        prosecutionCounselAdded = new ProsecutionCounselAdded(prosecutionCounsel, randomUUID());

        DefenceCounsel defenceCounsel = new DefenceCounsel(
                Arrays.asList(LocalDate.now()),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                STRING.next()
        );
        defenceCounselUpsert = new DefenceCounselAdded(defenceCounsel, randomUUID());

        nowsVariantsSavedEvent = NowsVariantsSavedEvent.nowsVariantsSavedEvent()
                .setHearingId(initiateHearingCommand.getHearing().getId())
                .setVariants(singletonList(Variant.variant()
                        .setKey(VariantKey.variantKey().setDefendantId(UUID.randomUUID()))
                        .setValue(VariantValue.variantValue())
                ));

        hearingExtended = new HearingExtended(initiateHearingCommand.getHearing().getId(), CourtApplication.courtApplication().withId(UUID.randomUUID()).build());
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
            apply(Stream.of(prosecutionCounselAdded));
            apply(Stream.of(defenceCounselUpsert));
            apply(getApplicationResponseSaved(initiateHearingCommand.getHearing().getCourtApplications().get(0)));
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now());
        final Target targetIn = saveDraftResultCommand.getTarget();

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.save-draft-result"), objectToJsonObjectConverter.convert(targetIn));

        final ResultLine resultLineIn = targetIn.getResultLines().get(0);
        final DelegatedPowers delegatedPowers = resultLineIn.getDelegatedPowers();
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
                )
        );

    }


    @Test
    public void shouldRaiseApplicationDraftResulted() throws Exception {

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final ApplicationDraftResultCommand applicationDraftResultCommand = applicationDraftResultCommandTemplate(initiateHearingCommand.getHearing().getId());

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.application-draft-result"), objectToJsonObjectConverter.convert(applicationDraftResultCommand));

        this.shareResultsCommandHandler.applicationDraftResult(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> APPLICATION_DRAFT_RESULT_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + APPLICATION_DRAFT_RESULT_EVENT_NAME, efound.get(), IsNull.notNullValue());

        assertThat(asPojo(efound.get(), ApplicationDraftResulted.class), isBean(ApplicationDraftResulted.class)
                .with(ApplicationDraftResulted::getTargetId, is(applicationDraftResultCommand.getTargetId()))
                .with(ApplicationDraftResulted::getApplicationId, is(applicationDraftResultCommand.getApplicationId()))
                .with(ApplicationDraftResulted::getDraftResult, is(applicationDraftResultCommand.getDraftResult()))
                .with(ApplicationDraftResulted::getHearingId, is(applicationDraftResultCommand.getHearingId()))
        );

    }

    @Test
    public void shouldRaiseApplicationDraftResultedWithOutCome() throws Exception {

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
        }};

        final UUID applicationId = randomUUID();
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final CourtApplicationOutcomeType courtApplicationOutcomeType = CourtApplicationOutcomeType.courtApplicationOutcomeType().withDescription("Admitted")
                .withId(UUID.randomUUID())
                .withSequence(1).build();

        final ApplicationDraftResultCommand applicationDraftResultCommand = applicationDraftResultWithOutcomeCommandTemplate(initiateHearingCommand.getHearing().getId(), applicationId, courtApplicationOutcomeType);

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.application-draft-result"), objectToJsonObjectConverter.convert(applicationDraftResultCommand));

        this.shareResultsCommandHandler.applicationDraftResult(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> APPLICATION_DRAFT_RESULT_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + APPLICATION_DRAFT_RESULT_EVENT_NAME, efound.get(), IsNull.notNullValue());

        assertThat(asPojo(efound.get(), ApplicationDraftResulted.class), isBean(ApplicationDraftResulted.class)
                .with(ApplicationDraftResulted::getTargetId, is(applicationDraftResultCommand.getTargetId()))
                .with(ApplicationDraftResulted::getApplicationId, is(applicationDraftResultCommand.getApplicationId()))
                .with(ApplicationDraftResulted::getDraftResult, is(applicationDraftResultCommand.getDraftResult()))
                .with(ApplicationDraftResulted::getHearingId, is(applicationDraftResultCommand.getHearingId()))
                .with(ApplicationDraftResulted::getApplicationOutcomeType, is(courtApplicationOutcomeType))
        );
    }

    @Test
    public void shouldRaiseResultsSharedEvent() throws Exception {

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now());
        final Target targetDraft = saveDraftResultCommand.getTarget();
        final ResultLine resultLineIn = targetDraft.getResultLines().get(0);
        targetDraft.setResultLines(null);
        final Prompt promptIn = resultLineIn.getPrompts().get(0);
        final DraftResultSaved draftResultSavedEvent = (new DraftResultSaved(targetDraft));
        final int initialCourtApplicationCount = initiateHearingCommand.getHearing().getCourtApplications().size();
        final ApplicationResponseSaved applicationResponseSaved = getApplicationResponseSaved(initiateHearingCommand.getHearing().getCourtApplications().get(0));

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselAdded));
            apply(Stream.of(defenceCounselUpsert));
            apply(Stream.of(nowsVariantsSavedEvent));
            apply(draftResultSavedEvent);
            apply(hearingExtended);
            apply(applicationResponseSaved);
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final ShareResultsCommand shareResultsCommand =
                TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate(initiateHearingCommand.getHearing().getId());

        shareResultsCommand.setCourtClerk(DelegatedPowers.delegatedPowers()
                .withFirstName("test")
                .withLastName("testington")
                .withUserId(randomUUID())
                .build());

        shareResultsCommand.setResultLines(Arrays.asList(
                new SharedResultsCommandResultLine(resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        targetDraft.getTargetId(),
                        targetDraft.getOffenceId(),
                        targetDraft.getDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream().map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(),
                                p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel())).collect(Collectors.toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        targetDraft.getApplicationId(),
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        resultLineIn.getAmendmentDate(),
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        null
                )
        ));

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.share-results"), objectToJsonObjectConverter.convert(shareResultsCommand));

        this.shareResultsCommandHandler.shareResult(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> HEARING_RESULTS_SHARED_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + HEARING_RESULTS_SHARED_EVENT_NAME, efound.get(), IsNull.notNullValue());

        final ResultsShared resultsShared = jsonObjectToObjectConverter.convert(efound.get().payloadAsJsonObject(), ResultsShared.class);

        assertThat(resultsShared, isBean(ResultsShared.class)
                .with(h -> h.getTargets().size(), is(1))
                .with(ResultsShared::getTargets, first(isBean(Target.class)
                        .with(Target::getTargetId, is(targetDraft.getTargetId()))
                        .with(t -> t.getResultLines().size(), is(shareResultsCommand.getResultLines().size()))
                        .with(Target::getResultLines, first(isBean(ResultLine.class)
                                .with(ResultLine::getResultLineId, is(resultLineIn.getResultLineId()))
                                .with(rl -> rl.getPrompts().size(), is(resultLineIn.getPrompts().size()))
                                .with(ResultLine::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getId, is(promptIn.getId()))))))
                ))
                .with(ResultsShared::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(targetDraft.getHearingId()))
                        .withValue(h -> h.getCourtApplications().size(), initialCourtApplicationCount + 1)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                        .withValue(CourtApplication::getId, hearingExtended.getCourtApplication().getId()
                                        )
                                )
                        ))
        );
    }

    private ApplicationResponseSaved getApplicationResponseSaved(final CourtApplication courtApplication) {
        return ApplicationResponseSaved.applicationResponseSaved()
                .setApplicationPartyId(courtApplication.getRespondents().get(0).getPartyDetails().getId())
                .setCourtApplicationResponse(CourtApplicationResponse.courtApplicationResponse()
                        .withApplicationResponseDate(LocalDate.now())
                        .withOriginatingHearingId(randomUUID())
                        .withApplicationId(courtApplication.getId())
                        .withApplicationResponseType(CourtApplicationResponseType.courtApplicationResponseType()
                                .withDescription("Admitted")
                                .withId(UUID.randomUUID())
                                .withSequence(1).build())
                        .build());
    }
}