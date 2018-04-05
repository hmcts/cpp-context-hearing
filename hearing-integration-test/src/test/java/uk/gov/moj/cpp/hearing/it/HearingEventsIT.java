package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

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
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

public class HearingEventsIT extends AbstractIT {

    private final UUID userId = randomUUID();

    @Test
    public void publishEvent_givenStartOfHearing() {

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        assertThat(hearingEventDefinition.isAlterable(), is(false));

        LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), initiateHearingCommand, hearingEventDefinition.getId(), false);

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

                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );

    }

    @Test
    public void publishHearingIgnoredEvent_givenNoHearing() {

        UUID hearingId = randomUUID();

        givenAUserHasLoggedInAsACourtClerk(userId);

        HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");

        LogEventCommand logEventCommand = logEventThatIsIgnored(requestSpec, asDefault(), hearingId, hearingEventDefinition.getId(),
                hearingEventDefinition.isAlterable(), "Hearing not found");
    }

    @Test
    public void publishEvent_givenIdentifyDefendantEvent() {

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Identify defendant");

        assertThat(hearingEventDefinition.isAlterable(), is(true));

        LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), initiateHearingCommand, hearingEventDefinition.getId(), true);

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

                                withJsonPath("$.events[0].alterable", is(true))
                        ))
                );

    }

    @Test
    public void publishEventCorrection_givenStartHearingEvent() {

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");
        LogEventCommand logEventCommand = logEvent(requestSpec, asDefault(), initiateHearingCommand, hearingEventDefinition.getId(), false);

        CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, logEventCommand.getHearingEventId(),
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

                                withJsonPath("$.events[0].alterable", is(false))
                        ))
                );
    }

    @Test
    public void publishMultipleEventsAndCorrection_shouldReturnInEventTimeOrder() {

        ZonedDateTime zonedDateTime = PAST_ZONED_DATE_TIME.next();

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        givenAUserHasLoggedInAsACourtClerk(userId);

        HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()));

        HearingEventDefinition startHearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Start Hearing");
        LogEventCommand startHearingLogEventCommand = logEvent(requestSpec,
                e -> e.withEventTime(zonedDateTime),
                initiateHearingCommand, startHearingEventDefinition.getId(), false);

        CorrectLogEventCommand correctLogEventCommand = correctLogEvent(requestSpec, startHearingLogEventCommand.getHearingEventId(),
                e -> e.withEventTime(zonedDateTime.plusMinutes(40)),
                initiateHearingCommand, startHearingEventDefinition.getId(), false);

        HearingEventDefinition identifyDefendantEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, "Identify defendant");
        LogEventCommand identifyDefendantLogEventCommand = logEvent(requestSpec,
                e -> e.withEventTime(zonedDateTime.plusMinutes(20)),
                initiateHearingCommand, identifyDefendantEventDefinition.getId(), true);


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

    private static HearingEventDefinition findEventDefinitionWithActionLabel(HearingEventDefinitionData hearingEventDefinitionData, String actionLabel) {
        return hearingEventDefinitionData.getEventDefinitions().stream().filter(d -> d.getActionLabel().equals(actionLabel)).findFirst().get();
    }

    public static HearingEventDefinitionData hearingDefinitionData(List<HearingEventDefinition> hearingEventDefinitions) {
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
