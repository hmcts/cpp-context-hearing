package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.Prompt;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedHearing;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedVariant;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    public static final String DOCUMENT_TEXT = "someDocumentText";

    @Before
    public void begin() {
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    @Test
    public void shouldRaiseDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final uk.gov.justice.json.schemas.core.Hearing hearing = hearingOne.getHearing();

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), LocalDate.now());

        testSaveDraftResult(saveDraftResultCommand, hearing);

        saveDraftResultCommand.getTarget().setDraftResult("draft result version 2");

        testSaveDraftResult(saveDraftResultCommand, hearing);
    }


    private void testSaveDraftResult(final SaveDraftResultCommand saveDraftResultCommand, final uk.gov.justice.json.schemas.core.Hearing hearing) {
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

        final Target target = saveDraftResultCommand.getTarget();
        final ResultLine resultLine = target.getResultLines().get(0);
        final Prompt prompt = resultLine.getPrompts().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), 30,
                isBean(HearingDetailsResponse.class)
                        .with(HearingDetailsResponse::getHearing, isBean(uk.gov.justice.json.schemas.core.Hearing.class)
                                .with(uk.gov.justice.json.schemas.core.Hearing::getId, is(hearing.getId()))
                                .with(uk.gov.justice.json.schemas.core.Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getId, is(hearing.getType().getId())))
                                .with(h->h.getTargets().size(), is(1))
                                .with(uk.gov.justice.json.schemas.core.Hearing::getTargets, first(isBean(Target.class)
                                        .with(uk.gov.justice.json.schemas.core.Target::getDefendantId, is(target.getDefendantId()))
                                        .with(uk.gov.justice.json.schemas.core.Target::getDraftResult, is(target.getDraftResult()))
                                        .with(uk.gov.justice.json.schemas.core.Target::getHearingId, is(target.getHearingId()))
                                        .with(uk.gov.justice.json.schemas.core.Target::getOffenceId, is(target.getOffenceId()))
                                        .with(uk.gov.justice.json.schemas.core.Target::getResultLines, first(isBean(ResultLine.class)
                                                .with(uk.gov.justice.json.schemas.core.ResultLine::getIsComplete, is(resultLine.getIsComplete()))
                                                .with(uk.gov.justice.json.schemas.core.ResultLine::getLevel, is(resultLine.getLevel()))
                                                .with(uk.gov.justice.json.schemas.core.ResultLine::getResultLabel, is(resultLine.getResultLabel()))
                                                .with(uk.gov.justice.json.schemas.core.ResultLine::getResultDefinitionId, is(resultLine.getResultDefinitionId()))
                                                .with(uk.gov.justice.json.schemas.core.ResultLine::getPrompts, first(isBean(Prompt.class)
                                                        .with(uk.gov.justice.json.schemas.core.Prompt::getId, is(prompt.getId()))
                                                ))
                                        ))
                                ))
                        ));

    }


    @Ignore("GPE-5480 - share results story will address this")
    @Test
    public void shareResults_shouldPersistNows() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        //TODO put this ordered data into save draft results command
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final UUID resultLineId1 = randomUUID();
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                //TODO GPE-5480 non mire compled results lines - need to be added with save draft result becfore sharing
                command -> {}
        ));

        poll(requestParams(getURL("hearing.get.nows", hearingOne.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
//                                withJsonPath("$.nows[0].defendantId", Is.is(hearingOne.getFirstDefendant().getId().toString()))
                        )));

    }

    @Test
    public void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines() {
        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        //TODO put this ordered data into save draft results command
        //the resultDefinitionIds must correspond to those in the noews reference data stub

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingOne.it(), orderedDate, allNows.it());

        final uk.gov.justice.json.schemas.core.Hearing hearing = hearingOne.getHearing();

        testSaveDraftResult(saveDraftResultCommand, hearing);

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        final JudicialRole judicialRole = hearing.getJudiciary().get(0);
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        //final DefenceCounsel defenceCounsel0 = hearing.getDefenceCounsels().get(0);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted", 30000)
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(SharedHearing.class)
                                .with(SharedHearing::getId, is(hearingOne.getHearingId()))
                                .with(SharedHearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                        .with(CourtCentre::getName, is(hearing.getCourtCentre().getName()))
                                        .with(CourtCentre::getRoomId, is(hearing.getCourtCentre().getRoomId()))
                                )
                                //.with(sh->sh.getDefenceCounsels().size(),  is(hearing.getDefenceCounsels().size()) )
                                //.with(SharedHearing::getDefenceCounsels, first(isBean(DefenceCounsel.class)
                                //       .with(DefenceCounsel::getId, is(defenceCounsel0))
                                //       ))
                                .with(SharedHearing::getHasSharedResults, is(hearing.getHasSharedResults()))
                                .with(sh->sh.getHearingDays().size(), is(hearing.getHearingDays().size()))
                                .with(SharedHearing::getHearingDays, first(isBean(HearingDay.class)
                                       .with(hd->hd.getSittingDay().toLocalDate(), is(hearingDay.getSittingDay().toLocalDate()))
                                       .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                       .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))
                                ))
                                .with(SharedHearing::getHearingLanguage, is(hearing.getHearingLanguage()))
                                .with(sh->sh.getJudiciary().size(), is(hearing.getJudiciary().size()))
                                .with(SharedHearing::getJudiciary, first(isBean(JudicialRole.class)
                                         .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                        .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                        .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                ))
                                .with(sh->sh.getJurisdictionType().name(), is(hearing.getJurisdictionType().name() ))
                                .with(sh->sh.getProsecutionCases().size(), is(hearing.getProsecutionCases().size()))
                                .with(SharedHearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                       .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                                       .with(ProsecutionCase::getCaseStatus, is(prosecutionCase.getCaseStatus()))
                                       //TODO more prosecution case fields
                                        .with(pc->pc.getDefendants().size(), is(prosecutionCase.getDefendants().size()))
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                             .with(Defendant::getId, is(defendant.getId()))
                                                .with(Defendant::getMitigation, is(defendant.getMitigation()))
                                                //TODO more defendant fields and sub objects
                                        ))
                                ))


                                .with(SharedHearing::getSharedResultLines, first(isBean(uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine.class)
                                        .with(uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine::getOrderedDate, is(orderedDate))
                                ))
                        )
                        .with(PublicHearingResulted::getVariants, first(isBean(SharedVariant.class)
                                .with(SharedVariant::getTemplateName, is(allNows.getFirstNowDefinition().getTemplateName()))
                                .with(SharedVariant::getDescription, is(allNows.getFirstNowDefinition().getName()))
                                .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                        ))
                ));

        //TODO GPE-5480 rationalise this
        ShareResultsCommand shareResultsCommand = UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command ->  {}
        ));

        publicEventResulted.waitFor();

        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows2 = setupNowsReferenceData(orderedDate2);
        final AllResultDefinitionsReferenceDataHelper allResultDefinitions2 = setupResultDefinitionsReferenceData(orderedDate2, allNows2.getFirstNowDefinitionFirstResultDefinitionId());

        final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                                .with(PublicHearingResulted::getHearing, isBean(SharedHearing.class)
                                                .with(SharedHearing::getId, is(hearingOne.getHearingId()))
                                        .with(SharedHearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId())))

        /*defenceCounsels;
        defendantAttendance;
        hasSharedResults;
        hearingDays;
        hearingLanguage;
        judiciary;
        jurisdictionType;
        prosecutionCases;
        prosecutionCounsels;
        reportingRestrictionReason;
        sharedResultLines;
        type;*/
                                                /*.with(SharedHearing::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantId()))
                                                                .with(Defendant::getCases, first(isBean(Case.class)
                                                .with(Case::getId, is(hearingOne.getFirstCaseId()))
                                                                                .with(Case::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                                                                ))
                                                                ))
                                                ))*/
                                                .with(SharedHearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                                        .with(SharedResultLine::getOrderedDate, is(orderedDate))
                                                ))
