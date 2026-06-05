package uk.gov.moj.cpp.hearing.command.handler;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveMultipleDraftResultsCommandTemplateWithInvalidTarget;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.handler.service.validation.ValidationIssue;
import uk.gov.moj.cpp.hearing.command.handler.service.validation.ValidationRequest;
import uk.gov.moj.cpp.hearing.command.result.DeleteDraftResultV2Command;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultV2Command;
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.command.result.UpdateDaysResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
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
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.command.handler.service.validation.ResultsValidator;
import uk.gov.moj.cpp.hearing.command.handler.service.validation.ValidationRequestMapper;
import uk.gov.moj.cpp.hearing.command.handler.service.validation.ValidationResponse;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsValidationFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;

import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"serial", "unchecked"})
@ExtendWith(MockitoExtension.class)
public class ShareResultsCommandHandlerTest {

    public static final String HEARING_RESULTS_SHARED_EVENT_NAME = "hearing.results-shared";

    private static InitiateHearingCommand initiateHearingCommand;
    private static ProsecutionCounselAdded prosecutionCounselAdded;
    private static DefenceCounselAdded defenceCounselUpsert;
    private static HearingExtended hearingExtended;
    private static uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent nowsVariantsSavedEvent;
    private static UUID metadataId;
    private static ZonedDateTime sharedTime;
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(ResultsShared.class, SaveDraftResultFailed.class, ResultsValidationFailed.class);
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
    @Mock
    private ResultsValidator resultsValidationClient;
    @Mock
    private ValidationRequestMapper validationRequestMapper;
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
                cpd.getDisabilityStatus(), cpd.getDocumentationLanguageNeeds(), cpd.getEthnicity(), firstName, cpd.getGender(), cpd.getHearingLanguageNeeds(), cpd.getInterpreterLanguageNeeds(), cpd.getIsAddressConfidential(),
                cpd.getLastName(), cpd.getMiddleName(), cpd.getNationalInsuranceNumber(), cpd.getNationalityCode(), cpd.getNationalityDescription(), cpd.getNationalityId(),
                cpd.getOccupation(), cpd.getOccupationCode(), cpd.getPersonMarkers(), cpd.getSpecificRequirements(), cpd.getTitle());

        final PersonDefendant newPersonDefendant = new PersonDefendant(curPd.getArrestSummonsNumber(), curPd.getBailConditions(),
                curPd.getBailReasons(), curPd.getBailStatus(), curPd.getCustodialEstablishment(), curPd.getCustodyTimeLimit(), curPd.getDriverLicenceCode(), curPd.getDriverLicenseIssue(),
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
        lenient().when(this.eventSource.getStreamById(initiateHearingCommand.getHearing().getId())).thenReturn(this.hearingEventStream);
        defendantDetailsUpdated = new DefendantDetailsUpdated(initiateHearingCommand.getHearing().getId(), convert(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0), "Test"));
    }


    @Test
    public void shouldRaiseFailedExceptionWhenSaveDaysDraftResults() throws Exception {
        final LocalDate hearingDay = LocalDate.now();
        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now(), hearingDay);
        final Target targetDraft = saveDraftResultCommand.getTarget();
        targetDraft.setResultLines(null);
        final DraftResultSaved draftResultSavedEvent = (new DraftResultSaved(targetDraft, HearingState.INITIALISED, randomUUID()));

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselAdded));
            apply(Stream.of(defenceCounselUpsert));
            apply(Stream.of(nowsVariantsSavedEvent));
            apply(draftResultSavedEvent);
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);


        final SaveMultipleDaysResultsCommand saveMultipleDaysResultsCommand = saveMultipleDraftResultsCommandTemplateWithInvalidTarget(initiateHearingCommand, LocalDate.now(), hearingDay);

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.save-days-draft-results").withUserId(randomUUID().toString()), objectToJsonObjectConverter.convert(saveMultipleDaysResultsCommand));

        this.shareResultsCommandHandler.saveMultipleDraftResultsForHearingDay(envelope);

        Stream<JsonEnvelope> argument = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final Optional<JsonEnvelope> efound = argument
                .filter(e -> "hearing.save-draft-result-failed".equals(e.metadata().name()))
                .findFirst();

        assertThat("expected:" + "hearing.save-draft-result-failed", efound.get(), IsNull.notNullValue());

        final SaveDraftResultFailed saveDraftResultFailed = jsonObjectToObjectConverter.convert(efound.get().payloadAsJsonObject(), SaveDraftResultFailed.class);

        assertThat(saveDraftResultFailed.getTarget().getTargetId(), is(saveMultipleDaysResultsCommand.getTargets().get(0).getTargetId()));
    }


    @Test
    public void shouldRaiseResultsSharedEvent() throws Exception {
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

        final ShareResultsCommand shareResultsCommand =
                TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate(initiateHearingCommand.getHearing().getId());

        shareResultsCommand.setCourtClerk(DelegatedPowers.delegatedPowers()
                .withFirstName("test")
                .withLastName("testington")
                .withUserId(randomUUID())
                .build());

        final List<UUID> childResultLineIds = of(randomUUID());
        final List<UUID> parentResultLineIds = of(randomUUID());

        shareResultsCommand.setResultLines(Arrays.asList(
                new SharedResultsCommandResultLine(resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
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
                        resultLineIn.getAmendmentDate(),
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        childResultLineIds,
                        parentResultLineIds,
                        targetDraft.getShadowListed(),
                        targetDraft.getDraftResult()
                )
        ));

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.share-results"), objectToJsonObjectConverter.convert(shareResultsCommand));

        this.shareResultsCommandHandler.shareResult(envelope);

        Stream<JsonEnvelope> argument = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final Optional<JsonEnvelope> efound = argument
                .filter(e -> HEARING_RESULTS_SHARED_EVENT_NAME.equals(e.metadata().name()))
                .findFirst();

        assertThat("expected:" + HEARING_RESULTS_SHARED_EVENT_NAME, efound.get(), IsNull.notNullValue());

        final ResultsShared resultsShared = jsonObjectToObjectConverter.convert(efound.get().payloadAsJsonObject(), ResultsShared.class);

        assertThat(resultsShared, isBean(ResultsShared.class)
                .with(h -> h.getTargets().size(), is(1))
                .with(ResultsShared::getTargets, first(isBean(Target.class)
                        .with(t -> t.getResultLines().size(), is(shareResultsCommand.getResultLines().size()))
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


    @Test
    public void shouldRaiseResultsSharedEventAfterDDCH() throws Exception {
        final LocalDate hearingDay = LocalDate.now();
        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now(), hearingDay);
        final Target targetDraft = saveDraftResultCommand.getTarget();
        final ResultLine resultLineIn = targetDraft.getResultLines().get(0);
        targetDraft.setResultLines(null);
        final DraftResultSaved draftResultSavedEvent = (new DraftResultSaved(targetDraft, HearingState.INITIALISED, randomUUID()));

        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(Stream.of(new HearingInitiated(initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselAdded));
            apply(Stream.of(defenceCounselUpsert));
            apply(Stream.of(nowsVariantsSavedEvent));
            apply(draftResultSavedEvent);
            apply(hearingExtended);
            apply(defendantDetailsUpdated);
        }};

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final ShareResultsCommand shareResultsCommand =
                TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate(initiateHearingCommand.getHearing().getId());

        shareResultsCommand.setCourtClerk(DelegatedPowers.delegatedPowers()
                .withFirstName("test")
                .withLastName("testington")
                .withUserId(randomUUID())
                .build());

        final List<UUID> childResultLineIds = of(randomUUID());
        final List<UUID> parentResultLineIds = of(randomUUID());

        shareResultsCommand.setResultLines(Arrays.asList(
                new SharedResultsCommandResultLine(resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
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
                        resultLineIn.getAmendmentDate(),
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        childResultLineIds,
                        parentResultLineIds,
                        targetDraft.getShadowListed(),
                        targetDraft.getDraftResult()
                )
        ));

        final JsonEnvelope envelope = envelopeFrom(metadataOf(metadataId, "hearing.command.share-results"), objectToJsonObjectConverter.convert(shareResultsCommand));

        this.shareResultsCommandHandler.shareResult(envelope);

        final Optional<JsonEnvelope> efound = verifyAppendAndGetArgumentFrom(this.hearingEventStream).filter(e -> HEARING_RESULTS_SHARED_EVENT_NAME.equals(e.metadata().name())).findFirst();
        assertThat("expected:" + HEARING_RESULTS_SHARED_EVENT_NAME, efound.get(), notNullValue());

        final ResultsShared resultsShared = jsonObjectToObjectConverter.convert(efound.get().payloadAsJsonObject(), ResultsShared.class);
        assertThat(resultsShared.getDefendantDetailsChanged().size(), is(1));
        assertThat(resultsShared.getDefendantDetailsChanged().get(0), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()));
    }

    private Target getNewTarget(final Target targetToCopyFrom) {
        return Target.target()
                .withHearingId(targetToCopyFrom.getHearingId())
                .withDefendantId(targetToCopyFrom.getDefendantId())
                .withDraftResult("")
                .withOffenceId(targetToCopyFrom.getOffenceId())
                .withTargetId(randomUUID())
                .withHearingDay(targetToCopyFrom.getHearingDay())
                .build();
    }

    @Test
    public void shouldSaveDraftResult() throws Exception {
        final Target target = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now(), LocalDate.now()).getTarget();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.saveDraftResults(any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.save-draft-result").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(target));

        shareResultsCommandHandler.saveDraftResult(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldSaveDraftResultV2() throws Exception {
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final SaveDraftResultV2Command command = SaveDraftResultV2Command.saveDraftResultCommand()
                .setHearingId(hearingId)
                .setHearingDay(LocalDate.now())
                .setVersion(1);
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.saveDraftResultV2(any(), any(), any(), any(), any(), any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.save-draft-result-v2").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.saveDraftResultV2(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldDeleteDraftResultV2() throws Exception {
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final DeleteDraftResultV2Command command = DeleteDraftResultV2Command.deleteDraftResultCommand()
                .setHearingId(hearingId)
                .setHearingDay(LocalDate.now());
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.deleteDraftResultV2(any(), any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.delete-draft-result-v2").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.deleteDraftResultV2(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldSaveDraftResultForHearingDay() throws Exception {
        final Target target = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now(), LocalDate.now()).getTarget();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.saveDraftResultForHearingDay(any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.save-days-draft-result").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(target));

        shareResultsCommandHandler.saveDraftResultForHearingDay(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldSaveMultipleDraftResult() throws Exception {
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final Target target = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withTargetId(randomUUID())
                .build();
        final SaveMultipleResultsCommand command = new SaveMultipleResultsCommand(hearingId, List.of(target));
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.saveAllDraftResults(any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.save-multiple-draft-results").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.saveMultipleDraftResult(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldShareResultV2() throws Exception {
        final ShareDaysResultsCommand command = TestTemplates.ShareResultsCommandTemplates
                .standardShareResultsPerDaysCommandTemplate(initiateHearingCommand.getHearing().getId())
                .setResultLines(List.of());
        command.setHearingDay(LocalDate.now());
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.shareResultsV2(any(), any(), any(), any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);
        when(clock.now()).thenReturn(sharedTime);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.share-results-v2"),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.shareResultV2(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldShareDaysResultsWhenValidationPasses() throws Exception {
        final ShareDaysResultsCommand command = TestTemplates.ShareResultsCommandTemplates
                .standardShareResultsPerDaysCommandTemplate(initiateHearingCommand.getHearing().getId())
                .setResultLines(List.of());
        command.setHearingDay(LocalDate.now());
        final Hearing hearing = initiateHearingCommand.getHearing();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.getHearing()).thenReturn(hearing);
        when(mockAggregate.shareResultForDay(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);
        final ValidationRequest validationRequest = new ValidationRequest(
                command.getHearingId().toString(), command.getHearingDay(), "MAGISTRATE", null, List.of(), List.of(), List.of());
        when(validationRequestMapper.toValidationRequest(any(), any())).thenReturn(validationRequest);
        when(resultsValidationClient.validate(any(), any())).thenReturn(ValidationResponse.passThrough());
        when(clock.now()).thenReturn(sharedTime);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.share-days-results").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.shareResultForDay(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldRaiseValidationFailedEventWhenShareDaysResultsHasErrors() throws Exception {
        final ShareDaysResultsCommand command = TestTemplates.ShareResultsCommandTemplates
                .standardShareResultsPerDaysCommandTemplate(initiateHearingCommand.getHearing().getId())
                .setResultLines(List.of());
        command.setHearingDay(LocalDate.now());
        final Hearing hearing = initiateHearingCommand.getHearing();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.getHearing()).thenReturn(hearing);
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);
        final ValidationRequest validationRequest = new ValidationRequest(
                command.getHearingId().toString(), command.getHearingDay(), "MAGISTRATE", null, List.of(), List.of(), List.of());
        when(validationRequestMapper.toValidationRequest(any(), any())).thenReturn(validationRequest);
        final ValidationIssue error = new ValidationIssue("RULE1", "ERROR", "Validation error", List.of());
        when(resultsValidationClient.validate(any(), any())).thenReturn(new ValidationResponse(false, List.of(error), List.of()));

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.share-days-results").withUserId(randomUUID().toString()),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.shareResultForDay(envelope);

        final Stream<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(hearingEventStream);
        final Optional<JsonEnvelope> validationFailed = appended
                .filter(e -> "hearing.events.results-validation-failed".equals(e.metadata().name()))
                .findFirst();
        assertThat(validationFailed.isPresent(), is(true));
    }

    @Test
    public void shouldUpdateResultLinesStatus() throws Exception {
        final UpdateResultLinesStatusCommand command = UpdateResultLinesStatusCommand.builder()
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withCourtClerk(DelegatedPowers.delegatedPowers().withUserId(randomUUID()).withFirstName("test").withLastName("test").build())
                .withLastSharedDateTime(ZonedDateTime.now())
                .withSharedResultLines(List.of())
                .build();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.updateResultLinesStatus(any(), any(), any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.update-result-lines-status"),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.updateResultLinesStatus(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldUpdateDaysResultLinesStatus() throws Exception {
        final UpdateDaysResultLinesStatusCommand command = UpdateDaysResultLinesStatusCommand.builder()
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withCourtClerk(DelegatedPowers.delegatedPowers().withUserId(randomUUID()).withFirstName("test").withLastName("test").build())
                .withLastSharedDateTime(ZonedDateTime.now())
                .withSharedResultLines(List.of())
                .withHearingDay(LocalDate.now())
                .build();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.updateDaysResultLinesStatus(any(), any(), any(), any(), any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.update-days-result-lines-status"),
                objectToJsonObjectConverter.convert(command));

        shareResultsCommandHandler.updateDaysResultLinesStatus(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldReplicateSharedResultsForHearing() throws Exception {
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final HearingAggregate mockAggregate = mock(HearingAggregate.class);
        when(mockAggregate.replicateSharedResultsForHearing(any())).thenReturn(Stream.empty());
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(mockAggregate);

        final JsonEnvelope envelope = envelopeFrom(
                metadataOf(metadataId, "hearing.command.replicate-results"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build());

        shareResultsCommandHandler.replicateAllSharedResultsForHearing(envelope);

        verify(hearingEventStream).append(any());
    }

    @Test
    public void shouldGetDistinctApplicationIdsFromResultLines() {
        final UUID appId1 = randomUUID();
        final UUID appId2 = randomUUID();

        final SharedResultsCommandResultLineV2 line1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withApplicationIds(appId1).build();
        final SharedResultsCommandResultLineV2 line2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withApplicationIds(appId2).build();
        final SharedResultsCommandResultLineV2 lineDuplicate = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withApplicationIds(appId1).build();
        final SharedResultsCommandResultLineV2 lineNullApp = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withApplicationIds(null).build();

        final Set<UUID> result = ShareResultsCommandHandler.getDistinctApplicationIdsFromResultLines(
                List.of(line1, line2, lineDuplicate, lineNullApp));

        assertThat(result.size(), is(2));
        assertThat(result.contains(appId1), is(true));
        assertThat(result.contains(appId2), is(true));
    }

    @Test
    public void shouldReturnEmptyListFromGetAdditionalApplicationsWhenNoIds() {
        final List<CourtApplication> result = shareResultsCommandHandler.getAdditionalApplications(Set.of(), randomUUID());

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnApplicationFromGetAdditionalApplicationsWhenLatestHearingDiffers() {
        final UUID applicationId = randomUUID();
        final UUID resultedHearingId = initiateHearingCommand.getHearing().getId();
        final UUID latestHearingId = randomUUID();

        final uk.gov.justice.services.eventsourcing.source.core.EventStream applicationStream = mock(uk.gov.justice.services.eventsourcing.source.core.EventStream.class);
        final uk.gov.justice.services.eventsourcing.source.core.EventStream latestHearingStream = mock(uk.gov.justice.services.eventsourcing.source.core.EventStream.class);

        final ApplicationAggregate appAggregate = mock(ApplicationAggregate.class);
        when(appAggregate.getHearingIds()).thenReturn(List.of(latestHearingId));

        final CourtApplication expectedApp = CourtApplication.courtApplication().withId(applicationId).build();
        final Hearing latestHearing = mock(Hearing.class);
        when(latestHearing.getCourtApplications()).thenReturn(List.of(expectedApp));
        final HearingAggregate latestHearingAggregate = mock(HearingAggregate.class);
        when(latestHearingAggregate.getHearing()).thenReturn(latestHearing);

        when(eventSource.getStreamById(applicationId)).thenReturn(applicationStream);
        when(eventSource.getStreamById(latestHearingId)).thenReturn(latestHearingStream);
        when(aggregateService.get(applicationStream, ApplicationAggregate.class)).thenReturn(appAggregate);
        when(aggregateService.get(latestHearingStream, HearingAggregate.class)).thenReturn(latestHearingAggregate);

        final List<CourtApplication> result = shareResultsCommandHandler.getAdditionalApplications(
                Set.of(applicationId), resultedHearingId);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getId(), is(applicationId));
    }
}
