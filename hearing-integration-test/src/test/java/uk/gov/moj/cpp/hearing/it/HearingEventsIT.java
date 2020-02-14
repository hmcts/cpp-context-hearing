package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.correctLogEvent;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEventForOverrideCourtRoom;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEventThatIsIgnored;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateHearingEvents;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;

@SuppressWarnings("unchecked")
@NotThreadSafe
public class HearingEventsIT extends AbstractIT {

    private static final UUID DEFENCE_COUNSEL_ID = randomUUID();
    private final UUID userId = randomUUID();

    private static final UUID START_HEARING_EVENT_DEFINITION_ID = fromString("b71e7d2a-d3b3-4a55-a393-6d451767fc05");
    private static final UUID RESUME_HEARING_EVENT_DEFINITION_ID = fromString("64476e43-2138-46d5-b58b-848582cf9b07");
    private static final UUID PAUSE_HEARING_EVENT_DEFINITION_ID = fromString("160ecb51-29ee-4954-bbbf-daab18a24fbb");
    private static final UUID END_HEARING_EVENT_DEFINITION_ID = fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe");
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));

    private static HearingEventDefinition findEventDefinitionWithActionLabel(final HearingEventDefinitionData hearingEventDefinitionData, final String actionLabel) {
        return hearingEventDefinitionData.getEventDefinitions().stream().filter(d -> d.getActionLabel().equals(actionLabel)).findFirst().get();
    }

    public static HearingEventDefinitionData hearingDefinitionData(final List<HearingEventDefinition> hearingEventDefinitions) {
        return new HearingEventDefinitionData(randomUUID(), hearingEventDefinitions);
    }

    public static List<HearingEventDefinition> hearingDefinitions() {

        return asList(
                new HearingEventDefinition(START_HEARING_EVENT_DEFINITION_ID, "Start Hearing", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(randomUUID(), "Identify defendant", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "Take Plea", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "Prosecution Opening", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "<counsel.name>", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "Sentencing", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(END_HEARING_EVENT_DEFINITION_ID, "End Hearing", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(PAUSE_HEARING_EVENT_DEFINITION_ID, "Pause", INTEGER.next(), STRING.next(), "PAUSE_RESUME", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(RESUME_HEARING_EVENT_DEFINITION_ID, "Resume", INTEGER.next(), STRING.next(), "PAUSE_RESUME", STRING.next(), INTEGER.next(), false)
        );
    }

    @Test
    public void publishEvent_givenStartOfHearing() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

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
    public void publishEvent_givenHearingForMags() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingTemplateForMagistrates()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(30, TimeUnit.SECONDS)
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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(),
                hearingOne.it(), hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

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

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        final LogEventCommand logEventCommand = logEventThatIsIgnored(requestSpec, asDefault(), hearingId, hearingEventDefinition.getId(),
                hearingEventDefinition.isAlterable(), "Hearing not found");
    }

    @Test
    public void publishEvent_givenIdentifyDefendantEvent() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Identify defendant");

        assertThat(hearingEventDefinition.isAlterable(), is(true));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), hearingOne.it(), hearingEventDefinition.getId(), true, DEFENCE_COUNSEL_ID, EVENT_TIME);

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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");
        final LogEventCommand logEventCommand =
                logEvent(requestSpec, asDefault(), hearingOne.it(),
                        hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, logEventCommand.getHearingEventId(),
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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition startHearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        final LogEventCommand startHearingLogEventCommand = logEvent(requestSpec,
                e -> e.withEventTime(zonedDateTime),
                hearingOne.it(), startHearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, zonedDateTime);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, startHearingLogEventCommand.getHearingEventId(),
                e -> e.withEventTime(zonedDateTime.plusMinutes(40)),
                hearingOne.it(), startHearingEventDefinition.getId(), false, zonedDateTime);

        final HearingEventDefinition identifyDefendantEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Identify defendant");
        final LogEventCommand identifyDefendantLogEventCommand = logEvent(requestSpec,
                e -> e.withEventTime(zonedDateTime.plusMinutes(20)),
                hearingOne.it(), identifyDefendantEventDefinition.getId(), true,
                DEFENCE_COUNSEL_ID, zonedDateTime);

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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

        final HearingEvent hearingEvent = new HearingEvent(logEventCommand.getHearingEventId(), "RL1");

        final String commandAPIEndPoint = MessageFormat.format(
                ENDPOINT_PROPERTIES.getProperty("hearing.update-hearing-events"),
                logEventCommand.getHearingId().toString());

        final JsonObject hearingEventsUpdated = updateHearingEvents(requestSpec,
                logEventCommand.getHearingId(), asList(hearingEvent),
                commandAPIEndPoint, CPP_UID_HEADER);

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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(30, TimeUnit.SECONDS)
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

        final InitiateHearingCommandHelper hearingTwo = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));
        hearingTwo.getHearing().setCourtCentre(hearingOne.getHearing().getCourtCentre());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionDataForSecondEvent = andHearingEventDefinitionsAreAvailable(new HearingEventDefinitionData(randomUUID(), hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinitionForSecondEvent = findEventDefinitionWithActionLabel(hearingEventDefinitionDataForSecondEvent, "Start Hearing");

        assertThat(hearingEventDefinitionForSecondEvent.isAlterable(), is(false));

        final LogEventCommand logEventCommandForSameHearing = logEventForOverrideCourtRoom(requestSpec, asDefault(), hearingTwo.it(),
                hearingEventDefinitionForSecondEvent.getId(), false, DEFENCE_COUNSEL_ID, true, EVENT_TIME);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingTwo.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(30, TimeUnit.SECONDS)
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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));
        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(new HearingEventDefinitionData(randomUUID(), hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommandForSameHearing = logEventForOverrideCourtRoom(requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, true, EVENT_TIME);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingOne.getHearingId(), EVENT_TIME.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(30, TimeUnit.SECONDS)
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

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, initiateHearingTemplateForMagistrates()));

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");
        final LogEventCommand logEventCommand =
                logEvent(requestSpec, asDefault(), hearingOne.it(),
                        hearingEventDefinition.getId(), false, DEFENCE_COUNSEL_ID, EVENT_TIME);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, logEventCommand.getHearingEventId(),
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
}
