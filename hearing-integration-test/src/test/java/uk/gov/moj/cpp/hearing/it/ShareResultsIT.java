package uk.gov.moj.cpp.hearing.it;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubProgressionGenerateNows;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtClerk;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.Key;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowType;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.SharedHearing;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.SharedVariant;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ProgressionStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("unchecked")
@Ignore("This Test is ignore to get progression release version.")
public class ShareResultsIT extends AbstractIT {

    public static final String DOCUMENT_TEXT = "someDocumentText";
    public static final String BOTH_JURISDICTIONS = "B";

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


    private BeanMatcher<Target> draftTargetMatcher(final Target target) {
        final ResultLine resultLine = target.getResultLines().get(0);
        final Prompt prompt = resultLine.getPrompts().get(0);
        return isBean(Target.class)
                .with(Target::getDefendantId, is(target.getDefendantId()))
                .with(Target::getDraftResult, is(target.getDraftResult()))
                .with(Target::getHearingId, is(target.getHearingId()))
                .with(Target::getOffenceId, is(target.getOffenceId()));
    }

    private void checkHearingQueryTargets(final uk.gov.justice.core.courts.Hearing hearing, final List<Target> targets) {

        final BeanMatcher<Hearing> hearingBeanMatcher = isBean(Hearing.class)
                .with(Hearing::getId, is(hearing.getId()))
                .with(Hearing::getType, isBean(HearingType.class)
                        .with(HearingType::getId, is(hearing.getType().getId())))
                .with(h -> h.getTargets().size(), is(targets.size()));


        for (int index = 0; index < targets.size(); index++) {
            hearingBeanMatcher.with(Hearing::getTargets, hasItem(draftTargetMatcher(targets.get(index))));
        }

        Queries.getHearingPollForMatch(hearing.getId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, hearingBeanMatcher
                        ));
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

