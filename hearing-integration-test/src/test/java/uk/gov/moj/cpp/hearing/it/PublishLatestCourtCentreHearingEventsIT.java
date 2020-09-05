package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.now;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.APPELLANT_OPPENS_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.DEFENCE_COUNCIL_NAME_OPENS_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.RESUME_HEARING_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.START_HEARING_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getFileForPath;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXmlForPubDisplay;

import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PublishLatestCourtCentreHearingEventsIT extends AbstractPublishLatestCourtCentreHearingIT {

    final private static UUID RESUME_ID_WHICH_IS_NOT_TO_BE_INCLUDED_IN_FILTER = RESUME_HEARING_EVENT_DEFINITION_ID;
    private static final String LISTING_COMMAND_PUBLISH_COURT_LIST = "hearing.command.publish-court-list";
    private static final String MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST = "application/vnd.hearing.publish-court-list+json";
    private static final String XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26 = "/xhibit-gateway/send-to-xhibit/WebPage.*.20191026163445\\.xml";
    private static final String XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_20 = "/xhibit-gateway/send-to-xhibit/WebPage.*.20191020163445\\.xml";
    private static final String XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_21 = "/xhibit-gateway/send-to-xhibit/WebPage.*.20191021163445\\.xml";
    private ZonedDateTime eventTime;
    private LocalDate localDate;

    @Before
    public void setUpTest() {
        eventTime = now().minusMinutes(5L).withZoneSameLocal(ZoneId.of("UTC"));
        localDate = eventTime.toLocalDate();
    }

    @Test
    public void shouldRequestToPublishCourtListOpenCaseProsecution() throws NoSuchAlgorithmException {
        createHearingEvent(randomUUID(), courtRoom2Id, defenceCounselId, OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId);

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "26");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        courtCentreId = sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        final String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_26);
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
        UUID hearingEventId = randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(hearingEventId, courtRoom3Id, defenceCounselId, DEFENCE_COUNCIL_NAME_OPENS_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId_1);
        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId_1, "21");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId_1);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId_1);

        final String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_21);
        assertThat(filePayload, containsString("E20906_Defence_CO_Name>Mr John Jones</E20906_Defence_CO_Name"));
    }

    @Test
    public void shouldRequestToPublishCourtList() throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(randomUUID(), courtRoom1Id, defenceCounselId, START_HEARING_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId_2);
        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId_2, "28");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId_2);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId_2);
    }

    @Test
    public void shouldRecordFailureIfSchemaValidationFails() throws NoSuchAlgorithmException {
        createHearingEvent(randomUUID(), courtRoom5Id, defenceCounselId, START_HEARING_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId_3);

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId_3, "28");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommandForExportFailed(publishCourtListJsonObject, courtCentreId_3);

        publishCourtListSteps.verifyExportFailedWithErrorMessage(courtCentreId_3, "GenerationFailedException: Could not validate XML against schema");
    }

    @Test
    public void shouldRequestToPublishCourtListAppellantOpens() throws NoSuchAlgorithmException {
        UUID hearingEventId = randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(hearingEventId, courtRoom4Id, defenceCounselId, APPELLANT_OPPENS_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId_4);
        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId_4, "20");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishCourtListCommand(publishCourtListJsonObject, courtCentreId_4);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId_4);

        final String filePayload = getFileForPath(XHIBIT_GATEWAY_SEND_WEB_PAGE_TO_XHIBIT_FILE_NAME_20);
        assertThat(filePayload, containsString("E20606_Appellant_CO_Name>TomAppellant BradyAppellant</E20606_Appellant_CO_Name"));
    }

    @Test
    public void shouldGetLatestHearingEvents() throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(randomUUID(), courtRoom1Id, defenceCounselId, START_HEARING_EVENT_DEFINITION_ID, eventTime, of(hearingTypeId), courtCentreId);
        final UUID expectedHearingEventId = randomUUID();
        final UUID hearingEventId = randomUUID();
        logEvent(expectedHearingEventId, requestSpec, asDefault(), hearing.it(), OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, false, fromString(defenceCounselId), eventTime.plusMinutes(10));
        pollHearingEventLog(hearing.getHearingId(), 2, eventTime.plusMinutes(10));
        ZonedDateTime updatedEventTime = eventTime.plusMinutes(15);
        logEvent(hearingEventId, requestSpec, asDefault(), hearing.it(), RESUME_ID_WHICH_IS_NOT_TO_BE_INCLUDED_IN_FILTER, false, fromString(defenceCounselId), updatedEventTime);
        pollHearingEventLog(hearing.getHearingId(), 3, updatedEventTime);

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();
        publishCourtListSteps.verifyLatestHearingEvents(hearing.getHearing(), eventTime.toLocalDate(), expectedHearingEventId);
    }

    private String sendPublishCourtListCommand(final JsonObject publishCourtListJsonObject, final String courtCentreId) {

        try (final Utilities.EventListener eventTopic = listenFor("hearing.event.publish-court-list-export-successful", "hearing.event")
                .withFilter(isJson(withJsonPath("$.publishStatus", is("EXPORT_SUCCESSFUL")
                )))) {

            makeCommand(requestSpec, LISTING_COMMAND_PUBLISH_COURT_LIST)
                    .ofType(MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST)
                    .withPayload(publishCourtListJsonObject.toString())
                    .withCppUserId(USER_ID_VALUE_AS_ADMIN)

                    .executeSuccessfully();

            eventTopic.waitFor();
        }
        return courtCentreId;
    }

    private String sendPublishCourtListCommandForExportFailed(final JsonObject publishCourtListJsonObject, final String courtCentreId) {

        try (final Utilities.EventListener eventTopic = listenFor("hearing.event.publish-court-list-export-failed", "hearing.event")
                .withFilter(isJson(withJsonPath("$.publishStatus", is("EXPORT_FAILED")
                )))) {

            makeCommand(requestSpec, LISTING_COMMAND_PUBLISH_COURT_LIST)
                    .ofType(MEDIA_TYPE_LISTING_COMMAND_PUBLISH_COURT_LIST)
                    .withPayload(publishCourtListJsonObject.toString())
                    .withCppUserId(USER_ID_VALUE_AS_ADMIN)

                    .executeSuccessfully();

            eventTopic.waitFor();
        }
        return courtCentreId;
    }

    private JsonObject buildPublishCourtListJsonString(final String courtCentreId, final String day) {
        return createObjectBuilder().add("courtCentreId", courtCentreId).add("createdTime", "2019-10-" + day + "T16:34:45.132Z").build();
    }

    private CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final UUID hearingEventId, final String courtRoomId, final String defenceCounselId, final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, String courtCenter) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateWithParam(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearing.it().getHearing().getId(), eventTime.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", Matchers.is(hearing.it().getHearing().getId().toString()))
                        ))
                );
        return hearing;

    }

    private String pollHearingEventLog(UUID hearingId, int size, ZonedDateTime eventTime) {

        ResponseData responseData = poll(requestParams(getURL("hearing.get-hearing-event-log", hearingId, eventTime.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", Matchers.is(hearingId.toString())),
                                withJsonPath("$.events", hasSize(size))
                        ))
                );

        return responseData.getPayload();
    }
}
