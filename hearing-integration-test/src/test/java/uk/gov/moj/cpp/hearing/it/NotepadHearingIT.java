package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubForReferenceDataResults;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.test.utils.core.http.ResponseData;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Test;

public class NotepadHearingIT extends AbstractIT {

    @Test
    public void shouldParseDataThatAreAvailableTodayWithAssociatedPrompts() {
        final String definitionQueryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP", LocalDate.now().toString());
        final String definitionUrl = getBaseUri() + "/" + definitionQueryAPIEndPoint;
        final String definitionMediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        final ResponseData responseData = poll(requestParams(definitionUrl, definitionMediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.originalText", is("RESTRAOP")),
                                withJsonPath("$.parts[0].value", is("Restraining order for period")),
                                withJsonPath("$.parts[0].state", is("RESOLVED"))
                        )));

        final JSONObject jsonObject = new JSONObject(responseData.getPayload());
        final String resultCode = jsonObject.getString("resultCode");
        final String promptsQueryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.prompt"), resultCode, LocalDate.now().toString());
        final String promptUrl = getBaseUri() + "/" + promptsQueryAPIEndPoint;
        final String promptMediaType = "application/vnd.hearing.notepad.parse-result-prompt+json";

        poll(requestParams(promptUrl, promptMediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.promptChoices[0].code", is("abc9bb61-cb5b-4cf7-be24-8866bcd2fc69")),
                                withJsonPath("$.promptChoices[0].label", is("Protected person")),
                                withJsonPath("$.promptChoices[0].type", is("TXT")),
                                withJsonPath("$.promptChoices[0].required", is(true))
                        )));
    }

    @Test
    public void shouldGetUnresolvedResultWhenLookingForFutureDefinition() {
        final LocalDate orderedDate = LocalDate.now().plusDays(1);
        String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP", orderedDate.toString());
        String url = getBaseUri() + "/" + queryAPIEndPoint;
        String mediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        poll(requestParams(url, mediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.originalText", is("RESTRAOP")),
                                withJsonPath("$.orderedDate", is(LocalDates.to(orderedDate))),
                                withJsonPath("$.parts[0].state", is("UNRESOLVED"))
                        )));

    }

    @Test
    public void shouldParseDataThatAreAvailableTomorrow() {
        String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP2", LocalDate.now().plusDays(1).toString());
        String url = getBaseUri() + "/" + queryAPIEndPoint;
        String mediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        poll(requestParams(url, mediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.originalText", is("RESTRAOP2")),
                                withJsonPath("$.parts[0].value", is("Restraining order for period")),
                                withJsonPath("$.parts[0].state", is("RESOLVED"))
                        )));

    }
}
