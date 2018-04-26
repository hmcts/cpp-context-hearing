package uk.gov.moj.cpp.hearing.it;

import org.junit.Ignore;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;

import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

public class ShareResultsIT extends AbstractIT {

    @Ignore("GPE-3392 wip")
    @Test
    public void publishResults() throws Exception {

        InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearing(requestSpec, asDefault());

        UseCases.updatePlea(requestSpec, initiateHearingCommand,  hearingUpdatePleaCommand -> {
            Plea.Builder plea = hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getPlea();
                    plea.withValue("NOT_GUILTY");
        });

        UseCases.updateVerdict(requestSpec, initiateHearingCommand, hearingUpdateVerdictCommand -> {
            VerdictValue.Builder verdictValue = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue();
            verdictValue.withCategory("GUILTY");
        });

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        //TODO - use POJO.
        makeCommand(requestSpec, "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload("{\n" +
                        "  \"resultLines\": [\n" +
                        "    {\n" +
                        "      \"id\": \"040a316f-a10b-44ee-aa8a-40722e2dda0e\",\n" +
                        "      \"personId\": \"ebd6d7ba-d040-4fe1-94bf-b300946f2391\",\n" +
                        "      \"caseId\": \"ab746921-d839-4867-bcf9-b41db8ebc852\",\n" +
                        "      \"offenceId\": \"25d8cbfa-8dd0-494f-82f5-e58df2e53a33\",\n" +
                        "      \"level\": \"OFFENCE\",\n" +
                        "      \"court\": \"aCourt\",\n" +
                        "      \"courtRoom\": \"courtRoom\",\n" +
                        "      \"clerkOfTheCourtId\": \"ab746921-d839-4867-bcf9-b41db8ebc852\",\n" +
                        "      \"clerkOfTheCourtFirstName\": \"David\",\n" +
                        "      \"clerkOfTheCourtLastName\": \"Walliams\",\n" +
                        "      \"resultLabel\": \"Imprisonment\",\n" +
                        "      \"prompts\": [\n" +
                        "        {\n" +
                        "          \"label\": \"Imprisonment duration\",\n" +
                        "          \"value\": \"1 year 6 months\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"label\": \"Prison\",\n" +
                        "          \"value\": \"Wormwood Scrubs\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": \"040a316f-a10b-44ee-aa8a-40722e2dda0a\",\n" +
                        "      \"lastSharedResultId\": \"17c02158-4322-4dd0-b913-0ada7fcf81b6\",\n" +
                        "      \"personId\": \"ebd6d7ba-d040-4fe1-94bf-b300946f2391\",\n" +
                        "      \"caseId\": \"ab746921-d839-4867-bcf9-b41db8ebc852\",\n" +
                        "      \"offenceId\": \"25d8cbfa-8dd0-494f-82f5-e58df2e53a33\",\n" +
                        "      \"level\": \"CASE\",\n" +
                        "      \"resultLabel\": \"Victim Surchange\",\n" +
                        "      \"court\": \"aCourt\",\n" +
                        "      \"courtRoom\": \"courtRoom\",\n" +
                        "      \"clerkOfTheCourtId\": \"ab746921-d839-4867-bcf9-b41db8ebc852\",\n" +
                        "      \"clerkOfTheCourtFirstName\": \"David\",\n" +
                        "      \"clerkOfTheCourtLastName\": \"Walliams\",\n" +
                        "      \"prompts\": [\n" +
                        "        {\n" +
                        "          \"label\": \"Amount\",\n" +
                        "          \"value\": \"£60\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .executeSuccessfully();


        //TODO - complete assertions. Need POJO used above before its worth doing this.
    }
}
