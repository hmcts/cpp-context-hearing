package uk.gov.moj.cpp.hearing.it;

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
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.correctLogEvent;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEventThatIsIgnored;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateHearingEvents;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

public class HearingEventsIT extends AbstractIT {

    private final UUID userId = randomUUID();
    private static final UUID COUNSEL_ID = randomUUID();
    @Test
    public void publishEvent_givenStartOfHearing() {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand =
                        logEvent(requestSpec, asDefault(), initiateHearingCommand,
                                        hearingEventDefinition.getId(), false, null, COUNSEL_ID);

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                initiateHearingCommand.getHearing().getId()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].counselId", is(logEventCommand.getCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

    }

    @Test
    public void publishEventWithWitness_givenStartOfHearing() {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(),
                        initiateHearingCommand, hearingEventDefinition.getId(), false, randomUUID(),
                        COUNSEL_ID);

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                initiateHearingCommand.getHearing().getId()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].witnessId", is(logEventCommand.getWitnessId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].counselId", is(logEventCommand.getCounselId().toString())),
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

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Identify defendant");

        assertThat(hearingEventDefinition.isAlterable(), is(true));

        final LogEventCommand logEventCommand =
                        logEvent(requestSpec, asDefault(), initiateHearingCommand,
                                        hearingEventDefinition.getId(), true, null, COUNSEL_ID);

        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                initiateHearingCommand.getHearing().getId()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(logEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(logEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(logEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(logEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].counselId", is(logEventCommand.getCounselId().toString())),
                                withJsonPath("$.events[0].alterable", is(true))
                        ))
                );

    }

    @Test
    public void publishEventCorrection_givenStartHearingEvent() {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");
        final LogEventCommand logEventCommand =
                        logEvent(requestSpec, asDefault(), initiateHearingCommand,
                                        hearingEventDefinition.getId(), false, null, COUNSEL_ID);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, logEventCommand.getHearingEventId(),
                asDefault(), initiateHearingCommand, hearingEventDefinition.getId(), false);


        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                initiateHearingCommand.getHearing().getId()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.events", hasSize(1)),

                                withJsonPath("$.events[0].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString())),
                                withJsonPath("$.events[0].recordedLabel", is(correctLogEventCommand.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", is(ZonedDateTimes.toString(correctLogEventCommand.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", is(ZonedDateTimes.toString(correctLogEventCommand.getLastModifiedTime()))),
                                withJsonPath("$.events[0].counselId", is(correctLogEventCommand.getCounselId().toString())),
                                withJsonPath("$.events[0].witnessId", is(correctLogEventCommand.getWitnessId().toString())),
                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );
    }

    @Test
    public void publishMultipleEventsAndCorrection_shouldReturnInEventTimeOrder() {

        final ZonedDateTime zonedDateTime = PAST_ZONED_DATE_TIME.next();

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition startHearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");
        final LogEventCommand startHearingLogEventCommand = logEvent(requestSpec,
                e -> e.withEventTime(zonedDateTime),
                        initiateHearingCommand, startHearingEventDefinition.getId(), false, null,
                        COUNSEL_ID);

        final CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, startHearingLogEventCommand.getHearingEventId(),
                e -> e.withEventTime(zonedDateTime.plusMinutes(40)),
                initiateHearingCommand, startHearingEventDefinition.getId(), false);

        final HearingEventDefinition identifyDefendantEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Identify defendant");
        final LogEventCommand identifyDefendantLogEventCommand = logEvent(requestSpec,
                e -> e.withEventTime(zonedDateTime.plusMinutes(20)),
                        initiateHearingCommand, identifyDefendantEventDefinition.getId(), true,
                        null, COUNSEL_ID);


        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                initiateHearingCommand.getHearing().getId()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(

                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.events", hasSize(2)),

                                withJsonPath("$.events[0].hearingEventId", is(identifyDefendantLogEventCommand.getHearingEventId().toString())),
                                withJsonPath("$.events[1].hearingEventId", is(correctLogEventCommand.getLatestHearingEventId().toString()))

                        ))
                );
    }

    @Test
    public void publishEvent_hearingEventsUpdated() {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        final LogEventCommand logEventCommand =
                        logEvent(requestSpec, asDefault(), initiateHearingCommand,
                                        hearingEventDefinition.getId(), false, randomUUID(),
                                        COUNSEL_ID);
        final HearingEvent hearingEvent =
                        new HearingEvent(logEventCommand.getHearingEventId(), "RL1");

        final String commandAPIEndPoint = MessageFormat.format(
                        ENDPOINT_PROPERTIES.getProperty("hearing.update-hearing-events"),
                        logEventCommand.getHearingId().toString());

        final JsonObject hearingEventsUpdated = updateHearingEvents(requestSpec,
                        logEventCommand.getHearingId(), Arrays.asList(hearingEvent),
                        commandAPIEndPoint, CPP_UID_HEADER);
        poll(requestParams(getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"),
                        initiateHearingCommand.getHearing().getId()), "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
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
    
    private static HearingEventDefinition findEventDefinitionWithActionLabel(final HearingEventDefinitionData hearingEventDefinitionData, final String actionLabel) {
        return hearingEventDefinitionData.getEventDefinitions().stream().filter(d -> d.getActionLabel().equals(actionLabel)).findFirst().get();
    }

    public static HearingEventDefinitionData hearingDefinitionData(final List<HearingEventDefinition> hearingEventDefinitions) {
        return new HearingEventDefinitionData(randomUUID(), hearingEventDefinitions);
    }

    public static List<HearingEventDefinition> hearingDefinitions() {

        return asList(
                new HearingEventDefinition(randomUUID(), "Start Hearing", "Call Case On", 1, "SENTENCING", null, null, null, false),
                new HearingEventDefinition(randomUUID(), "Identify defendant", "Defendant Identified", 2, "SENTENCING", null, null, null, true),
                new HearingEventDefinition(randomUUID(), "Take Plea", "Plea", 3, "SENTENCING", null, null, null, true),
                new HearingEventDefinition(randomUUID(), "Prosecution Opening", "Prosecution Opening", 4, "SENTENCING", null, null, null, true),
                new HearingEventDefinition(randomUUID(), "<counsel.name>", "Defence <counsel.name> mitigated for <defendant.name>", 5, "SENTENCING", "defendant.name,counsel.name", "Mitigation by:", "defending <defendant.name>", true),
                new HearingEventDefinition(randomUUID(), "Sentencing", "Sentencing", 6, "SENTENCING", null, null, null, true),
                new HearingEventDefinition(randomUUID(), "End Hearing", "Hearing Ended", 7, "SENTENCING", null, null, null, false),
                new HearingEventDefinition(randomUUID(), "Pause", "Hearing paused", 1, "PAUSE_RESUME", null, null, null, false),
                new HearingEventDefinition(randomUUID(), "Resume", "Hearing resumed", 2, "PAUSE_RESUME", null, null, null, false)
        );
    }

}
