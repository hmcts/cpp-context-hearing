package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections.ListUtils.unmodifiableList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.welshInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.applicationDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.applicationDraftResultWithOutcomeCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandForMultipleOffences;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplateForDeletedResult;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardAmendedResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubProgressionGenerateNows;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubFixedListForWelshValues;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ApplicationDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jayway.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ShareResultsIT extends AbstractIT {

    public static final String DOCUMENT_TEXT = "someDocumentText";
    public static final String BOTH_JURISDICTIONS = "B";
    protected static final List<String> dismissedResultList = unmodifiableList(
            asList(
                    "14d66587-8fbe-424f-a369-b1144f1684e3",
                    "f8bd4d1f-1467-4903-b1e6-d2249ccc8c25",
                    "8542b0d9-27f0-4df3-a4a3-0ac0a85c33ad"));
    protected static final List<String> withDrawnResultList = unmodifiableList(
            asList(
                    "6feb0f2e-8d1e-40c7-af2c-05b28c69e5fc",
                    "eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8",
                    "c8326b9e-56eb-406c-b74b-9f90c772b657",
                    "eaecff82-32da-4cc1-b530-b55195485cc7",
                    "4d5f25a5-9102-472f-a2da-c58d1eeb9c93"));
    protected static final List<String> guiltyResultList = unmodifiableList(
            asList(
                    "fc612b8f-9699-459f-9ea7-b307164e4754",
                    "ce23a452-9015-4619-968f-1628d7a271c9"));


    private static final UUID TRAIL_TYPE_ID_1 = randomUUID();

    @Before
    public void begin() {
        stubProgressionGenerateNows();
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    @Test
    public void testEmptyDraftResultWhenNoDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();

        Queries.getDraftResultsPollForMatch(hearing.getId(), 30, isBean(TargetListResponse.class)
                .with(response -> response.getTargets(), is(empty())));
    }

    @Test
    public void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows() {
        stubFixedListForWelshValues();
        shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows(false);
    }

    @Test
    public void shareResults_shouldPublishResults_When_Result_Is_Deleted() {
        stubFixedListForWelshValues();
        shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows(true);
    }

    @Test
    public void shareResults_whenOneOffenceisDismissedAndOtherISGuiltyInSingleHearing_expectNoPublicEventForDefendentCaseWithDrawnOrDismissed() {

        //Given
        //Hearing Initiated

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final UUID dismissedResultDefId = UUID.fromString("14d66587-8fbe-424f-a369-b1144f1684e3");

        final UUID guiltyResultDefId = UUID.fromString("ce23a452-9015-4619-968f-1628d7a271c9");


        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, asList(guiltyResultDefId, dismissedResultDefId));

        final ResultDefinition withDrawnResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(guiltyResultDefId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );
        final ResultDefinition dismissedResultDefinition = refDataHelper1.it().getResultDefinitions().stream()
                .filter(rd -> rd.getId().equals(dismissedResultDefId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid test data")
                );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt withDrawnResultDefinitionPrompt = withDrawnResultDefinition.getPrompts().get(0);

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt dismissedResultDefinitionPrompt = dismissedResultDefinition.getPrompts().get(0);

        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final List<Offence> offences = getOffences(initiateHearingCommand);
        final UUID offenceId = randomUUID();
        offences.add(Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, INDICATED_NOT_GUILTY).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingCommand));


        stubLjaDetails(hearingOne.getHearing().getCourtCentre().getId());


        final List<SaveDraftResultCommand> saveDraftResultCommandList = saveDraftResultCommandForMultipleOffences(hearingOne.it(), orderedDate, dismissedResultDefId);
        final List<Target> targets = new ArrayList<>();
        saveDraftResultCommandList.stream().forEach(
                saveDraftResultCommand -> {
                    targets.add(saveDraftResultCommand.getTarget());
                });
        final ResultLine resultLine1 = saveDraftResultCommandList.get(0).getTarget().getResultLines().get(0);
        resultLine1.setResultLineId(UUID.randomUUID());
        resultLine1.setResultDefinitionId(guiltyResultDefId);
        resultLine1.setOrderedDate(orderedDate);
        resultLine1.setPrompts(singletonList(Prompt.prompt()
                .withLabel(withDrawnResultDefinitionPrompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(withDrawnResultDefinitionPrompt.getId())
                .build()));

        final ResultLine resultLine2 = saveDraftResultCommandList.get(1).getTarget().getResultLines().get(0);
        resultLine2.setResultLineId(UUID.randomUUID());
        resultLine2.setResultDefinitionId(dismissedResultDefId);
        resultLine2.setOrderedDate(orderedDate);
        resultLine2.setPrompts(singletonList(Prompt.prompt()
                .withLabel(dismissedResultDefinitionPrompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(dismissedResultDefinitionPrompt.getId())
                .build()));


        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final EventListener publicEventForDefendentCaseWithDrawnOrDismissed = listenFor("public.hearing.defendant-case-withdrawn-or-dismissed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.defendantId", is(hearingOne.getFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );
        ;


        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();
        //When
        // Hearing result shared first with time with first offence as Dismissed and second offence result as guilty.
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);


        //then
        publicEventForDefendentCaseWithDrawnOrDismissed.expectNoneWithin(10000);
    }

    @Test
    public void shareResults_whenAllOffencesAreDismissedOrWithDrawnInSingleHearing_expectRaisePublicEventForDefendentCaseWithDrawnOrDismissed() {
        //Given
        //Hearing Initiated

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        setupNowsReferenceData(orderedDate);


        final UUID dismissedResultDefId = UUID.fromString("14d66587-8fbe-424f-a369-b1144f1684e3");

        final UUID withdrawnResultDefId = UUID.fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");

        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, asList(withdrawnResultDefId, dismissedResultDefId));

        final ResultDefinition withDrawnResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(withdrawnResultDefId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );
        final ResultDefinition dismissedResultDefinition = refDataHelper1.it().getResultDefinitions().stream()
                .filter(rd -> rd.getId().equals(dismissedResultDefId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid test data")
                );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt withDrawnResultDefinitionPrompt = withDrawnResultDefinition.getPrompts().get(0);

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt dismissedResultDefinitionPrompt = dismissedResultDefinition.getPrompts().get(0);

        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final List<Offence> offences = getOffences(initiateHearingCommand);
        final UUID offenceId = randomUUID();
        offences.add(Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, INDICATED_NOT_GUILTY).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingCommand));

        stubLjaDetails(hearingOne.getHearing().getCourtCentre().getId());

        final List<SaveDraftResultCommand> saveDraftResultCommandList = saveDraftResultCommandForMultipleOffences(hearingOne.it(), orderedDate, dismissedResultDefId);
        final List<Target> targets = new ArrayList<>();
        saveDraftResultCommandList.stream().forEach(
                saveDraftResultCommand -> {
                    targets.add(saveDraftResultCommand.getTarget());
                });
        final ResultLine resultLine1 = saveDraftResultCommandList.get(0).getTarget().getResultLines().get(0);
        resultLine1.setResultLineId(UUID.randomUUID());
        resultLine1.setResultDefinitionId(withdrawnResultDefId);
        resultLine1.setOrderedDate(orderedDate);
        resultLine1.setPrompts(singletonList(Prompt.prompt()
                .withLabel(withDrawnResultDefinitionPrompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(withDrawnResultDefinitionPrompt.getId())
                .build()));

        final ResultLine resultLine2 = saveDraftResultCommandList.get(1).getTarget().getResultLines().get(0);
        resultLine2.setResultLineId(UUID.randomUUID());
        resultLine2.setResultDefinitionId(dismissedResultDefId);
        resultLine2.setOrderedDate(orderedDate);
        resultLine2.setPrompts(singletonList(Prompt.prompt()
                .withLabel(dismissedResultDefinitionPrompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(dismissedResultDefinitionPrompt.getId())
                .build()));


        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final EventListener publicEventForDefendentCaseWithDrawnOrDismissed = listenFor("public.hearing.defendant-case-withdrawn-or-dismissed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.defendantId", is(hearingOne.getFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );
        ;


        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();

        //When
        // Hearing result shared first with time with first offence as Dismissed and second offence result as adjourned.
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);

        //Then
        publicEventForDefendentCaseWithDrawnOrDismissed.waitFor();

    }


    @Test
    public void shareResults_whenAllOffencesAreDismissedInMultipleHearing_expectRaisePublicEventForDefendentCaseWithDrawnOrDismissed() {

        //Given
        // Hearing Initiated
        stubFixedListForWelshValues();
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final UUID withDrawnResultDefId = UUID.fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, asList(allNows.getFirstPrimaryResultDefinitionId(), withDrawnResultDefId));

        final ResultDefinition now1MandatoryResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(allNows.getFirstPrimaryResultDefinitionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );
        final ResultDefinition withDrawnResultDefinition = refDataHelper1.it().getResultDefinitions().stream()
                .filter(rd -> rd.getId().equals(withDrawnResultDefId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid test data")
                );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt = now1MandatoryResultDefinition.getPrompts().get(0);

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt withDrawnResultDefinitionPrompt = withDrawnResultDefinition.getPrompts().get(0);

        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final List<Offence> offences = getOffences(initiateHearingCommand);
        final UUID offenceId = randomUUID();
        offences.add(Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, INDICATED_NOT_GUILTY).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingCommand));

        CourtCentre courtCentre = hearingOne.getHearing().getCourtCentre();
        stubLjaDetails(courtCentre.getId());
        stubGetReferenceDataCourtRooms(courtCentre, hearingOne.getHearing().getHearingLanguage());

        final EventListener publicEventForDefendentCaseWithDrawnOrDismissed = listenFor("public.hearing.defendant-case-withdrawn-or-dismissed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.defendantId", is(hearingOne.getFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );
        ;

        final List<SaveDraftResultCommand> saveDraftResultCommandList = saveDraftResultCommandForMultipleOffences(hearingOne.it(), orderedDate, withDrawnResultDefId);
        final List<Target> targets = new ArrayList<>();
        saveDraftResultCommandList.stream().forEach(
                saveDraftResultCommand -> {
                    targets.add(saveDraftResultCommand.getTarget());
                });
        final ResultLine resultLine1 = saveDraftResultCommandList.get(0).getTarget().getResultLines().get(0);
        resultLine1.setResultLineId(UUID.randomUUID());
        resultLine1.setResultDefinitionId(allNows.getFirstPrimaryResultDefinitionId());
        resultLine1.setOrderedDate(orderedDate);
        resultLine1.setPrompts(singletonList(Prompt.prompt()
                .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(now1MandatoryResultDefinitionPrompt.getId())
                .build()));

        final ResultLine resultLine2 = saveDraftResultCommandList.get(1).getTarget().getResultLines().get(0);
        resultLine2.setResultLineId(UUID.randomUUID());
        resultLine2.setResultDefinitionId(withDrawnResultDefId);
        resultLine2.setOrderedDate(orderedDate);
        resultLine2.setPrompts(singletonList(Prompt.prompt()
                .withLabel(withDrawnResultDefinitionPrompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(withDrawnResultDefinitionPrompt.getId())
                .build()));


        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);


        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();
        //when
        // Hearing result shared first with time with first offence as Dismissed and second offence result as withdrawn.
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);

        //then

        publicEventForDefendentCaseWithDrawnOrDismissed.expectNoneWithin(10000);


        //Test data creation for  another hearing result shared


        //given
        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        setupNowsReferenceData(orderedDate2, allNows.it());

        final AllResultDefinitionsReferenceDataHelper refDataHelper2 = setupResultDefinitionsReferenceData(orderedDate2, asList(withDrawnResultDefId));


        final ResultDefinition withDrawnResultDefinitionForSecodnHearing = refDataHelper2.it().getResultDefinitions().stream()
                .filter(rd -> rd.getId().equals(withDrawnResultDefId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid test data")
                );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt withDrawnResultDefinitionPrompt2 = withDrawnResultDefinitionForSecodnHearing.getPrompts().get(0);


        final SaveDraftResultCommand saveDraftResultCommand2 = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate2);

        final DelegatedPowers courtClerk2 = DelegatedPowers.delegatedPowers()
                .withFirstName("Siouxsie").withLastName("Sioux")
                .withUserId(randomUUID()).build();

        saveDraftResultCommand2.getTarget().setResultLines(asList(
                standardResultLineTemplate(UUID.randomUUID(), withDrawnResultDefId, orderedDate2).withPrompts(
                        singletonList(Prompt.prompt().withId(withDrawnResultDefinitionPrompt2.getId()).withValue("val0").withWelshValue("wval0")
                                .withFixedListCode("fixedList0").withLabel(withDrawnResultDefinitionPrompt2.getLabel()).build())
                ).build()));
        targets.clear();
        targets.add(saveDraftResultCommand2.getTarget());

        final ResultLine resultLineForSecondHearing = targets.get(0).getResultLines().get(0);

        resultLineForSecondHearing.setPrompts(singletonList(Prompt.prompt()
                .withLabel(withDrawnResultDefinitionPrompt2.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(withDrawnResultDefinitionPrompt2.getId())
                .build()));

        //when
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk2)
        ), targets);


        //then
        publicEventForDefendentCaseWithDrawnOrDismissed.waitFor();
    }

    private List<Offence> getOffences(InitiateHearingCommand initiateHearingCommand) {
        return initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
    }


    private void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows(final boolean checkIfResultDeleted) {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstPrimaryResultDefinitionId());

        final ResultDefinition now1MandatoryResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(allNows.getFirstPrimaryResultDefinitionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt = now1MandatoryResultDefinition.getPrompts().get(0);

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        CourtCentre courtCentre = hearingOne.getHearing().getCourtCentre();
        stubLjaDetails(courtCentre.getId());
        stubGetReferenceDataCourtRooms(courtCentre, hearingOne.getHearing().getHearingLanguage());

        ProsecutionCounselIT.createFirstProsecutionCounsel(hearingOne);

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final CommandHelpers.UpdatePleaCommandHelper pleaOne = new CommandHelpers.UpdatePleaCommandHelper(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        updatePleaTemplate(hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstCase().getId(), INDICATED_GUILTY, PleaValue.GUILTY,
                                false))
        );

        convictionDateListener.waitFor();

        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = buildCrackedIneffectiveVacatedTrialTypes(TRAIL_TYPE_ID_1).getCrackedIneffectiveVacatedTrialTypes().get(0);

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        ReferenceDataStub.stubCrackedIOnEffectiveTrialTypes(buildCrackedIneffectiveVacatedTrialTypes(TRAIL_TYPE_ID_1));
        TrialType addTrialType = TrialType.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTrialTypeId(TRAIL_TYPE_ID_1)
                .build();

        UseCases.setTrialType(requestSpec, hearingOne.getHearingId(), addTrialType);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCrackedIneffectiveTrial, Matchers.is(expectedTrialType))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue())))
                                                .with(Offence::getIndicatedPlea, isBean(IndicatedPlea.class)
                                                        .with(IndicatedPlea::getIndicatedPleaValue, is(pleaOne.getFirstIndicatedPleaValue()))
                                                        .with(IndicatedPlea::getIndicatedPleaDate, is(pleaOne.getFirstIndicatedPleaDate())))
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getOffenceId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())))
                                                .with(Offence::getConvictionDate, is(pleaOne.getFirstPleaDate()))))))))));

        SaveDraftResultCommand saveDraftResultCommand = null;

        if (checkIfResultDeleted) {
            saveDraftResultCommand = saveDraftResultCommandTemplateForDeletedResult(hearingOne.it(), orderedDate);
        } else {
            saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate);
        }
        final List<Target> targets = new ArrayList<>();

        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(
                singletonList(Prompt.prompt()
                        .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                        .withFixedListCode("fixedListCode")
                        .withValue("value1")
                        .withWelshValue("wvalue1")
                        .withId(now1MandatoryResultDefinitionPrompt.getId())
                        .build()));

        targets.add(saveDraftResultCommand.getTarget());

        final ResultLine resultLine1 = saveDraftResultCommand.getTarget().getResultLines().get(0);
        resultLine1.setResultLineId(UUID.randomUUID());
        resultLine1.setResultDefinitionId(allNows.getFirstPrimaryResultDefinitionId());
        resultLine1.setOrderedDate(orderedDate);

        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();

        testSaveDraftResult(saveDraftResultCommand, hearing, asList());

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        final JudicialRole judicialRole = hearing.getJudiciary().get(0);
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);


        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        //setup reference data for second ordered date
        setupNowsReferenceData(orderedDate2, allNows.it());

        final NowResultDefinitionRequirement firstNowNonMandatoryResultDefinition = allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> !rd.getMandatory())
                .findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
        final UUID secondNowPrimaryResultDefinitionId = allNows.it().getNows().get(1).getResultDefinitions().stream()
                .filter(rd -> rd.getMandatory())
                .map(NowResultDefinitionRequirement::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));

        final UUID firstNowNonMandatoryResultDefinitionId = firstNowNonMandatoryResultDefinition.getId();

        //need to get out prompt label here or put in to create draft label
        final AllResultDefinitionsReferenceDataHelper resultDefHelper = setupResultDefinitionsReferenceData(orderedDate2, asList(firstNowNonMandatoryResultDefinitionId, secondNowPrimaryResultDefinitionId));

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt firstNowNonMandatoryPrompt = resultDefHelper.it().getResultDefinitions().stream()
                .filter(rd -> firstNowNonMandatoryResultDefinitionId.equals(rd.getId())).findFirst().orElse(null)
                .getPrompts().get(0);

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt secondNowPrimaryPrompt = resultDefHelper.it().getResultDefinitions().stream()
                .filter(rd -> secondNowPrimaryResultDefinitionId.equals(rd.getId())).findFirst().orElse(null)
                .getPrompts().get(0);

        final SaveDraftResultCommand saveDraftResultCommand2 = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate2);

        saveDraftResultCommand2.getTarget().setResultLines(asList(
                standardResultLineTemplate(UUID.randomUUID(), firstNowNonMandatoryResultDefinitionId, orderedDate2).withPrompts(
                        singletonList(Prompt.prompt().withId(firstNowNonMandatoryPrompt.getId()).withValue("val0").withWelshValue("wval0")
                                .withFixedListCode("fixedList0").withLabel(firstNowNonMandatoryPrompt.getLabel()).build())
                ).build(),
                standardResultLineTemplate(UUID.randomUUID(), secondNowPrimaryResultDefinitionId, orderedDate2).withPrompts(
                        singletonList(Prompt.prompt().withId(secondNowPrimaryPrompt.getId()).withValue("val1").withWelshValue("wval1")
                                .withFixedListCode("fixedList1").withLabel(secondNowPrimaryPrompt.getLabel()).build())
                ).build()
        ));

        targets.add(saveDraftResultCommand2.getTarget());

        testSaveDraftResult(saveDraftResultCommand2, hearing, asList(saveDraftResultCommand.getTarget()));

        final DelegatedPowers courtClerk2 = DelegatedPowers.delegatedPowers()
                .withFirstName("Siouxsie").withLastName("Sioux")
                .withUserId(randomUUID()).build();

        final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk2)
        ), targets);

        publicEventResulted2.waitFor();

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    @Test
    public void shareResults_shouldSurfaceResultsLinesInGetHearings_resultLinesShouldBeAsLastSubmittedOnly() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate);

        saveDraftResultCommand.getTarget().setResultLines(
                asList(standardResultLineTemplate(UUID.randomUUID(), UUID.randomUUID(), orderedDate).build(),
                        standardResultLineTemplate(UUID.randomUUID(), UUID.randomUUID(), orderedDate).build())
        );

        final Hearing hearing = hearingOne.getHearing();

        testSaveDraftResult(saveDraftResultCommand, hearing, asList());

        saveDraftResultCommand.getTarget().setResultLines(
                singletonList(saveDraftResultCommand.getTarget().getResultLines().get(0))
        );

        saveDraftResultCommand.getTarget().setDraftResult("draft result version 2");

        testSaveDraftResult(saveDraftResultCommand, hearing, asList());
    }

    @Test
    public void testApplicationDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();
        final ApplicationDraftResultCommand applicationDraftResultCommand = applicationDraftResultCommandTemplate(hearingOne.getHearingId());

        testApplicationDraftResult(applicationDraftResultCommand, hearing);
        Queries.getApplicationDraftResultsPollForMatch(hearing.getId(), 30, isBean(ApplicationTargetListResponse.class)
                .with(response -> response.getHearingId(), is(hearing.getId())));
    }

    @Test
    public void testApplicationDraftResultWithApplicationOutcomeSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));
        final UUID applicationId = hearingOne.getHearing().getCourtApplications().get(0).getId();
        final CourtApplicationOutcomeType applicationOutcome = CourtApplicationOutcomeType.courtApplicationOutcomeType().withDescription("Granted")
                .withId(UUID.randomUUID())
                .withSequence(1).build();
        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();
        final ApplicationDraftResultCommand applicationDraftResultCommand = applicationDraftResultWithOutcomeCommandTemplate(hearingOne.getHearingId(), applicationId, applicationOutcome);

        testApplicationDraftResult(applicationDraftResultCommand, hearing);
        Queries.getApplicationDraftResultsPollForMatch(hearing.getId(), 30, isBean(ApplicationTargetListResponse.class)
                .with(response -> response.getHearingId(), is(hearing.getId())));

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, applicationId)
                                .with(CourtApplication::getApplicationOutcome, isBean(CourtApplicationOutcome.class)
                                        .with(CourtApplicationOutcome::getApplicationId, is(applicationId))
                                        .with(CourtApplicationOutcome::getApplicationOutcomeType, is(applicationOutcome))
                                )
                        ))
                )
        );
    }

    @Test
    public void shouldShareResultsInWelsh() {

        //Given
        //Hearing Initiated

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final UUID guiltyResultDefId = UUID.fromString("ce23a452-9015-4619-968f-1628d7a271c9");

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, asList(guiltyResultDefId));

        final ResultDefinition guiltyResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(guiltyResultDefId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt guiltyResultDefinitionPrompt = guiltyResultDefinition.getPrompts().get(0);

        InitiateHearingCommand initiateHearingCommand = welshInitiateHearingTemplate();

        final List<Offence> offences = getOffences(initiateHearingCommand);
        final UUID offenceId = randomUUID();
        offences.add(Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, INDICATED_GUILTY).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());

        final InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(requestSpec, initiateHearingCommand));

        final Hearing hearing = hearingCommandHelper.getHearing();

        stubLjaDetails(hearing.getCourtCentre().getId());

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate);
        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());
        final ResultLine resultLine = saveDraftResultCommand.getTarget().getResultLines().get(0);
        resultLine.setResultLineId(UUID.randomUUID());
        resultLine.setResultDefinitionId(guiltyResultDefId);
        resultLine.setOrderedDate(orderedDate);
        resultLine.setPrompts(singletonList(Prompt.prompt()
                .withLabel(guiltyResultDefinitionPrompt.getLabel())
                .withWelshLabel("welshLabel")
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(guiltyResultDefinitionPrompt.getId())
                .build()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingCommandHelper.getHearingId()))
                                .with(Hearing::getHearingLanguage, is(HearingLanguage.WELSH))
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getWelshDescription, is(hearing.getType().getWelshDescription())))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                        .with(CourtCentre::getWelshRoomName, is(hearing.getCourtCentre().getWelshRoomName()))
                                        .with(CourtCentre::getWelshName, is(hearing.getCourtCentre().getWelshName()))))));

        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();

        UseCases.shareResults(requestSpec, hearingCommandHelper.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);

        final JsonPath jsonPath = publicEventResulted.waitFor();

        final List<HashMap> defendantReferralReasons = jsonPath.getList("hearing.defendantReferralReasons", HashMap.class);
        assertThat(defendantReferralReasons.get(0).get("welshDescription").toString(), is(hearing.getDefendantReferralReasons().get(0).getWelshDescription()));
        final String actualOffenceTitleWelsh = jsonPath.getString("hearing.prosecutionCases[0].defendants[0].offences[0].offenceTitleWelsh");
        final String expectedOffenceTitleWelsh = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getOffenceTitleWelsh();
        assertThat(actualOffenceTitleWelsh, is(expectedOffenceTitleWelsh));
    }

    @Ignore("This test is failing on the  pipeline")
    @Test
    public void shareResults_shouldPublishResults_andAmendAndReShareResultsAgain() {
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstPrimaryResultDefinitionId());

        final ResultDefinition now1MandatoryResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(allNows.getFirstPrimaryResultDefinitionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt = now1MandatoryResultDefinition.getPrompts().get(0);

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        stubLjaDetails(hearingOne.getHearing().getCourtCentre().getId());

        ProsecutionCounselIT.createFirstProsecutionCounsel(hearingOne);

        final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                );

        final CommandHelpers.UpdatePleaCommandHelper pleaOne = new CommandHelpers.UpdatePleaCommandHelper(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        updatePleaTemplate(hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstCase().getId(), INDICATED_GUILTY, PleaValue.GUILTY,
                                false))
        );

        convictionDateListener.waitFor();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaDate, is(pleaOne.getFirstPleaDate()))
                                                        .with(Plea::getPleaValue, is(pleaOne.getFirstPleaValue())))
                                                .with(Offence::getConvictionDate, is(pleaOne.getFirstPleaDate()))))))))));

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate);

        final List<Target> targets = new ArrayList<>();

        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(
                singletonList(Prompt.prompt()
                        .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                        .withFixedListCode("fixedListCode")
                        .withValue("value1")
                        .withWelshValue("wvalue1")
                        .withId(now1MandatoryResultDefinitionPrompt.getId())
                        .build()));

        targets.add(saveDraftResultCommand.getTarget());

        final ResultLine resultLine1 = saveDraftResultCommand.getTarget().getResultLines().get(0);
        resultLine1.setResultLineId(UUID.randomUUID());
        resultLine1.setResultDefinitionId(allNows.getFirstPrimaryResultDefinitionId());
        resultLine1.setOrderedDate(orderedDate);

        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();

        testSaveDraftResult(saveDraftResultCommand, hearing, asList());

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);

        publicEventResulted.waitFor();


        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        //setup reference data for second ordered date
        setupNowsReferenceData(orderedDate2, allNows.it());

        final NowResultDefinitionRequirement firstNowNonMandatoryResultDefinition = allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> !rd.getMandatory())
                .findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
        final UUID secondNowPrimaryResultDefinitionId = allNows.it().getNows().get(1).getResultDefinitions().stream()
                .filter(rd -> rd.getMandatory())
                .map(NowResultDefinitionRequirement::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));

        final UUID firstNowNonMandatoryResultDefinitionId = firstNowNonMandatoryResultDefinition.getId();

        //need to get out prompt label here or put in to create draft label
        final AllResultDefinitionsReferenceDataHelper resultDefHelper = setupResultDefinitionsReferenceData(orderedDate2, asList(firstNowNonMandatoryResultDefinitionId, secondNowPrimaryResultDefinitionId));

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt firstNowNonMandatoryPrompt = resultDefHelper.it().getResultDefinitions().stream()
                .filter(rd -> firstNowNonMandatoryResultDefinitionId.equals(rd.getId())).findFirst().orElse(null)
                .getPrompts().get(0);

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt secondNowPrimaryPrompt = resultDefHelper.it().getResultDefinitions().stream()
                .filter(rd -> secondNowPrimaryResultDefinitionId.equals(rd.getId())).findFirst().orElse(null)
                .getPrompts().get(0);

        final SaveDraftResultCommand saveDraftResultCommand2 = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate2);

        saveDraftResultCommand2.getTarget().setResultLines(asList(
                standardAmendedResultLineTemplate(UUID.randomUUID(), firstNowNonMandatoryResultDefinitionId, orderedDate2)
                        .withPrompts(
                                singletonList(Prompt.prompt()
                                        .withId(firstNowNonMandatoryPrompt.getId())
                                        .withValue("val0")
                                        .withWelshValue("wval0")
                                        .withFixedListCode("fixedList0")
                                        .withLabel(firstNowNonMandatoryPrompt.getLabel())
                                        .build()))
                        .build(),
                standardAmendedResultLineTemplate(UUID.randomUUID(), secondNowPrimaryResultDefinitionId, orderedDate2)
                        .withPrompts(
                                singletonList(Prompt.prompt()
                                        .withId(secondNowPrimaryPrompt.getId())
                                        .withValue("val1")
                                        .withWelshValue("wval1")
                                        .withFixedListCode("fixedList1")
                                        .withLabel(secondNowPrimaryPrompt.getLabel())
                                        .build()))
                        .build()
        ));

        targets.add(saveDraftResultCommand2.getTarget());

        testSaveDraftResult(saveDraftResultCommand2, hearing, asList(saveDraftResultCommand.getTarget()));

        final DelegatedPowers courtClerk2 = DelegatedPowers.delegatedPowers()
                .withFirstName("Siouxsie").withLastName("Sioux")
                .withUserId(randomUUID()).build();

        final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk2)
        ), targets);

        publicEventResulted2.waitFor();

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    private void testSaveDraftResult(final SaveDraftResultCommand saveDraftResultCommand, final uk.gov.justice.core.courts.Hearing hearing, final List<Target> previousTargets) {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final Target target = saveDraftResultCommand.getTarget();
        final List<ResultLine> resultLines = target.getResultLines();
        // currently not sending result lines in draft
        target.setResultLines(null);
        final BeanMatcher beanMatcher = isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getTargetId, is(target.getTargetId()))
                .with(PublicHearingDraftResultSaved::getHearingId, is(target.getHearingId()))
                .with(PublicHearingDraftResultSaved::getDefendantId, is(target.getDefendantId()))
                .with(PublicHearingDraftResultSaved::getDraftResult, is(target.getDraftResult()))
                .with(PublicHearingDraftResultSaved::getOffenceId, is(target.getOffenceId()));

        final String expectedMetaDataContextUser = USER_ID_VALUE.toString();
        final String expectedMetaDataName = "public.hearing.draft-result-saved";
        final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(beanMatcher, expectedMetaDataName, expectedMetaDataContextUser);

        makeCommand(requestSpec, "hearing.save-draft-result")
                .ofType("application/vnd.hearing.save-draft-result+json")
                .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                .withPayload(saveDraftResultCommand.getTarget())
                .executeSuccessfully();

        publicEventResulted.waitFor();
        target.setResultLines(resultLines);

        final List<Target> targets = new ArrayList<>();
        targets.addAll(previousTargets);
        targets.add(saveDraftResultCommand.getTarget());

    }


    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate) {
        AllNows allnows = AllNows.allNows()
                .setNows(Arrays.asList(NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setWelshText("Welsh Text Primary")
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                // This causes a test failure but this field is under review .setText("ResultDefinitionLevel/" + STRING.next())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                                .setWelshText("Welsh Text Not Primary")
                                ))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setWelshText("NowLevel/" + STRING.next() + " Welsh")
                                .setWelshName("Welsh Name")
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false),
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setWelshText("Welsh Text Primary")
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                                .setWelshText("Welsh Text Not Primary")
                                ))
                                .setName(STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction(BOTH_JURISDICTIONS)
                                .setRemotePrintingRequired(false)
                                .setText(STRING.next())
                                .setWelshText("welshText")
                                .setWelshName("welshName")
                ));
        return setupNowsReferenceData(referenceDate, allnows);
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final AllNowsReferenceDataHelper allNows = h(data);
        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(final LocalDate referenceDate, final UUID resultDefinitionId) {
        return setupResultDefinitionsReferenceData(referenceDate, singletonList(resultDefinitionId));
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, List<UUID> resultDefinitionIds) {
        final String LISTING_OFFICER_USERGROUP = "Listing Officer";

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(
                        resultDefinitionIds.stream().map(
                                resultDefinitionId ->
                                        ResultDefinition.resultDefinition()
                                                .setId(resultDefinitionId)
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                .setFinancial("Y")
                                                .setCategory(getCategoryForResultDefinition(resultDefinitionId))
                                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setWelshLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                                .setReference(STRING.next())
                                                        )
                                                )
                                                .setLabel(STRING.next())
                                                .setWelshLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                        ).collect(Collectors.toList())
                ));

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }

    private String getCategoryForResultDefinition(final UUID resultDefId) {

        if (dismissedResultList.contains(resultDefId.toString()) || withDrawnResultList.contains(resultDefId.toString()) || guiltyResultList.contains(resultDefId.toString())) {
            return "F";
        }

        return "A";
    }


    private void testApplicationDraftResult(final ApplicationDraftResultCommand applicationDraftResultCommand, final uk.gov.justice.core.courts.Hearing hearing) {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final EventListener publicEvent = listenFor("public.hearing.application-draft-resulted")
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(applicationDraftResultCommand.getHearingId().toString())),
                        withJsonPath("$.draftResult", is(applicationDraftResultCommand.getDraftResult())))));
        makeCommand(requestSpec, "hearing.application-draft-result")
                .ofType("application/vnd.hearing.application-draft-result+json")
                .withPayload(applicationDraftResultCommand)
                .executeSuccessfully();

        publicEvent.waitFor();
    }

    private CrackedIneffectiveVacatedTrialTypes buildCrackedIneffectiveVacatedTrialTypes(final UUID trialTypeId) {
        List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(trialTypeId, "code", "InEffective", "fullDescription"));

        return new CrackedIneffectiveVacatedTrialTypes().setCrackedIneffectiveVacatedTrialTypes(trialList);
    }
}