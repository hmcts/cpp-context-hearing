package uk.gov.moj.cpp.hearing.command.handler;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
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
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.command.result.UpdateDaysResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Creating a new test file for V2 due to the poor static implementation of {@link
 * SaveHearingCaseNoteCommandHandler} meaning tests impact other tests and fail when ran together
 * but pass in isolation.
 */
@SuppressWarnings({"serial", "unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class ShareResultsCommandHandlerV2Test {

    private static final String HEARING_RESULTS_SHARED_V2_EVENT_NAME = "hearing.events.results-shared-v2";

    private static InitiateHearingCommand initiateHearingCommand;
    private static ProsecutionCounselAdded prosecutionCounselAdded;
    private static DefenceCounselAdded defenceCounselUpsert;
    private static HearingExtended hearingExtended;
    private static NowsVariantsSavedEvent nowsVariantsSavedEvent;
    private static UUID metadataId;
    private static ZonedDateTime sharedTime;
    private DefendantDetailsUpdated defendantDetailsUpdated;
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(DraftResultSaved.class, ResultsShared.class, SaveDraftResultFailed.class, ResultsSharedV2.class, ResultLinesStatusUpdated.class, DaysResultLinesStatusUpdated.class);
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
        metadataId = randomUUID();
        sharedTime = new UtcClock().now();
        final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );
        prosecutionCounselAdded = new ProsecutionCounselAdded(prosecutionCounsel, randomUUID());

        final DefenceCounsel defenceCounsel = new DefenceCounsel(
                Arrays.asList(LocalDate.now()),
                Arrays.asList(randomUUID()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                randomUUID()
        );
        defenceCounselUpsert = new DefenceCounselAdded(defenceCounsel, randomUUID());

        nowsVariantsSavedEvent = NowsVariantsSavedEvent.nowsVariantsSavedEvent()
                .setHearingId(initiateHearingCommand.getHearing().getId())
                .setVariants(singletonList(Variant.variant()
                        .setKey(VariantKey.variantKey().setDefendantId(randomUUID()))
                        .setValue(VariantValue.variantValue())
                ));

        hearingExtended = new HearingExtended(initiateHearingCommand.getHearing().getId(), null, null, null, CourtApplication.courtApplication().withId(randomUUID()).build(), null, null);
    }

    private static Defendant convert(final uk.gov.justice.core.courts.Defendant currentDefendant, String firstName) {
        Defendant defendant = new Defendant();
        defendant.setId(currentDefendant.getId());
        final PersonDefendant curPd = currentDefendant.getPersonDefendant();
        final Person cpd = curPd.getPersonDetails();
        Person person = new Person(cpd.getAdditionalNationalityCode(), cpd.getAdditionalNationalityDescription(), cpd.getAdditionalNationalityId(), cpd.getAddress(), cpd.getContact(), cpd.getDateOfBirth(),
                cpd.getDisabilityStatus(), cpd.getDocumentationLanguageNeeds(), cpd.getEthnicity(), firstName, cpd.getGender(), cpd.getInterpreterLanguageNeeds(),
                cpd.getLastName(), cpd.getMiddleName(), cpd.getNationalInsuranceNumber(), cpd.getNationalityCode(), cpd.getNationalityDescription(), cpd.getNationalityId(),
                cpd.getOccupation(), cpd.getOccupationCode(), cpd.getPersonMarkers(), cpd.getSpecificRequirements(), cpd.getTitle());

        final PersonDefendant newPersonDefendant = new PersonDefendant(curPd.getArrestSummonsNumber(), curPd.getBailConditions(),
                curPd.getBailReasons(), curPd.getBailStatus(), curPd.getCustodialEstablishment(), curPd.getCustodyTimeLimit(), curPd.getDriverLicenceCode(), curPd.getDriverLicenseIssue(),
                curPd.getDriverNumber(), curPd.getEmployerOrganisation(), curPd.getEmployerPayrollReference(),
                curPd.getPerceivedBirthYear(), person, curPd.getVehicleOperatorLicenceNumber());


        defendant.setPersonDefendant(newPersonDefendant);
        defendant.setProsecutionCaseId(currentDefendant.getProsecutionCaseId());
        return defendant;
    }

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        when(this.eventSource.getStreamById(initiateHearingCommand.getHearing().getId())).thenReturn(this.hearingEventStream);
        when(this.clock.now()).thenReturn(sharedTime);
        defendantDetailsUpdated = new DefendantDetailsUpdated(initiateHearingCommand.getHearing().getId(), convert(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0), "Test"));
    }

    @Test
    public void shouldRaiseResultsSharedV2Event() throws Exception {
        final LocalDate hearingDay = LocalDate.now();
        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now(), hearingDay);
        final Target targetDraft = saveDraftResultCommand.getTarget();
        final ResultLine resultLineIn = targetDraft.getResultLines().get(0);
        targetDraft.setResultLines(null);
        final Prompt promptIn = resultLineIn.getPrompts().get(0);
        final DraftResultSaved draftResultSavedEvent = (new DraftResultSaved(targetDraft, HearingState.INITIALISED, randomUUID()));
        final int initialCourtApplicationCount = initiateHearingCommand.getHearing().getCourtApplications().size();

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselAdded));
            apply(Stream.of(defenceCounselUpsert));
            apply(Stream.of(nowsVariantsSavedEvent));
            apply(draftResultSavedEvent);
            apply(hearingExtended);
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final ShareDaysResultsCommand shareDaysResultsCommand =
                TestTemplates.ShareResultsCommandTemplates.standardShareResultsPerDaysCommandTemplate(initiateHearingCommand.getHearing().getId());

        shareDaysResultsCommand.setCourtClerk(DelegatedPowers.delegatedPowers()
                .withFirstName("test")
                .withLastName("testington")
                .withUserId(randomUUID())
                .build());

        final List<UUID> childResultLineIds = of(randomUUID());
        final List<UUID> parentResultLineIds = of(randomUUID());

        shareDaysResultsCommand.setResultLines(Arrays.asList(
                new SharedResultsCommandResultLineV2(resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        targetDraft.getTargetId(),
                        targetDraft.getOffenceId(),
                        targetDraft.getDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream().map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(),
                                p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel(), p.getPromptRef())).collect(Collectors.toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        targetDraft.getApplicationId(),
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        Objects.nonNull(resultLineIn.getAmendmentDate()) ? resultLineIn.getAmendmentDate().atStartOfDay(ZoneId.systemDefault()) : null,
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        childResultLineIds,
                        parentResultLineIds
                )
        ));

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.share-results-v2"), objectToJsonObjectConverter.convert(shareDaysResultsCommand));

        this.shareResultsCommandHandler.shareResultV2(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> HEARING_RESULTS_SHARED_V2_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + HEARING_RESULTS_SHARED_V2_EVENT_NAME, efound.get(), notNullValue());

        final ResultsSharedV2 resultsShared = jsonObjectToObjectConverter.convert(efound.get().payloadAsJsonObject(), ResultsSharedV2.class);

        assertThat(resultsShared, isBean(ResultsSharedV2.class)
                .with(resultsSharedV2 -> resultsSharedV2.getIsReshare(), is(false))
                .with(h -> h.getTargets().size(), is(1))
                .with(ResultsSharedV2::getTargets, first(isBean(Target.class)
                        .with(Target::getTargetId, is(targetDraft.getTargetId()))
                        .with(t -> t.getResultLines().size(), is(shareDaysResultsCommand.getResultLines().size()))
                        .with(Target::getResultLines, first(isBean(ResultLine.class)
                                        .with(ResultLine::getResultLineId, is(resultLineIn.getResultLineId()))
                                        .with(rl -> rl.getPrompts().size(), is(resultLineIn.getPrompts().size()))
                                        .with(ResultLine::getPrompts, first(isBean(Prompt.class)
                                                .with(Prompt::getId, is(promptIn.getId()))))
                                        .with(ResultLine::getChildResultLineIds, is(childResultLineIds))
                                        .with(ResultLine::getParentResultLineIds, is(parentResultLineIds))
                                )
                        )
                ))
                .with(ResultsSharedV2::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(targetDraft.getHearingId()))
                        .withValue(h -> h.getCourtApplications().size(), initialCourtApplicationCount + 1)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                        .withValue(CourtApplication::getId, hearingExtended.getCourtApplication().getId()
                                        )
                                )
                        ))
        );
    }

    @Test
    public void shouldRaiseResultLinesStatusUpdatedEvent() throws Exception {

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
        }};

        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID courtClerkId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 5, 6);
        final ZonedDateTime sharedDateTime = ZonedDateTime.of(2022, 01, 01, 0, 0, 0, 0, ZoneId.of("UTC"));
        final UUID sharedResultLineId = randomUUID();

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final UpdateResultLinesStatusCommand command = UpdateResultLinesStatusCommand.builder()
                .withHearingId(hearingId)
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(courtClerkId)
                        .build())
                .withLastSharedDateTime(sharedDateTime)
                .withSharedResultLines(Arrays.asList(SharedResultLineId.builder()
                        .withSharedResultLineId(sharedResultLineId)
                        .build()))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.update-result-lines-status"), objectToJsonObjectConverter.convert(command));

        this.shareResultsCommandHandler.updateResultLinesStatus(envelope);

        final Optional<JsonEnvelope> envelopeFound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> "hearing.result-lines-status-updated".equals(e.metadata().name())).findFirst();
        assertThat(envelopeFound.get(), notNullValue());

        final ResultLinesStatusUpdated eventFound = jsonObjectToObjectConverter.convert(envelopeFound.get().payloadAsJsonObject(), ResultLinesStatusUpdated.class);

        assertThat(eventFound, isBean(ResultLinesStatusUpdated.class)
                .with(ResultLinesStatusUpdated::getHearingId, is(hearingId))
                .with(ResultLinesStatusUpdated::getLastSharedDateTime, is(sharedDateTime))
                .with(ResultLinesStatusUpdated::getCourtClerk, notNullValue())
                .with(ResultLinesStatusUpdated::getCourtClerk, isBean(DelegatedPowers.class)
                        .with(DelegatedPowers::getUserId, is(courtClerkId)))
                .with(rlsu -> rlsu.getSharedResultLines().size(), is(1))
                .with(ResultLinesStatusUpdated::getSharedResultLines, first(isBean(SharedResultLineId.class)
                        .with(SharedResultLineId::getSharedResultLineId, is(sharedResultLineId)))));

    }

    @Test
    public void shouldRaiseDaysResultLinesStatusUpdatedEvent() throws Exception {

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
        }};

        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID courtClerkId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 5, 6);
        final ZonedDateTime sharedDateTime = ZonedDateTime.of(2022, 01, 01, 0, 0, 0, 0, ZoneId.of("UTC"));
        final UUID sharedResultLineId = randomUUID();

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final UpdateDaysResultLinesStatusCommand command = UpdateDaysResultLinesStatusCommand.builder()
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(courtClerkId)
                        .build())
                .withLastSharedDateTime(sharedDateTime)
                .withSharedResultLines(Arrays.asList(SharedResultLineId.builder()
                        .withSharedResultLineId(sharedResultLineId)
                        .build()))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.update-days-result-lines-status"), objectToJsonObjectConverter.convert(command));

        this.shareResultsCommandHandler.updateDaysResultLinesStatus(envelope);

        final Optional<JsonEnvelope> envelopeFound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> "hearing.days-result-lines-status-updated".equals(e.metadata().name())).findFirst();
        assertThat(envelopeFound.get(), notNullValue());

        final DaysResultLinesStatusUpdated eventFound = jsonObjectToObjectConverter.convert(envelopeFound.get().payloadAsJsonObject(), DaysResultLinesStatusUpdated.class);

        assertThat(eventFound, isBean(DaysResultLinesStatusUpdated.class)
                .with(DaysResultLinesStatusUpdated::getHearingId, is(hearingId))
                .with(DaysResultLinesStatusUpdated::getHearingDay, is(hearingDay))
                .with(DaysResultLinesStatusUpdated::getLastSharedDateTime, is(sharedDateTime))
                .with(DaysResultLinesStatusUpdated::getCourtClerk, notNullValue())
                .with(DaysResultLinesStatusUpdated::getCourtClerk, isBean(DelegatedPowers.class)
                        .with(DelegatedPowers::getUserId, is(courtClerkId)))
                .with(rlsu -> rlsu.getSharedResultLines().size(), is(1))
                .with(DaysResultLinesStatusUpdated::getSharedResultLines, first(isBean(SharedResultLineId.class)
                        .with(SharedResultLineId::getSharedResultLineId, is(sharedResultLineId)))));

    }

}