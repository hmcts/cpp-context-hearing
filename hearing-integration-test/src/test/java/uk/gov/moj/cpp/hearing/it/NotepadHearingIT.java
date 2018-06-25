package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubForReferenceDataResults;

import java.text.MessageFormat;

import org.junit.Test;

public class NotepadHearingIT extends AbstractIT {

    @Test
    public void hearingResultReferenceData() {

        stubForReferenceDataResults();

        String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP");
        String url =  getBaseUri() + "/"  + queryAPIEndPoint;
        String mediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        poll(requestParams(url, mediaType).withHeader(USER_ID, USER_ID_VALUE).build())
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.originalText", is("RESTRAOP")) ,
                                withJsonPath("$.parts[0].value", is("Restraining order for period")),
                                withJsonPath("$.parts[0].state", is("RESOLVED"))
                        )));

    }
}
