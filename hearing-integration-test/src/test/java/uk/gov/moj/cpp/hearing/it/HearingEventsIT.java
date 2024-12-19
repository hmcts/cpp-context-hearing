package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs.enforcementAreaBacs;
import static uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit.organisationalUnit;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR;
import static uk.gov.moj.cpp.hearing.it.MatcherUtil.getPastDate;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearingEvents;
import static uk.gov.moj.cpp.hearing.it.UseCases.amendHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.getReference;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.UseCases.postHearingLogEventCommand;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub.stubDcumentCreateForHearingEventLog;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetPIReferenceDataEventMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataJudiciaries;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubAaagDetails;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubGetUserOrganisation;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUserAndOrganisation;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsForNames;

import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.core.http.RestPoller;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.Case;
import uk.gov.moj.cpp.hearing.eventlog.CourtCentre;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.jayway.jsonpath.ReadContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
@NotThreadSafe
public class HearingEventsIT extends AbstractIT {

    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));
    private final UUID DEFENCE_COUNSEL_ID = randomUUID();
    private static final String DOCUMENT_TEXT = STRING.next();
    private final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    @BeforeAll
    public static void setupPerClass() {
        UUID userId = randomUUID();
        setupAsAuthorisedUser(userId);
        stubGetPIReferenceDataEventMappings();
    }

    @Test
    public void publishEventAndCorrectIt_forCrownCourt() {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");
        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEventWithPublicEventCheck(randomUUID(), getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, STRING.next(), "testNote");

        pollForHearingEvents(hearingOne.getHearingId().toString(), EVENT_TIME.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                withJsonPath("$.events", hasSize(1)),
                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                withJsonPath("$.events[0].alterable", is(false)),
                withJsonPath("$.events[0].note", is("testNote"))
        });

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(getRequestSpec(), logEventCommand.getHearingEventId(),
                asDefault(), hearingOne.it(), hearingEventDefinition.getId(), false, EVENT_TIME);

        pollForHearingEvents(hearingOne.getHearingId().toString(), EVENT_TIME.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                withJsonPath("$.events", hasSize(1)),
                withJsonPath("$.events[0].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString())),
                withJsonPath("$.events[0].recordedLabel", is(correctLogEventCommand.getRecordedLabel())),
                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(correctLogEventCommand.getEventTime()))),
                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(correctLogEventCommand.getLastModifiedTime()))),
                withJsonPath("$.events[0].defenceCounselId", is(correctLogEventCommand.getDefenceCounselId().toString())),
                withJsonPath("$.events[0].alterable", is(false))
        });

    }

    @Test
    public void publishEventAndCorrectIt_ForMagsCourt() {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");
        final LogEventCommand logEventCommand =
                logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                        hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(getRequestSpec(), logEventCommand.getHearingEventId(),
                asDefault(), hearingOne.it(), hearingEventDefinition.getId(), false, EVENT_TIME);

        pollForHearingEvents(hearingOne.getHearingId().toString(), EVENT_TIME.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                withJsonPath("$.events", hasSize(1)),
                withJsonPath("$.events[0].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString())),
                withJsonPath("$.events[0].recordedLabel", is(correctLogEventCommand.getRecordedLabel())),
                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(correctLogEventCommand.getEventTime()))),
                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(correctLogEventCommand.getLastModifiedTime()))),
                withJsonPath("$.events[0].defenceCounselId", is(correctLogEventCommand.getDefenceCounselId().toString())),
                withJsonPath("$.events[0].alterable", is(false))
        });
    }

    @Test
    public void publishEventWithWitness_givenStartOfHearing() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(),
                hearingOne.it(), hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        pollForHearingEvents(hearingOne.getHearingId().toString(), EVENT_TIME.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                withJsonPath("$.events", hasSize(1)),

                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                withJsonPath("$.events[0].alterable", is(false))
        });

    }

    @Test
    public void publishHearingIgnoredEvent_givenNoHearing() {


        final UUID hearingId = randomUUID();

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        logEventThatIsIgnored(getRequestSpec(), asDefault(), hearingId, hearingEventDefinition.getId(),
                hearingEventDefinition.isAlterable(), "Hearing not found");
    }

    @Test
    public void publishEvent_givenIdentifyDefendantEvent() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Identify defendant");

        assertThat(hearingEventDefinition.isAlterable(), is(true));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(), hearingEventDefinition.getId(), true, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        pollForHearingEvents(hearingOne.getHearingId().toString(), EVENT_TIME.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                withJsonPath("$.events", hasSize(1)),
                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                withJsonPath("$.events[0].alterable", is(true))
        });

    }

    @Test
    public void publishMultipleEventsAndCorrection_shouldReturnInEventTimeOrder() {

        final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.of(2019, Month.APRIL, 10), LocalTime.of(22, 1), ZoneId.of("UTC"));

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition startHearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        final LogEventCommand startHearingLogEventCommand = logEvent(getRequestSpec(),
                e -> e.withEventTime(zonedDateTime),
                hearingOne.it(), startHearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, zonedDateTime, null);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(getRequestSpec(), startHearingLogEventCommand.getHearingEventId(),
                e -> e.withEventTime(zonedDateTime.plusMinutes(40)),
                hearingOne.it(), startHearingEventDefinition.getId(), false, zonedDateTime);

        final HearingEventDefinition identifyDefendantEventDefinition = findEventDefinitionWithActionLabel("Identify defendant");
        final LogEventCommand identifyDefendantLogEventCommand = logEvent(getRequestSpec(),
                e -> e.withEventTime(zonedDateTime.plusMinutes(20)),
                hearingOne.it(), identifyDefendantEventDefinition.getId(), true,
                DEFENCE_COUNSEL_ID, zonedDateTime, null);

        pollForHearingEvents(hearingOne.getHearingId().toString(), zonedDateTime.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                withJsonPath("$.events", hasSize(2)),
                withJsonPath("$.events[0].hearingEventId", is(identifyDefendantLogEventCommand.getHearingEventId().toString())),
                withJsonPath("$.events[1].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString()))

        });
    }

    @Test
    public void publishEvent_hearingEventsUpdated() {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        final HearingEvent hearingEvent = new HearingEvent(logEventCommand.getHearingEventId(), "RL1", "note");

        final String commandAPIEndPoint = MessageFormat.format(
                ENDPOINT_PROPERTIES.getProperty("hearing.update-hearing-events"),
                logEventCommand.getHearingId().toString());

        updateHearingEvents(getRequestSpec(),
                logEventCommand.getHearingId(), asList(hearingEvent),
                commandAPIEndPoint, getLoggedIdUserHeader());

        pollForHearingEvents(hearingOne.getHearingId().toString(), EVENT_TIME.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(logEventCommand.getHearingId().toString())),
                withJsonPath("$.events", hasSize(1)),

                withJsonPath("$.events[0].hearingEventId", is(hearingEvent.getHearingEventId().toString())),
                withJsonPath("$.events[0].recordedLabel",
                        is(hearingEvent.getRecordedLabel()))
        });

    }

    @Test
    public void amendEvent_givenStartOfHearing() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");
        assertThat(hearingEventDefinition.isAlterable(), is(false));

        amendHearing(getRequestSpec(), hearingOne.getHearingId(), SHARED_AMEND_LOCKED_ADMIN_ERROR);

        poll(requestParams(getURL("hearing.get-hearing", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.get.hearing+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingState", is(SHARED_AMEND_LOCKED_ADMIN_ERROR.toString())

                                )))
                );

    }

    @Test
    public void shouldRetrieveHearingEventLogForDocumentByDomain() {
        // Test does look up using case, application and hearing criteria

        final UUID userId = getLoggedInUser();
        stubUsersAndGroupsForNames(userId);
        stubGetReferenceDataJudiciaries();

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final String loggedInUsersResponsePayload = getPayload("stub-data/usersgroups.users.hmcts.organisation.json");
        stubUserAndOrganisation(userId, loggedInUsersResponsePayload.replace("%USER_ID%", userId.toString()));
        final JsonObject loggedInUserObject = stringToJsonObjectConverter.convert(loggedInUsersResponsePayload);
        final String organisation = getPayload("stub-data/usersgroups.get-hmcts-organisation-details.json")
                .replace("%ORGANISATION_ID%", loggedInUserObject.getString("organisationId"));
        stubGetUserOrganisation(loggedInUserObject.getString("organisationId"), organisation);

        final String progressionAAAGResponsePayload = getPayload("stub-data/progression.query.application.aaag.json")
                .replace("%APPLICATION_ID%", hearingOne.getCourtApplication().getId().toString());
        stubAaagDetails(hearingOne.getCourtApplication().getId().toString(), progressionAAAGResponsePayload);

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, new UtcClock().now(), "Hearing started", "testNote");

        logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, new UtcClock().now().minusDays(1), "Hearing started", "testNote");

        poll(requestParams(getURL("hearing.get-hearing-event-log-count", hearingOne.getHearingId(), LocalDate.now().toString()),
                "application/vnd.hearing.get-hearing-event-log-count+json").withHeader(USER_ID, userId))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.eventLogCountByHearingIdAndDate", is(1)),
                                withJsonPath("$.eventLogCountByHearingId", is(2))
                        ))
                );

        poll(requestParams(getURL("hearing.get-hearing-event-log-for-document-by-case", hearingOne.getFirstCase().getId()),
                "application/vnd.hearing.get-hearing-event-log-for-documents+json").withHeader(USER_ID, userId))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearings[0].hearingId", is(hearingOne.getHearingId().toString()))

                        ))
                );

        poll(requestParams(getURL("hearing.get-hearing-event-log-for-document-by-application", hearingOne.getCourtApplication().getId()),
                "application/vnd.hearing.get-hearing-event-log-for-documents+json").withHeader(USER_ID, userId))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearings[0].hearingId", is(hearingOne.getHearingId().toString()))

                        ))
                );

        pollEventLogForHearing(hearingOne.getHearingId().toString(), userId.toString(), new Matcher[]{
                withJsonPath("$.hearings[0].hearingId", is(hearingOne.getHearingId().toString()))
        });

        final UUID nonHmctsUserId = randomUUID();
        stubUsersAndGroupsForNames(nonHmctsUserId);
        final String nonHmctsUserPayload = getPayload("stub-data/usersgroups.users.nonhmcts.organisation.json");
        stubUserAndOrganisation(nonHmctsUserId, nonHmctsUserPayload.replace("%USER_ID%", userId.toString()));
        pollEventLogForHearing(hearingOne.getHearingId().toString(), nonHmctsUserId.toString(), new Matcher[]{
                withJsonPath("$.reason", is("Hearing Event Log For Document failed due to Organisation ID mismatch"))
        });

        poll(requestParams(getURL("hearing.get-hearing-event-log-count", hearingOne.getHearingId(), LocalDate.now().toString()),
                "application/vnd.hearing.get-hearing-event-log-count+json").withHeader(USER_ID, nonHmctsUserId))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.reason", is("Hearing Event Log Count failed due to Organisation ID mismatch"))
                        ))
                );
    }

    @Test
    public void shouldGeneratePdfForHearingEventLogExtract() {

        final UUID userId = getLoggedInUser();
        stubUsersAndGroupsForNames(userId);
        stubGetReferenceDataJudiciaries();
        stubDcumentCreateForHearingEventLog(DOCUMENT_TEXT);

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final String progressionAAAGResponsePayload = getPayload("stub-data/progression.query.application.aaag.json")
                .replace("%APPLICATION_ID%", hearingOne.getCourtApplication().getId().toString());
        stubAaagDetails(hearingOne.getCourtApplication().getId().toString(), progressionAAAGResponsePayload);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final String loggedInUsersResponsePayload = getPayload("stub-data/usersgroups.users.hmcts.organisation.json");
        stubUserAndOrganisation(userId, loggedInUsersResponsePayload.replace("%USER_ID%", userId.toString()));
        final JsonObject loggedInUserObject = stringToJsonObjectConverter.convert(loggedInUsersResponsePayload);
        final String organisation = getPayload("stub-data/usersgroups.get-hmcts-organisation-details.json")
                .replace("%ORGANISATION_ID%", loggedInUserObject.getString("organisationId"));
        stubGetUserOrganisation(loggedInUserObject.getString("organisationId"), organisation);


        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, new UtcClock().now(), "testNote");
        logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, new UtcClock().now().minusDays(1), "Hearing started", "testNote");
        logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, new UtcClock().now().minusDays(3), "Hearing started", "testNote");


        poll(requestParams(getURL("hearing.get-hearing-event-log-for-document-by-hearing", hearingOne.getHearingId()),
                "application/vnd.hearing.get-hearing-event-log-for-documents+json").withHeader(USER_ID, userId))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearings[0].hearingId", is(hearingOne.getHearingId().toString()))
                        ))
                );

        final String documentContentResponse = pollForResponse(userId.toString(), hearingOne.getHearingId());

        assertThat(documentContentResponse, is(notNullValue()));

    }

    private String pollForResponse(final String userId, final UUID hearingId, final Matcher... payloadMatchers) {

        return RestPoller.poll(requestParams(getURL("hearing.get-hearing-event-log-extract-for-document-by-hearing", hearingId),
                        "application/vnd.hearing.get-hearing-event-log-extract-for-documents+json")
                        .withHeader(USER_ID, userId).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(CoreMatchers.allOf(payloadMatchers))
                )
                .getPayload();
    }

    private CorrectLogEventCommand correctLogEvent(final RequestSpecification requestSpec,
                                                   final UUID hearingEventId,
                                                   final Consumer<CorrectLogEventCommand.Builder> consumer,
                                                   final InitiateHearingCommand initiateHearingCommand,
                                                   final UUID hearingEventDefinitionId,
                                                   final boolean alterable,
                                                   final ZonedDateTime eventTime) {
        final CorrectLogEventCommand logEvent = with(
                CorrectLogEventCommand.builder()
                        .withLastestHearingEventId(randomUUID()) // the new event id.
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withEventTime(eventTime)
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(STRING.next())
                , consumer).withDefenceCounselId(randomUUID()).build();

        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier();
        final String reference = isNull(prosecutionCaseIdentifier.getProsecutionAuthorityReference()) ? prosecutionCaseIdentifier.getCaseURN() : prosecutionCaseIdentifier.getProsecutionAuthorityReference();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.event-timestamp-corrected")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, isBean(PublicHearingEventLogged.class)
                        .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getHearingEventId, Is.is(logEvent.getLatestHearingEventId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getRecordedLabel, Is.is(logEvent.getRecordedLabel()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastHearingEventId, Is.is(hearingEventId))
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getEventTime, Is.is(logEvent.getEventTime().withZoneSameInstant(ZoneId.of("UTC"))))
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastModifiedTime, Is.is(logEvent.getLastModifiedTime().withZoneSameInstant(ZoneId.of("UTC"))))

                        )
                        .with(PublicHearingEventLogged::getCase, isBean(uk.gov.moj.cpp.hearing.eventlog.Case.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Case::getCaseUrn, Is.is(reference))
                        )
                        .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId, Is.is(logEvent.getHearingEventDefinitionId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::isPriority, Is.is(!alterable))
                        )
                        .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getCourtCentreId, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                                        .with(CourtCentre::getCourtCentreName, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                                        .with(CourtCentre::getCourtRoomId, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                                        .with(CourtCentre::getCourtRoomName, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                                )
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, Is.is(initiateHearingCommand.getHearing().getType().getDescription()))
                        )
                ));

        makeCommand(requestSpec, "hearing.correct-hearing-event")
                .withArgs(initiateHearingCommand.getHearing().getId(), hearingEventId) //the original hearing event id
                .ofType("application/vnd.hearing.correct-hearing-event+json")

                .withPayload(logEvent)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }

    private JsonObject updateHearingEvents(final RequestSpecification requestSpec,
                                           final UUID hearingId, final List<HearingEvent> hearingEvents,
                                           final String updateEventsEndpoint, final Header headers) {
        final JsonArrayBuilder hearingEventsArray = Json.createArrayBuilder();
        hearingEvents.stream().forEach(event -> {
            final JsonObject hearingEvent = Json.createObjectBuilder()
                    .add("hearingEventId", event.getHearingEventId().toString())
                    .add("recordedLabel", event.getRecordedLabel())
                    .build();
            hearingEventsArray.add(hearingEvent);
        });
        final JsonObject payload =
                Json.createObjectBuilder()
                        .add("hearingEvents", hearingEventsArray).build();
        final Utilities.EventListener publicEventTopic =
                listenFor("public.hearing.events-updated")
                        .withFilter(isJson(Matchers.allOf(new BaseMatcher<ReadContext>() {

                                                              @Override
                                                              public void describeTo(final Description description) {

                                                              }

                                                              @Override
                                                              public boolean matches(final Object o) {
                                                                  return true;
                                                              }
                                                          }, withJsonPath("$.hearingId",
                                        Is.is(hearingId
                                                .toString())),
                                withJsonPath("$.hearingEvents[0].hearingEventId",
                                        Is.is(hearingEvents.get(0).getHearingEventId()
                                                .toString())),
                                withJsonPath("$.hearingEvents[0].recordedLabel",
                                        Is.is(hearingEvents.get(0)
                                                .getRecordedLabel()))


                        )));

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-hearing-events+json")
                .body(payload.toString()).header(headers).when()
                .post(updateEventsEndpoint).then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        publicEventTopic.waitFor();

        return payload;
    }

    private LogEventCommand logEventThatIsIgnored(final RequestSpecification requestSpec,
                                                  final Consumer<LogEventCommand.Builder> consumer,
                                                  final UUID hearingId,
                                                  final UUID hearingEventDefinitionId,
                                                  final boolean alterable,
                                                  final String reason) {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(hearingId)
                        .withEventTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(STRING.next())
                , consumer).build();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.event-ignored")
                .withFilter(isJson(Matchers.allOf(
                        withJsonPath("$.hearingEventId", Is.is(logEvent.getHearingEventId().toString())),
                        withJsonPath("$.hearingId", Is.is(logEvent.getHearingId().toString())),
                        withJsonPath("$.alterable", Is.is(alterable)),
                        withJsonPath("$.hearingEventDefinitionId", Is.is(logEvent.getHearingEventDefinitionId().toString())),
                        withJsonPath("$.reason", Is.is(reason)),
                        withJsonPath("$.recordedLabel", Is.is(logEvent.getRecordedLabel())),
                        withJsonPath("$.eventTime", Is.is(ZonedDateTimes.toString(logEvent.getEventTime())))

                )));

        makeCommand(requestSpec, "hearing.log-hearing-event")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.log-hearing-event+json")
                .withPayload(logEvent)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }

    private LogEventCommand logEventWithPublicEventCheck(final UUID hearingEventId,
                                                         final RequestSpecification requestSpec,
                                                         final Consumer<LogEventCommand.Builder> consumer,
                                                         final InitiateHearingCommand initiateHearingCommand,
                                                         final UUID hearingEventDefinitionId,
                                                         final boolean alterable,
                                                         final UUID defenceCounselId,
                                                         final ZonedDateTime eventTime,
                                                         final String recordedLabel, String note) {
        stubOrganisationUnit(initiateHearingCommand.getHearing().getCourtCentre().getId().toString());


        final EnforcementAreaBacs enforcementAreaBacs = enforcementAreaBacs()
                .withBankAccntName("account name")
                .withBankAccntNum(1)
                .withBankAccntSortCode("867878")
                .withBankAddressLine1("address1")
                .withRemittanceAdviceEmailAddress("test@test.com")
                .build();

        final OrganisationalUnit organisationalUnit = organisationalUnit()
                .withOucode(null)
                .withLja(null)
                .withId(initiateHearingCommand.getHearing().getCourtCentre().getId().toString())
                .withIsWelsh(true)
                .withAddress1("address 1")
                .withWelshAddress1("Welsh address1")
                .withWelshAddress2("Welsh address2")
                .withWelshAddress3("Welsh address3")
                .withWelshAddress4("Welsh address4")
                .withWelshAddress4("Welsh address5")
                .withPostcode("AL4 9LG")
                .withOucodeL3WelshName(initiateHearingCommand.getHearing().getCourtCentre().getWelshName())
                .withEnforcementArea(enforcementAreaBacs)
                .build();
        ReferenceDataStub.stub(organisationalUnit);

        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(hearingEventId)
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(eventTime)
                        .withLastModifiedTime(getPastDate())
                        .withRecordedLabel(recordedLabel)
                        .withDefenceCounselId(defenceCounselId)
                        .withAlterable(alterable)
                        .withNote(note)
                , consumer).build();

        final String reference = getReference(initiateHearingCommand.getHearing());
        final BeanMatcher<PublicHearingEventLogged> matcher = isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getHearingEventId, Is.is(logEvent.getHearingEventId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getRecordedLabel, Is.is(logEvent.getRecordedLabel()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastHearingEventId, Is.is(nullValue()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getEventTime, Is.is(logEvent.getEventTime().withZoneSameInstant(ZoneId.of("UTC"))))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastModifiedTime, Is.is(logEvent.getLastModifiedTime().withZoneSameInstant(ZoneId.of("UTC"))))

                )
                .with(PublicHearingEventLogged::getCase, isBean(Case.class)
                        .with(Case::getCaseUrn, Is.is(reference))
                )
                .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId, Is.is(logEvent.getHearingEventDefinitionId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::isPriority, Is.is(!alterable))
                )
                .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getCourtCentreId, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                                .with(CourtCentre::getCourtCentreName, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                                .with(CourtCentre::getCourtRoomId, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                                .with(CourtCentre::getCourtRoomName, Is.is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                        )
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, Is.is(initiateHearingCommand.getHearing().getType().getDescription()))
                );
        try (final Utilities.EventListener publicEventTopic = listenFor("public.hearing.event-logged")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, matcher
                ))) {

            final Utilities.EventListener liveStatusEventListener = listenFor("public.hearing.live-status-published")
                    .withFilter(isJson(allOf(withJsonPath("$.courtCentreName", Is.is(initiateHearingCommand.getHearing().getCourtCentre().getName())),
                            withJsonPath("$.courtRooms", Matchers.hasSize(1)),
                            withJsonPath("$.courtRooms[0].courtRoomName", is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName())),
                            withJsonPath("$.courtRooms[0].sessions[0].sittings[0].hearing[0].hearingType", is(initiateHearingCommand.getHearing().getType().getDescription())),
                            withJsonPath("$.courtRooms[0].sessions[0].sittings[0].hearing[0].defendants[0].firstName", is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getFirstName())),
                            withJsonPath("$.courtRooms[0].sessions[0].sittings[0].hearing[0].defendants[0].lastName", is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getLastName())),
                            withJsonPath("$.courtRooms[0].sessions[0].sittings[0].hearing[0].defendants[0].arrestSummonsNumber", is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getArrestSummonsNumber())),
                            withJsonPath("$.courtRooms[0].sessions[0].sittings[0].hearing[0].defendants[0].organisationName", is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getEmployerOrganisation().getName())),
                            withJsonPath("$.courtRooms[0].sessions[0].sittings[0].hearing[0].defendants[0].bailStatus", is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailStatus().getDescription()))
                    )));

            postHearingLogEventCommand(requestSpec, initiateHearingCommand, logEvent);

            liveStatusEventListener.waitFor();
            publicEventTopic.waitFor();
        }

        return logEvent;
    }

    private void pollEventLogForHearing(final String hearingId, final String userId, final Matcher[] matchers) {
        poll(requestParams(getURL("hearing.get-hearing-event-log-for-document-by-hearing", hearingId),
                "application/vnd.hearing.get-hearing-event-log-for-documents+json").withHeader(USER_ID, userId))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                matchers
                        ))
                );
    }

}
