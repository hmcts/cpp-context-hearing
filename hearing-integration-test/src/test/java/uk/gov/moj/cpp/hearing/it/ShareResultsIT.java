package uk.gov.moj.cpp.hearing.it;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.ShareResultsCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdatePleaCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdateVerdictCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;
import uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType;

import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
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

        InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate().build())
        );

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

        InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate().build())
        );

        final UpdatePleaCommandHelper pleaOne = new UpdatePleaCommandHelper(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceIdForFirstDefendant(),
                        updatePleaTemplate(hearingOne.getFirstOffenceIdForFirstDefendant(), TestTemplates.PleaValueType.NOT_GUILTY).build())
        );

        final UpdateVerdictCommandHelper verdictTwo = new UpdateVerdictCommandHelper(
                UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                        updateVerdictTemplate(
                                hearingOne.getFirstCaseId(),
                                hearingOne.getFirstDefendantId(),
                                hearingOne.getFirstOffenceIdForFirstDefendant(),
                                VerdictCategoryType.GUILTY
                        ).build())
        );

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

        final ShareResultsCommandHelper shareResultsOne = new ShareResultsCommandHelper(
                UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                        standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId()),
                        command -> {
                            command.getCompletedResultLines().get(0).setResultDefinitionId(defaultReferenceDataUUID);
                        })
                )
        );

        publicEventResulted.waitFor();
    }
}
