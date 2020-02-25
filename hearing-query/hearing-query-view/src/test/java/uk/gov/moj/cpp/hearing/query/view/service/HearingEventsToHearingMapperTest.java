package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent.hearingEvent;

import uk.gov.moj.cpp.hearing.mapping.HearingEventJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventsToHearingMapperTest {
    private static final UUID FINISHED_EVENT = UUID.fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe");
    @Test
    public void shouldRetrieveLatestHearingEvent() {

        final UUID hearingId = randomUUID();

        final HearingEvent hearingEvent_1 = hearingEvent()
                .setHearingEventDefinitionId(UUID.randomUUID())
                .setLastModifiedTime(now().minusMinutes(10))
                .setHearingId(hearingId);

        final ZonedDateTime expectedlastModifiedTime = now();
        final HearingEvent hearingEvent_2 = hearingEvent()
                .setHearingEventDefinitionId(FINISHED_EVENT)
                .setLastModifiedTime(expectedlastModifiedTime)
                .setEventDate(LocalDate.now())
                .setHearingId(hearingId);

        final HearingEventsToHearingMapper hearingEventsToHearingMapper = new HearingEventsToHearingMapper(
                asList(hearingEvent_1, hearingEvent_2),
                asList(hearing().build()),
                new ArrayList<>());

        final uk.gov.justice.core.courts.HearingEvent resultHearingEvent = getActiveHearingEventBy(hearingEventsToHearingMapper, hearingId).get();

        assertThat(resultHearingEvent.getLastModifiedTime(), is(expectedlastModifiedTime));
    }


    public Optional<uk.gov.justice.core.courts.HearingEvent> getActiveHearingEventBy(final HearingEventsToHearingMapper mapper,
                                                                                     final UUID hearingId) {
        final List<HearingEvent> hearingEventList = mapper.getActiveHearingEvents()
                .stream()
                .filter(hearingEvent -> hearingEvent.getHearingId().equals(hearingId))
                .collect(toList());

        return Optional.ofNullable((hearingEventList
                .stream()
                .max(comparing(HearingEvent::getLastModifiedTime)).map(HearingEventJPAMapper::fromJPA).orElse(null)));
    }
}