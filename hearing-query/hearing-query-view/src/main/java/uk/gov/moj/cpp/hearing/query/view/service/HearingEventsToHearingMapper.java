package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.mapping.HearingEventJPAMapper.fromJPA;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"squid:S3655"})
//Warning suppressed because hearingEventList Optional will not be empty.
public class HearingEventsToHearingMapper {

    private List<HearingEvent> allHearingEvents;
    private List<HearingEvent> activeHearingEvents;

    private List<Hearing> hearingList;
    private Set<UUID> activeHearingIds = new HashSet<>();
    private Map<UUID, UUID> hearingIdAndEventDefinitionIds = new HashMap<>();

    public HearingEventsToHearingMapper(final List<HearingEvent> activeHearingEvents,
                                        final List<Hearing> hearingList,
                                        final List<HearingEvent> allHearingEvents) {

        this.allHearingEvents = allHearingEvents;
        this.activeHearingEvents = activeHearingEvents;
        this.hearingList = hearingList;
        for (final HearingEvent hearingEvent : activeHearingEvents) {
            final UUID hearingId = hearingEvent.getHearingId();
            activeHearingIds.add(hearingId);
        }

        for (final HearingEvent hearingEvent : allHearingEvents) {
            final UUID hearingId = hearingEvent.getHearingId();
            final UUID hearingEventDefinitionId = hearingEvent.getHearingEventDefinitionId();
                hearingIdAndEventDefinitionIds.putIfAbsent(hearingId, hearingEventDefinitionId);
        }
    }

    public Optional<uk.gov.justice.core.courts.HearingEvent> getAllHearingEventBy(final UUID hearingId) {
        final List<HearingEvent> hearingEventList = allHearingEvents
                .stream()
                .filter(hearingEvent -> hearingEvent.getHearingId().equals(hearingId))
                .collect(toList());

        return Optional.ofNullable((hearingEventList
                .stream()
                .max(comparing(HearingEvent::getLastModifiedTime)).map(this::mapFromJPA).orElse(null)));
    }

    private uk.gov.justice.core.courts.HearingEvent mapFromJPA(final HearingEvent hearingEvent)  {
        return  fromJPA(hearingEvent);
    }

    public List<Hearing> getHearingList() {
        return hearingList;
    }

    public List<HearingEvent> getActiveHearingEvents() {
        return activeHearingEvents;
    }

    public Set<UUID> getActiveHearingIds() {
        return activeHearingIds;
    }

    public Map<UUID, UUID> getHearingIdAndEventDefinitionIds() {
        return hearingIdAndEventDefinitionIds;
    }
}
