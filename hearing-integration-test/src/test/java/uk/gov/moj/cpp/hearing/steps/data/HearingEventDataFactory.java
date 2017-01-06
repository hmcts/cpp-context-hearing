package uk.gov.moj.cpp.hearing.steps.data;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingEventDataFactory {

    public static HearingEvent hearingEventWithMissingTimestamp(final UUID hearingId) {
        final UUID hearingEventId = randomUUID();
        final String randomRecordedLabel = STRING.next();

        return new HearingEvent(hearingEventId, hearingId, randomRecordedLabel, null);
    }

    public static HearingEvent hearingStartedEvent(final UUID hearingId) {
        final UUID hearingEventId = randomUUID();
        final String hearingStartedLabel = "Hearing Started";
        final ZonedDateTime timestamp = new UtcClock().now();

        return new HearingEvent(hearingEventId, hearingId, hearingStartedLabel, timestamp);
    }

}
