package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.core.courts.DelegatedPowers.delegatedPowers;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.createFirstProsecutionCounsel;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplateWithApplication;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefendantAttendanceCommandTemplates.updateDefendantAttendanceTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VERDICT_TYPE_GUILTY_CODE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VERDICT_TYPE_GUILTY_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllNowsMetaData;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllResultDefinitions;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.ResultDefinitionUtil.getCategoryForResultDefinition;

import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.AttendanceType;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.SecondaryCJSCode;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.TestTemplates;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;


@SuppressWarnings({"squid:S2699"})
public class ShareResultsV2IT extends AbstractIT {

    private static final UUID NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID = fromString("66cd749a-1d51-11e8-accf-0ed5f89f718b");
    private static final UUID ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID = fromString("10115268-8efc-49fe-b8e8-feee216a03da");
    private static final UUID RD_FINE = fromString("969f150c-cd05-46b0-9dd9-30891efcc766");

    @Test
    public void shouldShareResultsForHearingWithMultipleCasesWithApplication() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final UUID withDrawnResultDefId = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        assertHearingWithMultipleCasesCreatedAndResultAreNotShared(hearing);

        stubCourtRoom(hearing);

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt
                now1MandatoryResultDefinitionPrompt = getMandatoryNowResultDefPrompt(orderedDate, withDrawnResultDefId, allNows);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplateWithApplication(hearingCommand.it(), orderedDate);
        setPromptForSaveDraftResultCommand(now1MandatoryResultDefinitionPrompt, saveDraftResultCommand);

        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        assertHearingResultsAreShared(hearing);
    }

    @Test
    public void shouldShareResultsForHearingWithMultipleCasesWithApplicationClearVerdict() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final UUID withDrawnResultDefId = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
        final UUID ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID = fromString("10115268-8efc-49fe-b8e8-feee216a03da");

        final LocalDate hearingDay = LocalDate.now();

        final InitiateHearingCommandHelper hearingCommand = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final Hearing hearing = hearingCommand.getHearing();

        assertHearingWithMultipleCasesCreatedAndResultAreNotShared(hearing);

        stubCourtRoom(hearing);

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt
                now1MandatoryResultDefinitionPrompt = getMandatoryNowResultDefPrompt(orderedDate, withDrawnResultDefId, allNows);
        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplateWithApplication(hearingCommand.it(), orderedDate);
        setPromptForSaveDraftResultCommand(now1MandatoryResultDefinitionPrompt, saveDraftResultCommand);

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt
                now1MandatoryResultDefinitionPrompt1 = getMandatoryNowResultDefPrompt(orderedDate, ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID, allNows);
        final SaveDraftResultCommand saveDraftResultCommand1 = saveDraftResultCommandTemplateWithApplication(hearingCommand.it(), orderedDate);
        setPromptForSaveDraftResultCommand(now1MandatoryResultDefinitionPrompt1, saveDraftResultCommand1);

        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());

        final List<Target> targets1 = new ArrayList<>();
        targets1.add(saveDraftResultCommand1.getTarget());

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdictCommandHelper = updateDefendantAndChangeVerdict(hearingCommand);

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        assertHearingResultsAreShared(hearing);

        Queries.getHearingPollForMatch(hearingCommand.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingCommand.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(updateVerdictCommandHelper.getFirstVerdict().getOffenceId()))
                                                .with(Offence::getVerdict, is(notNullValue()))
                                        ))
                                ))
                        ))
                )
        );

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(getRequestSpec(), hearingCommand.getHearingId(),
                updateVerdictTemplate(
                        hearingCommand.getHearingId(),
                        hearingCommand.getFirstOffenceForFirstDefendantForFirstCase().getId()
                )));


        shareDaysResultWithCourtClerk(hearing, targets1, hearingDay);

        assertHearingResultsAreShared(hearing);

        Queries.getHearingPollForMatch(hearingCommand.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingCommand.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(updateVerdict.getFirstVerdict().getOffenceId()))
                                                .with(Offence::getVerdict, is(nullValue()))
                                        ))
                                ))
                        ))
                )
        );


    }

    @Test
    public void doesNotIncludeParentGuardianPromptInPublicHearingPayloadIfFalseWhenShareResult() {
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final UUID withDrawnResultDefId = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        assertHearingWithMultipleCasesCreatedAndResultAreNotShared(hearing);

        stubCourtRoom(hearing);
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt
                now1MandatoryResultDefinitionPrompt = getMandatoryNowResultDefPrompt(orderedDate, withDrawnResultDefId, allNows);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingCommand.it(), orderedDate, hearingDay);
        setPromptForSaveDraftResultCommand(now1MandatoryResultDefinitionPrompt, saveDraftResultCommand);

        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {
            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getList("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].judicialResultPrompts").size(), is(1));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].judicialResultPrompts[0].promptReference"), is("bailConditionReason"));
        }
        assertHearingResultsAreShared(hearing);
    }

    private CommandHelpers.UpdateVerdictCommandHelper updateDefendantAndChangeVerdict(InitiateHearingCommandHelper initiateHearingCommandHelper) {
        updateDefendantAttendance(initiateHearingCommandHelper);

        stubCourtCentre(initiateHearingCommandHelper.getHearing());

        createFirstProsecutionCounsel(initiateHearingCommandHelper);

        return changeVerdict(initiateHearingCommandHelper, fromString(VERDICT_TYPE_GUILTY_ID), VERDICT_TYPE_GUILTY_CODE);
    }

    private void updateDefendantAttendance(final InitiateHearingCommandHelper hearingOne) {
        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        try (final Utilities.EventListener publicDefendantAttendanceUpdated = listenFor("public.hearing.defendant-attendance-updated", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
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

    private void stubCourtCentre(final Hearing hearing) {
        stubCourtCentre(hearing, null);
    }

    private CommandHelpers.UpdateVerdictCommandHelper changeVerdict(final InitiateHearingCommandHelper hearingOne, final UUID verdictTypeId, final String verdictCode) {
        try (final Utilities.EventListener verdictUpdatedPublicEventListener = listenFor("public.hearing.verdict-updated")
                .withFilter(isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))
                        ))
                );
             final Utilities.EventListener offenceVerdictUpdatedPublicEventListener = listenFor("public.hearing.hearing-offence-verdict-updated")
                     .withFilter(isJson(allOf(
                                     withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                     withJsonPath("$.verdict.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString()))
                             ))
                     )
        ) {

            final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            TestTemplates.VerdictCategoryType.GUILTY,
                            verdictTypeId,
                            verdictCode)
            ));

            verdictUpdatedPublicEventListener.waitFor();
            offenceVerdictUpdatedPublicEventListener.waitFor();
            return updateVerdict;
        }
    }

    private void stubCourtCentre(final Hearing hearing, final String ljaCode) {
        final CourtCentre courtCentre = hearing.getCourtCentre();
        hearing.getProsecutionCases().forEach(prosecutionCase -> stubLjaDetails(courtCentre, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId(), ljaCode));
        stubGetReferenceDataCourtRooms(courtCentre, hearing.getHearingLanguage(), ouId3, ouId4);
    }

    private void shareDaysResultWithCourtClerk(final Hearing hearing, final List<Target> targets, final LocalDate hearingDay) {
        final DelegatedPowers courtClerk1 = delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();
        ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
        shareDaysResultsCommand.setHearingDay(hearingDay);
        shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                shareDaysResultsCommand,
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }

    private SaveDraftResultCommand setPromptForSaveDraftResultCommand(final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt now1MandatoryResultDefinitionPrompt, final SaveDraftResultCommand saveDraftResultCommand) {
        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(
                asList(Prompt.prompt()
                                .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                                .withPromptRef(now1MandatoryResultDefinitionPrompt.getReference())
                                .withFixedListCode("fixedListCode")
                                .withValue("value1")
                                .withWelshValue("wvalue1")
                                .withId(now1MandatoryResultDefinitionPrompt.getId())
                                .build(),
                        Prompt.prompt()
                                .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                                .withPromptRef("PARENT_GAURDIAN_TO_PAY")
                                .withValue("false")
                                .withWelshValue("false")
                                .withId(now1MandatoryResultDefinitionPrompt.getId())
                                .build()));
        saveDraftResultCommand.getTarget().getResultLines().get(0).setResultDefinitionId(saveDraftResultCommand.getTarget().getResultLines().get(0).getPrompts().get(0).getId());
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
                                                .setPrompts(asList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(resultDefinitionId)
                                                                .setMandatory(true)
                                                                .setLabel("promptLabel")
                                                                .setWelshLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                                .setReference("bailConditionReason"),
                                                        uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(resultDefinitionId)
                                                                .setMandatory(false)
                                                                .setLabel("Parent Guardian to Pay")
                                                                .setWelshLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                                .setReference("PARENT_GAURDIAN_TO_PAY")
                                                        )
                                                )
                                                .setSecondaryCJSCodes(getSecondaryCjsCodes())
                                                .setLabel("resultLabel")
                                                .setDrivingTestStipulation(1)
                                                .setPointsDisqualificationCode("TT99")
                                                .setDvlaCode("C")
                                                .setWelshLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                .setCanBeSubjectOfBreach(true)
                                                .setCanBeSubjectOfVariation(true)
                                                .setLevel("O")
                                                .setPostHearingCustodyStatus("B")
                                                .setResultDefinitionGroup("Bail Conditions")
                        ).collect(Collectors.toList())
                ));

        stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }

    private void assertHearingWithMultipleCasesCreatedAndResultAreNotShared(final Hearing hearing) {
        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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

    private void assertHearingResultsAreShared(final Hearing hearing) {
        getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    private List<SecondaryCJSCode> getSecondaryCjsCodes() {
        final List<SecondaryCJSCode> secondaryCJSCodes = new ArrayList<>();
        final SecondaryCJSCode firstSecondaryCJSCode = new SecondaryCJSCode();
        firstSecondaryCJSCode.setCjsCode("1234");
        firstSecondaryCJSCode.setText("SecondaryCJSCode text1");

        final SecondaryCJSCode secondSecondaryCJSCode = new SecondaryCJSCode();
        secondSecondaryCJSCode.setCjsCode("5678");
        secondSecondaryCJSCode.setText("SecondaryCJSCode text2");

        secondaryCJSCodes.add(firstSecondaryCJSCode);
        secondaryCJSCodes.add(secondSecondaryCJSCode);

        return secondaryCJSCodes;
    }

}
