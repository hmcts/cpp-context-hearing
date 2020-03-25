package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubGetProgressionProsecutionCases;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRoomMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtXhibitCourtMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataEventMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataJudiciaries;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataXhibitHearingTypes;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForPubDisplay;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForWebPage;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.stubExhibitFileUpload;

import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishLatestCourtCentreHearingEventsIT extends AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishLatestCourtCentreHearingEventsIT.class);
    final private static String DEFENCE_COUNCIL_NAME_OPENS = "Defence counsel.name opens case regarding defendant defendant.name";
    final private static String APPELLANT_OPENS = "Appellant opens";
    final private static String START_HEARING = "Start Hearing";
    final private static String APPELLANT_OPENS_ID = "50fb4a64-943d-4a2a-afe6-4b5c9e99e043";
    final private static String OPEN_CASE_PROSECUTION_ID = "e9060336-4821-4f46-969c-e08b33b48071";
    final private static String RESUME_ID_WHICH_IS_NOT_TO_BE_INCLUDED_IN_FILTER = "64476e43-2138-46d5-b58b-848582cf9b07";
    final private static String OPEN_CASE_PROSECUTION = "Open case prosecution";
    private static final String LISTING_COMMAND_PUBLISH_COURT_LIST = "hearing.command.publish-court-list";
    private static final String MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST = "application/vnd.hearing.publish-court-list+json";

    private static final ZonedDateTime EVENT_TIME = now().minusMinutes(5l).withZoneSameLocal(ZoneId.of("UTC"));
    private static final LocalDate localDate = EVENT_TIME.toLocalDate();

    private String courtCentreId;
    private String courtRoom1Id;
    private String courtRoom2Id;
    private String defenceCounselId;
    private UUID caseId;
    private static UUID hearingTypeId;

    @BeforeClass
    public static void setUp(){
        hearingTypeId = UUID.fromString("9cc41e45-b594-4ba6-906e-1a4626b08fed");
        stubGetReferenceDataXhibitHearingTypes();
    }

    @Before
    public void initStub() {
        courtCentreId = randomUUID().toString();
        courtRoom1Id = randomUUID().toString();
        courtRoom2Id = randomUUID().toString();
        defenceCounselId = randomUUID().toString();
        caseId = randomUUID();

        stubExhibitFileUpload();
        stubGetReferenceDataCourtRoomMappings(courtRoom1Id, courtRoom2Id);

        stubGetReferenceDataCourtXhibitCourtMappings();
        stubOrganisationUnit(courtCentreId);

        stubGetReferenceDataEventMappings();
        stubGetProgressionProsecutionCases(caseId);
        stubGetReferenceDataJudiciaries();
    }

    @Test
    public void shouldRequestToPublishCourtListOpenCaseProsecution() throws NoSuchAlgorithmException {
        createHearingEvent(courtRoom2Id, defenceCounselId, OPEN_CASE_PROSECUTION, of(hearingTypeId));

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
    }

    @Ignore("To be fixed in SCSL-466")
    @Test
    public void shouldRequestToPublishCourtListDefenceCouncilOpensCase() throws NoSuchAlgorithmException {
        createHearingEvent(courtRoom2Id, defenceCounselId, DEFENCE_COUNCIL_NAME_OPENS, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "27");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getSentXmlForWebPage();
        assertThat(filePayload, containsString("E20906_Defence_CO_Name>Mr John Jones</E20906_Defence_CO_Name"));
    }

    @Test
    public void shouldRequestToPublishCourtList() throws NoSuchAlgorithmException {
        createHearingEvent(courtRoom1Id, defenceCounselId, START_HEARING, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "28");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
    }

    @Test
    public void shouldRequestToPublishCourtListAppellantOpens() throws NoSuchAlgorithmException {
        createHearingEvent(courtRoom2Id, defenceCounselId, APPELLANT_OPENS, of(hearingTypeId));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "25");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getSentXmlForWebPage();
        assertThat(filePayload, containsString("E20606_Appellant_CO_Name>TomAppellant BradyAppellant</E20606_Appellant_CO_Name"));
    }

    @Ignore("To be fixed in SCSL-466")
    @Test
    public void shouldGetLatestHearingEvents() throws NoSuchAlgorithmException {
        final UUID expectedHearingEventId = randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(courtRoom1Id, defenceCounselId, START_HEARING, EVENT_TIME, of(hearingTypeId));

        logEvent(randomUUID(), requestSpec, asDefault(), hearing.it(), fromString(APPELLANT_OPENS_ID), false, fromString(defenceCounselId), EVENT_TIME.plusMinutes(5));
        logEvent(expectedHearingEventId, requestSpec, asDefault(), hearing.it(), fromString(OPEN_CASE_PROSECUTION_ID), false, fromString(defenceCounselId), EVENT_TIME.plusMinutes(10));
        logEvent(randomUUID(), requestSpec, asDefault(), hearing.it(), fromString(RESUME_ID_WHICH_IS_NOT_TO_BE_INCLUDED_IN_FILTER), false, fromString(defenceCounselId), EVENT_TIME.plusMinutes(15));

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();
        publishCourtListSteps.verifyLatestHearingEvents(hearing.getHearing(), EVENT_TIME.toLocalDate(), expectedHearingEventId);
    }

    private void sendPublishCourtListCommand(final JsonObject publishCourtListJsonObject) {
        final String updateHearingUrl = String.format("%s/%s", getBaseUri(), format(ENDPOINT_PROPERTIES.getProperty(LISTING_COMMAND_PUBLISH_COURT_LIST)));
        final String request = publishCourtListJsonObject.toString();

        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n\n", updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInSystemUserHeader());

        final Response response = new RestClient().postCommand(updateHearingUrl, MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST, request, getLoggedInSystemUserHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }

    private JsonObject buildPublishCourtListJsonString(final String courtCentreId, final String day) {
        return createObjectBuilder().add("courtCentreId", courtCentreId).add("createdTime", "2019-10-" + day + "T16:34:45.132Z").build();
    }

    private final CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final String courtRoomId, final String defenceCounselId, final String actionLabel, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateWithParam(fromString(courtCentreId), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));

        givenAUserHasLoggedInAsACourtClerk(randomUUID());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(actionLabel);
        assertThat(hearingEventDefinition.isAlterable(), is(false));

        logEvent(getRequestSpec(), asDefault(), hearing.it(), hearingEventDefinition.getId(), false, fromString(defenceCounselId), eventTime);
        return hearing;
    }

    private final CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final String courtRoomId, final String defenceCounselId, final String actionLabel, final Optional<UUID> hearingTypeId) throws NoSuchAlgorithmException {
        return createHearingEvent(courtRoomId, defenceCounselId, actionLabel, EVENT_TIME, hearingTypeId);
    }
}