/*                                                .with(SharedHearing::getSharedResultLines, second(isBean(SharedResultLine.class)
                                                        .with(SharedResultLine::getOrderedDate, is(orderedDate2))
                                                ))*/
                                )
/*                                .with(PublicHearingResulted::getVariants, first(isBean(SharedVariant.class)
                                        .with(SharedVariant::getTemplateName, is(allNows.getFirstNowDefinition().getTemplateName()))
                                        .with(SharedVariant::getDescription, is(allNows.getFirstNowDefinition().getName()))
                                        .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                                ))
                                .with(PublicHearingResulted::getVariants, second(isBean(SharedVariant.class)
                                        .with(SharedVariant::getTemplateName, is(allNows2.getFirstNowDefinition().getTemplateName()))
                                        .with(SharedVariant::getDescription, is(allNows2.getFirstNowDefinition().getName()))
                                        .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                                ))*/
                ));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                //TODO GPE set complete result lines in save draft results not here
                command -> {}

        ));

        publicEventResulted2.waitFor();

        final EventListener publicEventResulted3 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                                .with(PublicHearingResulted::getHearing, isBean(SharedHearing.class)
                                                .with(SharedHearing::getId, is(hearingOne.getHearingId()))
                                                /*.with(Hearing::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantId()))
                                                                .with(Defendant::getCases, first(isBean(Case.class)
                                                .with(Case::getId, is(hearingOne.getFirstCaseId()))
                                                                                .with(Case::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                                                                ))
                                                                ))
                                                ))*/
                                                .with(SharedHearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                                        .with(SharedResultLine::getOrderedDate, is(orderedDate))
                                                ))
                                )
/*                                .with(PublicHearingResulted::getVariants, first(isBean(SharedVariant.class)
                                        .with(SharedVariant::getTemplateName, is(allNows.getFirstNowDefinition().getTemplateName()))
                                        .with(SharedVariant::getDescription, is(allNows.getFirstNowDefinition().getName()))
                                        .with(SharedVariant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                                ))*/
                ));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                //TODO GPE-5480 set result lines in share results
                command -> {}
        )
        );

        publicEventResulted3.waitFor();
    }

    @Ignore("GPE-5480 - share results story will address this")
    @Test
    public void shareResults_shouldSurfaceResultsLinesInGetHearings_resultLinesShouldBeAsLastSubmittedOnly() {

        final UUID resultLine1 = randomUUID(), resultLine2 = randomUUID(), resultLine3 = randomUUID();

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        /*UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLine1, resultLine2, orderedDate),
                command -> {
                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());
                }));*/

        /*UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLine2, resultLine3, orderedDate),
                command -> {
                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());
                }));*/

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.resultLines", hasSize(3))//TODO - should only be the last 2 lines that have been shared
                        )));
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(LocalDate referenceDate) {
        final AllNowsReferenceDataHelper allNows = h(AllNows.allNows()
                .setNows(singletonList(NowDefinition.now()
                        .setId(randomUUID())
                        .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(true)
                                .setPrimary(true)
                        ))
                        .setName(STRING.next())
                        .setTemplateName(STRING.next())
                )));

        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, UUID resultDefinitionId) {
        final String LISTING_OFFICER_USERGROUP = "Listing Officer";

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(singletonList(ResultDefinition.resultDefinition()
                                .setId(resultDefinitionId)
                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                        )
                                )
                        )
                ));

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }
}
