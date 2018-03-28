package uk.gov.moj.cpp.hearing.it;

import com.google.common.io.Resources;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.io.IOException;
import java.text.MessageFormat;

import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

public class DefenceCounselIT extends AbstractIT {


    @Test
    public void hearingAddDefenceCounselTest() throws IOException {

        InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearingMultipleDefendants(requestSpec, 3);
        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        System.out.println("hearingAddDefenceCounselTest hearingId==" + hearingId);

        final String personId1 = randomUUID().toString();
        final String attendeeId1 = randomUUID().toString();
        final String defendantId1 = initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString();
        final String status1 = STRING.next();
        final String firstName1 = "David" + STRING.next();
        final String lastName1 = "Bowie" + STRING.next();
        final String title1 = "Mr" + STRING.next();

        final String defendantId2 = initiateHearingCommand.getHearing().getDefendants().get(1).getId().toString();
        final String status2 = STRING.next();

        final String personId3 = randomUUID().toString();
        final String attendeeId3 = randomUUID().toString();
        final String defendantId3 = initiateHearingCommand.getHearing().getDefendants().get(2).getId().toString();
        final String status3 = STRING.next();
        final String firstName3 = "Richard" + STRING.next();
        final String lastName3 = "Richards" + STRING.next();
        final String title3 = "Colonel" + STRING.next();

        final String addDefenceCounselCommandPayload1 = createAddDefenceCounselCommandPayload(personId1, attendeeId1, status1, defendantId1, firstName1, lastName1, title1);
        final String addDefenceCounselCommandPayload2 = createAddDefenceCounselCommandPayload(personId1, attendeeId1, status2, defendantId2, firstName1, lastName1, title1);
        final String addDefenceCounselCommandPayload3 = createAddDefenceCounselCommandPayload(personId3, attendeeId3, status3, defendantId3, firstName3, lastName3, title3);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload1).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-defence-counsels"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.defence-counsels+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(1)),
                                withJsonPath("$.defence-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.defence-counsels[0].personId", is(personId1)),
                                withJsonPath("$.defence-counsels[0].status", is(status1)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", is(defendantId1)
                                ))));

        // Edit Defence Counsel
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload2).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(1)),
                                withJsonPath("$.defence-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.defence-counsels[0].personId", is(personId1)),
                                withJsonPath("$.defence-counsels[0].status", is(status2)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", is(defendantId2)
                                ))));

        // Add another defence counsel
        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-defence-counsel+json")
                .body(addDefenceCounselCommandPayload3).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.defence-counsels", hasSize(2)),
                                withJsonPath("$.defence-counsels[0].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.defence-counsels[0].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.defence-counsels[0].status", isOneOf(status2, status3)),
                                withJsonPath("$.defence-counsels[0].defendantIds[0].defendantId", isOneOf(defendantId2, defendantId3)),
                                withJsonPath("$.defence-counsels[1].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.defence-counsels[1].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.defence-counsels[1].status", isOneOf(status2, status3)),
                                withJsonPath("$.defence-counsels[1].defendantIds[0].defendantId", isOneOf(defendantId2, defendantId3))
                        )));


        final String queryAPIEndPoint2 = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String url2 = getBaseUri() + "/" + queryAPIEndPoint2;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url2, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.attendees.defenceCounsels[0].status", is(status2)),
                                withJsonPath("$.attendees.defenceCounsels[0].firstName", is(firstName1)),
                                withJsonPath("$.attendees.defenceCounsels[0].lastName", is(lastName1)),
                                withJsonPath("$.attendees.defenceCounsels[0].title", is(title1)),
                                withJsonPath("$.attendees.defenceCounsels[1].attendeeId", is(attendeeId3)),
                                withJsonPath("$.attendees.defenceCounsels[1].status", is(status3)),
                                withJsonPath("$.attendees.defenceCounsels[1].firstName", is(firstName3)),
                                withJsonPath("$.attendees.defenceCounsels[1].lastName", is(lastName3)),
                                withJsonPath("$.attendees.defenceCounsels[1].title", is(title3))
                        )));


    }


    private String createAddDefenceCounselCommandPayload(final String personId, final String attendeeId, final String status,
                                                         final String defendantId,
                                                         final String firstName, final String lastName, final String title) throws IOException {
        String addDefenceCounselPayload = Resources.toString(
                getResource("hearing.command.add-defence-counsel.json"),
                defaultCharset());

        addDefenceCounselPayload = addDefenceCounselPayload.replace("$personId", personId);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$attendeeId", attendeeId);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$status", status);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$defendantId", defendantId);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$firstName", firstName);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$lastName", lastName);
        addDefenceCounselPayload = addDefenceCounselPayload.replace("$title", title);

        return addDefenceCounselPayload;
    }


}
