package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.moj.cpp.hearing.it.PublishLatestCourtCentreHearingEventsIT.XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitNewFile;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitNewSentXmlForPubDisplay;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.countFilesAt;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.countSentXmlForPubDisplay;
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

    /**
     * Clean every table that can carry state across tests in this class. All five tests bind
     * their hearings to the SAME static {@code caseId} (see
     * {@link AbstractPublishLatestCourtCentreHearingIT}), so residual
     * {@code is_court_list_restricted=true} on {@code ha_case} (the {@code ProsecutionCase}
     * entity's table — see its {@code @Table} annotation) or {@code ha_defendant} from one
     * test poisons the next. The {@code court_list_publish_status} row is also dropped so the
     * FIRST publish in this class cannot inherit {@code EXPORT_SUCCESSFUL} from a prior test
     * class's publish — without this,
     * {@code verifyCourtListPublishStatusReturnedWhenQueryingFromAPI} returns on the stale
     * status before the current publish has produced a file.
     */
    private void cleanRestrictionTables() {
        cleanDatabase("ha_hearing",
                "ha_case",
                "ha_defendant",
                "ha_hearing_day",
                "ha_hearing_event",
                "court_list_publish_status");
    }

    @BeforeEach
    public void setUpTest() {
        cleanRestrictionTables();
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
        cleanRestrictionTables();
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

        // Case/application fully restricted: the schema forbids an empty <cases/>, so the whole <cases> element is
        // omitted from the court room rather than emitted empty. The court-room-level status event is still present.
        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, not(containsString("<cases")));
        assertThat(filePayloadForPubDisplay, not(containsString("<cases")));

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

        final int webPageCountBeforeSecondPublish = countFilesAt(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        final int pubDisplayCountBeforeSecondPublish = countSentXmlForPubDisplay();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
        awaitNewFile(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageCountBeforeSecondPublish);
        awaitNewSentXmlForPubDisplay(pubDisplayCountBeforeSecondPublish);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedCasesXMLValueForWeb = "<cases>";

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

        // Wait for defendant restriction to land in the projection before publishing.
        // The restricted defendant is retained (with a masked name), not dropped, so it is still present in the projection.
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants", hasSize(1)));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");
        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedCasesXMLValueForWeb = "<caseDetails>";
        // Restricted defendant is masked (present with ****** name), NOT dropped - so the defendants element is
        // populated ("<defendants>") rather than an empty self-closing "<defendants/>", and the name fields carry ******.
        final String maskedFirstName = "<firstname>******</firstname>";
        final String maskedLastName = "<lastname>******</lastname>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString("<defendants>"));
        assertThat(filePayload, containsString(maskedFirstName));
        assertThat(filePayload, containsString(maskedLastName));

        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString("<defendants>"));
        assertThat(filePayloadForPubDisplay, containsString(maskedFirstName));
        assertThat(filePayloadForPubDisplay, containsString(maskedLastName));

        // disable restriction
        courtListRestrictionSteps.hideDefendantFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));

        // Wait for defendant un-restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants", hasSize(1)));

        final int webPageCountBeforeSecondPublish = countFilesAt(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        final int pubDisplayCountBeforeSecondPublish = countSentXmlForPubDisplay();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
        awaitNewFile(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageCountBeforeSecondPublish);
        awaitNewSentXmlForPubDisplay(pubDisplayCountBeforeSecondPublish);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();
        final String expectedDefendantXMLValueForWeb = "<defendants>";

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

        // Case/application fully restricted: the schema forbids an empty <cases/>, so the whole <cases> element is
        // omitted from the court room rather than emitted empty. The court-room-level status event is still present.
        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, not(containsString("<cases")));
        assertThat(filePayloadForPubDisplay, not(containsString("<cases")));

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

        final int webPageCountBeforeSecondPublish = countFilesAt(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        final int pubDisplayCountBeforeSecondPublish = countSentXmlForPubDisplay();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
        awaitNewFile(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageCountBeforeSecondPublish);
        awaitNewSentXmlForPubDisplay(pubDisplayCountBeforeSecondPublish);

        filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedCasesXMLValueForWeb = "<cppurn>";
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

        // Wait for applicant restriction to land in the projection before publishing
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                hasNoJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants[0].firstName"));

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

        final int webPageCountBeforeSecondPublish = countFilesAt(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        final int pubDisplayCountBeforeSecondPublish = countSentXmlForPubDisplay();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
        awaitNewFile(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageCountBeforeSecondPublish);
        awaitNewSentXmlForPubDisplay(pubDisplayCountBeforeSecondPublish);

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

        // Wait for the young-defendant restriction to land in the projection before publishing.
        // The youth is retained (with a masked name), not dropped, so it is still present in the projection.
        courtListRestrictionSteps.waitForRestrictionProjection(courtCentreId, eventTime.toLocalDate(),
                withJsonPath("$.court.courtSites[0].courtRooms[0].cases.casesDetails[0].defendants", hasSize(1)));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");
        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        final String filePayloadForPubDisplay = getSentXmlForPubDisplay();

        final String expectedCasesXMLValueForWeb = "<caseDetails>";
        // The youth defendant is masked (present with ****** name), NOT dropped - so the defendants element is
        // populated ("<defendants>") rather than an empty self-closing "<defendants/>" that XHIBIT cannot display,
        // and the name fields carry ******.
        final String maskedFirstName = "<firstname>******</firstname>";
        final String maskedLastName = "<lastname>******</lastname>";

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString("<defendants>"));
        assertThat(filePayload, containsString(maskedFirstName));
        assertThat(filePayload, containsString(maskedLastName));
        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString("<defendants>"));
        assertThat(filePayloadForPubDisplay, containsString(maskedFirstName));
        assertThat(filePayloadForPubDisplay, containsString(maskedLastName));
    }

}
