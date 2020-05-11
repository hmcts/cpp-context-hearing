package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.APPELLANT_OPPENS_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.DEFENCE_COUNCIL_NAME_OPENS_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.RESUME_HEARING_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.START_HEARING_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForPubDisplay;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForWebPage;

import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishLatestCourtCentreHearingEventsIT extends AbstractPublishLatestCourtCentreHearingIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishLatestCourtCentreHearingEventsIT.class);
    final private static UUID RESUME_ID_WHICH_IS_NOT_TO_BE_INCLUDED_IN_FILTER = RESUME_HEARING_EVENT_DEFINITION_ID;
    private static final String LISTING_COMMAND_PUBLISH_COURT_LIST = "hearing.command.publish-court-list";
    private static final String MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST = "application/vnd.hearing.publish-court-list+json";

    private ZonedDateTime eventTime;
    private LocalDate localDate;

    @Before
    public void setUpTest() {
        eventTime = now().minusMinutes(5L).withZoneSameLocal(ZoneId.of("UTC"));
        localDate = eventTime.toLocalDate();
    }


    @Test
    public void shouldRequestToPublishCourtListOpenCaseProsecution() throws NoSuchAlgorithmException {
        createHearingEvent(randomUUID(), courtRoom2Id, defenceCounselId, OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getSentXmlForWebPage();
        final String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedDefendantXMLValueForWeb = "<defendants>\n" +
                "                            <defendant/>\n" +
                "                        </defendants>";

        final String expectedDefendantXMLValueForPublic = "<defendants>\n" +
                "                                    <defendant/>\n" +
                "                                </defendants>";

        assertThat(filePayload, containsString("E20903_PCO_Type>E20903_Prosecution_Opening</E20903_PCO_Type"));
        assertThat(filePayload, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString("activecase>1</activecase"));
        assertThat(filePayloadForPubDisplay, containsString(expectedDefendantXMLValueForPublic));
        assertThat(filePayloadForPubDisplay, containsString("E20903_PCO_Type>E20903_Prosecution_Opening</E20903_PCO_Type"));

        assertThat(filePayloadForPubDisplay, containsString("<judgename>Recorder Mark J Ainsworth</judgename>"));
    }

    @Test
    public void shouldRequestToPublishCourtListDefenceCouncilOpensCase() throws NoSuchAlgorithmException {
        createHearingEvent(randomUUID(), courtRoom2Id, defenceCounselId, DEFENCE_COUNCIL_NAME_OPENS_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "27");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getSentXmlForWebPage();
        assertThat(filePayload, containsString("E20906_Defence_CO_Name>Mr John Jones</E20906_Defence_CO_Name"));
    }

    @Test
    public void shouldRequestToPublishCourtList() throws NoSuchAlgorithmException {
        createHearingEvent(randomUUID(), courtRoom1Id, defenceCounselId, START_HEARING_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "28");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
    }

    @Test
    public void shouldRequestToPublishCourtListAppellantOpens() throws NoSuchAlgorithmException {
        createHearingEvent(randomUUID(), courtRoom2Id, defenceCounselId, APPELLANT_OPPENS_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "25");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getSentXmlForWebPage();
        assertThat(filePayload, containsString("E20606_Appellant_CO_Name>TomAppellant BradyAppellant</E20606_Appellant_CO_Name"));
    }

    @Test
    public void shouldGetLatestHearingEvents() throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(randomUUID(), courtRoom1Id, defenceCounselId, START_HEARING_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId));
        final UUID expectedHearingEventId = randomUUID();
        logEvent(expectedHearingEventId, requestSpec, asDefault(), hearing.it(), OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, false, fromString(defenceCounselId), eventTime.plusMinutes(10));
        logEvent(randomUUID(), requestSpec, asDefault(), hearing.it(), RESUME_ID_WHICH_IS_NOT_TO_BE_INCLUDED_IN_FILTER, false, fromString(defenceCounselId), eventTime.plusMinutes(15));

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();
        publishCourtListSteps.verifyLatestHearingEvents(hearing.getHearing(), eventTime.toLocalDate(), expectedHearingEventId);
    }

    private void sendPublishCourtListCommand(final JsonObject publishCourtListJsonObject) {
        final String updateHearingUrl = String.format("%s/%s", getBaseUri(), format(ENDPOINT_PROPERTIES.getProperty(LISTING_COMMAND_PUBLISH_COURT_LIST)));
        final String request = publishCourtListJsonObject.toString();

        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n with user {}\n\n", updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInSystemUserHeader());

        final Response response = new RestClient().postCommand(updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInSystemUserHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }

    private JsonObject buildPublishCourtListJsonString(final String courtCentreId, final String day) {
        return createObjectBuilder().add("courtCentreId", courtCentreId).add("createdTime", "2019-10-" + day + "T16:34:45.132Z").build();
    }

    private CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final UUID hearingEventId, final String courtRoomId, final String defenceCounselId, final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateWithParam(fromString(courtCentreId), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime);
        return hearing;
    }
}
