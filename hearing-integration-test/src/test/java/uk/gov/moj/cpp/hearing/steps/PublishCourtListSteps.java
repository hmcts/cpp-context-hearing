package uk.gov.moj.cpp.hearing.steps;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.SECONDS;
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

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.it.AbstractIT;

import java.time.ZonedDateTime;

public class PublishCourtListSteps extends AbstractIT {

    private static final String MEDIA_TYPE_QUERY_COURT_LIST_STATUS = "application/vnd.hearing.court.list.publish.status+json";
    private static final String MEDIA_TYPE_QUERY_HEARINGS_BY_COURT_CENTRE = "application/vnd.hearing.get-hearings-by-court-centre+json";

    public void verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(final String courtCentreId) {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.court.list.publish.status"), courtCentreId);
        final String searchCourtListUrl = String.format("%s/%s", baseUri, queryPart);

        poll(requestParams(searchCourtListUrl, MEDIA_TYPE_QUERY_COURT_LIST_STATUS).withHeader(USER_ID, getLoggedInUser())).timeout(30, SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(

                                withJsonPath("$.publishCourtListStatus.courtCentreId",
                                        equalTo(courtCentreId)),
                                withJsonPath("$.publishCourtListStatus.lastUpdated",
                                        is(notNullValue())),
                                withJsonPath("$.publishCourtListStatus.publishStatus",
                                        equalTo("EXPORT_SUCCESSFUL"))
                        )));
    }

    public void verifyLatestHearingEvents(final Hearing hearing, final ZonedDateTime modifiedTime) {

        givenAUserHasLoggedInAsASystemUser(USER_ID_VALUE_AS_ADMIN);

        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearings-by-court-centre"), hearing.getCourtCentre().getId(), modifiedTime);
        final String searchCourtListUrl = String.format("%s/%s", baseUri, queryPart);

        poll(requestParams(searchCourtListUrl, MEDIA_TYPE_QUERY_HEARINGS_BY_COURT_CENTRE).withHeader(USER_ID, getLoggedInUser()))
                .until(status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.court.courtName", equalTo(hearing.getCourtCentre().getName())),
                                withJsonPath("$.court.courtSites[0].courtRooms[0].courtRoomName", equalTo(hearing.getCourtCentre().getRoomName()))
                        )));
    }
}
