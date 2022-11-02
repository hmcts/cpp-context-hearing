package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.CourtCentre.courtCentre;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Gender.MALE;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.justice.core.courts.HearingType.hearingType;
import static uk.gov.justice.core.courts.JudicialRole.judicialRole;
import static uk.gov.justice.core.courts.JudicialRoleType.judicialRoleType;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.justice.core.courts.Person.person;
import static uk.gov.justice.core.courts.PersonDefendant.personDefendant;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;
import static uk.gov.moj.cpp.hearing.it.PublishLatestCourtCentreHearingEventsIT.XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getFileForPath;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForPubDisplay;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.moj.cpp.hearing.steps.CourtListRestrictionSteps;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;


@NotThreadSafe
public class CourtListRestrictionIT extends AbstractPublishLatestCourtCentreHearingIT {

    private ZonedDateTime eventTime;


    @Before
    public void setUpTest() {
        cleanDatabase("ha_hearing");
        eventTime = now().minusMinutes(5L).withZoneSameLocal(ZoneId.of("UTC"));
    }

    @Test
    public void shouldRequestToPublishCourtListWithCaseRestriction() throws Exception {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEvent(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideCaseFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.caseIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

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
    public void shouldIgnoreRequestToCourtListRestrictionWhenNoHearingIsInitiated() throws Exception {
        final UUID userId = randomUUID();
        setupAsMagistrateUser(userId);
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();
        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final Hearing hearing =  createHearingForToday(hearingId, courtCentreId, roomId, userId, null);

        courtListRestrictionSteps.hideCaseFromXhibit(hearing , true);

        try (final Utilities.EventListener eventTopic = listenFor("hearing.hearing-change-ignored", "hearing.event")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString())))))
        {
            eventTopic.waitFor();
        }

    }

    @Test
    public void shouldRequestToPublishCourtListWithDefendantRestrictionOnOff() throws Exception {
        final CourtListRestrictionSteps courtListRestrictionSteps = new CourtListRestrictionSteps();

        CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEvent(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideDefendantFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

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

        CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEventForApplication(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideApplicationFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

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

        CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = courtListRestrictionSteps.createHearingEventForApplication(caseId, randomUUID(), courtRoom2Id, randomUUID().toString(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId, eventTime.toLocalDate());

        courtListRestrictionSteps.hideApplicationApplicantFromXhibit(initiateHearingCommandHelper.getHearing(), true);

        courtListRestrictionSteps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(initiateHearingCommandHelper.getHearing().getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));

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

    private Hearing createHearingForToday(final UUID hearingId, final UUID courtCentreId, final UUID roomId, final UUID userId, List<CourtApplication> courtApplicationList) {
        final UUID prosecutionCaseId = randomUUID();
        final String DEFENDANT_LAST_NAME = "LAST_NAME";
        final String OFFENCE_WORDING = "OFFENCE WORDING";
        final String OFFENCE_TITLE = "OFFENCE TITLE";
        final String DEFENDANT_FIRST_NAME = "FIRST_NAME";
        final LocalDate DEFENDANT_DOB = LocalDate.now().minusYears(50);


        return hearing()
                .withId(hearingId)
                .withCourtCentre(courtCentre()
                        .withId(courtCentreId)
                        .withName("Lavender hill")
                        .withRoomId(roomId)
                        .build())
                .withHearingDays(Arrays.asList(
                        hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(0)
                                .withSittingDay(ZonedDateTime.now())
                                .build(),
                        hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(1)
                                .withSittingDay(ZonedDateTime.now().plusDays(1))
                                .build(),
                        hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(2)
                                .withSittingDay(ZonedDateTime.now().plusSeconds(30))
                                .build()

                ))
                .withJudiciary(singletonList(judicialRole()
                        .withJudicialId(randomUUID())
                        .withJudicialRoleType(judicialRoleType()
                                .withJudiciaryType("Type")
                                .build())
                        .withUserId(userId)
                        .build()))
                .withProsecutionCases(singletonList(prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                .withProsecutionAuthorityId(randomUUID())
                                .withProsecutionAuthorityCode("code")
                                .withCaseURN("caseURN")
                                .build())
                        .withInitiationCode(InitiationCode.J)
                        .withDefendants(singletonList(defendant()
                                .withId(randomUUID())
                                .withCourtProceedingsInitiated(ZonedDateTime.now())
                                .withMasterDefendantId(randomUUID())
                                .withProsecutionCaseId(prosecutionCaseId)
                                .withPersonDefendant(personDefendant()
                                        .withPersonDetails(person()
                                                .withFirstName(DEFENDANT_FIRST_NAME)
                                                .withLastName(DEFENDANT_LAST_NAME)
                                                .withDateOfBirth(DEFENDANT_DOB).withGender(MALE)
                                                .build())
                                        .build())
                                .withOffences(singletonList(offence()
                                        .withId(randomUUID())
                                        .withOffenceDefinitionId(randomUUID())
                                        .withOffenceCode("code")
                                        .withStartDate(LocalDate.now().plusDays(10))
                                        .withOffenceTitle(OFFENCE_TITLE)
                                        .withWording(OFFENCE_WORDING)
                                        .withReportingRestrictions(Arrays.asList(ReportingRestriction.reportingRestriction().withId(randomUUID()).withLabel("Yes")
                                                .withJudicialResultId(randomUUID()).build()))
                                        .build()))
                                .build()))
                        .build()))
                .withCourtApplications(courtApplicationList)
                .withJurisdictionType(MAGISTRATES)
                .withType(hearingType()
                        .withId(randomUUID())
                        .withDescription("Trial")
                        .build()).build();

    }

}
