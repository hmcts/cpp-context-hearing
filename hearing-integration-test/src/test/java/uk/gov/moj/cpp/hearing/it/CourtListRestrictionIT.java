package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.PublishLatestCourtCentreHearingEventsIT.XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getFileForPath;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForPubDisplay;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.steps.CourtListRestrictionSteps;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.JsonObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@NotThreadSafe
public class CourtListRestrictionIT extends AbstractPublishLatestCourtCentreHearingIT {

    private ZonedDateTime eventTime;

    @BeforeEach
    public void setUpTest() {
        cleanDatabase("ha_hearing");
        eventTime = new UtcClock().now().minusMinutes(5L);
    }

    /**
     * Hearings created here use templates that explicitly set
     * {@code reportingRestrictionReason=""} (the
     * {@code initiateHearingTemplateWithParamNoReportingRestriction*} variants).
     * They sit in the same {@code courtCentreId}/{@code courtRoom2Id} bucket
     * as
     * {@code PublishLatestCourtCentreHearingEventsIT.shouldRequestToPublishCourtListOpenCaseProsecution}
     * and would otherwise leak into that test's PUB-DISPLAY XML, breaking its
     * defendant-redaction assertion. Clean up after each method so nothing
     * survives this class.
     */
    @AfterEach
    public void tearDownTest() {
        cleanDatabase("ha_hearing");
    }

    @Test
    public void shouldRequestToPublishCourtListWithCaseRestriction() throws Exception {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEvent(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideCaseFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.caseIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

        // Wait for the restriction projection to land before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails", hasSize(0)));

        JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        String expectedCasesXMLValueForWeb = "<cases/>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideCaseFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.caseIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));

        // Wait for the un-restriction projection to land before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails", hasSize(1)));

        publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();

        expectedCasesXMLValueForWeb = "<cases>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
    }

    @Test
    public void shouldRequestToPublishCourtListWithDefendantRestrictionOnOff() throws Exception {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEvent(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideDefendantFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

        // Wait for defendant restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants", hasSize(0)));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");
        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedCasesXMLValueForWeb = "<caseDetails>";
        String expectedDefendantXMLValueForWeb = "<defendants/>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString(expectedDefendantXMLValueForWeb));

        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedDefendantXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideDefendantFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));

        // Wait for defendant un-restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants", hasSize(1)));

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();
        expectedDefendantXMLValueForWeb = "<defendants>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayload, containsString("firstname"));
        assertThat(filePayload, containsString("middlename"));
        assertThat(filePayload, containsString("lastname"));

        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString("firstname"));
        assertThat(filePayloadForPubDisplay, containsString("middlename"));
        assertThat(filePayloadForPubDisplay, containsString("lastname"));
    }

    @Test
    public void shouldRequestToPublishCourtListForApplicationRestrictionOnOff() throws NoSuchAlgorithmException {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEventForApplication(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideApplicationFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

        // Wait for application restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails", hasSize(0)));

        JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        String expectedCasesXMLValueForWeb = "<cases/>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideApplicationFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));

        // Wait for application un-restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails", hasSize(1)));

        publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();

        expectedCasesXMLValueForWeb = "<cppurn>";
        String expectedDefendantXMLValueForWeb = "<defendant>";
        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
    }

    @Test
    public void shouldRequestToPublishCourtListForApplicationApplicantRestrictionOnOff() throws NoSuchAlgorithmException {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEventForApplication(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideApplicationApplicantFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

        // Wait for applicant restriction to land in the projection before publishing.
        // When restricted the applicant is nulled out and the defendant serialises as {} (no keys),
        // so we assert on the object being empty rather than firstName being null (absent key ≠ null).
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants[0]", is(java.util.Collections.emptyMap())));

        JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        String expectedApplicantXMLValueForWeb = "<defendant/>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedApplicantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedApplicantXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideApplicationApplicantFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));

        // Wait for applicant un-restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants[0].firstName", org.hamcrest.CoreMatchers.notNullValue()));

        publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();

        String expectedCasesXMLValueForWeb = "<cppurn>";
        expectedApplicantXMLValueForWeb = "<defendant>";
        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString(expectedApplicantXMLValueForWeb));
        assertThat(filePayload, containsString("firstname"));
        assertThat(filePayload, containsString("middlename"));
        assertThat(filePayload, containsString("lastname"));

        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedApplicantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString("firstname"));
        assertThat(filePayloadForPubDisplay, containsString("middlename"));
        assertThat(filePayloadForPubDisplay, containsString("lastname"));
    }

    @Test
    public void shouldRequestToPublishCourtListWhenYoungDefendantIsRestrictedOnInitiate() throws Exception {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        final InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEventWithYoungDefendant(
                caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        // Wait for the young-defendant restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants", hasSize(0)));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");
        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        final String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedCasesXMLValueForWeb = "<caseDetails>";
        final String expectedDefendantXMLValueForWeb = "<defendants/>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedDefendantXMLValueForWeb));
    }

}
