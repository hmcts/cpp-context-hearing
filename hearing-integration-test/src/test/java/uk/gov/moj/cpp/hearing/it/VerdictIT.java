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
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.andHearingHasBeenConfirmed;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingUpdateVerdictIgnoredPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.thenHearingVerdictUpdatedPublicEventShouldBePublished;
import static uk.gov.moj.cpp.hearing.steps.data.factory.ProgressionDataFactory.hearingConfirmedFor;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import com.jayway.restassured.response.Response;

import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

public class VerdictIT extends AbstractIT {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_NUMBER_OF_JURORS = "numberOfJurors";
    private static final String FIELD_NUMBER_OF_SPLIT_JURORS = "numberOfSplitJurors";
    private static final String FIELD_UNANIMOUS = "unanimous";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_VALUE_CATEGORY = "category";
    private static final String FIELD_VERDICT_DATE = "verdictDate";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String VERDICT_COLLECTION = "verdicts";
    private static final String FIELD_VERDICT_ID = "verdictId";
    
    @Ignore
    @Test
    public void hearingAddVerdict() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        // and
        final String hearingId = initiateHearing(UUID.randomUUID().toString());
        final String caseId = newUuid();
        final String personId = newUuid();
        final String defendantId = newUuid();
        final String verdictId  = newUuid();
        final String offenceId = newUuid();
        final String verdictDate = LocalDate.now().toString();
        final Integer numberOfJurors = RandomGenerator.values(10, 11, 12).next();
        final Integer numberOfSplitJurors = RandomGenerator.values(1, 2, 3).next();
        final Boolean unanimous = RandomGenerator.BOOLEAN.next();

        final String verdictCategory = "GUILTY";
        
        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        final String body = getStringFromResource("hearing.update-verdict.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID", verdictId)
                .replace("VERDICT_CATEGORY", verdictCategory)
                .replace("VERDICT_DATE", verdictDate)
                .replace("RANDOM_OFFENCE_ID", offenceId)
                .replace("RANDOM_DEFENDANT_ID", defendantId)
                .replace("10", numberOfJurors.toString())
                .replace("54321", numberOfSplitJurors.toString())
                .replace("false", unanimous.toString())
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

