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
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitFileForPathSinceContaining;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitFileForPathSinceContainingAll;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitSentXmlForPubDisplaySinceContaining;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.awaitSentXmlForPubDisplaySinceContainingAll;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getPublicDisplayPutRequestCount;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getPutRequestCount;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.steps.CourtListRestrictionSteps;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.JsonObject;

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
        courtListRestrictionSteps.waitForCaseCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getProsecutionCases().get(0).getId(), true);

        JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        int webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        int publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String expectedCasesXMLValueForWeb = "<cases/>";
        String filePayload = awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, expectedCasesXMLValueForWeb);
        String filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContaining(publicDisplayRequestsBefore, expectedCasesXMLValueForWeb);

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideCaseFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.caseIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        courtListRestrictionSteps.waitForCaseCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getProsecutionCases().get(0).getId(), false);

        publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        expectedCasesXMLValueForWeb = "<cases>";
        filePayload = awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, expectedCasesXMLValueForWeb);
        filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContaining(publicDisplayRequestsBefore, expectedCasesXMLValueForWeb);

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
        courtListRestrictionSteps.waitForDefendantCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), true);

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");
        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        int webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        int publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String expectedCasesXMLValueForWeb = "<caseDetails>";
        String expectedDefendantXMLValueForWeb = "<defendants/>";
        String filePayload = awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, expectedDefendantXMLValueForWeb);
        String filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContaining(publicDisplayRequestsBefore, expectedDefendantXMLValueForWeb);

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
        courtListRestrictionSteps.waitForDefendantCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), false);

        webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        expectedDefendantXMLValueForWeb = "<defendants>";
        filePayload = awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, "firstname");
        filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContaining(publicDisplayRequestsBefore, "firstname");

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
        courtListRestrictionSteps.waitForApplicationCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getId(),
                initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getId(), true);

        JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        int webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        int publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String expectedCasesXMLValueForWeb = "<cases/>";
        String filePayload = awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, expectedCasesXMLValueForWeb);
        String filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContaining(publicDisplayRequestsBefore, expectedCasesXMLValueForWeb);

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideApplicationFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        courtListRestrictionSteps.waitForApplicationCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getId(),
                initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getId(), false);

        publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String applicationReference = initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getApplicationReference();
        expectedCasesXMLValueForWeb = "<cppurn>";
        String expectedDefendantXMLValueForWeb = "<defendant>";
        filePayload = awaitFileForPathSinceContainingAll(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, applicationReference, expectedDefendantXMLValueForWeb);
        filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContainingAll(publicDisplayRequestsBefore, applicationReference, expectedDefendantXMLValueForWeb);

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

        final String applicationReference = initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getApplicationReference();

        courtListRestrictionSteps.hideApplicationApplicantFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
        courtListRestrictionSteps.waitForApplicationApplicantCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getId(),
                initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getApplicant().getId(), true);

        JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        int webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        int publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String expectedApplicantXMLValueForWeb = "<defendant/>";
        String filePayload = awaitFileForPathSinceContainingAll(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, applicationReference, expectedApplicantXMLValueForWeb);
        String filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContainingAll(publicDisplayRequestsBefore, applicationReference, expectedApplicantXMLValueForWeb);

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedApplicantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedApplicantXMLValueForWeb));

        // disable restriction
        courtListRestrictionSteps.hideApplicationApplicantFromXhibit(initiateHearingCommandHelper.getHearing(), false);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        courtListRestrictionSteps.waitForApplicationApplicantCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getId(),
                initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getApplicant().getId(), false);

        publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        String expectedCasesXMLValueForWeb = "<cppurn>";
        expectedApplicantXMLValueForWeb = "<defendant>";
        final String applicantFirstName = initiateHearingCommandHelper.getHearing().getCourtApplications().get(0).getApplicant().getPersonDetails().getFirstName();
        filePayload = awaitFileForPathSinceContainingAll(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, applicationReference, applicantFirstName);
        filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContainingAll(publicDisplayRequestsBefore, applicationReference, applicantFirstName);

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
        courtListRestrictionSteps.waitForDefendantCourtListRestriction(
                initiateHearingCommandHelper.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), true);

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");
        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        int webPageRequestsBefore = getPutRequestCount(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
        int publicDisplayRequestsBefore = getPublicDisplayPutRequestCount();
        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);
        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String expectedCasesXMLValueForWeb = "<caseDetails>";
        final String expectedDefendantXMLValueForWeb = "<defendants/>";
        final String filePayload = awaitFileForPathSinceContaining(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26, webPageRequestsBefore, expectedDefendantXMLValueForWeb);
        final String filePayloadForPubDisplay = awaitSentXmlForPubDisplaySinceContaining(publicDisplayRequestsBefore, expectedDefendantXMLValueForWeb);

        assertThat(filePayload, containsString(E20903_PCO_TYPE));
        assertThat(filePayload, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayload, containsString(expectedDefendantXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(E20903_PCO_TYPE));
        assertThat(filePayloadForPubDisplay, containsString(expectedCasesXMLValueForWeb));
        assertThat(filePayloadForPubDisplay, containsString(expectedDefendantXMLValueForWeb));
    }

}
