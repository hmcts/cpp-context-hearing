package uk.gov.moj.cpp.hearing.repository;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.LONG;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class HearingEventRepositoryTest extends BaseTransactionalTest {

    private static final UUID HEARING_ID_1 = randomUUID();
    private static final UUID HEARING_ID_2 = randomUUID();
    private static final boolean ALTERABLE = BOOLEAN.next();

    private static final UUID HEARING_EVENT_ID_1 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_1 = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime LAST_MODIFIED_TIME = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_ID_2 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_2 = randomUUID();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final ZonedDateTime EVENT_TIME_2 = EVENT_TIME.plusMinutes(1);
    private static final ZonedDateTime LAST_MODIFIED_TIME_2 = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_ID_3 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_3 = randomUUID();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final ZonedDateTime EVENT_TIME_3 = EVENT_TIME_2.plusMinutes(1);
    private static final ZonedDateTime LAST_MODIFIED_TIME_3 = PAST_ZONED_DATE_TIME.next();

    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final UUID COURT_ROOM_ID = randomUUID();

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Test
    public void shouldLogAnHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE)
        );

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findOptionalById(HEARING_EVENT_ID_1);

        assertThat(hearingEvent.isPresent(), is(true));
        assertThat(hearingEvent.orElse(null).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvent.orElse(null).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_1));
        assertThat(hearingEvent.orElse(null).getId(), is(HEARING_EVENT_ID_1));
        assertThat(hearingEvent.orElse(null).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvent.orElse(null).getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvent.orElse(null).getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(hearingEvent.orElse(null).isDeleted(), is(false));
    }

    @Test
    public void shouldNotBeAbleToSeeADeletedHearingEvent() {
        givenNoHearingEventsExist();

        hearingEventRepository.save(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE)
                        .setDeleted(true)
        );

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findOptionalById(HEARING_EVENT_ID_1);

        assertThat(hearingEvent.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventsForAHearingWhichAreNotDeleted() {
        givenHearingEventsExistWithDeletedOnes();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID_1);
        assertThat(hearingEvents.size(), is(2));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(0).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_1));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID_1));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvents.get(0).getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvents.get(0).getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(hearingEvents.get(0).isDeleted(), is(false));

        assertThat(hearingEvents.get(1).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(1).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(1).getEventTime(), is(EVENT_TIME_2));
        assertThat(hearingEvents.get(1).getLastModifiedTime(), is(LAST_MODIFIED_TIME_2));
        assertThat(hearingEvents.get(1).isDeleted(), is(false));
    }

    @Test
    public void shouldGetHearingEventsForAHearing() {
        givenHearingEventsExistWithDeletedOnes();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID_1, EVENT_TIME.toLocalDate());
        assertThat(hearingEvents.size(), is(2));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(0).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_1));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID_1));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvents.get(0).getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvents.get(0).getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(hearingEvents.get(0).isDeleted(), is(false));

        assertThat(hearingEvents.get(1).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(1).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(1).getEventTime(), is(EVENT_TIME_2));
        assertThat(hearingEvents.get(1).getLastModifiedTime(), is(LAST_MODIFIED_TIME_2));
        assertThat(hearingEvents.get(1).isDeleted(), is(false));
    }

    @Test
    public void shouldReturnEmptyEventsWhenNoHearingEventsExistForAHearing() {
        givenNoHearingEventsExist();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID_1);

        assertThat(hearingEvents.size(), is(0));
    }

    @Test
    public void shouldNotThrowExceptionWhenHearingEventIsRequestedWhichDoesNotExist() {
        givenNoHearingEventsExist();

        final Optional<HearingEvent> hearingEvent = hearingEventRepository.findOptionalById(HEARING_EVENT_ID_1);

        assertThat(hearingEvent.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventsInChronologicalOrderByEventTime() {
        givenHearingEventsExistInRandomOrder();

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID_1);
        assertThat(hearingEvents.size(), is(3));

        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID_1));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(2).getId(), is(HEARING_EVENT_ID_3));
    }

    @Test
    public void shouldReturnHearingEventsForTheSameCourtRoomWhenAvailable() {
        givenHearingExistsWithCourtCentre();
        givenHearingEventsForDifferentHearings();

        final List<HearingEvent> hearingEvents = hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate());
        assertThat(hearingEvents.size(), is(1));
        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID_1));
    }

    @Test
    public void shouldReturnEmptyListForHearingEvents_AsNoHearingEventsRecodedForTheSameRoom() {
        givenHearingExistsWithCourtCentre();
        HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .setId(HEARING_EVENT_ID_1)
                .setHearingId(HEARING_ID_1)
                .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_3)
                .setRecordedLabel(RECORDED_LABEL)
                .setEventTime(EVENT_TIME)
                .setLastModifiedTime(LAST_MODIFIED_TIME)
                .setAlterable(ALTERABLE);
        hearingEventRepository.save(hearingEvent);

        final List<HearingEvent> hearingEvents = hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate());
        assertThat(hearingEvents.size(), is(0));
    }

    @Test
    public void shouldFindHearingEventByCourCentreId() {
        givenHearingExistsWithCourtCentre();

        hearingEventRepository.save(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(false)
        );

        final List<HearingEvent> hearingEventList = hearingEventRepository.findBy(COURT_CENTRE_ID, LAST_MODIFIED_TIME.minusMinutes(10l));

        assertThat(hearingEventList.size(), is(1));
        final HearingEvent hearingEvent = hearingEventList.get(0);

        assertThat(hearingEvent.getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvent.getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_1));
        assertThat(hearingEvent.getId(), is(HEARING_EVENT_ID_1));
        assertThat(hearingEvent.getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvent.getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvent.getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(hearingEvent.isDeleted(), is(false));
    }

    @Test
    public void shouldGetLatestHearingsForCourtCentreList() {
        givenHearingExistsWithCourtCentre();
        givenHearingEventsExistWithNotRequiredEventDefinitions();
        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(COURT_CENTRE_ID);
        final Set<UUID> hearingEventRequiredDefinitionsIds = new HashSet();
        hearingEventRequiredDefinitionsIds.add(HEARING_EVENT_DEFINITION_ID_1);
        hearingEventRequiredDefinitionsIds.add(HEARING_EVENT_DEFINITION_ID_2);

        final List<HearingEvent> hearingEvents = hearingEventRepository.findLatestHearingsForThatDay(courtCentreIds, EVENT_TIME.toLocalDate(),hearingEventRequiredDefinitionsIds);
        assertThat(hearingEvents.size(), is(1));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(0).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(0).getEventTime(), is(EVENT_TIME_2));
        assertThat(hearingEvents.get(0).getLastModifiedTime(), is(LAST_MODIFIED_TIME_2));
    }

    @Test
    public void shouldGetActiveHearingsForCourtCentreList() {
        givenHearingExistsWithCourtCentre();
        givenHearingEventsExistWithNotRequiredEventDefinitions();
        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(COURT_CENTRE_ID);
        final Set<UUID> hearingEventRequiredDefinitionsIds = new HashSet();
        hearingEventRequiredDefinitionsIds.add(HEARING_EVENT_DEFINITION_ID_1);
        hearingEventRequiredDefinitionsIds.add(HEARING_EVENT_DEFINITION_ID_2);

        final List<HearingEvent> hearingEvents = hearingEventRepository.findBy(courtCentreIds, EVENT_TIME,hearingEventRequiredDefinitionsIds);
        assertThat(hearingEvents.size(), is(2));

        assertThat(hearingEvents.get(0).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(0).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_1));
        assertThat(hearingEvents.get(0).getId(), is(HEARING_EVENT_ID_1));
        assertThat(hearingEvents.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(hearingEvents.get(0).getEventTime(), is(EVENT_TIME));
        assertThat(hearingEvents.get(0).getLastModifiedTime(), is(LAST_MODIFIED_TIME));


        assertThat(hearingEvents.get(1).getHearingId(), is(HEARING_ID_1));
        assertThat(hearingEvents.get(1).getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(hearingEvents.get(1).getId(), is(HEARING_EVENT_ID_2));
        assertThat(hearingEvents.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(hearingEvents.get(1).getEventTime(), is(EVENT_TIME_2));
        assertThat(hearingEvents.get(1).getLastModifiedTime(), is(LAST_MODIFIED_TIME_2));
    }

    private void givenHearingEventsExistWithNotRequiredEventDefinitions() {
        final List<HearingEvent> hearingEvents = newArrayList(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventDate(EVENT_TIME.toLocalDate())
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE),
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_2)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_2)
                        .setRecordedLabel(RECORDED_LABEL_2)
                        .setEventDate(EVENT_TIME_2.toLocalDate())
                        .setEventTime(EVENT_TIME_2)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_2)
                        .setAlterable(ALTERABLE) ,

                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_3)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_3)
                        .setRecordedLabel(RECORDED_LABEL_3)
                        .setEventDate(EVENT_TIME_3.toLocalDate())
                        .setEventTime(EVENT_TIME_3)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_3)
                        .setAlterable(ALTERABLE)
                        .setDeleted(false)
        );

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenHearingEventsExistWithDeletedOnes() {
        final List<HearingEvent> hearingEvents = newArrayList(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventDate(EVENT_TIME.toLocalDate())
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE),
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_2)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_2)
                        .setRecordedLabel(RECORDED_LABEL_2)
                        .setEventDate(EVENT_TIME_2.toLocalDate())
                        .setEventTime(EVENT_TIME_2)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_2)
                        .setAlterable(ALTERABLE),
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_3)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_3)
                        .setRecordedLabel(RECORDED_LABEL_3)
                        .setEventDate(EVENT_TIME_3.toLocalDate())
                        .setEventTime(EVENT_TIME_3)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_3)
                        .setAlterable(ALTERABLE)
                        .setDeleted(true)
        );

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenHearingEventsExistInRandomOrder() {
        final List<HearingEvent> hearingEvents = newArrayList(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE),
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_2)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_2)
                        .setRecordedLabel(RECORDED_LABEL_2)
                        .setEventTime(EVENT_TIME_2)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_2)
                        .setAlterable(ALTERABLE),
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_3)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_3)
                        .setRecordedLabel(RECORDED_LABEL_3)
                        .setEventTime(EVENT_TIME_3)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_3)
                        .setAlterable(ALTERABLE)
        );

        shuffle(hearingEvents, new Random(LONG.next()));

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(3));
    }

    private void givenNoHearingEventsExist() {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findAll();
        assertThat(hearingEvents.size(), is(0));
    }

    private void givenHearingExistsWithCourtCentre() {
        final Hearing hearing = new Hearing();
        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setId(COURT_CENTRE_ID);
        courtCentre.setRoomId(COURT_ROOM_ID);
        hearing.setId(HEARING_ID_1);
        hearing.setCourtCentre(courtCentre);
        hearingRepository.save(hearing);
    }

    private void givenHearingEventsForDifferentHearings() {
        final List<HearingEvent> hearingEvents = newArrayList(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(HEARING_ID_1)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_1)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventDate(EVENT_TIME.toLocalDate())
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setDeleted(false)
                        .setAlterable(false),
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_2)
                        .setHearingId(HEARING_ID_2)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_3)
                        .setRecordedLabel(RECORDED_LABEL_2)
                        .setEventDate(EVENT_TIME_2.toLocalDate())
                        .setEventTime(EVENT_TIME_2)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_2)
                        .setDeleted(false)
                        .setAlterable(false)
        );

        shuffle(hearingEvents, new Random(LONG.next()));

        hearingEvents.forEach(hearingEvent -> hearingEventRepository.save(hearingEvent));

        assertThat(hearingEventRepository.findAll(), hasSize(2));
    }
}