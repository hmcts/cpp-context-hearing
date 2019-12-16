package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.it.HearingEventsIT.hearingDefinitionData;
import static uk.gov.moj.cpp.hearing.it.HearingEventsIT.hearingDefinitions;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRoomMappings;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.stubExhibitFileUpload;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishLatestCourtCentreHearingEventsIT extends AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishLatestCourtCentreHearingEventsIT.class);

    private static final String LISTING_COMMAND_PUBLISH_COURT_LIST = "hearing.command.publish-court-list";
    private static final String MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST = "application/vnd.hearing.publish-court-list+json";

    private static final ZonedDateTime EVENT_TIME = now().minusMinutes(5l).withZoneSameLocal(ZoneId.of("UTC"));

    private static final String courtCentreId = randomUUID().toString();

    @Before
    public void initStub() {
        stubExhibitFileUpload();
        stubGetReferenceDataCourtRoomMappings(courtCentreId);
    }

    @Test
    public void shouldRequestToPublishCourtList() {

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId);

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

    }

    @Test
    public void shouldGetLatestHearingEvents() throws NoSuchAlgorithmException {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, initiateHearingTemplateWithParam(fromString("0c5eead7-e337-44a8-9d0e-fa3378b12fc5"), "CourtRoom 1", 2019, 7, 5)));


        givenAUserHasLoggedInAsACourtClerk(randomUUID());

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        logEvent(requestSpec, asDefault(), hearing.it(), hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME);

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();
        publishCourtListSteps.verifyLatestHearingEvents(hearing.getHearing(), now().minusMinutes(10l));
    }

    private void sendPublishCourtListCommand(final JsonObject publishCourtListJsonObject) {

        final String updateHearingUrl = String.format("%s/%s", baseUri, format(ENDPOINT_PROPERTIES.getProperty(LISTING_COMMAND_PUBLISH_COURT_LIST)));
        final String request = publishCourtListJsonObject.toString();

        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n\n", updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInSystemUserHeader());

        final Response response = restClient.postCommand(updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInSystemUserHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }


    private JsonObject buildPublishCourtListJsonString(final String courtCentreId) {
        return createObjectBuilder().add("courtCentreId", courtCentreId).add("createdTime", "2019-10-30T16:34:45.132Z").build();
    }

    private static HearingEventDefinition findEventDefinitionWithActionLabel(final HearingEventDefinitionData hearingEventDefinitionData, final String actionLabel) {
        return hearingEventDefinitionData.getEventDefinitions().stream().filter(d -> d.getActionLabel().equals(actionLabel)).findFirst().get();
    }
}
