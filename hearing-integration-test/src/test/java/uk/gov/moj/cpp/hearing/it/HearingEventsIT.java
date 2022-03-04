package uk.gov.moj.cpp.hearing.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.JsonObject;
import java.text.MessageFormat;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR;
import static uk.gov.moj.cpp.hearing.it.UseCases.*;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

@SuppressWarnings("unchecked")
@NotThreadSafe
public class HearingEventsIT extends AbstractIT {

    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));
    private final UUID DEFENCE_COUNSEL_ID = randomUUID();

    @BeforeClass
    public static void setupPerClass() {
        UUID userId = randomUUID();
        setupAsAuthorisedUser(userId);
    }

    @Test
    public void publishEvent_givenStartOfHearing() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());


        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, "testNote");

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false)),
                                withJsonPath("$.events[0].note", is("testNote"))
                        ))
                );

    }

    @Test
    public void publishEvent_givenHearingForMags() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());


        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

    }

    @Test
    public void publishEventWithWitness_givenStartOfHearing() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(),
                hearingOne.it(), hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                hearingOne.getHearingId(), EVENT_TIME.toLocalDate()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

    }

    @Test
    public void publishHearingIgnoredEvent_givenNoHearing() {


        final UUID hearingId = randomUUID();

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        final LogEventCommand logEventCommand = logEventThatIsIgnored(getRequestSpec(), asDefault(), hearingId, hearingEventDefinition.getId(),
                hearingEventDefinition.isAlterable(), "Hearing not found");
    }

    @Test
    public void publishEvent_givenIdentifyDefendantEvent() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Identify defendant");

        assertThat(hearingEventDefinition.isAlterable(), is(true));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(), hearingEventDefinition.getId(), true, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                hearingOne.getHearingId(), EVENT_TIME.toLocalDate()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(true))
                        ))
                );

    }

    @Test
    public void publishEventCorrection_givenStartHearingEvent() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");
        final LogEventCommand logEventCommand =
                logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                        hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(getRequestSpec(), logEventCommand.getHearingEventId(),
                asDefault(), hearingOne.it(), hearingEventDefinition.getId(), false, EVENT_TIME);


        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                hearingOne.getHearingId(), EVENT_TIME.toLocalDate()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(correctLogEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(correctLogEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(correctLogEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(correctLogEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );
    }

    @Test
    public void publishMultipleEventsAndCorrection_shouldReturnInEventTimeOrder() {


        final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.of(2019, Month.APRIL, 10), LocalTime.of(22, 1), ZoneId.of("UTC"));

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

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

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                hearingOne.getHearingId(), zonedDateTime.toLocalDate()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(2)),

                                withJsonPath("$.events[0].hearingEventId", is(identifyDefendantLogEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[1].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString()))

                        ))
                );
    }

    @Test
    public void publishEvent_hearingEventsUpdated() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        final HearingEvent hearingEvent = new HearingEvent(logEventCommand.getHearingEventId(), "RL1" ,"note");

        final String commandAPIEndPoint = MessageFormat.format(
                ENDPOINT_PROPERTIES.getProperty("hearing.update-hearing-events"),
                logEventCommand.getHearingId().toString());

        final JsonObject hearingEventsUpdated = updateHearingEvents(getRequestSpec(),
                logEventCommand.getHearingId(), asList(hearingEvent),
                commandAPIEndPoint, getLoggedIdUserHeader());

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                hearingOne.getHearingId(), EVENT_TIME.toLocalDate()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(logEventCommand.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(hearingEvent.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel",
                                        is(hearingEvent.getRecordedLabel()))
                        ))
                );

    }

    @Test
    public void publishEvent_givenStartOfHearing_pauseActiveHearing() throws JsonProcessingException {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

        final InitiateHearingCommandHelper hearingTwo = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        hearingTwo.getHearing().setCourtCentre(hearingOne.getHearing().getCourtCentre());

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinitionForSecondEvent = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinitionForSecondEvent.isAlterable(), is(false));

        final LogEventCommand logEventCommandForSameHearing = logEventForOverrideCourtRoom(getRequestSpec(), asDefault(), hearingTwo.it(),
                hearingEventDefinitionForSecondEvent.getId(), false, DEFENCE_COUNSEL_ID, true, EVENT_TIME);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingTwo.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingTwo.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].hearingEventId", is(logEventCommandForSameHearing.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommandForSameHearing.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommandForSameHearing.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommandForSameHearing.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommandForSameHearing.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

    }

    @Test
    public void publishEvent_givenStartOfHearing_NoActiveHearingsReturned() throws JsonProcessingException {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommandForSameHearing = logEventForOverrideCourtRoom(getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, true, EVENT_TIME);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].hearingEventId", is(logEventCommandForSameHearing.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommandForSameHearing.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommandForSameHearing.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommandForSameHearing.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(logEventCommandForSameHearing.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

    }

    @Test
    public void publishEventCorrection_givenStartHearingForMags() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Start Hearing");
        final LogEventCommand logEventCommand =
                logEvent(getRequestSpec(), asDefault(), hearingOne.it(),
                        hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME, null);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(getRequestSpec(), logEventCommand.getHearingEventId(),
                asDefault(), hearingOne.it(), hearingEventDefinition.getId(), false, EVENT_TIME);

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                hearingOne.getHearingId(), EVENT_TIME.toLocalDate()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(correctLogEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(correctLogEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(correctLogEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].defenceCounselId", is(correctLogEventCommand.getDefenceCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );
    }

    @Test
    public void amendEvent_givenStartOfHearing() {


        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

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
}
