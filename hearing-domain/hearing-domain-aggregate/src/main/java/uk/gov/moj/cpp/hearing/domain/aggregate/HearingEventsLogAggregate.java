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

    public Stream<Object> logHearingEvent(UUID hearingId, UUID hearingEventId, String recordedLabel, ZonedDateTime timestamp) {
        if(hearingEventWithId(hearingEventId).isPresent()) {
            return Stream.empty();
        }

        hearingEvents.add(new HearingEvent(hearingEventId, timestamp));
        return Stream.of(new HearingEventLogged(hearingEventId, hearingId, recordedLabel, timestamp));
    }

    public Stream<Object> correctEvent(UUID hearingId, UUID hearingEventId, ZonedDateTime timestamp) {
        Optional<HearingEvent> eventToChange = hearingEventWithId(hearingEventId);

        if(!eventToChange.isPresent()) {
            //This cannot happen as events cannot yet deleted. We should reconsider this case when we play that story.
            return Stream.empty();
        }

        if(eventToChange.get().getTimestamp() == timestamp) {
            return Stream.empty();
        }

        eventToChange.get().setTimestamp(timestamp);

        return Stream.of(new HearingEventCorrected(hearingId, hearingEventId, timestamp));
    }

    private void onEventLogged(HearingEventLogged eventLogged) {

        hearingEvents.add(new HearingEvent(eventLogged.getId(), eventLogged.getTimestamp()));
    }

    private void onEventCorrected(HearingEventCorrected eventCorrected) {
        Optional<HearingEvent> eventToChange = hearingEventWithId(eventCorrected.getHearingEventId());

        if(eventToChange.isPresent()) {
            eventToChange.get().setTimestamp(eventCorrected.getTimestamp());
        }
    }

    private Optional<HearingEvent> hearingEventWithId(UUID eventId) {
        return hearingEvents.stream().filter(event -> eventId.equals(event.getHearingEventId())).findFirst();
    }

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(HearingEventLogged.class).apply(this::onEventLogged),
                when(HearingEventCorrected.class).apply(this::onEventCorrected),
                otherwiseDoNothing()
        );
    }


}
