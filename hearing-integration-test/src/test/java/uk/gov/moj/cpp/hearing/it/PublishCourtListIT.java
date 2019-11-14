package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;

import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishCourtListIT extends AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishCourtListIT.class);

    private static final String LISTING_COMMAND_PUBLISH_COURT_LIST = "hearing.command.publish-court-list";
    private static final String MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST = "application/vnd.hearing.publish-court-list+json";


    @Test
    public void shouldRequestToPublishCourtList() {

        final UUID courtCentreId = UUID.randomUUID();

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId);

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId.toString());

    }

    private void sendPublishCourtListCommand(final JsonObject publishCourtListJsonObject) {

        final String updateHearingUrl = String.format("%s/%s", baseUri, format(ENDPOINT_PROPERTIES.getProperty(LISTING_COMMAND_PUBLISH_COURT_LIST)));
        final String request = publishCourtListJsonObject.toString();

        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n\n", updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInHeader());

        final Response response = restClient.postCommand(updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }


    private JsonObject buildPublishCourtListJsonString(final UUID courtCentreId) {
        return createObjectBuilder().add("courtCentreId", courtCentreId.toString()).add("createdTime", "2019-10-30T16:34:45.132Z").build();
    }
}
