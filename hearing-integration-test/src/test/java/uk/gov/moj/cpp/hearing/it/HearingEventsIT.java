package uk.gov.moj.cpp.hearing.it;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingIsNotStarted;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andLogsAnotherEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andUserLogsAnEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenItFailsForMissingTimestamp;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenOnlySpecifiedHearingEventIsRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenTheEventsShouldBeListedInTheSpecifiedOrder;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenTheHearingEventHasTheUpdatedTimestamp;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserAttemptsToStartAHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserCorrectsTheTimeOfTheHearingEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserLogsAnEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingEventWithMissingTimestamp;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingStartedEvent;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.identifyDefendantEvent;

import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.jayway.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;

public class HearingEventsIT {

    private final UUID userId = randomUUID();
    private final UUID hearingId = randomUUID();

    private final ZonedDateTime PAST_TIMESTAMP = PAST_ZONED_DATE_TIME.next();

    @Test
    public void shouldBeAbleToStartAHearingByAnAuthorisedUser() {
        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingIsNotStarted(userId, hearingId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        whenUserLogsAnEvent(userId, hearingStartedEvent);

        thenOnlySpecifiedHearingEventIsRecorded(userId, hearingStartedEvent);
    }

    @Test
    public void shouldRejectAnHearingEventWhenTimestampIsMissing() {
        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingIsNotStarted(userId, hearingId);

        final Response response = whenUserAttemptsToStartAHearing(userId, hearingEventWithMissingTimestamp(hearingId));
        thenItFailsForMissingTimestamp(response);
    }

    @Test
    public void shouldBeAbleToCorrectTimeOfAHearingEvent() {
        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        andUserLogsAnEvent(userId, hearingStartedEvent);

        final UUID newHearingEventId = randomUUID();
        whenUserCorrectsTheTimeOfTheHearingEvent(userId, hearingStartedEvent, PAST_TIMESTAMP, newHearingEventId);

        thenTheHearingEventHasTheUpdatedTimestamp(userId, hearingStartedEvent, PAST_TIMESTAMP, newHearingEventId);
    }

    @Test
    public void shouldReceiveHearingEventsInChronologicalOrder() {
        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        andUserLogsAnEvent(userId, hearingStartedEvent);

        final HearingEvent identifyDefendantEvent = identifyDefendantEvent(hearingId);
        andLogsAnotherEvent(userId, identifyDefendantEvent);

        final UUID newHearingEventId = randomUUID();
        whenUserCorrectsTheTimeOfTheHearingEvent(userId, identifyDefendantEvent, PAST_TIMESTAMP, newHearingEventId);

        final HearingEvent updatedIdentifyDefendantEvent = identifyDefendantEvent.builder()
                .withId(newHearingEventId)
                .withTimestamp(PAST_TIMESTAMP)
                .build();
        thenTheEventsShouldBeListedInTheSpecifiedOrder(userId, hearingId, asList(updatedIdentifyDefendantEvent, hearingStartedEvent));
    }

    @Test
    @Ignore("WIP")
    public void shouldBeAbleToGenerateAndRecordMitigationEventsThatRequireDefendantAndDefenceCounsel() {
        // Given option to log event with defendant and defence counsel is available

        // and user has logged in as court clerk

        // when user records mitigation by defence counsel for defendant

        // then the mitigation is recorded along with defence counsel and defendant
    }

}
