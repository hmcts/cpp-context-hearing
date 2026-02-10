package uk.gov.moj.cpp.hearing.it;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubStagingenforcementCourtRoomsOutstandingFines;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.OutstandingFinesQuery;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class DefendantOutstandingFinesByCourtRoomIT extends AbstractIT {

    final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    @BeforeEach
    public void setUp() {
        setUpPerTest();
        stubStagingenforcementCourtRoomsOutstandingFines();
    }

    @Test
    public void shouldPostComputeOutstandingFines() throws JsonProcessingException {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final CourtCentre courtCentre = initiateHearingCommand.getHearing().getCourtCentre();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final OutstandingFinesQuery payload = OutstandingFinesQuery.newBuilder()
                .withCourtCentreId(courtCentre.getId())
                .withCourtRoomIds(of(courtCentre.getRoomId()))
                .withHearingDate(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate())
                .build();

        try (Response response = query(getBaseUri() + "/hearing-query-api/query/api/rest/hearing/outstanding-fines",
                "application/vnd.hearing.query.outstanding-fines+json",
                Utilities.JsonUtil.toJsonString(payload),
                headers())) {

            final JsonPath jsonPath =  new JsonPath(response.readEntity(String.class));

            assertThat(jsonPath.getString("courtRooms[0].courtRoomName"), is("room1"));
            assertThat(jsonPath.getString("courtRooms[0].outstandingFines[0].defendantName"), is("Abbie ARMSTRONG"));
        }
    }

    private static MultivaluedMap<String, Object> headers() {
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(USER_ID, UUID.randomUUID());
        return headers;
    }

    public Response query(final String url, final String contentType, final String requestPayload, final MultivaluedMap<String, Object> headers) {
        Entity<String> entity = Entity.entity(requestPayload, MediaType.valueOf(contentType));
        return ResteasyClientBuilderFactory.clientBuilder().build().target(url).request().headers(headers).post(entity);
    }


}