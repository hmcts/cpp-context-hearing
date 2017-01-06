package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingIsNotStarted;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenHearingEventIsRecorded;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.thenItFailsForMissingTimestamp;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserAttemptsToStartAHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.whenUserStartsAHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingEventWithMissingTimestamp;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingStartedEvent;

import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.util.UUID;

import com.jayway.restassured.response.Response;
import org.junit.Test;

public class HearingEventsIT {

    private final UUID userId = randomUUID();
    private final UUID hearingId = randomUUID();

    @Test
    public void shouldBeAbleToStartAHearingByAnAuthorisedUser() {
        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingIsNotStarted(userId, hearingId);

        final HearingEvent hearingStartedEvent = hearingStartedEvent(hearingId);
        whenUserStartsAHearing(userId, hearingStartedEvent);

        thenHearingEventIsRecorded(userId, hearingStartedEvent);
    }

    @Test
    public void shouldRejectAnHearingEventWhenTimestampIsMissing() {
        givenAUserHasLoggedInAsACourtClerk(userId);
        andHearingIsNotStarted(userId, hearingId);

        final Response response = whenUserAttemptsToStartAHearing(userId, hearingEventWithMissingTimestamp(hearingId));
        thenItFailsForMissingTimestamp(response);
    }

}
