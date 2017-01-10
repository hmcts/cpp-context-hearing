package uk.gov.moj.cpp.hearing.domain.aggregate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import uk.gov.justice.domain.aggregate.Aggregate;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.doNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingEnded;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingStarted;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;

public class HearingEventsLogAggregate implements Aggregate {

    private List<HearingEvent> hearingEvents = new ArrayList<>();

    public Stream<Object> logHearingEvent(final UUID hearingId, final UUID hearingEventId, final String recordedLabel, final ZonedDateTime timestamp) {
        if(hearingEventWithId(hearingEventId).isPresent()) {
            return Stream.empty();
        }

        hearingEvents.add(new HearingEvent(hearingEventId, timestamp));
        return Stream.of(new HearingEventLogged(hearingEventId, hearingId, recordedLabel, timestamp));
    }

    public Stream<Object> correctEvent(final UUID hearingId, final UUID hearingEventId, final ZonedDateTime timestamp) {
        Optional<HearingEvent> eventToChange = hearingEventWithId(hearingEventId);

        if(!eventToChange.isPresent()) {
            //This cannot happen as events cannot yet deleted. We should reconsider this case when we play that story.
            return Stream.empty();
        }

        if(eventToChange.get().getTimestamp() == timestamp) {
            return Stream.empty();
        }

        HearingEvent replacementEvent = new HearingEvent(eventToChange.get().getHearingEventId(), timestamp);
        hearingEvents.set(hearingEvents.indexOf(eventToChange.get()), replacementEvent);

        return Stream.of(new HearingEventCorrected(hearingId, hearingEventId, timestamp));
    }

    private void onEventLogged(final HearingEventLogged eventLogged) {

        hearingEvents.add(new HearingEvent(eventLogged.getId(), eventLogged.getTimestamp()));
    }

    private void onEventCorrected(final HearingEventCorrected eventCorrected) {
        Optional<HearingEvent> eventToChange = hearingEventWithId(eventCorrected.getHearingEventId());

        if(eventToChange.isPresent()) {
            HearingEvent replacementEvent = new HearingEvent(eventToChange.get().getHearingEventId(), eventCorrected.getTimestamp());
            hearingEvents.set(hearingEvents.indexOf(eventToChange.get()), replacementEvent);
        }
    }

    private Optional<HearingEvent> hearingEventWithId(final UUID eventId) {
        return hearingEvents.stream().filter(event -> eventId.equals(event.getHearingEventId())).findFirst();
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingEventLogged.class).apply(this::onEventLogged),
                when(HearingEventCorrected.class).apply(this::onEventCorrected),
                otherwiseDoNothing()
        );
    }


}
