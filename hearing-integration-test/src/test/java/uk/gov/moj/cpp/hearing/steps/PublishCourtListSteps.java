package uk.gov.moj.cpp.hearing.steps;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsASystemUser;

import uk.gov.moj.cpp.hearing.it.AbstractIT;

import java.time.ZonedDateTime;
import java.util.UUID;

public class PublishCourtListSteps extends AbstractIT {

    private static final String MEDIA_TYPE_QUERY_COURT_LIST_STATUS = "application/vnd.hearing.court.list.publish.status+json";
    private static final String MEDIA_TYPE_QUERY_HEARINGS_BY_COURT_CENTRE = "application/vnd.hearing.get-hearings-by-court-centre+json";

    public void verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(final String courtCentreId) {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.court.list.publish.status"), courtCentreId);
        final String searchCourtListUrl = String.format("%s/%s", baseUri, queryPart);

        poll(requestParams(searchCourtListUrl, MEDIA_TYPE_QUERY_COURT_LIST_STATUS).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.publishCourtListStatuses[0].courtCentreId",
                                        equalTo(courtCentreId)),
                                withJsonPath("$.publishCourtListStatuses[0].lastUpdated",
                                        is(notNullValue())),
                                withJsonPath("$.publishCourtListStatuses[0].publishStatus",
                                        equalTo("COURT_LIST_REQUESTED")),
                                withJsonPath("$.publishCourtListStatuses[0].errorMessage",
                                        equalTo("")),
                                withJsonPath("$.publishCourtListStatuses[1].courtCentreId",
                                        equalTo(courtCentreId)),
                                withJsonPath("$.publishCourtListStatuses[1].lastUpdated",
                                        is(notNullValue())),
                                withJsonPath("$.publishCourtListStatuses[1].publishStatus",
                                        equalTo("COURT_LIST_PRODUCED")),
                                withJsonPath("$.publishCourtListStatuses[1].errorMessage",
                                        equalTo("")),
                                withJsonPath("$.publishCourtListStatuses[2].courtCentreId",
                                        equalTo(courtCentreId)),
                                withJsonPath("$.publishCourtListStatuses[2].lastUpdated",
                                        is(notNullValue())),
                                withJsonPath("$.publishCourtListStatuses[2].publishStatus",
                                        equalTo("EXPORT_SUCCESSFUL")),
                                withJsonPath("$.publishCourtListStatuses[2].errorMessage",
                                        equalTo(""))
                        )));
    }

    public void verifyLatestHearingEvents(final UUID courtCentreId, final ZonedDateTime modifiedTime) {

        givenAUserHasLoggedInAsASystemUser(USER_ID_VALUE_AS_ADMIN);

        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearings-by-court-centre"), courtCentreId, modifiedTime);
        final String searchCourtListUrl = String.format("%s/%s", baseUri, queryPart);

        poll(requestParams(searchCourtListUrl, MEDIA_TYPE_QUERY_HEARINGS_BY_COURT_CENTRE).withHeader(USER_ID, getLoggedInUser()))
                .until(status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.courtCentreId", equalTo(courtCentreId.toString()))
                        )));
    }
}
