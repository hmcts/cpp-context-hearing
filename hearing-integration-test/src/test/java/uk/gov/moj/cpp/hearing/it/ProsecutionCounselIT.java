package uk.gov.moj.cpp.hearing.it;

import com.google.common.io.Resources;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.io.IOException;
import java.text.MessageFormat;

import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
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

public class ProsecutionCounselIT extends AbstractIT {

    private String createAddProsecutionCounselCommandPayload(final String personId, final String attendeeId, final String status,
                                                             final String firstName, final String lastName, final String title
    ) throws IOException {
        String addProsecutionCounselPayload = Resources.toString(
                getResource("hearing.command.add-prosecution-counsel.json"),
                defaultCharset());

        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$personId", personId);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$attendeeId", attendeeId);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$status", status);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$firstName", firstName);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$lastName", lastName);
        addProsecutionCounselPayload = addProsecutionCounselPayload.replace("$title", title);
        return addProsecutionCounselPayload;
    }

    @Test
    public void hearingAddProsecutionCounselTest() throws IOException {
        InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearingMultipleDefendants(requestSpec, 1);
        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        System.out.println("hearingAddProsecutionCounselTest hearingId==" + hearingId);
        final String personId1 = randomUUID().toString();
        final String attendeeId1 = randomUUID().toString();
        final String status1 = STRING.next();
        final String firstName1 = STRING.next();
        final String lastName1 = STRING.next();
        final String title1 = STRING.next();

        final String status2 = STRING.next();

        final String personId3 = randomUUID().toString();
        final String attendeeId3 = randomUUID().toString();
        final String status3 = STRING.next();
        final String firstName3 = STRING.next();
        final String lastName3 = STRING.next();
        final String title3 = STRING.next();

        final String addProsecutionCounselCommandPayload1 = createAddProsecutionCounselCommandPayload(personId1, attendeeId1, status1, firstName1, lastName1, title1);
        final String addProsecutionCounselCommandPayload2 = createAddProsecutionCounselCommandPayload(personId1, attendeeId1, status2, firstName1, lastName1, title1);
        final String addProsecutionCounselCommandPayload3 = createAddProsecutionCounselCommandPayload(personId3, attendeeId3, status3, firstName3, lastName3, title3);

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);

        // Add a prosecution Counsel to a hearing

        final Response writeResponse1 = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload1).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse1.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing-query-api-prosecution-counsels"), hearingId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.prosecution-counsels+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(1)),
                                withJsonPath("$.prosecution-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.prosecution-counsels[0].personId", is(personId1)),
                                withJsonPath("$.prosecution-counsels[0].status", is(status1)
                                ))));

        // Update the prosecution's details

        final Response writeResponse3 = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload2).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse3.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(1)),
                                withJsonPath("$.prosecution-counsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.prosecution-counsels[0].personId", is(personId1)),
                                withJsonPath("$.prosecution-counsels[0].status", is(status2)
                                ))));

        // Add another prosecution Counsel to the same hearing
        final Response writeResponse2 = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.add-prosecution-counsel+json")
                .body(addProsecutionCounselCommandPayload3).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse1.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.prosecution-counsels", hasSize(2)),
                                withJsonPath("$.prosecution-counsels[0].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.prosecution-counsels[0].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.prosecution-counsels[0].status", isOneOf(status2, status3)),
                                withJsonPath("$.prosecution-counsels[1].attendeeId", isOneOf(attendeeId1, attendeeId3)),
                                withJsonPath("$.prosecution-counsels[1].personId", isOneOf(personId1, personId3)),
                                withJsonPath("$.prosecution-counsels[1].status", isOneOf(status2, status3))
                        )));
        final String queryAPIEndPoint2 = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String url2 = getBaseUri() + "/" + queryAPIEndPoint2;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url2, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.prosecutionCounsels[0].attendeeId", is(attendeeId1)),
                                withJsonPath("$.attendees.prosecutionCounsels[0].status", is(status2)),
                                withJsonPath("$.attendees.prosecutionCounsels[0].firstName", is(firstName1)),
                                withJsonPath("$.attendees.prosecutionCounsels[0].lastName", is(lastName1)),
                                withJsonPath("$.attendees.prosecutionCounsels[0].title", is(title1)),
                                withJsonPath("$.attendees.prosecutionCounsels[1].attendeeId", is(attendeeId3)),
                                withJsonPath("$.attendees.prosecutionCounsels[1].status", is(status3)),
                                withJsonPath("$.attendees.prosecutionCounsels[1].firstName", is(firstName3)),
                                withJsonPath("$.attendees.prosecutionCounsels[1].lastName", is(lastName3)),
                                withJsonPath("$.attendees.prosecutionCounsels[1].title", is(title3))
                        )));


    }

}
