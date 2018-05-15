package uk.gov.moj.cpp.hearing.it;


import com.google.common.io.Resources;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplateWithOnlyMandatoryFields;

public class GenerateNowsIT extends AbstractIT {


    @Test
    public void shouldAddNows() throws IOException {

        InitiateHearingCommand initiateHearing = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();

        final Hearing hearing = initiateHearing.getHearing();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        String defendantId = randomUUID().toString();
        String materialId = randomUUID().toString();
        String nowsId = randomUUID().toString();
        String nowsTypeId = randomUUID().toString();
        String sharedResultId = randomUUID().toString();

        String nowsJson = Resources.toString(Resources.getResource("hearing.generate-nows.json"), Charset.defaultCharset())
                .replace("HEARING_ID", hearing.getId().toString())
                .replace("DEFENDANT_ID", defendantId)
                .replace("NOW_ID", nowsId)
                .replace("MATERIAL_ID", materialId)
                .replace("NOWTYPE_ID", nowsTypeId)
                .replace("RESULT_ID", sharedResultId);

        final String commandUrl = getBaseUri() + "/" + ENDPOINT_PROPERTIES.getProperty("hearing.generate-nows");
        Response writeResponse = given()
                .contentType("application/vnd.hearing.generate-nows+json").log().all()
                .body(nowsJson).header(CPP_UID_HEADER).when()
                .post(commandUrl)
                .then().extract().response();

        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));


        TestUtilities.EventListener publicEventTopicNows = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearing.getId().toString()))));
        publicEventTopicNows.waitFor();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.nows"), initiateHearing.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.nows+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].defendantId", is(defendantId)),
                                withJsonPath("$.nows[0].nowsTypeId", is(nowsTypeId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].language", is("welsh")),
                                withJsonPath("$.nows[0].material[0].status", is("Requested")),
                                withJsonPath("$.nows[0].nowResult[0].sharedResultId", is(sharedResultId)),
                                withJsonPath("$.nows[0].nowResult[0].sequence", is(1))
                        )));

        final String searchAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.query.search-by-material-id"), materialId);
        final String searchUrl = getBaseUri() + "/" + searchAPIEndPoint;

        final String searchResponseType = "application/vnd.hearing.query.search-by-material-id+json";

        poll(requestParams(searchUrl, searchResponseType).withHeader(CPP_UID_HEADER_AS_ADMIN.getName(), CPP_UID_HEADER_AS_ADMIN.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.allowedUserGroups[0]", is("courtAdmin")),
                                withJsonPath("$.allowedUserGroups[1]", is("defence"))
                        )));
    }
}