package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    @Test
    public void shouldRaiseDraftResultSaved() throws Exception {

        final SaveDraftResultCommand saveDraftResultCommand = TestTemplates.saveDraftResultCommandTemplateWithHearingId(UseCases.initiateHearing(requestSpec, asDefault()));

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
    public void shouldRaiseResultsSharedEvent() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearing(requestSpec, asDefault());

        UseCases.updatePlea(requestSpec, initiateHearingCommand, hearingUpdatePleaCommand -> {
            Plea.Builder plea = hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getPlea();
            plea.withValue("NOT_GUILTY");
        });

        UseCases.updateVerdict(requestSpec, initiateHearingCommand, hearingUpdateVerdictCommand -> {
            VerdictValue.Builder verdictValue = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue();
            verdictValue.withCategory("GUILTY");
        });

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(isJson(allOf(
                        withJsonPath("$._metadata.name", is("public.hearing.resulted")),
                        withJsonPath("$._metadata.context.user", is(USER_ID_VALUE.toString())),
                        withJsonPath("$.hearing.id", is(initiateHearingCommand.getHearing().getId().toString())),
                        withJsonPath("$.hearing.defendants[0].id", is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].id", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getCaseId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].bailStatus", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getBailStatus())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].id", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.verdictCategory", is("GUILTY")),
                        withJsonPath("$.hearing.defendants[0].cases[0].offences[0].plea.value", is("NOT_GUILTY"))
                )));

        final ShareResultsCommand shareResultsCommand = with(basicShareResultsCommandTemplate(initiateHearingCommand), template -> {
            template.getResultLines().forEach(rl -> rl.setPersonId(initiateHearingCommand.getHearing().getDefendants().get(0).getPersonId()));
        });

        makeCommand(requestSpec, "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(shareResultsCommand)
                .executeSuccessfully();

        publicEventResulted.waitFor();
    }
}