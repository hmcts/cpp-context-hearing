package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.shareResultsCommandTemplate;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;


@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    @Test
    public void publishResults() throws Exception {

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

        makeCommand(requestSpec, "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(shareResultsCommandTemplate(initiateHearingCommand))
                .executeSuccessfully();

        publicEventResulted.waitFor();
    }
}