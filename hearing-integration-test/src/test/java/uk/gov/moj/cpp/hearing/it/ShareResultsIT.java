package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
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

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.Prompt;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.publichearingresulted.Key;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedHearing;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedVariant;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
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
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    public static final String DOCUMENT_TEXT = "someDocumentText";

    @Before
    public void begin() {
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    @Test
    public void testEmptyDraftResultWhenNoDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final uk.gov.justice.json.schemas.core.Hearing hearing = hearingOne.getHearing();

        Queries.getDraftResultsPollForMatch(hearing.getId(), 30, isBean(TargetListResponse.class)
                .with(response -> response.getTargets(), is(empty())));
    }


    private BeanMatcher<Target> matcher(final Target target) {
        ResultLine resultLine = target.getResultLines().get(0);
        Prompt prompt = resultLine.getPrompts().get(0);
        return isBean(Target.class)
                .with(Target::getDefendantId, is(target.getDefendantId()))
                .with(Target::getDraftResult, is(target.getDraftResult()))
                .with(Target::getHearingId, is(target.getHearingId()))
                .with(Target::getOffenceId, is(target.getOffenceId()))
                .withValue(t -> t.getResultLines().size(), target.getResultLines().size())
                .with(Target::getResultLines, hasItem(isBean(ResultLine.class)
                        .with(ResultLine::getIsComplete, is(resultLine.getIsComplete()))
                        .with(ResultLine::getLevel, is(resultLine.getLevel()))
                        .with(ResultLine::getResultLabel, is(resultLine.getResultLabel()))
                        .with(ResultLine::getResultDefinitionId, is(resultLine.getResultDefinitionId()))
                        .with(ResultLine::getPrompts, first(isBean(Prompt.class)
                                .with(uk.gov.justice.json.schemas.core.Prompt::getId, is(prompt.getId()))
                        ))
                ));
    }

    private void checkHearingQueryTargets(final uk.gov.justice.json.schemas.core.Hearing hearing, final List<Target> targets) {

        final BeanMatcher<Hearing> hearingBeanMatcher = isBean(Hearing.class)
                .with(Hearing::getId, is(hearing.getId()))
                .with(Hearing::getType, isBean(HearingType.class)
                        .with(HearingType::getId, is(hearing.getType().getId())))
                .with(h -> h.getTargets().size(), is(targets.size()));


        for (int index = 0; index < targets.size(); index++) {
            hearingBeanMatcher.with(Hearing::getTargets, hasItem(matcher(targets.get(index))));
        }

        Queries.getHearingPollForMatch(hearing.getId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, hearingBeanMatcher
                        ));
    }

    private void testSaveDraftResult(final SaveDraftResultCommand saveDraftResultCommand, final uk.gov.justice.json.schemas.core.Hearing hearing, final List<Target> previousTargets) {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final BeanMatcher beanMatcher = BeanMatcher.isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getTargetId, is(saveDraftResultCommand.getTarget().getTargetId()))
                .with(PublicHearingDraftResultSaved::getHearingId, is(saveDraftResultCommand.getTarget().getHearingId()))
                .with(PublicHearingDraftResultSaved::getDefendantId, is(saveDraftResultCommand.getTarget().getDefendantId()))
                .with(PublicHearingDraftResultSaved::getDraftResult, is(saveDraftResultCommand.getTarget().getDraftResult()))
                .with(PublicHearingDraftResultSaved::getOffenceId, is(saveDraftResultCommand.getTarget().getOffenceId()));

        final String expectedMetaDataContextUser = USER_ID_VALUE.toString();
        final String expectedMetaDataName = "public.hearing.draft-result-saved";
        final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(beanMatcher, expectedMetaDataName, expectedMetaDataContextUser);

        makeCommand(requestSpec, "hearing.save-draft-result")
                .ofType("application/vnd.hearing.save-draft-result+json")
                .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                .withPayload(saveDraftResultCommand)
                .executeSuccessfully();

        publicEventResulted.waitFor();

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

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate);

        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(
                singletonList(Prompt.prompt()
                        .withLabel(now1MandatoryResultDefinitionPrompt.getLabel())
                        .withFixedListCode("fixedListCode")
                        .withValue("value1")
                        .withId(UUID.randomUUID())
                        .build())
        );

        final ResultLine resultLine1 = saveDraftResultCommand.getTarget().getResultLines().get(0);
        resultLine1.setResultLineId(UUID.randomUUID());
        resultLine1.setResultDefinitionId(allNows.getFirstPrimaryResultDefinitionId());
        resultLine1.setOrderedDate(orderedDate);

        final uk.gov.justice.json.schemas.core.Hearing hearing = hearingOne.getHearing();

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

        final EventListener publicEventResulted = listenFor("public.hearing.resulted", 30000)
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(SharedHearing.class)
                                .with(SharedHearing::getId, is(hearingOne.getHearingId()))
                                .with(SharedHearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                        .with(CourtCentre::getName, is(hearing.getCourtCentre().getName()))
                                        .with(CourtCentre::getRoomId, is(hearing.getCourtCentre().getRoomId()))
                                )
                                .with(SharedHearing::getHasSharedResults, is(hearing.getHasSharedResults()))
                                .with(sh -> sh.getHearingDays().size(), is(hearing.getHearingDays().size()))
                                .with(SharedHearing::getHearingDays, first(isBean(HearingDay.class)
                                        .with(hd -> hd.getSittingDay().toLocalDate(), is(hearingDay.getSittingDay().toLocalDate()))
                                        .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                        .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))
                                ))
                                .with(SharedHearing::getHearingLanguage, is(hearing.getHearingLanguage()))
                                .with(sh -> sh.getJudiciary().size(), is(hearing.getJudiciary().size()))
                                .with(SharedHearing::getJudiciary, first(isBean(JudicialRole.class)
                                        .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                        .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                ))
                                .with(sh -> sh.getJurisdictionType().name(), is(hearing.getJurisdictionType().name()))
                                .with(sh -> sh.getProsecutionCases().size(), is(hearing.getProsecutionCases().size()))
                                .with(SharedHearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                                        .with(ProsecutionCase::getCaseStatus, is(prosecutionCase.getCaseStatus()))
                                        .with(pc -> pc.getDefendants().size(), is(prosecutionCase.getDefendants().size()))
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getId, is(defendant.getId()))
                                                .with(Defendant::getMitigation, is(defendant.getMitigation()))
                                        ))
                                ))

                                .with(SharedHearing::getSharedResultLines, first(isBean(uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine.class)
                                        .with(uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine::getOrderedDate, is(orderedDate))
                                        .with(SharedResultLine::getCourtClerk, isBean(CourtClerk.class)
                                                .with(CourtClerk::getFirstName, is(courtClerk1.getFirstName()))
                                                .with(CourtClerk::getLastName, is(courtClerk1.getLastName()))
                                                .with(CourtClerk::getId, is(courtClerk1.getId()))
                                        )
                                ))
                        )
                        .with(PublicHearingResulted::getVariants, first(isBean(SharedVariant.class)
                                .with(SharedVariant::getTemplateName, is(nowDefinition1.getTemplateName()))
                                .with(SharedVariant::getDescription, is(nowDefinition1.getName()))
                                .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                        ))
                ));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> {
                    command.setCourtClerk(courtClerk1);
                }
        ));

        publicEventResulted.waitFor();


        poll(requestParams(getURL("hearing.get.nows", hearingOne.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].defendantId", is(hearingOne.getFirstDefendantForFirstCase().getId().toString()))
                        )));


        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        //setup reference data for second ordered date
        setupNowsReferenceData(orderedDate2, allNows.it());

        final ResultDefinitions firstNowNonMandatoryResultDefinition = allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> !rd.getMandatory())
                .findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
        final UUID secondNowPrimaryResultDefinitionId = allNows.it().getNows().get(1).getResultDefinitions().stream()
                .filter(rd -> rd.getMandatory())
                .map(ResultDefinitions::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));

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
                        singletonList(Prompt.prompt().withId(UUID.randomUUID()).withValue("val0")
                                .withFixedListCode("fixedList0").withLabel(firstNowNonMandatoryPrompt.getLabel()).build())
                ).build(),
                standardResultLineTemplate(UUID.randomUUID(), secondNowPrimaryResultDefinitionId, orderedDate2).withPrompts(
                        singletonList(Prompt.prompt().withId(UUID.randomUUID()).withValue("val1")
                                .withFixedListCode("fixedList1").withLabel(secondNowPrimaryPrompt.getLabel()).build())
                ).build()
        ));

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
                                        .with(SharedResultLine::getCourtClerk, isBean(CourtClerk.class)
                                                .withValue(CourtClerk::getId, courtClerk1.getId())
                                                .withValue(CourtClerk::getFirstName, courtClerk1.getFirstName())
                                                .withValue(CourtClerk::getLastName, courtClerk1.getLastName())
                                        )

                                ))
                                .with(SharedHearing::getSharedResultLines, hasItem(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate2))
                                        .with(SharedResultLine::getCourtClerk, isBean(CourtClerk.class)
                                                .withValue(CourtClerk::getId, courtClerk2.getId())
                                                .withValue(CourtClerk::getFirstName, courtClerk2.getFirstName())
                                                .withValue(CourtClerk::getLastName, courtClerk2.getLastName())
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
        ));

        publicEventResulted2.waitFor();

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
                                .setResultDefinitions(asList(ResultDefinitions.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        ResultDefinitions.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                ))
                                .setName(STRING.next())
                                .setTemplateName(STRING.next()),
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(ResultDefinitions.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        ResultDefinitions.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                ))
                                .setName(STRING.next())
                                .setTemplateName(STRING.next())

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
                                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                        )
                                                )
                        ).collect(Collectors.toList())
                ));

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }
}