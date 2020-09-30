package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Queries.getDraftResultsPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResults;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defendant;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.addOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithDefendantJudicialResultsForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithOffenceDateCode;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.welshInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.applicationDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.applicationDraftResultWithOutcomeCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandForMultipleOffences;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplateForDeletedResult;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardAmendedResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardInitiateHearingTemplateWithDefendantJudicialResults;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefendantAttendanceCommandTemplates.updateDefendantAttendanceTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VERDICT_TYPE_GUILTY_CODE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VERDICT_TYPE_GUILTY_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllNowsMetaData;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllResultDefinitions;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataResultDefinitionsWithDefaultValues;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.ResultDefinitionUtil.getCategoryForResultDefinition;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.AttendanceType;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.CustodialEstablishment;
import uk.gov.justice.core.courts.DefenceOrganisation;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ApplicationDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.PublicHearingSaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
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
import uk.gov.moj.cpp.hearing.test.TestUtilities;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import com.jayway.restassured.path.json.JsonPath;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

@SuppressWarnings({"squid:S2699"})
public class ShareResultsIT extends AbstractIT {

    private static final UUID NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID = fromString("66cd749a-1d51-11e8-accf-0ed5f89f718b");
    private static final UUID ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID = fromString("10115268-8efc-49fe-b8e8-feee216a03da");
    private static final UUID RD_FINE = fromString("969f150c-cd05-46b0-9dd9-30891efcc766");
    private static final UUID DISMISSED_RESULT_DEF_ID = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
    private static final UUID GUILTY_RESULT_DEF_ID = fromString("ce23a452-9015-4619-968f-1628d7a271c9");
    private static final UUID WITHDRAWN_RESULT_DEF_ID = fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");
    private static final String GUILTY = "GUILTY";

    @Test
    public void testEmptyDraftResultWhenNoDraftResultSaved() {

        final InitiateHearingCommandHelper initiateHearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        getDraftResultsPollForMatch(initiateHearingCommandHelper.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .with(TargetListResponse::getTargets, is(empty())));
    }

    @Test
    public void shouldShareResultsForHearingWithMultipleCases() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final UUID withDrawnResultDefId = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        assertHearingWithMultipleCasesCreatedAndResultAreNotShared(hearing);

