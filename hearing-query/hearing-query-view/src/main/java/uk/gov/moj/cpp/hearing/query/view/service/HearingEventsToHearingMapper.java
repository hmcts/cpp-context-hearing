package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.mapping.HearingEventJPAMapper.fromJPA;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S3655"})
//Warning suppressed because hearingEventList Optional will not be empty.
public class HearingEventsToHearingMapper {

    private List<HearingEvent> hearingEvents;
    private List<Hearing> hearingList;

    public HearingEventsToHearingMapper(final List<HearingEvent> hearingEvents, final List<Hearing> hearingList) {
        this.hearingEvents = hearingEvents;
        this.hearingList = hearingList;
    }

    public uk.gov.justice.core.courts.HearingEvent getHearingEventBy(final UUID hearingId) {
        final List<HearingEvent> hearingEventList = hearingEvents
                .stream()
                .filter(hearingEvent -> hearingEvent.getHearingId().equals(hearingId))
                .collect(toList());

        return fromJPA(hearingEventList
                .stream()
                .max(comparing(HearingEvent::getLastModifiedTime)).get());
    }

    public List<Hearing> getHearingList() {
        return hearingList;
    }
}
