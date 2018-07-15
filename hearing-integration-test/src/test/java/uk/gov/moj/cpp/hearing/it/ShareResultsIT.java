package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.resultPromptTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    @Before
    public void begin() {
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    @Test
    public void shouldRaiseDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final SaveDraftResultCommand saveDraftResultCommand = with(saveDraftResultCommandTemplate(hearingOne.it()),
                template -> template.setHearingId(hearingOne.getHearingId()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(isJson(allOf(
                        withJsonPath("$._metadata.name", is("public.hearing.draft-result-saved")),
                        withJsonPath("$._metadata.context.user", is(USER_ID_VALUE.toString())),
                        withJsonPath("$.hearingId", is(saveDraftResultCommand.getHearingId().toString())),
                        withJsonPath("$.defendantId", is(saveDraftResultCommand.getDefendantId().toString())),
                        withJsonPath("$.targetId", is(saveDraftResultCommand.getTargetId().toString())),
                        withJsonPath("$.offenceId", is(saveDraftResultCommand.getOffenceId().toString())),
                        withJsonPath("$.draftResult", is(saveDraftResultCommand.getDraftResult()))
                )));

        makeCommand(requestSpec, "hearing.save-draft-result")
                .ofType("application/vnd.hearing.save-draft-result+json")
                .withArgs(saveDraftResultCommand.getHearingId())
                .withPayload(saveDraftResultCommand)
                .executeSuccessfully();

        publicEventResulted.waitFor();
    }

    @Test
    public void shareResults_shouldPersistNows() {

        LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final UUID resultLineId1 = randomUUID();
        final UUID resultLineId2 = randomUUID();
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLineId1, resultLineId2, orderedDate),
                command -> {

                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());

                    command.getCompletedResultLines().get(0).getPrompts().set(0, ResultPrompt.builder()
                            .withId(allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getId())
                            .withLabel(allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getLabel())
                            .withValue(command.getCompletedResultLines().get(0).getPrompts().get(0).getValue())
                            .build());


                    command.getCompletedResultLines().forEach(rl -> rl.setDefendantId(hearingOne.getFirstDefendantId()));

                }));

        poll(requestParams(getURL("hearing.get.nows", hearingOne.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].defendantId", Is.is(hearingOne.getFirstDefendant().getId().toString()))
                        )));

    }

    @Test
    public void shareResults_shouldPublishResults_andVariantsShouldBePersistedAndCollatedForShareResults() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.hearing.id", is(hearingOne.getHearingId().toString())),
                        withJsonPath("$.hearing.defendants[0].id", is(hearingOne.getFirstDefendantId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].id", is(hearingOne.getFirstCaseId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].bailStatus", is(hearingOne.it().getHearing().getDefendants().get(0).getDefendantCases().get(0).getBailStatus())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.hearing.sharedResultLines[0].orderedDate", is(orderedDate.toString())),
                        withJsonPath("$.hearing.sharedResultLines[1].orderedDate", is(orderedDate.toString())),
                        withJsonPath("$.variants[0].templateName", is(allNows.getFirstNowDefinition().getTemplateName())),
                        withJsonPath("$.variants[0].description", is(allNows.getFirstNowDefinition().getName())),
                        withJsonPath("$.variants[0].status", is("BUILDING"))
                )));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), randomUUID(), randomUUID(), orderedDate),
                command -> {
                    CommandHelpers.ShareResultsCommandHelper c = h(command);

                    c.getFirstCompletedResultLine().setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());

                    c.getFirstCompletedResultLine().setPrompts(singletonList(
                            resultPromptTemplate(
                                    allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getId(),
                                    allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getLabel(),
                                    STRING.next()
                            )
                    ));

                    c.getFirstCompletedResultLine().setDefendantId(hearingOne.getFirstDefendantId());
                    c.getSecondCompletedResultLine().setDefendantId(hearingOne.getFirstDefendantId());
                }));

        publicEventResulted.waitFor();

        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows2 = setupNowsReferenceData(orderedDate2);

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions2 = setupResultDefinitionsReferenceData(orderedDate2, allNows2.getFirstNowDefinitionFirstResultDefinitionId());

        final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.hearing.id", is(hearingOne.getHearingId().toString())),
                        withJsonPath("$.hearing.defendants[0].id", is(hearingOne.getFirstDefendantId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].id", is(hearingOne.getFirstCaseId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].bailStatus", is(hearingOne.it().getHearing().getDefendants().get(0).getDefendantCases().get(0).getBailStatus())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.hearing.sharedResultLines[0].orderedDate", is(orderedDate2.toString())),
                        withJsonPath("$.hearing.sharedResultLines[1].orderedDate", is(orderedDate2.toString())),
                        withJsonPath("$.variants[0].templateName", is(allNows.getFirstNowDefinition().getTemplateName())),
                        withJsonPath("$.variants[0].description", is(allNows.getFirstNowDefinition().getName())),
                        withJsonPath("$.variants[0].status", is("BUILDING")),
                        withJsonPath("$.variants[1].templateName", is(allNows2.getFirstNowDefinition().getTemplateName())),
                        withJsonPath("$.variants[1].description", is(allNows2.getFirstNowDefinition().getName())),
                        withJsonPath("$.variants[1].status", is("BUILDING"))
                )));

        ShareResultsCommand command2 = UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), randomUUID(), randomUUID(), orderedDate2),
                command -> {
                    CommandHelpers.ShareResultsCommandHelper c = h(command);

                    c.getFirstCompletedResultLine().setPrompts(singletonList(
                            resultPromptTemplate(
                                    allResultDefinitions2.getFirstResultDefinitionFirstResultPrompt().getId(),
                                    allResultDefinitions2.getFirstResultDefinitionFirstResultPrompt().getLabel(),
                                    STRING.next()
                            )
                    ));

                    c.getFirstCompletedResultLine().setResultDefinitionId(allNows2.getFirstNowDefinitionFirstResultDefinitionId());

                    c.getFirstCompletedResultLine().setDefendantId(hearingOne.getFirstDefendantId());
                    c.getSecondCompletedResultLine().setDefendantId(hearingOne.getFirstDefendantId());
                }));

        publicEventResulted2.waitFor();
    }

    @Test
    public void shareResults_shouldSurfaceResultsLinesInGetHearings_resultLinesShouldBeAsLastSubmittedOnly() {

        LocalDate orderedDate = PAST_LOCAL_DATE.next();

        UUID resultLine1 = randomUUID(), resultLine2 = randomUUID(), resultLine3 = randomUUID();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLine1, resultLine2, orderedDate),
                command -> {
                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());
                }));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLine2, resultLine3, orderedDate),
                command -> {
                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());
                }));

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
                                .setPrompts(singletonList(Prompt.prompt()
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
