package uk.gov.moj.cpp.hearing.it;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.waitAtMost;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubStagingenforcementCourtRoomsOutstandingFines;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.OutstandingFinesQuery;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class DefendantOutstandingFinesByCourtRoomIT extends AbstractIT {

    @BeforeEach
    public void setUp() {
        setUpPerTest();
        stubStagingenforcementCourtRoomsOutstandingFines();
    }

    @Test
    public void shouldPostComputeOutstandingFines() throws Exception {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final CourtCentre courtCentre = initiateHearingCommand.getHearing().getCourtCentre();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        final CommandHelpers.InitiateHearingCommandHelper initiate = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        final UUID correlationId = randomUUID();
        getRequestSpec().header(HeaderConstants.CLIENT_CORRELATION_ID, correlationId);
        makeCommand(getRequestSpec(), "hearing.compute-outstanding-fines")
                .ofType("application/vnd.hearing.compute-outstanding-fines+json")
                .withPayload(
                        OutstandingFinesQuery.newBuilder()
                                .withCourtCentreId(courtCentre.getId())
                                .withCourtRoomIds(Arrays.asList(courtCentre.getRoomId()))
                                .withHearingDate(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate())
                                .build()
                )
                .executeSuccessfully();
        final Person personDetails = initiate.getFirstDefendantForFirstCase().getPersonDefendant().getPersonDetails();

        waitAtMost(Duration.ofSeconds(30))
                .untilAsserted(() -> verifyStagingenforcementCourtRoomsOutstandingFines(Arrays.asList(
                        personDetails.getFirstName(),
                        personDetails.getLastName(),
                        personDetails.getNationalInsuranceNumber()
                        ))
                );

    }

    public static void verifyStagingenforcementCourtRoomsOutstandingFines(final List<String> expectedValues) {

        final RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathMatching("/stagingenforcement-service/command/api/rest/stagingenforcement/court/rooms/outstanding-fines"));
        expectedValues.forEach(
                expectedValue -> requestPatternBuilder.withRequestBody(containing(expectedValue))
        );
        verify(requestPatternBuilder);
    }



}