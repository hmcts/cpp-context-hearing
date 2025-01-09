package uk.gov.moj.cpp.hearing.command.handler;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Target2.target2;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.command.result.SaveDraftResultV2Command.saveDraftResultCommand;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR;
import static uk.gov.moj.cpp.hearing.domain.HearingState.VALIDATED;
import static uk.gov.moj.cpp.hearing.domain.ResultsErrorType.HEARING_LOCKED;
import static uk.gov.moj.cpp.hearing.domain.ResultsErrorType.SAVE_RESULTS_NOT_PERMITTED;
import static uk.gov.moj.cpp.hearing.domain.ResultsErrorType.VERSION_OFF_SEQUENCE;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
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
import uk.gov.moj.cpp.hearing.command.result.DeleteDraftResultV2Command;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultV2Command;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.command.result.UpdateDaysResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.ResultsError;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ManageResultsFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.test.ObjectConverters;
import uk.gov.moj.cpp.hearing.test.TestTemplates;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Creating a new test file for V2 due to the poor static implementation of {@link
 * SaveHearingCaseNoteCommandHandler} meaning tests impact other tests and fail when ran together
 * but pass in isolation.
 */
@SuppressWarnings({"serial", "unchecked"})
@ExtendWith(MockitoExtension.class)
public class ShareResultsCommandHandlerV2Test {

    private static final String HEARING_RESULTS_SHARED_V2_EVENT_NAME = "hearing.events.results-shared-v2";
    private static final String DRAFT_RESULT_SAVED_V2_EVENT_NAME = "hearing.draft-result-saved-v2";
    private static final String HEARING_COMMAND_SAVE_DRAFT_RESULT_V2 = "hearing.command.save-draft-result-v2";
    private static final String HEARING_MANAGE_RESULTS_FAILED = "hearing.manage-results-failed";

    private static InitiateHearingCommand initiateHearingCommand;
    private static ProsecutionCounselAdded prosecutionCounselAdded;
    private static DefenceCounselAdded defenceCounselUpsert;
    private static HearingExtended hearingExtended;
    private static NowsVariantsSavedEvent nowsVariantsSavedEvent;
    private static UUID metadataId;
    private static ZonedDateTime sharedTime;
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(DraftResultSavedV2.class, DraftResultSaved.class, ResultsShared.class, SaveDraftResultFailed.class,
            ResultsSharedV2.class, ResultLinesStatusUpdated.class, DaysResultLinesStatusUpdated.class, ManageResultsFailed.class);
    private DefendantDetailsUpdated defendantDetailsUpdated;
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

