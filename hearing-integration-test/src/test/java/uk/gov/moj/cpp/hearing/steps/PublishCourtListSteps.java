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
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorizedAndSystemUser;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.it.AbstractIT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishCourtListSteps extends AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishCourtListSteps.class);
    private static final String MEDIA_TYPE_QUERY_COURT_LIST_STATUS = "application/vnd.hearing.court.list.publish.status+json";
    private static final String MEDIA_TYPE_QUERY_HEARINGS_BY_COURT_CENTRE = "application/vnd.hearing.latest-hearings-by-court-centres+json";

    private static final int PUBLISH_COURT_LIST_DEFAULT_POLL_TIMEOUT_IN_SEC = 60 * 5;
    private static final int PUBLISH_COURT_LIST_DEFAULT_POLL_INTERVAL = 1;

    public void verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(final String courtCentreId) {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.court.list.publish.status"), courtCentreId);
        final String searchCourtListUrl = String.format("%s/%s", getBaseUri(), queryPart);

        poll(requestParams(searchCourtListUrl, MEDIA_TYPE_QUERY_COURT_LIST_STATUS).withHeader(USER_ID, getLoggedInUser()))
                .timeout(PUBLISH_COURT_LIST_DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .pollInterval(PUBLISH_COURT_LIST_DEFAULT_POLL_INTERVAL, SECONDS)
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

    public void verifyLatestHearingEvents(final Hearing hearing, final LocalDate hearingEventDate,
                                          final UUID expectedHearingEventId) {

        setupAsAuthorizedAndSystemUser(USER_ID_VALUE_AS_ADMIN);

        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.latest-hearings-by-court-centres"), hearing.getCourtCentre().getId(), hearingEventDate);
        final String searchCourtListUrl = String.format("%s/%s", getBaseUri(), queryPart);

        poll(requestParams(searchCourtListUrl, MEDIA_TYPE_QUERY_HEARINGS_BY_COURT_CENTRE).withHeader(USER_ID, getLoggedInSystemUserHeader()))
                .timeout(PUBLISH_COURT_LIST_DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .pollInterval(PUBLISH_COURT_LIST_DEFAULT_POLL_INTERVAL, SECONDS)
                .until(status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.court.courtName", equalTo(hearing.getCourtCentre().getName())),
                                withJsonPath("$.court.courtSites[0].courtRooms[0].courtRoomName", equalTo(hearing.getCourtCentre().getRoomName())),
                                withJsonPath("$.court.courtSites[0].courtRooms[0].hearingEvent.id", equalTo(expectedHearingEventId.toString()))
                        )));
    }

    public void verifyExportFailedWithErrorMessage(final String courtCentreId, final String errorMessageSubstring) {

        final String whereCondition = String.format(" court_centre_id='%s' and publish_status='EXPORT_FAILED' and error_message ilike '%s%%'", courtCentreId, errorMessageSubstring);

        waitUntilDataPersist("court_list_publish_status", whereCondition, 1);
    }

    private void waitUntilDataPersist(final String tableName, final String criteria, final int count) {
        Awaitility.await()
                .atMost(Duration.FIVE_MINUTES)
                .until(() -> countExportStatus(tableName, criteria) == count);
    }

    private int countExportStatus(final String tableName, final String criteria) {
        try (final Connection viewStoreConnection = testJdbcConnectionProvider.getViewStoreConnection("hearing");
             final Statement statement = viewStoreConnection.createStatement()) {
            final String sql = String.format("select count(1) from %s where %s", tableName, criteria);
            final ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException exception) {
            LOGGER.error(String.format("Failed to count from table %s with condition %s", tableName, criteria), exception);
        }

        return 0;
    }

}
