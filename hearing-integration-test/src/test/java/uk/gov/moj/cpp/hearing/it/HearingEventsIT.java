package uk.gov.moj.cpp.hearing.it;

import java.time.ZonedDateTime;
import java.util.Arrays;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andCorrectsTheTimeOfThatHearingEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingIsNotStarted;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventIsRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenItFailsForMissingTimestamp;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenTheEventsShouldBeListedInTheSpecifiedOrder;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenTheHearingEventHasTheUpdatedTimestamp;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserAttemptsToStartAHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserLogsAnEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingEventWithMissingTimestamp;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingStartedEvent;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.identifyDefendentEvent;

import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.util.UUID;

import com.jayway.restassured.response.Response;
import org.junit.Test;

public class HearingEventsIT {

    private final UUID userId = randomUUID();
    private final UUID hearingId = randomUUID();

    private final ZonedDateTime PAST_TIMESTAMP = ZonedDateTime.now().minusMonths(2);
    private final ZonedDateTime DIFFERENT_TIMESTAMP = PAST_ZONED_DATE_TIME.next();

    @Test
    public void shouldBeAbleToStartAHearingByAnAuthorisedUser() {
        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingIsNotStarted(userId, hearingId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        whenUserLogsAnEvent(userId, hearingStartedEvent);

        thenHearingEventIsRecorded(userId, hearingStartedEvent);
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
        whenUserLogsAnEvent(userId, hearingStartedEvent);

        andCorrectsTheTimeOfThatHearingEvent(userId, hearingStartedEvent, DIFFERENT_TIMESTAMP);

        thenTheHearingEventHasTheUpdatedTimestamp(userId, hearingStartedEvent.getHearingId(), DIFFERENT_TIMESTAMP);
    }

    @Test
    public void shouldReceiveTimesInChronologicalOrder() {
        givenAUserHasLoggedInAsACourtClerk(userId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        whenUserLogsAnEvent(userId, hearingStartedEvent);

        final HearingEvent identifyDefendentEvent = identifyDefendentEvent(hearingId);
        whenUserLogsAnEvent(userId, identifyDefendentEvent);

        final HearingEvent updatedIdentifyDefendentEvent = new HearingEvent(identifyDefendentEvent.getId(), identifyDefendentEvent.getHearingId(), identifyDefendentEvent.getRecordedLabel(), PAST_TIMESTAMP);
        andCorrectsTheTimeOfThatHearingEvent(userId, identifyDefendentEvent, PAST_TIMESTAMP);

        thenTheEventsShouldBeListedInTheSpecifiedOrder(userId, hearingId, Arrays.asList(updatedIdentifyDefendentEvent, hearingStartedEvent));
    }

}