        System.out.println(numberOfJurors);
        System.out.println(numberOfSplitJurors);
        System.out.println(unanimous);

        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        print,
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), is(verdictId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), is(offenceId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s.%s", VERDICT_COLLECTION, FIELD_VALUE, FIELD_VALUE_CATEGORY), is(verdictCategory)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_NUMBER_OF_JURORS), is(numberOfJurors)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_NUMBER_OF_SPLIT_JURORS), is(numberOfSplitJurors)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_UNANIMOUS), is(unanimous))
                        )));
        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);
    }

    private String newUuid() {
        return randomUUID().toString();
    }

    private Matcher<ResponseData> print = new BaseMatcher<ResponseData>() {
        @Override
        public boolean matches(Object o) {
            System.out.println("matching " + ((ResponseData)o).getPayload());
            return true;
        }

        @Override
        public void describeTo(Description description) {
        }
    };
    
    @Ignore
    @Test
    public void hearingAddUpdateMultipleVerdicts() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        // and
        final String hearingId = initiateHearing(UUID.randomUUID().toString());
        final String caseId = newUuid();
        final String personId = newUuid();
        final String defendantId = newUuid();
        final String verdictId_1  = newUuid();
        final String verdictId_2 = newUuid();
        final String offenceId_1 = newUuid();
        final String offenceId_2 = newUuid();
        final String verdictDate = LocalDate.now().toString();
        final Integer numberOfJurors = RandomGenerator.values(10, 11, 12).next();
        final Integer numberOfSplitJurors = RandomGenerator.values(1, 2, 3).next();
        final Boolean unanimous = RandomGenerator.BOOLEAN.next();

        final String originalVerdictCategory = "NOT GUILTY";
        final String updatedVerdictValue = "GUILTY";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String body = getStringFromResource("hearing.update-multiple-verdicts.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID_1", verdictId_1)
                .replace("VERDICT_CATEGORY_1", originalVerdictCategory)
                .replace("VERDICT_DATE_1", verdictDate)
                .replace("RANDOM_OFFENCE_ID_1", offenceId_1)
                .replace("RANDOM_VERDICT_ID_2", verdictId_2)
                .replace("VERDICT_CATEGORY_2", originalVerdictCategory)
                .replace("VERDICT_DATE_2", verdictDate)
                .replace("RANDOM_OFFENCE_ID_2", offenceId_2)
                .replace("RANDOM_DEFENDANT_ID", defendantId)
                .replace("RANDOM_PERSON_ID", personId)
                .replace("54321", numberOfSplitJurors.toString())
                .replace("false", unanimous.toString())
                .replace("10", numberOfJurors.toString());


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
                        print,
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[0].%s.%s", VERDICT_COLLECTION, FIELD_VALUE, FIELD_VALUE_CATEGORY), is(originalVerdictCategory)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),

                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[1].%s.%s", VERDICT_COLLECTION, FIELD_VALUE, FIELD_VALUE_CATEGORY), is(originalVerdictCategory))
                        )));
        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);
        //Update verdict value and call command endpooint
        body = body.replace(originalVerdictCategory, updatedVerdictValue);

        writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-verdict+json")
                .body(body).header(CPP_UID_HEADER).when().post(commandAPIEndPoint)
                .then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));

        //query for updated values
        poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(
                        status().is(OK),
                        print,
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[0].%s.%s", VERDICT_COLLECTION, FIELD_VALUE, FIELD_VALUE_CATEGORY), is(updatedVerdictValue)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),

                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_PERSON_ID), is(personId)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), isOneOf(verdictId_1, verdictId_2)),
                                withJsonPath(format("$.%s[1].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), isOneOf(offenceId_1, offenceId_2)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_DATE), is(verdictDate)),
                                withJsonPath(format("$.%s[1].%s.%s", VERDICT_COLLECTION, FIELD_VALUE, FIELD_VALUE_CATEGORY), is(updatedVerdictValue))

                        )));
        // and
        thenHearingVerdictUpdatedPublicEventShouldBePublished(hearingId);
    }

    @Ignore
    @Test
    public void hearingAddVerdictWithWrongOffenceVerdictMapping() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        // and
        final String hearingId = initiateHearing(UUID.randomUUID().toString());
        final String caseId = newUuid();
        final String personId = newUuid();
        final String defendantId = newUuid();
        final String verdictId_1  = newUuid();
        final String verdictId_2 = newUuid();
        final String offenceId_1 = newUuid();
        final String verdictDate = LocalDate.now().toString();

        final String verdictCategory = "GUILTY";

        final String commandAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId);
        String body = getStringFromResource("hearing.update-verdict.json").replace("RANDOM_CASE_ID", caseId)
                .replace("RANDOM_VERDICT_ID", verdictId_1)
                .replace("VERDICT_CATEGORY", verdictCategory)
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
                        print,
                        payload().isJson(allOf(
                                withJsonPath(format("$.%s", VERDICT_COLLECTION), IsCollectionWithSize.hasSize(1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_VERDICT_ID), is(verdictId_1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_CASE_ID), is(caseId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_HEARING_ID), is(hearingId)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_OFFENCE_ID), is(offenceId_1)),
                                withJsonPath(format("$.%s[0].%s", VERDICT_COLLECTION, FIELD_DEFENDANT_ID), is(defendantId)),
                                withJsonPath(format("$.%s[0].%s.%s", VERDICT_COLLECTION, FIELD_VALUE, FIELD_VALUE_CATEGORY), is(verdictCategory)),
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

    private static String initiateHearing(final String hearingId) throws IOException {
        final JsonObject hearingConfirmed = hearingConfirmedFor(UUID.fromString(hearingId));
        andHearingHasBeenConfirmed(hearingConfirmed);
        return hearingId;
    }
}
