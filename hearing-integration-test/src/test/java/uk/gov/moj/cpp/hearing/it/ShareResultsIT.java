package uk.gov.moj.cpp.hearing.it;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.ShareResultsCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdatePleaCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdateVerdictCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;
import uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    @Test
    public void shouldRaiseDraftResultSaved() {

        InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

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
    public void shouldRaiseResultsSharedEvent() {
        final UUID primaryResultDefinitionId = UUID.fromString("87631590-bd78-49b2-bd6f-ad7030904e73");
        final UUID mandatoryPromptId = UUID.randomUUID();
        final String mandatoryPromptLabel = "label1";
        AllNows allNows = AllNows.allNows()
                .setNows(Arrays.asList(
                        NowDefinition.now().setId(UUID.randomUUID())
                                .setResultDefinitions(Arrays.asList(
                                        ResultDefinitions.resultDefinitions()
                                                .setId(primaryResultDefinitionId)
                                                .setMandatory(true)
                                                .setPrimaryResult(true)
                                ))
                ));

        ReferenceDataStub.stubGetAllNowsMetaData(allNows);

        final String userGroup1 = "DefenseCounsel";
        AllResultDefinitions allResultDefinitions = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
                Arrays.asList(ResultDefinition.resultDefinition()
                        .setId(primaryResultDefinitionId)
                        .setUserGroups(Arrays.asList(userGroup1))
                        .setPrompts(
                                Arrays.asList(
                                        Prompt.prompt().setId(mandatoryPromptId)
                                                .setMandatory(true)
                                                .setLabel("label1")
                                                .setUserGroups(Arrays.asList(userGroup1))
                                )
                        )
                )
        );

        ReferenceDataStub.stubGetAllResultDefinitions(allResultDefinitions);

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final UpdatePleaCommandHelper pleaOne = h(UseCases.updatePlea(requestSpec, hearingOne.getHearingId(),
                hearingOne.getFirstOffenceIdForFirstDefendant(),
                updatePleaTemplate(hearingOne.getFirstOffenceIdForFirstDefendant(), TestTemplates.PleaValueType.NOT_GUILTY).build())
        );

        final UpdateVerdictCommandHelper verdictTwo = h(UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                updateVerdictTemplate(
                        hearingOne.getFirstCaseId(),
                        hearingOne.getFirstDefendantId(),
                        hearingOne.getFirstOffenceIdForFirstDefendant(),
                        VerdictCategoryType.GUILTY
                ).build()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.hearing.id", is(hearingOne.getHearingId().toString())),
                        withJsonPath("$.hearing.defendants[0].id", is(hearingOne.getFirstDefendantId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].id", is(hearingOne.getFirstCaseId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].bailStatus", is(hearingOne.it().getHearing().getDefendants().get(0).getDefendantCases().get(0).getBailStatus())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.verdictCategory", is(verdictTwo.getFirstVerdictCategory())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].plea.value", is(pleaOne.getFirstPleaValue().name()))
                )));

        // this will change when real reference data service is available and can be stubbed out
        // this matches DefaultNowsReferenceData
        final UUID defaultReferenceDataUUID = UUID.fromString("87631590-bd78-49b2-bd6f-ad7030904e73");
        final UUID resultLineId1 = UUID.randomUUID();
        final UUID resultLineId2 = UUID.randomUUID();
        final ShareResultsCommandHelper shareResultsOne = new ShareResultsCommandHelper(
                UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                        standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLineId1, resultLineId2),
                        command -> {
                            command.getCompletedResultLines().get(0).setResultDefinitionId(defaultReferenceDataUUID);
                            command.getCompletedResultLines().get(0).setResultDefinitionId(primaryResultDefinitionId);
                            ResultPrompt original = command.getCompletedResultLines().get(0).getPrompts().get(0);
                            command.getCompletedResultLines().get(0).getPrompts().set(0, ResultPrompt.builder().withId(mandatoryPromptId).withLabel(mandatoryPromptLabel).withValue(original.getLabel()).build());
                            command.getCompletedResultLines().forEach(rl -> rl.setDefendantId(hearingOne.getFirstDefendantId()));

                        })
                )
        );


        publicEventResulted.waitFor();

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingOne.getHearingId()),
                "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.resultLines", hasSize(2))
                        )));

        final ShareResultsCommandHelper reShareResultsOne = new ShareResultsCommandHelper(
                UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                        standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLineId1, UUID.randomUUID()),
                        command -> {
                            command.getCompletedResultLines().get(0).setResultDefinitionId(defaultReferenceDataUUID);
                            command.getCompletedResultLines().get(0).setResultDefinitionId(primaryResultDefinitionId);
                            ResultPrompt original = command.getCompletedResultLines().get(0).getPrompts().get(0);
                            command.getCompletedResultLines().get(0).getPrompts().set(0, ResultPrompt.builder().withId(mandatoryPromptId).withLabel(mandatoryPromptLabel).withValue(original.getLabel()).build());
                            command.getCompletedResultLines().forEach(rl -> rl.setDefendantId(hearingOne.getFirstDefendantId()));

                        })
                )
        );
        publicEventResulted.waitFor();

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingOne.getHearingId()),
                "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.resultLines", hasSize(3))
                        )));
    }


}