        stubCourtRoom(hearing);

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt
                now1MandatoryResultDefinitionPrompt = getMandatoryNowResultDefPrompt(orderedDate, withDrawnResultDefId, allNows);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingCommand.it(), orderedDate);
        setPromptForSaveDraftResultCommand(now1MandatoryResultDefinitionPrompt, saveDraftResultCommand);

        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());

        testSaveDraftResult(saveDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareResultWithCourtClerk(hearing, targets);

        assertHearingResultsAreShared(hearing);

    }

    private CommandHelpers.UpdateVerdictCommandHelper updateDefendantAndChangeVerdict(InitiateHearingCommandHelper initiateHearingCommandHelper) {
        updateDefendantAttendance(initiateHearingCommandHelper);

        stubCourtCentre(initiateHearingCommandHelper.getHearing());

        ProsecutionCounselIT.createFirstProsecutionCounsel(initiateHearingCommandHelper);

        return changeVerdict(initiateHearingCommandHelper, fromString(VERDICT_TYPE_GUILTY_ID), VERDICT_TYPE_GUILTY_CODE);
    }

    private List<Target> completeSetupAndShareResults(AllNowsReferenceDataHelper allNows, InitiateHearingCommandHelper initiateHearingCommandHelper, SaveDraftResultCommand saveDraftResultCommand, LocalDate orderDate) {
        setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0),
                findPrompt(setupResultDefinitionsReferenceData(orderDate, singletonList(allNows.getFirstPrimaryResultDefinitionId())),
                        allNows.getFirstPrimaryResultDefinitionId()), allNows.getFirstPrimaryResultDefinitionId(), orderDate);

        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());

        testSaveDraftResult(saveDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(getCourtClerk())
        ), singletonList(saveDraftResultCommand.getTarget()));

        orderDate = PAST_LOCAL_DATE.next();
        //setup reference data for second ordered date
        setupNowsReferenceData(orderDate, allNows.it());
        return targets;
    }

    private void assertPublicHearingResultedHasCorrectPayload(final EventListener publicEventResultedListener, final CrackedIneffectiveTrial expectedTrialType, final Hearing hearing) {
        final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.id"), is(VERDICT_TYPE_GUILTY_ID));
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.verdictCode"), is(VERDICT_TYPE_GUILTY_CODE));
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.category"), is("Guilty"));
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.categoryType"), is("GUILTY_BY_JURY_CONVICTED"));
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.sequence"), is("1"));
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.description"), is("Guilty"));
        assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].verdict.verdictType.cjsVerdictCode"), is("G"));

        assertHearingHasSharedResults(expectedTrialType, hearing);
    }

    private void shareResultWithCourtClerk(final Hearing hearing, final List<Target> targets) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        shareResults(getRequestSpec(), hearing.getId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }

    @Test
    public void shouldShareResultsWithFullVerdictTypeInformation() {

        LocalDate orderDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);

        final InitiateHearingCommandHelper initiateHearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdictCommandHelper = updateDefendantAndChangeVerdict(initiateHearingCommandHelper);

        SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate);

        final uk.gov.justice.core.courts.Hearing hearing = initiateHearingCommandHelper.getHearing();

        stubCourtRoom(hearing);

        final LocalDate convictionDateBasedOnVerdict = updateVerdictCommandHelper.getFirstVerdict().getVerdictDate();

        final CrackedIneffectiveTrial expectedTrialType = getExpectedTrialType(initiateHearingCommandHelper, updatePleaWithoutChangingConvictionDate(initiateHearingCommandHelper), convictionDateBasedOnVerdict);

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {

            completeSetupAndShareResults(allNows, initiateHearingCommandHelper, saveDraftResultCommand, orderDate);
            assertPublicHearingResultedHasCorrectPayload(publicEventResulted, expectedTrialType, hearing);
        }
    }

    private SaveDraftResultCommand setPromptForSaveDraftResultCommand(final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt, final SaveDraftResultCommand saveDraftResultCommand) {
        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(
                singletonList(Prompt.prompt()
                        .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                        .withFixedListCode("fixedListCode")
                        .withValue("value1")
                        .withWelshValue("wvalue1")
                        .withId(now1MandatoryResultDefinitionPrompt.getId())
                        .build()));
        return saveDraftResultCommand;
    }

    private uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt getMandatoryNowResultDefPrompt(final LocalDate orderedDate, final UUID withDrawnResultDefId, final AllNowsReferenceDataHelper allNows) {
        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, asList(allNows.getFirstPrimaryResultDefinitionId(), withDrawnResultDefId));

        final ResultDefinition now1MandatoryResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(allNows.getFirstPrimaryResultDefinitionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );

        return now1MandatoryResultDefinition.getPrompts().get(0);
    }

    private void stubCourtRoom(final Hearing hearing) {
        CourtCentre courtCentre = hearing.getCourtCentre();
        stubLjaDetails(courtCentre, hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId());
        stubGetReferenceDataCourtRooms(courtCentre, hearing.getHearingLanguage(), ouId3, ouId4);
    }

    private InitiateHearingCommandHelper getHearingCommand(final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure) {
        return h(UseCases.initiateHearing(getRequestSpec(),
                InitiateHearingCommand.initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));
    }

    private HashMap<UUID, Map<UUID, List<UUID>>> getUuidMapForMultipleCaseStructure() {
        HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), TestUtilities.asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), TestUtilities.asList(randomUUID()));
        caseStructure.put(randomUUID(), value);
        caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(randomUUID(), randomUUID())));
        caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(randomUUID())));
        return caseStructure;
    }

    @Test
    public void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows() {

        LocalDate orderDate = PAST_LOCAL_DATE.next();

        shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows(false, orderDate);
    }

    @Test
    public void shareResults_whenOneOffenceIsDismissedAndOtherIsGuiltyInSingleHearing_expectNoPublicEventForDefendantCaseWithdrawnOrDismissed() {

        LocalDate orderDate = PAST_LOCAL_DATE.next();

        //Given
        //Hearing Initiated
        final Pair<InitiateHearingCommandHelper, List<Target>> returnValues = givenHearingInitiatedWithDismissedResultDef(asList(GUILTY_RESULT_DEF_ID, DISMISSED_RESULT_DEF_ID), orderDate);
        final InitiateHearingCommandHelper initiateHearingCommandHelper = returnValues.getLeft();
        final List<Target> targets = returnValues.getRight();
        try (final EventListener publicEventForDefendantCaseWithdrawnOrDismissed = getPublicEventForDefendantCaseWithdrawnOrDismissed(initiateHearingCommandHelper)) {
            //When
            // Hearing result shared first with time with first offence as Dismissed and second offence result as guilty.
            UseCases.shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            //then
            publicEventForDefendantCaseWithdrawnOrDismissed.expectNoneWithin(DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS);
        }
    }

    @Test
    public void shareResults_whenAllOffencesAreDismissedOrWithdrawnInSingleHearing_expectRaisePublicEventForDefendantCaseWithDrawnOrDismissed() {
        //Given
        //Hearing Initiated
        LocalDate orderDate = PAST_LOCAL_DATE.next();
        setupNowsReferenceData(orderDate);
        final Pair<InitiateHearingCommandHelper, List<Target>> returnValues = givenHearingInitiatedWithDismissedResultDef(asList(WITHDRAWN_RESULT_DEF_ID, DISMISSED_RESULT_DEF_ID), orderDate);
        final InitiateHearingCommandHelper initiateHearingCommandHelper = returnValues.getLeft();
        final List<Target> targets = returnValues.getRight();
        try (final EventListener publicEventForDefendantCaseWithdrawnOrDismissed = getPublicEventForDefendantCaseWithdrawnOrDismissed(initiateHearingCommandHelper)) {
            //When
            // Hearing result shared first with time with first offence as Dismissed and second offence result as adjourned.
            UseCases.shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            //Then
            publicEventForDefendantCaseWithdrawnOrDismissed.waitFor();
        }
    }

    @Test
    public void shareResults_whenAllOffencesAreDismissedInMultipleHearing_expectRaisePublicEventForDefendantCaseWithdrawnOrDismissed() {

        //Given
        // Hearing Initiated
        LocalDate orderDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);
        final Pair<InitiateHearingCommandHelper, List<Target>> returnValues = givenHearingInitiatedWithDismissedResultDef(asList(allNows.getFirstPrimaryResultDefinitionId(), DISMISSED_RESULT_DEF_ID), orderDate);
        final InitiateHearingCommandHelper initiateHearingCommandHelper = returnValues.getLeft();
        final List<Target> targets = returnValues.getRight();
        try (final EventListener publicEventForDefendantCaseWithdrawnOrDismissed = getPublicEventForDefendantCaseWithdrawnOrDismissed(initiateHearingCommandHelper)) {

            //when
            // Hearing result shared first with time with first offence as Dismissed and second offence result as withdrawn.
            UseCases.shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            //then

            publicEventForDefendantCaseWithdrawnOrDismissed.expectNoneWithin(DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS);

            //Test data creation for another hearing result shared
            //given
            targets.clear();
            orderDate = PAST_LOCAL_DATE.next();

            setupNowsReferenceData(orderDate, allNows.it());

            final AllResultDefinitionsReferenceDataHelper refDataHelper = setupResultDefinitionsReferenceData(orderDate, singletonList(DISMISSED_RESULT_DEF_ID));

            final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate);
            setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0), findPrompt(refDataHelper, DISMISSED_RESULT_DEF_ID), DISMISSED_RESULT_DEF_ID, orderDate);
            targets.add(saveDraftResultCommand.getTarget());

            //when
            UseCases.shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            //then
            publicEventForDefendantCaseWithdrawnOrDismissed.waitFor();
        }
    }


    /**
     * DDCH -Defendant details changed, if there are any changes to the defendant in name (Include
     * first name, last name, middle name) or Organisation name, address, date of birth ,
     * nationality from last hearing then when sharing results application should send "DDCH" result
     * in SPI OUT
     */
    @Test
    public void shareResultShouldNotPublishDDCHResultsWhenInitiateHearingHaveDDCHJudicialResult() {

        //Given and When
        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplateWithDefendantJudicialResults();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //Then
        shareResultsShouldNotHaveDDCH(targets, hearingOne);

    }


    @Test
    public void shareResultShouldPublishDDCHResultsWhenDefendantDetailsChanged() throws Exception {

        //Given

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //When
        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        //Then
        shareAndVerifyDDCH(targets, hearingOne);

    }

    @Test
    public void saveDraftResultFailedWhenUnknownTargetIdUsedForKnownOffenceDefendantCombination() throws Exception {

        //Given

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), PAST_LOCAL_DATE.next());

        // use an unknown target ID for offence / defendant combination having previous draft result
        saveDraftResultCommand.getTarget().setTargetId(randomUUID());

        saveDraftResultWithInvalidTargetIdAndCheckForFailedEvent(saveDraftResultCommand);

    }

    @Test
    public void shareResultShouldNotPublishDDCHResultsAfterDDCHShareAndAmendOtherResultAndReShare() throws Exception {

        //Given

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //And
        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        //And
        shareAndVerifyDDCH(targets, hearingOne);

        //When
        amendResultsToShare(hearingOne, targets, initiateHearing.getHearing());

        shareResultsShouldNotHaveDDCH(targets, hearingOne);
    }

    @Test
    public void shareResultShouldPublishOffenceDateCode() throws Exception {

        //Given

        final Integer offenceDateCode = 4;
        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplateWithOffenceDateCode(offenceDateCode);

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //When
        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        //Then
        shareAndVerifyOffenceDateCode(targets, hearingOne, offenceDateCode);

    }

    @Test
    public void shareResultShouldPublishAddDefendantOffenceDateCode() throws Exception {

        //Given

        final Integer offenceDateCode = 4;
        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplateWithOffenceDateCode(offenceDateCode);

        HearingDay hearingDay = initiateHearing.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final Integer newOffenceDateCode = 3;

        //When

        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        addDefendant(hearingOne, newOffenceDateCode);

        //Then
        shareAndVerifyOffenceDateCode(targets, hearingOne, offenceDateCode, newOffenceDateCode);

    }


    @Test
    public void shareResultShouldPublishOffenceDateCodeAfterNewOffenceAdded() throws Exception {

        //Given

        final Integer offenceDateCode = 4;
        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplateWithOffenceDateCode(offenceDateCode);

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //When
        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        final Integer newOffenceDateCode = 3;

        addOffence(hearingOne, newOffenceDateCode);

        //Then
        shareAndVerifyMultiOffenceOffenceDateCode(targets, hearingOne, offenceDateCode, newOffenceDateCode);

    }

    @Test
    public void shareResultShouldPublishOffenceDateCodeAfterOffenceUpdated() throws Exception {

        //Given

        final Integer offenceDateCode = 4;
        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplateWithOffenceDateCode(offenceDateCode);

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //When
        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        final Integer newOffenceDateCode = 3;

        updateOffence(hearingOne, newOffenceDateCode);

        //Then
        shareAndVerifyMultiOffenceOffenceDateCode(targets, hearingOne, newOffenceDateCode);

    }

    private void shareResultsShouldNotHaveDDCH(final List<Target> targets, final InitiateHearingCommandHelper hearingOne) {
        final DelegatedPowers courtClerk2 = DelegatedPowers.delegatedPowers()
                .withFirstName("Siouxsie").withLastName("Sioux")
                .withUserId(randomUUID()).build();

        try (final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            //Then
            shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk2)
            ), targets);

            JsonPath reShare = publicEventResulted2.waitFor();
            assertThat(reShare.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults"), nullValue());
        }

    }


    @Test
    public void reShareDefendantDetailsChangedShouldNotPublishDDCHResults() throws Exception {

        //Given
        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //And
        updateDefendantDetails(initiateHearing, hearingOne, "Test", "Test");

        //And
        shareAndVerifyDDCH(targets, hearingOne);

        //When
        updateDefendantDetails(initiateHearing, hearingOne, "Test2", "Test");

        //Then
        shareResultsShouldNotHaveDDCH(targets, hearingOne);
    }

    @Test
    public void shareResultShouldPublishDefenceAssociationWhenDefenceAssociationAdded() throws Exception {

        //Given

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //When
        updateDefendantDetailsWithDefenceAssociation(initiateHearing, hearingOne, "Test", TRUE, "Test Ltd");

        //Then
        shareAndVerifyDefenceAssociation(targets, hearingOne);

    }


    @Test
    public void shareResultShouldNotPublishDefenceAssociationWhenDefenceAssociationAddedLaterCleared() throws Exception {

        //Given

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();

        final InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        //When
        updateDefendantDetailsWithDefenceAssociation(initiateHearing, hearingOne, "Test", TRUE, "Test Ltd");

        updateDefendantDetailsWithNoDefenceAssociation(initiateHearing, hearingOne);

        //Then
        shareAndVerifyNoDefenceAssociation(targets, hearingOne);

    }

    private void shareAndVerifyOffenceDateCode(final List<Target> targets, final InitiateHearingCommandHelper hearingOne, final Integer... offenceDateCodes) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk1)
            ), targets);

            JsonPath result = publicEventResulted.waitFor();
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].cjsCode"), is("4592"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].resultText"), is("Defendant's details changed"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].judicialResultTypeId"), is("8c67b30a-418c-11e8-842f-0ed5f89f718b"));
            assertThat(result.getInt("hearing.prosecutionCases[0].defendants[0].offences[0].offenceDateCode"), is(offenceDateCodes[0]));
            if (offenceDateCodes.length > 1) {
                assertThat(result.getInt("hearing.prosecutionCases[0].defendants[1].offences[0].offenceDateCode"), is(offenceDateCodes[1]));
            }
        }
    }

    private void shareAndVerifyMultiOffenceOffenceDateCode(final List<Target> targets, final InitiateHearingCommandHelper hearingOne, final Integer... offenceDateCodes) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk1)
            ), targets);

            JsonPath result = publicEventResulted.waitFor();
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].cjsCode"), is("4592"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].resultText"), is("Defendant's details changed"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].judicialResultTypeId"), is("8c67b30a-418c-11e8-842f-0ed5f89f718b"));
            assertThat(result.getInt("hearing.prosecutionCases[0].defendants[0].offences[0].offenceDateCode"), is(offenceDateCodes[0]));
            if (offenceDateCodes.length > 1) {
                assertThat(result.getInt("hearing.prosecutionCases[0].defendants[0].offences[1].offenceDateCode"), is(offenceDateCodes[1]));
            }
        }
    }

    private void addDefendant(final CommandHelpers.InitiateHearingCommandHelper hearingOne, final Integer offenceDateCode) throws Exception {
        UUID newDefendantId = randomUUID();
        CoreTestTemplates.CoreTemplateArguments args = defaultArguments();
        args.setStructure(toMap(newDefendantId, toMap(randomUUID(), TestUtilities.asList(randomUUID()))));
        args.setCourtProceedingsInitiated(ZonedDateTime.now(ZoneOffset.UTC));
        args.setOffenceDateCode(offenceDateCode);
        Defendant addNewDefendant = defendant(hearingOne.getFirstCase().getId(), args,
                new uk.gov.moj.cpp.hearing.test.Pair<>(newDefendantId, TestUtilities.asList(randomUUID())))
                .withAssociatedPersons(TestUtilities.asList(associatedPerson(defaultArguments()).build()))
                .withProsecutionCaseId(hearingOne.getFirstDefendantForFirstCase().getProsecutionCaseId())
                .build();

        UseCases.addDefendant(addNewDefendant);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                        .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)))
                                        .with(p -> p.getDefendants().size(), is(2))
                                )
                        )));

    }


    public void addOffence(final CommandHelpers.InitiateHearingCommandHelper hearingOne, final Integer offenceDateCode) throws Exception {

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceAdded = h(UseCases.updateOffences(
                addOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffencesToAdd(singletonList(randomUUID()))
                                .setOffenceDateCode(offenceDateCode)
                )
        ));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItems(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                        ))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceAdded.getFirstOffenceFromAddedOffences().getId()))
                                                .with(Offence::getOffenceCode, is(offenceAdded.getFirstOffenceFromAddedOffences().getOffenceCode()))
                                                .with(Offence::getWording, is(offenceAdded.getFirstOffenceFromAddedOffences().getWording()))
                                                .with(Offence::getStartDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getStartDate()))
                                                .with(Offence::getEndDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getEndDate()))
                                                .with(Offence::getCount, is(offenceAdded.getFirstOffenceFromAddedOffences().getCount()))
                                                .with(Offence::getConvictionDate, is(offenceAdded.getFirstOffenceFromAddedOffences().getConvictionDate()))
                                        ))
                                ))
                        ))
                )
        );
    }

    private void updateOffence(final CommandHelpers.InitiateHearingCommandHelper hearingOne, final Integer offenceDateCode) throws Exception {

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper offenceUpdates = h(UseCases.updateOffences(
                updateOffencesForDefendantTemplate(
                        updateOffencesForDefendantArguments(
                                hearingOne.getFirstCase().getId(),
                                hearingOne.getFirstDefendantForFirstCase().getId()
                        )
                                .setOffencesToUpdate(singletonList(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                .setOffenceDateCode(offenceDateCode)
                )
        ));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getId()))
                                                .with(Offence::getOffenceCode, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getOffenceCode()))
                                                .with(Offence::getWording, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getWording()))
                                                .with(Offence::getStartDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getStartDate()))
                                                .with(Offence::getEndDate, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getEndDate()))
                                                .with(Offence::getCount, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getCount()))
                                                .with(Offence::getLaaApplnReference, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getLaaApplnReference()))
                                                .with(Offence::getIsDiscontinued, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIsDiscontinued()))
                                                .with(Offence::getIntroducedAfterInitialProceedings, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getIntroducedAfterInitialProceedings()))
                                                .with(Offence::getProceedingsConcluded, is(offenceUpdates.getFirstOffenceFromUpdatedOffences().getProceedingsConcluded()))

                                        ))
                                ))
                        ))
                )
        );
    }

    private void shareAndVerifyDDCH(final List<Target> targets, final InitiateHearingCommandHelper hearingOne) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk1)
            ), targets);

            JsonPath result = publicEventResulted.waitFor();
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].cjsCode"), is("4592"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].resultText"), is("Defendant's details changed"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].defendantCaseJudicialResults[0].judicialResultTypeId"), is("8c67b30a-418c-11e8-842f-0ed5f89f718b"));
        }
    }

    private void shareAndVerifyNoDefenceAssociation(final List<Target> targets, final InitiateHearingCommandHelper hearingOne) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk1)
            ), targets);

            JsonPath result = publicEventResulted.waitFor();
            assertThat(result.getJsonObject("hearing.prosecutionCases[0].defendants[0].associatedDefenceOrganisation"), nullValue());
        }
    }

    private void shareAndVerifyDefenceAssociation(final List<Target> targets, final InitiateHearingCommandHelper hearingOne) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk1)
            ), targets);

            JsonPath result = publicEventResulted.waitFor();
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].associatedDefenceOrganisation.applicationReference"), is("Test"));
            assertThat(result.getString("hearing.prosecutionCases[0].defendants[0].associatedDefenceOrganisation.defenceOrganisation.organisation.name"), is("Test Ltd"));
            assertThat(result.getBoolean("hearing.prosecutionCases[0].defendants[0].associatedDefenceOrganisation.isAssociatedByLAA"), is(true));
        }
    }

    private void saveDraftResultsWithShadowListedFlag(final InitiateHearingCommandHelper hearing, final UUID targetId) {
        final JsonObject saveDraftResultsCommand = createObjectBuilder()
                .add("draftResult", "draft results content")
                .add("hearingId", hearing.getHearing().toString())
                .add("offenceId", hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())
                .add("defendantId", hearing.getFirstDefendantForFirstCase().getId().toString())
                .add("targetId", targetId.toString())
                .add("shadowListed", true)
                .build();


        final BeanMatcher beanMatcher = isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getHearingId, is(hearing.getHearingId()));

        final String expectedMetaDataContextUser = getLoggedInUser().toString();
        final String expectedMetaDataName = "public.hearing.draft-result-saved";
        try (final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(beanMatcher, expectedMetaDataName, expectedMetaDataContextUser)) {
            makeCommand(getRequestSpec(), "hearing.save-draft-result")
                    .ofType("application/vnd.hearing.save-draft-result+json")
                    .withArgs(hearing.getHearingId().toString())
                    .withPayload(saveDraftResultsCommand.toString())
                    .executeSuccessfully();

            publicEventResulted.waitFor();
        }
    }

    private void shareResultsAndValidateShadowListedOffences(final InitiateHearingCommandHelper hearing, final UUID targetId) throws IOException {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        final String eventPayloadString = getStringFromResource("hearing.share-results.json")
                .replaceAll("TARGET_ID", targetId.toString())
                .replaceAll("DEFENDANT_ID", hearing.getFirstDefendantForFirstCase().getId().toString())
                .replaceAll("OFFENCE_ID", hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())
                .replaceAll("SHARED_DATE", LocalDate.now().format(dateFormatter));

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getHearingId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getHearingId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            JsonPath result = publicEventResulted.waitFor();
            assertThat(result.getString("shadowListedOffences[0]"), is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId().toString()));
        }
    }

    private InitiateHearingCommandHelper createInitiateHearingCommandHelper(final InitiateHearingCommand initiateHearing, final List<Target> targets) {
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, singletonList(allNows.getFirstPrimaryResultDefinitionId()));

        final ResultDefinition now1MandatoryResultDefinition =
                nowsResultDefinition(refDataHelper1, allNows.getFirstPrimaryResultDefinitionId());

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt = now1MandatoryResultDefinition.getPrompts().get(0);

        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0)
                .getPersonDefendant().setCustodialEstablishment(
                CustodialEstablishment.custodialEstablishment()
                        .withCustody("POLICE")
                        .withName("East Croydon Police Station")
                        .withId(randomUUID())
                        .build());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearing));


        updateDefendantAttendance(hearingOne);


        final CommandHelpers.UpdatePleaCommandHelper pleaOne = createProsecutionCounsel(hearingOne);

        setCrackedIneffectiveTrial(hearingOne, pleaOne);

        saveDraftResult(orderedDate, allNows, now1MandatoryResultDefinitionPrompt, hearingOne, targets, saveDraftResultCommandTemplate(hearingOne.it(), orderedDate));
        return hearingOne;
    }

    private InitiateHearingCommandHelper createInitiateHearingCommandHelperForNextHearing(final InitiateHearingCommand initiateHearing, final LocalDate orderDate) {
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);
        setupNowsReferenceData(LocalDate.now(), allNows.it());

        return h(UseCases.initiateHearing(getRequestSpec(), initiateHearing));
    }

    private ResultDefinition nowsResultDefinition(final AllResultDefinitionsReferenceDataHelper refDataHelper1, final UUID firstPrimaryResultDefinitionId) {
        return refDataHelper1.it().getResultDefinitions().stream()
                .filter(rd -> rd.getId().equals(firstPrimaryResultDefinitionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid test data")
                );
    }

    private void amendResultsToShare(final InitiateHearingCommandHelper hearingOne, final List<Target> targets, final Hearing hearing) {

        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate2);
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate2);

        //setup reference data for second ordered date
        setupNowsReferenceData(orderedDate2, allNows.it());

        final NowResultDefinitionRequirement firstNowNonMandatoryResultDefinition = allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> !rd.getMandatory())
                .findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
        final UUID secondNowPrimaryResultDefinitionId = allNows.it().getNows().get(1).getResultDefinitions().stream()
                .filter(NowResultDefinitionRequirement::getMandatory)
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
                standardResultLineTemplate(randomUUID(), firstNowNonMandatoryResultDefinitionId, orderedDate2).withPrompts(
                        singletonList(Prompt.prompt().withId(firstNowNonMandatoryPrompt.getId()).withValue("val0").withWelshValue("wval0")
                                .withFixedListCode("fixedList0").withLabel(firstNowNonMandatoryPrompt.getLabel()).build())
                ).build(),
                standardResultLineTemplate(randomUUID(), secondNowPrimaryResultDefinitionId, orderedDate2).withPrompts(
                        singletonList(Prompt.prompt().withId(secondNowPrimaryPrompt.getId()).withValue("val1").withWelshValue("wval1")
                                .withFixedListCode("fixedList1").withLabel(secondNowPrimaryPrompt.getLabel()).build())
                ).build()
        ));
        saveDraftResultCommand2.getTarget().setTargetId(targets.get(0).getTargetId());

        targets.add(saveDraftResultCommand2.getTarget());

        testSaveDraftResult(saveDraftResultCommand2);
    }

    private CrackedIneffectiveTrial setCrackedIneffectiveTrial(final InitiateHearingCommandHelper hearingOne, final CommandHelpers.UpdatePleaCommandHelper pleaOne) {
        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = INEFFECTIVE_TRIAL_TYPE;

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getDate(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        TrialType addTrialType = TrialType.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
        return expectedTrialType;
    }

    private CommandHelpers.UpdatePleaCommandHelper createProsecutionCounsel(final InitiateHearingCommandHelper hearingOne) {
        CourtCentre courtCentre = hearingOne.getHearing().getCourtCentre();
        stubLjaDetails(courtCentre, hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId());
        stubGetReferenceDataCourtRooms(courtCentre, hearingOne.getHearing().getHearingLanguage(), ouId3, ouId4);

        ProsecutionCounselIT.createFirstProsecutionCounsel(hearingOne);

        try (final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                )) {

            final CommandHelpers.UpdatePleaCommandHelper pleaOne = new CommandHelpers.UpdatePleaCommandHelper(
                    UseCases.updatePlea(getRequestSpec(), hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            updatePleaTemplate(hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                                    hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstCase().getId(), INDICATED_GUILTY, GUILTY, false))
            );

            convictionDateListener.waitFor();
            return pleaOne;
        }
    }

    private uk.gov.moj.cpp.hearing.command.defendant.Defendant convert(final Defendant currentDefendant, final String firstName) {

        uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant = new uk.gov.moj.cpp.hearing.command.defendant.Defendant();
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
        defendant.setMasterDefendantId(randomUUID());
        return defendant;
    }


    private uk.gov.moj.cpp.hearing.command.defendant.Defendant convertWithDefenceAssociate(final Defendant currentDefendant, final AssociatedDefenceOrganisation associatedDefenceOrganisation) {

        uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant = new uk.gov.moj.cpp.hearing.command.defendant.Defendant();
        defendant.setId(currentDefendant.getId());
        final PersonDefendant curPd = currentDefendant.getPersonDefendant();
        final Person cpd = curPd.getPersonDetails();
        Person person = new Person(cpd.getAdditionalNationalityCode(), cpd.getAdditionalNationalityDescription(), cpd.getAdditionalNationalityId(), cpd.getAddress(), cpd.getContact(), cpd.getDateOfBirth(),
                cpd.getDisabilityStatus(), cpd.getDocumentationLanguageNeeds(), cpd.getEthnicity(), cpd.getFirstName(), cpd.getGender(), cpd.getInterpreterLanguageNeeds(),
                cpd.getLastName(), cpd.getMiddleName(), cpd.getNationalInsuranceNumber(), cpd.getNationalityCode(), cpd.getNationalityDescription(), cpd.getNationalityId(),
                cpd.getOccupation(), cpd.getOccupationCode(), cpd.getPersonMarkers(), cpd.getSpecificRequirements(), cpd.getTitle());

        final PersonDefendant newPersonDefendant = new PersonDefendant(curPd.getArrestSummonsNumber(), curPd.getBailConditions(),
                curPd.getBailReasons(), curPd.getBailStatus(), curPd.getCustodialEstablishment(), curPd.getCustodyTimeLimit(), curPd.getDriverLicenceCode(), curPd.getDriverLicenseIssue(),
                curPd.getDriverNumber(), curPd.getEmployerOrganisation(), curPd.getEmployerPayrollReference(),
                curPd.getPerceivedBirthYear(), person, curPd.getVehicleOperatorLicenceNumber());


        defendant.setPersonDefendant(newPersonDefendant);
        defendant.setProsecutionCaseId(currentDefendant.getProsecutionCaseId());
        defendant.setMasterDefendantId(randomUUID());
        defendant.setAssociatedDefenceOrganisation(associatedDefenceOrganisation);
        return defendant;
    }

    private void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows(final boolean checkIfResultDeleted, LocalDate orderDate) {

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper = setupResultDefinitionsReferenceData(orderDate, singletonList(allNows.getFirstPrimaryResultDefinitionId()));

        final InitiateHearingCommandHelper initiateHearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        updateDefendantAttendance(initiateHearingCommandHelper);

        stubCourtCentre(initiateHearingCommandHelper.getHearing());

        ProsecutionCounselIT.createFirstProsecutionCounsel(initiateHearingCommandHelper);

        final CommandHelpers.UpdatePleaCommandHelper pleaOne = changeConvictionDate(initiateHearingCommandHelper);

        final CrackedIneffectiveTrial expectedTrialType = getExpectedTrialType(initiateHearingCommandHelper, pleaOne, pleaOne.getFirstPleaDate());

        SaveDraftResultCommand saveDraftResultCommand;

        if (checkIfResultDeleted) {
            saveDraftResultCommand = saveDraftResultCommandTemplateForDeletedResult(initiateHearingCommandHelper.it(), orderDate);
        } else {
            saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate);
        }

        setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0), findPrompt(refDataHelper, allNows.getFirstPrimaryResultDefinitionId()), allNows.getFirstPrimaryResultDefinitionId(), orderDate);

        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());

        testSaveDraftResult(saveDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(getCourtClerk())
        ), singletonList(saveDraftResultCommand.getTarget()));


        orderDate = PAST_LOCAL_DATE.next();
        //setup reference data for second ordered date
        setupNowsReferenceData(orderDate, allNows.it());

        final UUID firstNowNonMandatoryResultDefinitionId = getResultDefinitionId(allNows, false);

        final UUID secondNowPrimaryResultDefinitionId = getResultDefinitionId(allNows, true);

        //need to get out prompt label here or put in to create draft label
        final AllResultDefinitionsReferenceDataHelper resultDefHelper = setupResultDefinitionsReferenceData(orderDate, asList(firstNowNonMandatoryResultDefinitionId, secondNowPrimaryResultDefinitionId));

        saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate);

        saveDraftResultCommand.getTarget().setResultLines(asList(
                getResultLine(firstNowNonMandatoryResultDefinitionId, findPrompt(resultDefHelper, firstNowNonMandatoryResultDefinitionId), orderDate),
                getResultLine(secondNowPrimaryResultDefinitionId, findPrompt(resultDefHelper, secondNowPrimaryResultDefinitionId), orderDate)
        ));
        saveDraftResultCommand.getTarget().setTargetId(targets.get(0).getTargetId());

        targets.add(saveDraftResultCommand.getTarget());

        testSaveDraftResult(saveDraftResultCommand);

        final uk.gov.justice.core.courts.Hearing hearing = initiateHearingCommandHelper.getHearing();

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                                .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                        .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {

            shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            publicEventResulted.waitFor();

        }

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                        .with(Hearing::getHasSharedResults, is(true))));

    }

    @Test
    public void shareResults_shouldSurfaceResultsLinesInGetHearings_resultLinesShouldBeAsLastSubmittedOnly() {

        LocalDate orderDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);
        setupResultDefinitionsReferenceData(orderDate, singletonList(allNows.getFirstNowDefinitionFirstResultDefinitionId()));

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderDate);

        saveDraftResultCommand.getTarget().setResultLines(
                asList(standardResultLineTemplate(randomUUID(), randomUUID(), orderDate).build(),
                        standardResultLineTemplate(randomUUID(), randomUUID(), orderDate).build())
        );

        testSaveDraftResult(saveDraftResultCommand);

        saveDraftResultCommand.getTarget().setResultLines(
                singletonList(saveDraftResultCommand.getTarget().getResultLines().get(0))
        );

        saveDraftResultCommand.getTarget().setDraftResult("draft result version 2");

        testSaveDraftResult(saveDraftResultCommand);
    }

    @Test
    public void testApplicationDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();
        final ApplicationDraftResultCommand applicationDraftResultCommand = applicationDraftResultCommandTemplate(hearingOne.getHearingId());

        testApplicationDraftResult(applicationDraftResultCommand);
        Queries.getApplicationDraftResultsPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(ApplicationTargetListResponse.class)
                .with(ApplicationTargetListResponse::getHearingId, is(hearing.getId())));
    }

    @Test
    public void testApplicationDraftResultWithApplicationOutcomeSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID applicationId = hearingOne.getHearing().getCourtApplications().get(0).getId();
        final CourtApplicationOutcomeType applicationOutcome = CourtApplicationOutcomeType.courtApplicationOutcomeType().withDescription("Granted")
                .withId(randomUUID())
                .withSequence(1).build();
        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();
        final ApplicationDraftResultCommand applicationDraftResultCommand = applicationDraftResultWithOutcomeCommandTemplate(hearingOne.getHearingId(), applicationId, applicationOutcome);

        testApplicationDraftResult(applicationDraftResultCommand);
        Queries.getApplicationDraftResultsPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(ApplicationTargetListResponse.class)
                .with(ApplicationTargetListResponse::getHearingId, is(hearing.getId())));

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

        LocalDate orderDate = PAST_LOCAL_DATE.next();
        setupNowsReferenceData(orderDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper = setupResultDefinitionsReferenceData(orderDate, singletonList(GUILTY_RESULT_DEF_ID));

        InitiateHearingCommand initiateHearingCommand = welshInitiateHearingTemplate();

        addOffenceToInitiateHearingCommand(initiateHearingCommand, INDICATED_GUILTY);

        final InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        final Hearing hearing = hearingCommandHelper.getHearing();

        stubCourtCentre(hearingCommandHelper.getHearing());

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderDate);

        setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0), findPrompt(refDataHelper, GUILTY_RESULT_DEF_ID), GUILTY_RESULT_DEF_ID, orderDate);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingCommandHelper.getHearingId()))
                                .with(Hearing::getHearingLanguage, is(HearingLanguage.WELSH))
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getWelshDescription, is(hearing.getType().getWelshDescription())))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                        .with(CourtCentre::getWelshRoomName, is(hearing.getCourtCentre().getWelshRoomName()))
                                        .with(CourtCentre::getWelshName, is(hearing.getCourtCentre().getWelshName()))))))) {


            UseCases.shareResults(getRequestSpec(), hearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), singletonList(saveDraftResultCommand.getTarget()));

            final JsonPath jsonPath = publicEventResulted.waitFor();

            final List<HashMap> defendantReferralReasons = jsonPath.getList("hearing.defendantReferralReasons", HashMap.class);
            assertThat(defendantReferralReasons.get(0).get("welshDescription").toString(), is(hearing.getDefendantReferralReasons().get(0).getWelshDescription()));
            final String actualOffenceTitleWelsh = jsonPath.getString("hearing.prosecutionCases[0].defendants[0].offences[0].offenceTitleWelsh");
            final String expectedOffenceTitleWelsh = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getOffenceTitleWelsh();
            assertThat(actualOffenceTitleWelsh, is(expectedOffenceTitleWelsh));
        }
    }

    @Test
    public void shareResults_shouldPublishResults_andAmendAndReShareResultsAgain() {

        LocalDate orderDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);

        final AllResultDefinitionsReferenceDataHelper refDataHelper = setupResultDefinitionsReferenceData(orderDate, singletonList(allNows.getFirstPrimaryResultDefinitionId()));

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        stubCourtCentre(hearingOne.getHearing());

        ProsecutionCounselIT.createFirstProsecutionCounsel(hearingOne);

        final CommandHelpers.UpdatePleaCommandHelper pleaOne = changeConvictionDate(hearingOne);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderDate);

        setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0), findPrompt(refDataHelper, allNows.getFirstPrimaryResultDefinitionId()), allNows.getFirstPrimaryResultDefinitionId(), orderDate);

        final List<Target> targets = new ArrayList<>();

        targets.add(saveDraftResultCommand.getTarget());

        testSaveDraftResult(saveDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final uk.gov.justice.core.courts.Hearing hearing = hearingOne.getHearing();
        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {

            UseCases.shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            publicEventResulted.waitFor();


            orderDate = PAST_LOCAL_DATE.next();
            //setup reference data for second ordered date
            setupNowsReferenceData(orderDate, allNows.it());

            final UUID firstNowNonMandatoryResultDefinitionId = getResultDefinitionId(allNows, false);
            final UUID secondNowPrimaryResultDefinitionId = getResultDefinitionId(allNows, true);


            //need to get out prompt label here or put in to create draft label
            final AllResultDefinitionsReferenceDataHelper resultDefHelper = setupResultDefinitionsReferenceData(orderDate, asList(firstNowNonMandatoryResultDefinitionId, secondNowPrimaryResultDefinitionId));

            final SaveDraftResultCommand saveDraftResultCommand2 = saveDraftResultCommandTemplate(hearingOne.it(), orderDate);

            saveDraftResultCommand2.getTarget().setResultLines(asList(
                    getAmendedResultLine(firstNowNonMandatoryResultDefinitionId, findPrompt(resultDefHelper, firstNowNonMandatoryResultDefinitionId), orderDate),
                    getAmendedResultLine(secondNowPrimaryResultDefinitionId, findPrompt(resultDefHelper, secondNowPrimaryResultDefinitionId), orderDate)
            ));
            saveDraftResultCommand2.getTarget().setTargetId(targets.get(0).getTargetId());
            targets.add(saveDraftResultCommand2.getTarget());

            testSaveDraftResult(saveDraftResultCommand2);

            UseCases.shareResults(getRequestSpec(), hearingOne.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(getCourtClerk())
            ), targets);

            publicEventResulted.waitFor();

            Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                    .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                            .with(Hearing::getId, is(hearing.getId()))
                            .with(Hearing::getHasSharedResults, is(true))));
        }
    }

    private void testSaveDraftResult(final SaveDraftResultCommand saveDraftResultCommand) {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final Target target = saveDraftResultCommand.getTarget();
        final List<ResultLine> resultLines = target.getResultLines();
        // currently not sending result lines in draft
        target.setResultLines(null);
        final BeanMatcher<PublicHearingDraftResultSaved> beanMatcher = isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getTargetId, is(target.getTargetId()))
                .with(PublicHearingDraftResultSaved::getHearingId, is(target.getHearingId()))
                .with(PublicHearingDraftResultSaved::getDefendantId, is(target.getDefendantId()))
                .with(PublicHearingDraftResultSaved::getDraftResult, is(target.getDraftResult()))
                .with(PublicHearingDraftResultSaved::getOffenceId, is(target.getOffenceId()));

        final String expectedMetaDataContextUser = getLoggedInUser().toString();
        final String expectedMetaDataName = "public.hearing.draft-result-saved";
        try (final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(beanMatcher, expectedMetaDataName, expectedMetaDataContextUser)) {

            makeCommand(getRequestSpec(), "hearing.save-draft-result")
                    .ofType("application/vnd.hearing.save-draft-result+json")
                    .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                    .withPayload(saveDraftResultCommand.getTarget())
                    .executeSuccessfully();

            publicEventResulted.waitFor();
        }
        target.setResultLines(resultLines);
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate) {
        AllNows allnows = AllNows.allNows()
                .setNows(Arrays.asList(NowDefinition.now()
                                .setId(NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID)
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(RD_FINE)
                                                .setMandatory(true)
                                                .setWelshText("Welsh Text Primary")
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
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
                                .setId(ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID)
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(fromString("de946ddc-ad77-44b1-8480-8bbc251cdcfb")) // FIDICI
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
                                .setText("NowLevel/" + STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false)
                                .setText(STRING.next())
                                .setWelshText("welshText")
                                .setWelshName("welshName")
                ));
        return setupNowsReferenceData(referenceDate, allnows);
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final AllNowsReferenceDataHelper allNows = h(data);
        stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, List<UUID> resultDefinitionIds) {
        final String LISTING_OFFICER_USER_GROUP = "Listing Officer";

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(
                        resultDefinitionIds.stream().map(
                                resultDefinitionId ->
                                        ResultDefinition.resultDefinition()
                                                .setId(resultDefinitionId)
                                                .setRank(1)
                                                .setIsAvailableForCourtExtract(true)
                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                .setFinancial("Y")
                                                .setCategory(getCategoryForResultDefinition(resultDefinitionId))
                                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setWelshLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                                .setReference(STRING.next())
                                                        )
                                                )
                                                .setLabel(STRING.next())
                                                .setWelshLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                        ).collect(Collectors.toList())
                ));

        stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }

    private void testApplicationDraftResult(final ApplicationDraftResultCommand applicationDraftResultCommand) {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        try (final EventListener publicEvent = listenFor("public.hearing.application-draft-resulted")
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(applicationDraftResultCommand.getHearingId().toString())),
                        withJsonPath("$.draftResult", is(applicationDraftResultCommand.getDraftResult())))))) {
            makeCommand(getRequestSpec(), "hearing.application-draft-result")
                    .ofType("application/vnd.hearing.application-draft-result+json")
                    .withPayload(applicationDraftResultCommand)
                    .executeSuccessfully();

            publicEvent.waitFor();
        }
    }

    private uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt findPrompt(final AllResultDefinitionsReferenceDataHelper refDataHelper, final UUID resultDefId) {
        final ResultDefinition resultDefinition =
                refDataHelper.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(resultDefId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );
        return resultDefinition.getPrompts().get(0);
    }

    private void setResultLine(final ResultLine resultLine, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt prompt, final UUID resultDefId, final LocalDate orderDate) {
        resultLine.setResultLineId(randomUUID());
        resultLine.setResultDefinitionId(resultDefId);
        resultLine.setOrderedDate(orderDate);
        resultLine.setPrompts(singletonList(Prompt.prompt()
                .withLabel(prompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(prompt.getId())
                .build()));
    }

    private DelegatedPowers getCourtClerk() {
        return DelegatedPowers.delegatedPowers()
                .withFirstName(STRING.next()).withLastName(STRING.next())
                .withUserId(randomUUID()).build();
    }

    private Pair<InitiateHearingCommandHelper, List<Target>> givenHearingInitiatedWithDismissedResultDef(final List<UUID> resultDefinitionIds, final LocalDate orderDate) {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        addOffenceToInitiateHearingCommand(initiateHearingCommand, INDICATED_NOT_GUILTY);

        final InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        stubCourtCentre(hearingCommandHelper.getHearing());

        final List<SaveDraftResultCommand> saveDraftResultCommandList = saveDraftResultCommandForMultipleOffences(hearingCommandHelper.it(), orderDate, DISMISSED_RESULT_DEF_ID);

        final List<Target> targets = new ArrayList<>();
        saveDraftResultCommandList.forEach(saveDraftResultCommand -> targets.add(saveDraftResultCommand.getTarget()));

        final AllResultDefinitionsReferenceDataHelper refDataHelper = setupResultDefinitionsReferenceData(orderDate, resultDefinitionIds);

        for (int i = 0; i < saveDraftResultCommandList.size(); i++) {
            setResultLine(saveDraftResultCommandList.get(i).getTarget().getResultLines().get(0), findPrompt(refDataHelper, resultDefinitionIds.get(i)), resultDefinitionIds.get(i), orderDate);
        }

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        return Pair.of(hearingCommandHelper, targets);
    }

    private EventListener getPublicEventForDefendantCaseWithdrawnOrDismissed(final InitiateHearingCommandHelper hearingCommandHelper) {
        return listenFor("public.hearing.defendant-case-withdrawn-or-dismissed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.defendantId", is(hearingCommandHelper.getFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingCommandHelper.getFirstCase().getId().toString()))
                        ))
                );
    }

    private void stubCourtCentre(final Hearing hearing) {
        final CourtCentre courtCentre = hearing.getCourtCentre();
        stubLjaDetails(courtCentre, hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId());
        stubGetReferenceDataCourtRooms(courtCentre, hearing.getHearingLanguage(), ouId3, ouId4);
    }

    private void addOffenceToInitiateHearingCommand(final InitiateHearingCommand initiateHearingCommand, final IndicatedPleaValue indicatedPleaValue) {
        final UUID offenceId = randomUUID();
        initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().add(Offence.offence()
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
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, indicatedPleaValue).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());
    }

    private CommandHelpers.UpdatePleaCommandHelper changeConvictionDate(final InitiateHearingCommandHelper hearingOne) {
        try (final EventListener convictionDateListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                        ))
                )) {

            final CommandHelpers.UpdatePleaCommandHelper pleaOne = new CommandHelpers.UpdatePleaCommandHelper(
                    UseCases.updatePlea(getRequestSpec(), hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            updatePleaTemplate(hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                                    hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstCase().getId(), INDICATED_GUILTY, GUILTY, false))
            );

            convictionDateListener.waitFor();
            return pleaOne;
        }
    }

    private CommandHelpers.UpdatePleaCommandHelper updatePleaWithoutChangingConvictionDate(final InitiateHearingCommandHelper hearingOne) {
        try (final EventListener hearingPleaUpdatedListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString()))
                        )
                );
             final EventListener convictionDateChangedListener = listenFor("public.hearing.offence-conviction-date-changed")
                     .withFilter(isJson(allOf(
                             withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                             withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))
                             ))
                     )
        ) {

            final CommandHelpers.UpdatePleaCommandHelper pleaOne = new CommandHelpers.UpdatePleaCommandHelper(
                    UseCases.updatePlea(getRequestSpec(), hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            updatePleaTemplate(hearingOne.getHearingId(), hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                                    hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstCase().getId(), INDICATED_GUILTY, GUILTY, false))
            );

            hearingPleaUpdatedListener.waitFor();
            convictionDateChangedListener.expectNoneWithin(DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS);
            return pleaOne;
        }
    }

    private void updateDefendantAttendance(final InitiateHearingCommandHelper hearingOne) {
        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        try (final EventListener publicDefendantAttendanceUpdated = listenFor("public.hearing.defendant-attendance-updated", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(DefendantAttendanceUpdated.class, isBean(DefendantAttendanceUpdated.class)
                        .with(DefendantAttendanceUpdated::getHearingId, is(hearingId))
                        .with(DefendantAttendanceUpdated::getDefendantId, is(defendantId))
                        .with(DefendantAttendanceUpdated::getAttendanceDay, isBean(AttendanceDay.class)
                                .with(AttendanceDay::getDay, is(dateOfAttendance))
                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON)))))) {


            h(UseCases.updateDefendantAttendance(getRequestSpec(), updateDefendantAttendanceTemplate(hearingId, defendantId, dateOfAttendance, AttendanceType.IN_PERSON)));

            publicDefendantAttendanceUpdated.waitFor();
        }
    }

    private UUID getResultDefinitionId(final AllNowsReferenceDataHelper allNows, final boolean mandatory) {
        return allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> rd.getMandatory() == mandatory)
                .map(NowResultDefinitionRequirement::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
    }

    private ResultLine getResultLine(final UUID resultDefinitionId, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt prompt, final LocalDate orderDate) {
        return standardResultLineTemplate(randomUUID(), resultDefinitionId, orderDate).withPrompts(
                singletonList(Prompt.prompt().withId(prompt.getId()).withValue("val0").withWelshValue("wval0")
                        .withFixedListCode("fixedList0").withLabel(prompt.getLabel()).build())
        ).build();
    }

    private ResultLine getAmendedResultLine(final UUID resultDefinitionId, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt prompt, final LocalDate orderDate) {
        return standardAmendedResultLineTemplate(randomUUID(), resultDefinitionId, orderDate).withPrompts(
                singletonList(Prompt.prompt().withId(prompt.getId()).withValue("val0").withWelshValue("wval0")
                        .withFixedListCode("fixedList0").withLabel(prompt.getLabel()).build())
        ).build();
    }

    private CrackedIneffectiveTrial getExpectedTrialType(final InitiateHearingCommandHelper hearingOne, final CommandHelpers.UpdatePleaCommandHelper pleaOne, final LocalDate convictionDateToUse) {
        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = INEFFECTIVE_TRIAL_TYPE;
        final CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getDate(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());


        final TrialType addTrialType = TrialType.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
                                                .with(Offence::getConvictionDate, is(convictionDateToUse))))))))));
        return expectedTrialType;
    }


    private void assertHearingWithMultipleCasesCreatedAndResultAreNotShared(final Hearing hearing) {
        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getProsecutionCases, MatcherUtil.getProsecutionCasesMatchers(hearing.getProsecutionCases()))
                        .with(Hearing::getHasSharedResults, is(false))
                )
        );
    }

    private Hearing saveDraftResult(final LocalDate orderedDate, final AllNowsReferenceDataHelper allNows, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt, final InitiateHearingCommandHelper hearingOne, final List<Target> targets, final SaveDraftResultCommand saveDraftResultCommand) {
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
        resultLine1.setResultLineId(randomUUID());
        resultLine1.setResultDefinitionId(allNows.getFirstPrimaryResultDefinitionId());
        resultLine1.setOrderedDate(orderedDate);

        final Hearing hearing = hearingOne.getHearing();

        testSaveDraftResult(saveDraftResultCommand);
        return hearing;
    }

    private void updateDefendantDetails(final InitiateHearingCommand initiateHearing, final InitiateHearingCommandHelper hearingOne, final String firstName, String firstNameToVerify) throws Exception {
        CaseDefendantDetails caseDefendantDetails = new CaseDefendantDetails();
        caseDefendantDetails.setDefendants(singletonList(convert(initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0), firstName)));
        final CommandHelpers.CaseDefendantDetailsHelper defendantUpdates = h(UseCases.updateDefendants(caseDefendantDetails));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, 3, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, CoreMatchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, CoreMatchers.is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getId()))
                                        .with(Defendant::getProsecutionCaseId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getProsecutionCaseId()))
                                        .with(Defendant::getNumberOfPreviousConvictionsCited, CoreMatchers.is(defendantUpdates.getFirstDefendant().getNumberOfPreviousConvictionsCited()))
                                        .with(Defendant::getProsecutionAuthorityReference, CoreMatchers.is(defendantUpdates.getFirstDefendant().getProsecutionAuthorityReference()))
                                        .with(Defendant::getWitnessStatement, CoreMatchers.is(defendantUpdates.getFirstDefendant().getWitnessStatement()))
                                        .with(Defendant::getWitnessStatementWelsh, CoreMatchers.is(defendantUpdates.getFirstDefendant().getWitnessStatementWelsh()))
                                        .with(Defendant::getMitigation, CoreMatchers.is(defendantUpdates.getFirstDefendant().getMitigation()))
                                        .with(Defendant::getMitigationWelsh, CoreMatchers.is(defendantUpdates.getFirstDefendant().getMitigationWelsh()))
                                        .with(Defendant::getIsYouth, CoreMatchers.is(defendantUpdates.getFirstDefendant().getIsYouth()))
                                        .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)
                                                .with(PersonDefendant::getArrestSummonsNumber, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getArrestSummonsNumber()))
                                                .with(PersonDefendant::getBailStatus, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getBailStatus()))
                                                .with(PersonDefendant::getCustodyTimeLimit, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getCustodyTimeLimit()))
                                                .with(PersonDefendant::getDriverNumber, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getDriverNumber()))
                                                .with(PersonDefendant::getEmployerPayrollReference, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getEmployerPayrollReference()))
                                                .with(PersonDefendant::getPerceivedBirthYear, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPerceivedBirthYear()))
                                                .with(PersonDefendant::getCustodialEstablishment, isBean(CustodialEstablishment.class)
                                                        .with(CustodialEstablishment::getId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getCustodialEstablishment().getId()))
                                                )
                                                .with(PersonDefendant::getPersonDetails, isBean(Person.class)
                                                        .with(Person::getFirstName, CoreMatchers.is(firstNameToVerify))
                                                        .with(Person::getLastName, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getLastName()))
                                                        .with(Person::getAddress, isBean(Address.class)
                                                                .with(Address::getAddress1, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress1()))
                                                                .with(Address::getAddress2, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress2()))
                                                                .with(Address::getAddress3, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress3()))
                                                                .with(Address::getAddress4, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getAddress4()))
                                                                .with(Address::getPostcode, CoreMatchers.is(defendantUpdates.getFirstDefendant().getPersonDefendant().getPersonDetails().getAddress().getPostcode()))
                                                        )))
                                ))))));
    }

    private void updateDefendantDetailsWithDefenceAssociation(final InitiateHearingCommand initiateHearing, final InitiateHearingCommandHelper hearingOne, final String applicationReference, Boolean isLAARep, String organisationNumber) throws Exception {
        CaseDefendantDetails caseDefendantDetails = new CaseDefendantDetails();
        AssociatedDefenceOrganisation associatedDefenceOrganisation = AssociatedDefenceOrganisation.associatedDefenceOrganisation()
                .withApplicationReference(applicationReference)
                .withIsAssociatedByLAA(isLAARep)
                .withFundingType(FundingType.REPRESENTATION_ORDER)
                .withAssociationStartDate(LocalDate.now())
                .withDefenceOrganisation(DefenceOrganisation.defenceOrganisation().withOrganisation(Organisation.organisation().withName(organisationNumber).build()).build())
                .build();
        caseDefendantDetails.setDefendants(asList(convertWithDefenceAssociate(initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0), associatedDefenceOrganisation)));
        final CommandHelpers.CaseDefendantDetailsHelper defendantUpdates = h(UseCases.updateDefendants(caseDefendantDetails));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, 3, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, CoreMatchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, CoreMatchers.is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getId()))
                                        .with(Defendant::getProsecutionCaseId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getProsecutionCaseId()))
                                        .with(Defendant::getAssociatedDefenceOrganisation, isBean(AssociatedDefenceOrganisation.class)
                                                .with(AssociatedDefenceOrganisation::getApplicationReference, CoreMatchers.is(applicationReference))
                                        )

                                ))))));
    }

    private void updateDefendantDetailsWithNoDefenceAssociation(final InitiateHearingCommand initiateHearing, final InitiateHearingCommandHelper hearingOne) throws Exception {
        CaseDefendantDetails caseDefendantDetails = new CaseDefendantDetails();
        caseDefendantDetails.setDefendants(asList(convertWithDefenceAssociate(initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0), null)));
        final CommandHelpers.CaseDefendantDetailsHelper defendantUpdates = h(UseCases.updateDefendants(caseDefendantDetails));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, 3, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, CoreMatchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, CoreMatchers.is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getId()))
                                        .with(Defendant::getProsecutionCaseId, CoreMatchers.is(defendantUpdates.getFirstDefendant().getProsecutionCaseId()))
                                        .with(Defendant::getAssociatedDefenceOrganisation, nullValue())

                                ))))));
    }

    private CommandHelpers.UpdateVerdictCommandHelper changeVerdict(final InitiateHearingCommandHelper hearingOne, final UUID verdictTypeId, final String verdictCode) {
        try (final EventListener verdictUpdatedPublicEventListener = listenFor("public.hearing.verdict-updated")
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))
                        ))
                )) {

            final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    UpdateVerdictCommandTemplates.updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            VerdictCategoryType.GUILTY,
                            verdictTypeId,
                            verdictCode)
            ));

            verdictUpdatedPublicEventListener.waitFor();
            return updateVerdict;
        }
    }

    @Test
    public void shouldShareResultForAGivenResultDefinitionIDAndLabelsCombination() {
        final List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = generateResultDefinitionAndLabelCombination();
        resultCodeAndPromptLabels.forEach(resultCodeAndPromptLabelsItem -> shareResultWithResultDefinitionAndLabel(resultCodeAndPromptLabelsItem.getKey(), resultCodeAndPromptLabelsItem.getValue()));
    }

    private List<Pair<UUID, List<String>>> generateResultDefinitionAndLabelCombination() {

        List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = new ArrayList<>();

        resultCodeAndPromptLabels.addAll(getFCOSTResultCodeAndPromptLabelPairs());
        resultCodeAndPromptLabels.addAll(getFCOMPResultCodeAndPromptLabelPairs());
        resultCodeAndPromptLabels.addAll(getTIMPResultCodeAndPromptLabelPairs());
        resultCodeAndPromptLabels.addAll(getCCCSUResultCodeAndPromptLabelPairs());
        resultCodeAndPromptLabels.addAll(getPARAOResultCodeAndPromptLabelPairs());

        return resultCodeAndPromptLabels;
    }

    private void shareResultWithResultDefinitionAndLabel(UUID resultDefinitionID, List<String> labels) {
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        updateHearingWithOffences(initiateHearingCommand);

        final InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        stubLjaDetails(hearing.getHearing().getCourtCentre(), hearing.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId());

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearing.it(), orderedDate);

        saveDraftResultCommand.getTarget().setResultLines(singletonList(
                standardResultLineTemplate(randomUUID(), resultDefinitionID, orderedDate)
                        .withPrompts(labels.stream().map(label -> Prompt.prompt()
                                .withLabel(label)
                                .withValue("randomValue")
                                .withWelshValue("randomWelshValue")
                                .withId(randomUUID())
                                .build())
                                .collect(Collectors.toList()))
                        .build()));

        final List<Target> targets = singletonList(saveDraftResultCommand.getTarget());
        testSaveDraftResult(saveDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        Queries.getHearingPollForMatch(hearing.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getHearingId()))
                        .with(Hearing::getHasSharedResults, is(false))));


        UseCases.shareResults(getRequestSpec(), hearing.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk)
        ), targets);

        Queries.getHearingPollForMatch(hearing.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getHearingId()))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    private List<Pair<UUID, List<String>>> getFCOSTResultCodeAndPromptLabelPairs() {
        List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = new ArrayList<>();
        UUID fcostResultCode = fromString("76d43772-0660-4a33-b5c6-8f8ccaf6b4e3");

        List<String> fcostOneOffAndRequiredPromptLabelCombination1 = new ArrayList<>();
        fcostOneOffAndRequiredPromptLabelCombination1.add("Amount of costs");
        fcostOneOffAndRequiredPromptLabelCombination1.add("Major creditor name");

        List<String> fcostOneOffAndRequiredPromptLabelCombination2 = new ArrayList<>();
        fcostOneOffAndRequiredPromptLabelCombination2.add("Amount of costs");
        fcostOneOffAndRequiredPromptLabelCombination2.add("Minor creditor title");

        List<String> fcostOneOffAndRequiredPromptLabelCombination3 = new ArrayList<>();
        fcostOneOffAndRequiredPromptLabelCombination3.add("Amount of costs");
        fcostOneOffAndRequiredPromptLabelCombination3.add("Minor creditor company name");

        resultCodeAndPromptLabels.add(Pair.of(fcostResultCode, fcostOneOffAndRequiredPromptLabelCombination1));
        resultCodeAndPromptLabels.add(Pair.of(fcostResultCode, fcostOneOffAndRequiredPromptLabelCombination2));
        resultCodeAndPromptLabels.add(Pair.of(fcostResultCode, fcostOneOffAndRequiredPromptLabelCombination3));

        return resultCodeAndPromptLabels;
    }

    private List<Pair<UUID, List<String>>> getFCOMPResultCodeAndPromptLabelPairs() {
        final List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = new ArrayList<>();
        UUID fcompResultCode = fromString("ae89b99c-e0e3-47b5-b218-24d4fca3ca53");

        List<String> fcompOneOffAndRequiredPromptLabelCombination1 = new ArrayList<>();
        fcompOneOffAndRequiredPromptLabelCombination1.add("Amount of compensation");
        fcompOneOffAndRequiredPromptLabelCombination1.add("Major creditor name");

        List<String> fcompOneOffAndRequiredPromptLabelCombination2 = new ArrayList<>();
        fcompOneOffAndRequiredPromptLabelCombination2.add("Amount of compensation");
        fcompOneOffAndRequiredPromptLabelCombination2.add("Minor creditor title");

        resultCodeAndPromptLabels.add(Pair.of(fcompResultCode, fcompOneOffAndRequiredPromptLabelCombination1));
        resultCodeAndPromptLabels.add(Pair.of(fcompResultCode, fcompOneOffAndRequiredPromptLabelCombination2));

        return resultCodeAndPromptLabels;
    }

    private List<Pair<UUID, List<String>>> getTIMPResultCodeAndPromptLabelPairs() {
        final List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = new ArrayList<>();
        UUID resultCode = fromString("6cb15971-c945-4398-b7c9-3f8b743a4de3");

        List<String> requiredPromptLabels = Arrays
                .asList("Prison Name",
                        "Prison address line 1",
                        "Probation team to be notified");
        List<String> oneOffAndRequiredPromptLabelCombination = new ArrayList<>(requiredPromptLabels);
        oneOffAndRequiredPromptLabelCombination.add("Total imprisonment period");

        resultCodeAndPromptLabels.add(Pair.of(resultCode, oneOffAndRequiredPromptLabelCombination));

        return resultCodeAndPromptLabels;
    }

    private List<Pair<UUID, List<String>>> getCCCSUResultCodeAndPromptLabelPairs() {
        final List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = new ArrayList<>();
        UUID cccsuResultCode = fromString("3c03001c-56b3-4c64-bc0f-a3c57c3f424f");

        List<String> cccsuRequiredPromptLabels = new ArrayList<>();
        cccsuRequiredPromptLabels.add("Committed to the Crown Court");
        cccsuRequiredPromptLabels.add("Bail remand days to count (tagged days)");
        cccsuRequiredPromptLabels.add("PSR ordered?");

        List<String> cccsuOneOffAndRequiredPromptLabelCombination1 = new ArrayList<>(cccsuRequiredPromptLabels);
        cccsuOneOffAndRequiredPromptLabelCombination1.add("Victim personal statement to be presented to court by prosecutor or other person");

        resultCodeAndPromptLabels.add(Pair.of(cccsuResultCode, cccsuOneOffAndRequiredPromptLabelCombination1));

        return resultCodeAndPromptLabels;
    }

    private List<Pair<UUID, List<String>>> getPARAOResultCodeAndPromptLabelPairs() {
        final List<Pair<UUID, List<String>>> resultCodeAndPromptLabels = new ArrayList<>();
        UUID paraoResultCode = fromString("158f2196-fe4e-4e69-bdfb-eeaba8abb432");

        List<String> paraoRequiredPromptLabels = new ArrayList<>();
        paraoRequiredPromptLabels.add("This order lasts for");
        paraoRequiredPromptLabels.add("Order details");
        paraoRequiredPromptLabels.add("Responsible officer");

        List<String> paraoOneOffAndRequiredPromptLabelCombination1 = new ArrayList<>(paraoRequiredPromptLabels);
        paraoOneOffAndRequiredPromptLabelCombination1.add("This order is made because the parent/guardian has been convicted of failing to comply with a school attendance order");

        resultCodeAndPromptLabels.add(Pair.of(paraoResultCode, paraoOneOffAndRequiredPromptLabelCombination1));

        return resultCodeAndPromptLabels;
    }

    private void updateHearingWithOffences(final InitiateHearingCommand initiateHearingCommand) {
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
    }

    private List<Offence> getOffences(final InitiateHearingCommand initiateHearingCommand) {
        return initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
    }

    private void assertHearingHasSharedResults(final CrackedIneffectiveTrial expectedTrialType, final Hearing hearing) {
        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    private void assertHearingResultsAreShared(final Hearing hearing) {
        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    @Test
    public void shouldShareResultsWithShadowListedOffence() throws Exception {
        LocalDate orderDate = PAST_LOCAL_DATE.next();
        stubGetReferenceDataResultDefinitionsWithDefaultValues(orderDate);

        final InitiateHearingCommand initiateHearing = standardInitiateHearingTemplateWithDefendantJudicialResultsForMagistrates();
        final UUID targetId = randomUUID();
        final InitiateHearingCommandHelper hearing = createInitiateHearingCommandHelperForNextHearing(initiateHearing, orderDate);

        stubCourtRoom(hearing.getHearing());

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        saveDraftResultsWithShadowListedFlag(hearing, targetId);
        shareResultsAndValidateShadowListedOffences(hearing, targetId);
    }

    private void saveDraftResultWithInvalidTargetIdAndCheckForFailedEvent(final SaveDraftResultCommand saveDraftResultCommand) {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final Target target = saveDraftResultCommand.getTarget();

        try (final EventListener publicEventFailed = listenFor("public.hearing.save-draft-result-failed")
                .withFilter(convertStringTo(PublicHearingSaveDraftResultFailed.class, isBean(PublicHearingSaveDraftResultFailed.class)
                        .with(PublicHearingSaveDraftResultFailed::getTargetId, is(target.getTargetId()))
                        .with(PublicHearingSaveDraftResultFailed::getHearingId, is(target.getHearingId()))
                        .with(PublicHearingSaveDraftResultFailed::getDefendantId, is(target.getDefendantId()))
                        .with(PublicHearingSaveDraftResultFailed::getDraftResult, is(target.getDraftResult()))
                        .with(PublicHearingSaveDraftResultFailed::getOffenceId, is(target.getOffenceId())
                        )))) {

            makeCommand(getRequestSpec(), "hearing.save-draft-result")
                    .ofType("application/vnd.hearing.save-draft-result+json")
                    .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                    .withPayload(saveDraftResultCommand.getTarget())
                    .executeSuccessfully();

            publicEventFailed.waitFor();
        }
    }

    @Test
    public void saveDraftResult_AndRemoveTargetAsSystemUser() throws Exception {

        //Given

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        final List<Target> targets = new ArrayList<>();
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = createInitiateHearingCommandHelper(initiateHearing, targets);

        final UUID targetId = targets.get(0).getTargetId();
        final UUID hearingId = hearingOne.getHearingId();

        getDraftResultsPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .with(TargetListResponse::getTargets, hasSize(1))
        );

        removeDraftTarget(hearingId, targetId);

        getDraftResultsPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .with(TargetListResponse::getTargets, is(empty())));

    }


    private void removeDraftTarget(final UUID hearingId, final UUID targetId) {

        try (final Utilities.EventListener targetRemovedEvent = listenFor("hearing.target-removed", "hearing.event")
                .withFilter(convertStringTo(TargetRemoved.class, isBean(TargetRemoved.class)
                        .with(TargetRemoved::getTargetId, is(targetId))
                        .with(TargetRemoved::getHearingId, is(hearingId))
                ))) {

            final JsonObject payload = createObjectBuilder().add("targetIds", createArrayBuilder().add(targetId.toString()).build()).build();
            makeCommand(getRequestSpec(), "hearing.remove-targets")
                    .ofType("application/vnd.hearing.remove-targets+json")
                    .withArgs(hearingId)
                    .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                    .withPayload(payload.toString())
                    .executeSuccessfully();

            targetRemovedEvent.waitFor();

        }
    }

}