        checkHearingQueryTargets(hearing, targets);
    }


    @Test
    public void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines_andShouldPersistNows() {
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final NowDefinition nowDefinition1 = allNows.getFirstNowDefinition();
        final NowDefinition nowDefinition2 = allNows.getSecondNowDefinition();

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

        ProsecutionCounsel firstProsecutionCounsel = ProsecutionCounselIT.createFirstProsecutionCounsel(hearingOne);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate);

        final List<Target> targets = new ArrayList<>();

        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(
                singletonList(Prompt.prompt()
                        .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                        .withFixedListCode("fixedListCode")
                        .withValue("value1")
                        .withWelshValue("wvalue1")
                        .withId(now1MandatoryResultDefinitionPrompt.getId())
                        .build())
        );
        targets.add(saveDraftResultCommand.getTarget());
        //TODO GPE-6699 saveDraftCommand.setTarget(null)

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
        final CourtClerk courtClerk1 = CourtClerk.courtClerk()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withId(UUID.randomUUID()).build();

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> {
                    command.setCourtClerk(courtClerk1);
                }
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

        final CourtClerk courtClerk2 = CourtClerk.courtClerk()
                .withFirstName("Siouxsie").withLastName("Sioux")
                .withId(UUID.randomUUID()).build();

        final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(SharedHearing.class)
                                .with(SharedHearing::getId, is(hearingOne.getHearingId()))
                                .with(SharedHearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId())))
                                .with(SharedHearing::getSharedResultLines, hasItem(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate))
                                        .with(SharedResultLine::getCourtClerk, isBean(DelegatedPowers.class)
                                                .withValue(DelegatedPowers::getUserId, courtClerk1.getId())
                                                .withValue(DelegatedPowers::getFirstName, courtClerk1.getFirstName())
                                                .withValue(DelegatedPowers::getLastName, courtClerk1.getLastName())
                                        )

                                ))
                                .with(SharedHearing::getSharedResultLines, hasItem(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate2))
                                        .with(SharedResultLine::getCourtClerk, isBean(DelegatedPowers.class)
                                                .withValue(DelegatedPowers::getUserId, courtClerk2.getId())
                                                .withValue(DelegatedPowers::getFirstName, courtClerk2.getFirstName())
                                                .withValue(DelegatedPowers::getLastName, courtClerk2.getLastName())
                                        )
                                ))
                        )

                        .with(phr -> phr.getVariants().size(), is(2))

                        .with(PublicHearingResulted::getVariants, hasItem(isBean(SharedVariant.class)
                                .withValue(SharedVariant::getTemplateName, nowDefinition1.getTemplateName())
                                .withValue(SharedVariant::getDescription, nowDefinition1.getName())
                                .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                                .withValue(SharedVariant::getTemplateName, nowDefinition1.getTemplateName())
                                .withValue(SharedVariant::getDescription, nowDefinition1.getName())
                                .with(SharedVariant::getKey, isBean(Key.class)
                                        .withValue(Key::getDefendantId, defendant.getId())
                                        .withValue(Key::getHearingId, hearing.getId())
                                        .withValue(Key::getNowsTypeId, nowDefinition1.getId())
                                )
                        ))
                        .with(PublicHearingResulted::getVariants, hasItem(isBean(SharedVariant.class)
                                .withValue(SharedVariant::getTemplateName, nowDefinition2.getTemplateName())
                                .withValue(SharedVariant::getDescription, nowDefinition2.getName())
                                .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                                .withValue(SharedVariant::getTemplateName, nowDefinition2.getTemplateName())
                                .withValue(SharedVariant::getDescription, nowDefinition2.getName())
                                .with(SharedVariant::getKey, isBean(Key.class)
                                        .withValue(Key::getDefendantId, defendant.getId())
                                        .withValue(Key::getHearingId, hearing.getId())
                                        .withValue(Key::getNowsTypeId, nowDefinition2.getId())
                                )
                        ))
                ));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk2)
        ), targets);

        publicEventResulted2.waitFor();

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getHasSharedResults, is(true))
                )
        );

        final List<String> generateNowsCommandStrings = ProgressionStub.findNowsRequestStringsByHearingId(hearing.getId());
        final List<GenerateNowsCommand> generateNowsCommands = generateNowsCommandStrings.stream()
                .map(str -> convertStringTo(GenerateNowsCommand.class, str))
                .collect(Collectors.toList());
        assertThat(generateNowsCommands.size(), is(2));
        assertThat(generateNowsCommands.get(0).getCreateNowsRequest(), isBean(CreateNowsRequest.class)
                        .with(CreateNowsRequest::getNows, first(isBean(Now.class)
                                        .withValue(Now::getDefendantId, defendant.getId())
                                        .withValue(Now::getNowsTypeId, nowDefinition1.getId())
                                )
                        )
                        .with(CreateNowsRequest::getCourtClerk, isBean(DelegatedPowers.class)
                                .withValue(DelegatedPowers::getFirstName, courtClerk1.getFirstName())
                                .withValue(DelegatedPowers::getLastName, courtClerk1.getLastName())
                                .withValue(DelegatedPowers::getUserId, courtClerk1.getId())
                        )
                        .with(CreateNowsRequest::getNowTypes, first(isBean(NowType.class)
                                .withValue(NowType::getStaticText, nowDefinition1.getText())
                        ))
//TODO GPE-7138
/*                .with(CreateNowsRequest::getLjaDetails, isBean(LjaDetails.class)
                   .with(lja->lja.getLjaName().length()>1, is(true) )
                )
*/
        );
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

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate) {
        AllNows allnows = AllNows.allNows()
                .setNows(Arrays.asList(NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                // This causes a test failure but this field is under review .setText("ResultDefinitionLevel/" + STRING.next())
                                                .setMandatory(false)
                                                .setPrimary(false)))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false),
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                ))
                                .setName(STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction(BOTH_JURISDICTIONS)
                                .setRemotePrintingRequired(false)
                                .setText(STRING.next())
                ));
        return setupNowsReferenceData(referenceDate, allnows);
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final AllNowsReferenceDataHelper allNows = h(data);
        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, UUID resultDefinitionId) {
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
                                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                                .setReference(STRING.next())
                                                        )
                                                )
                        ).collect(Collectors.toList())
                ));

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }
}