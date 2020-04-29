package uk.gov.moj.cpp.hearing.it;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.changeCourtRoomsStubWithAdding;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubStagingenforcementOutstandingFines;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Person;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.AddRequestForOutstandingFines;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.jayway.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AddRequestForOutstandingFinesIT extends AbstractIT {
    //todo based on jndi, we need to change

    public static void verifyStagingenforcementRequestOutstandingFines(final List<String> expectedValues) {

        final RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathMatching("/stagingenforcement-service/command/api/rest/stagingenforcement/outstanding-fines"));
        expectedValues.forEach(
                expectedValue -> requestPatternBuilder.withRequestBody(containing(expectedValue))
        );
        verify(requestPatternBuilder);
    }

    @Before
    public void setUp() {
        setUpPerTest();
        stubStagingenforcementOutstandingFines();
    }

    @Test
    public void shouldPostComputeOutstandingFines() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        final CommandHelpers.InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        stubCourtRooms(hearingCommandHelper);


        makeCommand(getRequestSpec(), "hearing.add-request-for-outstanding-fines")
                .ofType("application/vnd.hearing.add-request-for-outstanding-fines+json")
                .withCppUserId(getLoggedInAdminUser())
                .withPayload(
                        AddRequestForOutstandingFines.newBuilder()
                                .withHearingDate(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate())
                                .build()
                )
                .executeSuccessfully();
        final Person personDetails = hearingCommandHelper.getFirstDefendantForFirstCase().getPersonDefendant().getPersonDetails();

        waitAtMost(new Duration(30, TimeUnit.SECONDS))
                .until(() -> verifyStagingenforcementRequestOutstandingFines(Arrays.asList(
                        personDetails.getFirstName(),
                        personDetails.getLastName(),
                        personDetails.getNationalInsuranceNumber(),
                        "AAAAAA01",
                        LocalDate.now().plusDays(1).toString()
                        ))
                );

    }

    public void stubCourtRooms(final CommandHelpers.InitiateHearingCommandHelper commandHelper) {
        changeCourtRoomsStubWithAdding(createObjectBuilder()
                .add("id", commandHelper.getHearing().getCourtCentre().getId().toString())
                .add("oucode", "AAAAAA01")
                .add("courtrooms", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("id", commandHelper.getHearing().getCourtCentre().getRoomId().toString())
                                .build())
                        .add(createObjectBuilder()
                                .add("id", "0c329efc-0c9a-4057-b119-e45147b82591")
                                .build())
                        .build())
                .build());
    }

}