    @BeforeAll
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
                cpd.getDisabilityStatus(), cpd.getDocumentationLanguageNeeds(), cpd.getEthnicity(), firstName, cpd.getGender(), cpd.getHearingLanguageNeeds(), cpd.getInterpreterLanguageNeeds(),
                cpd.getLastName(), cpd.getMiddleName(), cpd.getNationalInsuranceNumber(), cpd.getNationalityCode(), cpd.getNationalityDescription(), cpd.getNationalityId(),
                cpd.getOccupation(), cpd.getOccupationCode(), cpd.getPersonMarkers(), cpd.getSpecificRequirements(), cpd.getTitle());

        final PersonDefendant newPersonDefendant = new PersonDefendant(curPd.getArrestSummonsNumber(), curPd.getBailConditions(),
                curPd.getBailReasons(), curPd.getBailStatus(), curPd.getCustodialEstablishment(),
                curPd.getCustodyTimeLimit(), curPd.getDriverLicenceCode(), curPd.getDriverLicenseIssue(),
                curPd.getDriverNumber(), curPd.getEmployerOrganisation(), curPd.getEmployerPayrollReference(),
                curPd.getPerceivedBirthYear(), person, curPd.getPoliceBailConditions(), curPd.getPoliceBailStatus(), curPd.getVehicleOperatorLicenceNumber());


        defendant.setPersonDefendant(newPersonDefendant);
        defendant.setProsecutionCaseId(currentDefendant.getProsecutionCaseId());
        return defendant;
    }

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        when(this.eventSource.getStreamById(initiateHearingCommand.getHearing().getId())).thenReturn(this.hearingEventStream);
        defendantDetailsUpdated = new DefendantDetailsUpdated(initiateHearingCommand.getHearing().getId(), convert(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0), "Test"));
    }

    @Test
    public void givenNoExistingVersionRecordedForHearingDay_whenSaveDraftResultsWithFirstVersion_shouldRaiseDraftResultSavedV2Event() throws Exception {

        final UUID userId = UUID.randomUUID();
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
        }};
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final LocalDate hearingDay = LocalDate.now();
        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand()
                .setHearingId(initiateHearingCommand.getHearing().getId())
                .setHearingDay(hearingDay)
                .setVersion(1);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));

        this.shareResultsCommandHandler.saveDraftResultV2(envelope);
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream).anyMatch(e -> DRAFT_RESULT_SAVED_V2_EVENT_NAME.equals(e.metadata().name())), is(true));
    }

    @Test
    public void givenVersionRecordedForHearingDay_whenSaveDraftResultsWithCorrectIncrementVersion_shouldRaiseDraftResultSavedV2Event() throws Exception {

        final UUID userId = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(initialDraftResultSavedV2));
        }};
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand().setHearingId(hearingId).setHearingDay(hearingDay).setVersion(2);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));

        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> DRAFT_RESULT_SAVED_V2_EVENT_NAME.equals(e.metadata().name())), is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), DraftResultSavedV2.class), isBean(DraftResultSavedV2.class)
                .with(DraftResultSavedV2::getHearingId, is(hearingId))
                .with(DraftResultSavedV2::getHearingDay, is(hearingDay))
                .with(DraftResultSavedV2::getAmendedResultVersion, is(2))
        );
    }

    @Test
    public void givenVersionRecordedForAHearingDay_whenSaveDraftResultsForDifferentHearingDayWithVersion_shouldRaiseDraftResultSavedV2Event() throws Exception {

        final UUID userId = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay1 = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final int initialVersion = 1;
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay1, someJsonObject, userId, initialVersion);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(initialDraftResultSavedV2));
        }};
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);
        final LocalDate hearingDay2 = LocalDate.now();
        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand().setHearingId(hearingId).setHearingDay(hearingDay2).setVersion(initialVersion);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));

        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> DRAFT_RESULT_SAVED_V2_EVENT_NAME.equals(e.metadata().name())), is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), DraftResultSavedV2.class), isBean(DraftResultSavedV2.class)
                .with(DraftResultSavedV2::getHearingId, is(hearingId))
                .with(DraftResultSavedV2::getHearingDay, is(hearingDay2))
                .with(DraftResultSavedV2::getAmendedResultVersion, is(initialVersion))
        );
    }

    @Test
    public void givenVersionRecordedForHearingDay_whenSaveDraftResultsWithInCorrectVersionIncrementReceived_shouldRaiseManageResultsFailedEvent() throws Exception {

        final UUID userId = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(initialDraftResultSavedV2));
        }};
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand().setHearingId(hearingId).setHearingDay(hearingDay).setVersion(5);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));

        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        final ManageResultsFailed expectedEvent = new ManageResultsFailed(hearingId, VALIDATED,
                VERSION_OFF_SEQUENCE.toError("Save draft results failed for version: 5, lastUpdatedVersion: 1"),
                hearingDay, 1, userId, 5, userId);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), is(true));
        final ManageResultsFailed actualEvent = ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class);
        assertManageResultsFailed(actualEvent, expectedEvent);
    }

    @Test
    public void givenDraftResultsValidated_whenSaveDraftResultsWithInCorrectVersionIncrementReceived_shouldRaiseManageResultsFailedEvent() throws Exception {

        final UUID userId = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final int initialVersion = 1;
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, initialVersion);
        final ResultAmendmentsValidated resultAmendmentsValidated = new ResultAmendmentsValidated(hearingId, randomUUID(), ZonedDateTime.now());
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(initialDraftResultSavedV2));
            apply(Stream.of(resultAmendmentsValidated));
        }};
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand().setHearingId(hearingId).setHearingDay(hearingDay).setVersion(initialVersion + 1);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));

        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        final ManageResultsFailed expectedEvent = new ManageResultsFailed(hearingId, VALIDATED,
                SAVE_RESULTS_NOT_PERMITTED.toError("Save results not permitted! Hearing is in VALIDATED state"),
                hearingDay, 1, userId, 2, userId);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), is(true));
        final ManageResultsFailed actualEvent = ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class);
        assertManageResultsFailed(actualEvent, expectedEvent);
    }

    @Test
    public void givenResultsLocked_whenSaveDraftResultsByDifferentUser_shouldRaiseManageResultsFailedEvent() throws Exception {

        final UUID caseId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final int initialVersion = 1;
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, initialVersion);
        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId, SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final ResultsSharedV3 resultsSharedV3 = ResultsSharedV3.builder()
                .withNewAmendmentResults(asList(new NewAmendmentResult(randomUUID(), ZonedDateTime.now())))
                .withTargets(TestUtilities.asList(getTarget(caseId, initiateHearingCommand.getHearing())))
                .withHearingDay(hearingDay)
                .withHearing(initiateHearingCommand.getHearing())
                .build();
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(initialDraftResultSavedV2));
            apply(Stream.of(resultsSharedV3));
            apply(Stream.of(hearingAmended));
        }};
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultV2Command saveDraftResultV2Command = saveDraftResultCommand().setHearingId(hearingId).setHearingDay(hearingDay).setVersion(initialVersion + 1);
        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, HEARING_COMMAND_SAVE_DRAFT_RESULT_V2).withUserId(userId2.toString()), objectToJsonObjectConverter.convert(saveDraftResultV2Command));

        this.shareResultsCommandHandler.saveDraftResultV2(envelope);

        final ManageResultsFailed expectedEvent = new ManageResultsFailed(hearingId, SHARED_AMEND_LOCKED_ADMIN_ERROR,
                HEARING_LOCKED.toError("Save results not permitted! Hearing locked by different user " + userId),
                hearingDay, 1, userId, 2, userId2);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), is(true));
        final ManageResultsFailed actualEvent = ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class);
        assertManageResultsFailed(actualEvent, expectedEvent);
    }

    @Test
    public void shouldRaiseDeleteDraftResultsV2Event() throws Exception {

        final UUID userId = UUID.randomUUID();
        HearingAmended hearingAmended = new HearingAmended(initiateHearingCommand.getHearing().getId(), userId, SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final DeleteDraftResultV2Command deleteDraftResultV2Command = DeleteDraftResultV2Command.deleteDraftResultCommand()
                .setHearingId(initiateHearingCommand.getHearing().getId())
                .setHearingDay(LocalDate.now());

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.delete.draft-result-v2").withUserId(userId.toString()), objectToJsonObjectConverter.convert(deleteDraftResultV2Command));

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(hearingAmended));
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        this.shareResultsCommandHandler.deleteDraftResultV2(envelope);
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

        shareDaysResultsCommand.setResultLines(getResultLines(resultLineIn, targetDraft, childResultLineIds, parentResultLineIds));

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.share-results-v2"), objectToJsonObjectConverter.convert(shareDaysResultsCommand));

        this.shareResultsCommandHandler.shareResultV2(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> HEARING_RESULTS_SHARED_V2_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + HEARING_RESULTS_SHARED_V2_EVENT_NAME, efound.get(), notNullValue());

        final ResultsSharedV2 resultsShared = jsonObjectToObjectConverter.convert(efound.get().payloadAsJsonObject(), ResultsSharedV2.class);

        assertThat(resultsShared, isBean(ResultsSharedV2.class)
                .with(resultsSharedV2 -> resultsSharedV2.getIsReshare(), is(false))
                .with(h -> h.getTargets().size(), is(1))
                .with(ResultsSharedV2::getTargets, first(isBean(Target.class)
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

    private static List<SharedResultsCommandResultLineV2> getResultLines(final ResultLine resultLineIn, final Target targetDraft, final List<UUID> childResultLineIds, final List<UUID> parentResultLineIds) {
        return List.of(
                new SharedResultsCommandResultLineV2(
                        null,
                        resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        targetDraft.getOffenceId(),
                        targetDraft.getDefendantId(),
                        targetDraft.getMasterDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream().map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(),
                                p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel(), p.getPromptRef())).collect(Collectors.toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        targetDraft.getApplicationId(),
                        null,
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        null,
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        childResultLineIds,
                        parentResultLineIds,
                        targetDraft.getShadowListed(),
                        targetDraft.getDraftResult(),
                        "",
                        "I",
                        false
                )
        );
    }

    private static void assertManageResultsFailed(final ManageResultsFailed actualEvent, final ManageResultsFailed expectedEvent) {
        assertThat(actualEvent, isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, is(expectedEvent.getHearingId()))
                .with(ManageResultsFailed::getHearingDay, is(expectedEvent.getHearingDay()))
                .with(ManageResultsFailed::getLastUpdatedVersion, is(expectedEvent.getLastUpdatedVersion()))
                .with(ManageResultsFailed::getLastUpdatedByUserId, is(expectedEvent.getLastUpdatedByUserId()))
                .with(ManageResultsFailed::getVersion, is(expectedEvent.getVersion()))
                .with(ManageResultsFailed::getUserId, is(expectedEvent.getUserId()))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, is(expectedEvent.getResultsError().getType()))
                        .with(ResultsError::getCode, is(expectedEvent.getResultsError().getCode()))
                        .with(ResultsError::getReason, is(expectedEvent.getResultsError().getReason())))
        );
    }

    private Target2 getTarget(final UUID caseId, final Hearing hearing) {
        return target2().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(randomUUID())
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(randomUUID())
                        .withNonStandaloneAncillaryResult(false)
                        .withCategory("I")
                        .build()))
                .withHearingDay(hearing.getHearingDays().get(0).getSittingDay().toLocalDate())
                .build();
    }
}