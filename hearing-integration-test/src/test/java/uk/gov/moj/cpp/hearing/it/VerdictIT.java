package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingUpdateVerdictIgnoredPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingVerdictUpdatedPublicEventShouldBePublished;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class VerdictIT extends AbstractIT {


    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_VERDICT_DATE = "verdictDate";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String VERDICT_COLLECTION = "verdicts";
    private static final String FIELD_VERDICT_ID = "verdictId";

    private String hearingId;
    private String caseId;
    private String personId;
    private String defendantId;
    private String verdictId_1;
    private String verdictId_2;
    private String offenceId_1;
    private String offenceId_2;
    private String verdictDate;


    @Before
    public void setup() {
        hearingId = randomUUID().toString();
        caseId = randomUUID().toString();
        personId = randomUUID().toString();
        defendantId = randomUUID().toString();
        verdictId_1 = randomUUID().toString();
        offenceId_1 = randomUUID().toString();
        offenceId_2 = randomUUID().toString();
        verdictId_2 = randomUUID().toString();
        verdictDate = LocalDate.now().toString();
    }

    @Test
    public void hearingAddVerdict() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String verdictValue = "GUILTY";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        final String body = getStringFromResource("hearing.update-verdict.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID", verdictId_1)
                .replace("VERDICT_VALUE", verdictValue)
                .replace("VERDICT_DATE", verdictDate)
                .replace("RANDOM_OFFENCE_ID", offenceId_1)
                .replace("RANDOM_DEFENDANT_ID", defendantId)
                .replace("RANDOM_PERSON_ID", personId);

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.verdicts.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.verdicts+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), is(verdictId_1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), is(offenceId_1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VALUE), is(verdictValue)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId))
                        )));

        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);
    }

    @Test
    public void hearingAddUpdateMultipleVerdicts() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);


        final String originalVerdictValue = "NOT GUILTY";
        final String updatedVerdictValue = "GUILTY";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String body = getStringFromResource("hearing.update-multiple-verdicts.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID_1", verdictId_1)
                .replace("VERDICT_VALUE_1", originalVerdictValue)
                .replace("VERDICT_DATE_1", verdictDate)
                .replace("RANDOM_OFFENCE_ID_1", offenceId_1)
                .replace("RANDOM_VERDICT_ID_2", verdictId_2)
                .replace("VERDICT_VALUE_2", originalVerdictValue)
                .replace("VERDICT_DATE_2", verdictDate)
                .replace("RANDOM_OFFENCE_ID_2", offenceId_2)
                .replace("RANDOM_DEFENDANT_ID", defendantId)
                .replace("RANDOM_PERSON_ID", personId);

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.verdicts.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.verdicts+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VALUE), is(originalVerdictValue)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),

                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VALUE), is(originalVerdictValue))

                        )));

        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);
        //Update verdict value and call command endpooint
        body = body.replace(originalVerdictValue, updatedVerdictValue);

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        //query for updated values
        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VALUE), is(updatedVerdictValue)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),

                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VALUE), is(updatedVerdictValue))

                        )));
        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);
    }

    @Test
    public void hearingAddVerdictWithWrongOffenceVerdictMapping() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String verdictValue = "GUILTY";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String body = getStringFromResource("hearing.update-verdict.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID", verdictId_1)
                .replace("VERDICT_VALUE", verdictValue)
                .replace("VERDICT_DATE", verdictDate)
                .replace("RANDOM_OFFENCE_ID", offenceId_1)
                .replace("RANDOM_DEFENDANT_ID", defendantId)
                .replace("RANDOM_PERSON_ID", personId);

        Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.verdicts.by.case.id"), caseId);

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.case.verdicts+json";

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), is(verdictId_1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), is(offenceId_1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VALUE), is(verdictValue)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId))
                        )));

        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);

        // updating the offence with different verdict(id) , it should ignore updateVerdict call

        body = body.replace(verdictId_1, verdictId_2);

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        thenHearingUpdateVerdictIgnoredPublicEventShouldBePublished(hearingId);
    }

}
