package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent.hearingEvent;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.mapping.HearingEventJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Init;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventsToHearingMapperTest {
    private static final UUID FINISHED_EVENT = UUID.fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe");

    @Mock
    private HearingEvent activeHearingEvents;
    @Mock
    private Hearing hearingList;

    @Mock
    private HearingEvent hearingEventMock;

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

    @Test
    public void shouldGetAllHearingEventBy(){
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = new HearingEvent();
        hearingEvent.setHearingId(hearingId);
        hearingEvent.setLastModifiedTime(ZonedDateTime.now());
        hearingEvent.setEventDate(LocalDate.now());
        HearingEventsToHearingMapper mapper = new HearingEventsToHearingMapper(Arrays.asList(activeHearingEvents),
                Arrays.asList(hearingList), Arrays.asList(hearingEvent));
        final Optional<uk.gov.justice.core.courts.HearingEvent> hearingEvents = mapper.getAllHearingEventBy(hearingId);
        assertThat(hearingEvents.isPresent(), is(true));
        assertThat(hearingEvents.get().getHearingId(), is(hearingId));
    }

    @Test
    public void shouldGetHearingsList(){
        HearingEventsToHearingMapper mapper = new HearingEventsToHearingMapper(Arrays.asList(activeHearingEvents),
                Arrays.asList(hearingList), Arrays.asList(hearingEventMock));
        final List<Hearing> hearingList = mapper.getHearingList();
        assertThat(hearingList.size(), is(1));
    }

    @Test
    public void shouldGetActiveHearingIds(){
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = new HearingEvent();
        hearingEvent.setHearingId(hearingId);
        hearingEvent.setLastModifiedTime(ZonedDateTime.now());
        hearingEvent.setEventDate(LocalDate.now());
        HearingEventsToHearingMapper mapper = new HearingEventsToHearingMapper(Arrays.asList(hearingEvent),
                Arrays.asList(hearingList), Arrays.asList(hearingEventMock));
        final Set<UUID> activeHearingIds = mapper.getActiveHearingIds();
        assertThat(activeHearingIds.size(), is(1));
        assertThat(activeHearingIds.stream().findFirst().get(), is(hearingId));
    }

    @Test
    public void shouldGetHearingIdAndEventDefinitionIds(){
        final UUID hearingId = randomUUID();
        final UUID hearingDefId = randomUUID();
        final HearingEvent hearingEvent = new HearingEvent();
        hearingEvent.setHearingId(hearingId);
        hearingEvent.setLastModifiedTime(ZonedDateTime.now());
        hearingEvent.setEventDate(LocalDate.now());
        hearingEvent.setHearingEventDefinitionId(hearingDefId);
        HearingEventsToHearingMapper mapper = new HearingEventsToHearingMapper(Arrays.asList(activeHearingEvents),
                Arrays.asList(hearingList), Arrays.asList(hearingEvent));
        final Map<UUID, UUID> hearingIdAndEventDefinitionIds = mapper.getHearingIdAndEventDefinitionIds();
        assertThat(hearingIdAndEventDefinitionIds.size(), is(1));
        assertThat(hearingIdAndEventDefinitionIds.get(hearingId), is(hearingDefId));
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