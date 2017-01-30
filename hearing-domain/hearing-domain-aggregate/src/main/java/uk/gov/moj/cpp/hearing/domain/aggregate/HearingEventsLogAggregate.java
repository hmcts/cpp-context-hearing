package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Stream.concat;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeletionIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

public class HearingEventsLogAggregate implements Aggregate {

    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing Event not found";

    private ConcurrentSkipListSet<UUID> hearingEventsLogged = new ConcurrentSkipListSet<>();
    private ConcurrentSkipListSet<UUID> hearingEventsDeleted = new ConcurrentSkipListSet<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingEventLogged.class).apply(this::onEventLogged),
                when(HearingEventDeleted.class).apply(this::onEventDeleted),
                when(HearingEventDeletionIgnored.class).apply(this::onEventDeletionIgnored),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> logHearingEvent(final UUID hearingId, final UUID hearingEventId, final String recordedLabel,
                                          final ZonedDateTime timestamp) {
        if (hearingEventPreviouslyLogged(hearingEventId)) {
            return apply(Stream.of(new HearingEventIgnored(hearingEventId, hearingId, recordedLabel, timestamp, REASON_ALREADY_LOGGED)));
        } else if (hearingEventPreviouslyDeleted(hearingEventId)) {
            return apply(Stream.of(new HearingEventIgnored(hearingEventId, hearingId, recordedLabel, timestamp, REASON_ALREADY_DELETED)));
        }

        return apply(Stream.of(new HearingEventLogged(hearingEventId, hearingId, recordedLabel, timestamp)));
    }

    public Stream<Object> correctEvent(final UUID hearingId, final UUID hearingEventId, final String recordedLabel,
                                       final ZonedDateTime timestamp, final UUID latestHearingEventId) {
        return concat(logHearingEvent(hearingId, latestHearingEventId, recordedLabel, timestamp), deleteHearingEvent(hearingEventId));
    }

    public Stream<Object> deleteHearingEvent(final UUID hearingEventId) {
        if (!hearingEventPreviouslyLogged(hearingEventId)) {
            return apply(Stream.of(new HearingEventDeletionIgnored(hearingEventId, REASON_EVENT_NOT_FOUND)));
        }

        return apply(Stream.of(new HearingEventDeleted(hearingEventId)));
    }

    private void onEventLogged(final HearingEventLogged eventLogged) {
        hearingEventsLogged.add(eventLogged.getHearingEventId());
    }

    private void onEventDeleted(final HearingEventDeleted eventDeleted) {
        hearingEventsDeleted.add(eventDeleted.getHearingEventId());
    }

    private void onEventDeletionIgnored(final HearingEventDeletionIgnored eventDeletionIgnored) {
        hearingEventsDeleted.add(eventDeletionIgnored.getHearingEventId());
    }

    private boolean hearingEventPreviouslyLogged(final UUID hearingEventId) {
        return hearingEventsLogged.contains(hearingEventId);
    }

    private boolean hearingEventPreviouslyDeleted(final UUID hearingEventId) {
        return hearingEventsDeleted.contains(hearingEventId);
    }

}
