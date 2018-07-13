package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.AttendeeDeleted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

@RunWith(MockitoJUnitRunner.class)
public class AttendeeDeletedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private AttendeeHearingDateRespository attendeeHearingDateRespository;
    
    @InjectMocks
    private AttendeeDeletedEventListener attendeeDeletedEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private static final UUID hearingId = randomUUID();
    private static final UUID attendeeId = randomUUID();
    private static final UUID hearingDateId = randomUUID();
    private static final UUID futureHearingDateId = randomUUID();
    private static final ZonedDateTime startDateTime = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime futureDateTime = FUTURE_ZONED_DATE_TIME.next();
    private static Hearing hearingSingleDay;
    private static Hearing hearingMultipleDays;

    @BeforeClass
    public static void init() {
        hearingSingleDay = Hearing.builder()
            .withId(hearingId)
            .withHearingType(STRING.next())
            .withCourtCentreId(randomUUID())
            .withCourtCentreName(STRING.next())
            .withRoomId(randomUUID())
            .withRoomName(STRING.next())
            .withHearingDays(asList(
                    HearingDate.builder()
                        .withId(new HearingSnapshotKey(hearingDateId, hearingId))
                        .withDateTime(startDateTime)
                        .withDate(startDateTime.toLocalDate())
                        .build()
                    ))
            .withJudge(Judge.builder()
                .withId(new HearingSnapshotKey(attendeeId, hearingId))
                .withPersonId(randomUUID())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next()))
            .build();
        hearingMultipleDays = Hearing.builder()
                .withId(hearingId)
                .withHearingType(STRING.next())
                .withCourtCentreId(randomUUID())
                .withCourtCentreName(STRING.next())
                .withRoomId(randomUUID())
                .withRoomName(STRING.next())
                .withHearingDays(asList(
                        HearingDate.builder()
                            .withId(new HearingSnapshotKey(hearingDateId, hearingId))
                            .withDateTime(startDateTime)
                            .withDate(startDateTime.toLocalDate())
                            .build(),
                        HearingDate.builder()
                            .withId(new HearingSnapshotKey(futureHearingDateId, hearingId))
                            .withDateTime(futureDateTime)
                            .withDate(futureDateTime.toLocalDate())
                            .build()
                        ))
                .withJudge(Judge.builder()
                    .withId(new HearingSnapshotKey(attendeeId, hearingId))
                    .withPersonId(randomUUID())
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withTitle(STRING.next()))
                .build();
    }

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldRemoveAnAttendeeFromCurrentHearingDayWhenAttendeeDeletedEventIsRaised() throws Exception {

        assertThat(hearingSingleDay.getAttendees().size(), is(1));
        assertThat(hearingSingleDay.getHearingDays().size(), is(1));

        final AttendeeDeleted attendeeDeleted = new AttendeeDeleted(hearingId, attendeeId, startDateTime.toLocalDate());

        when(hearingRepository.findById(hearingId)).thenReturn(hearingSingleDay);
        when(attendeeHearingDateRespository.delete(hearingId, attendeeId, hearingDateId)).thenReturn(1);

        attendeeDeletedEventListener.onAttendeeDeleted(envelopeFrom(metadataWithRandomUUID("hearing.events.attendee-deleted"),
                objectToJsonObjectConverter.convert(attendeeDeleted)));

        final ArgumentCaptor<UUID> hearingIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> attendeeIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> hearingDateIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(this.attendeeHearingDateRespository).delete(hearingIdArgumentCaptor.capture(), attendeeIdArgumentCaptor.capture(), hearingDateIdArgumentCaptor.capture());

        assertThat(hearingIdArgumentCaptor.getValue(), is(hearingId));
        assertThat(attendeeIdArgumentCaptor.getValue(), is(attendeeId));
        assertThat(hearingDateIdArgumentCaptor.getValue(), is(hearingDateId));
    }

    @Test
    public void shouldRemoveAnAttendeeFromCurrentAndFutureHearingDayWhenAttendeeDeletedEventIsRaised() throws Exception {

        assertThat(hearingMultipleDays.getAttendees().size(), is(1));
        assertThat(hearingMultipleDays.getHearingDays().size(), is(2));

        final AttendeeDeleted attendeeDeleted = new AttendeeDeleted(hearingId, attendeeId, startDateTime.toLocalDate());

        when(hearingRepository.findById(hearingId)).thenReturn(hearingMultipleDays);
        when(attendeeHearingDateRespository.delete(hearingId, attendeeId, hearingDateId)).thenReturn(1);
        when(attendeeHearingDateRespository.delete(hearingId, attendeeId, futureHearingDateId)).thenReturn(1);

        attendeeDeletedEventListener.onAttendeeDeleted(envelopeFrom(metadataWithRandomUUID("hearing.events.attendee-deleted"),
                objectToJsonObjectConverter.convert(attendeeDeleted)));

        final ArgumentCaptor<UUID> hearingIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> attendeeIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> hearingDateIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(this.attendeeHearingDateRespository, times(2)).delete(hearingIdArgumentCaptor.capture(), attendeeIdArgumentCaptor.capture(), hearingDateIdArgumentCaptor.capture());

        assertThat(hearingIdArgumentCaptor.getValue(), is(hearingId));
        assertThat(attendeeIdArgumentCaptor.getValue(), is(attendeeId));
        assertThat(hearingDateIdArgumentCaptor.getAllValues(), hasItems(hearingDateId, futureHearingDateId));
    }

    @Test
    public void shouldFailureToFindHearingWhenAttendeeDeletedEventIsRaised() {
        final AttendeeDeleted attendeeDeleted = new AttendeeDeleted(hearingId, attendeeId, startDateTime.toLocalDate());

        when(hearingRepository.findById(hearingId)).thenReturn(null);

        attendeeDeletedEventListener.onAttendeeDeleted(envelopeFrom(metadataWithRandomUUID("hearing.events.attendee-deleted"),
                objectToJsonObjectConverter.convert(attendeeDeleted)));

       verifyNoMoreInteractions(attendeeHearingDateRespository);
    }

    @Test
    public void shouldFailureToFindHearingDayWhenAttendeeDeletedEventIsRaised() throws Exception {
        final AttendeeDeleted attendeeDeleted = new AttendeeDeleted(hearingId, attendeeId, startDateTime.toLocalDate());

        when(hearingRepository.findById(hearingId)).thenReturn(hearingSingleDay);
        when(attendeeHearingDateRespository.delete(hearingId, attendeeId, hearingDateId)).thenReturn(0);

        attendeeDeletedEventListener.onAttendeeDeleted(envelopeFrom(metadataWithRandomUUID("hearing.events.attendee-deleted"),
                objectToJsonObjectConverter.convert(attendeeDeleted)));

        final ArgumentCaptor<UUID> hearingIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> attendeeIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> hearingDateIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(this.attendeeHearingDateRespository).delete(hearingIdArgumentCaptor.capture(), attendeeIdArgumentCaptor.capture(), hearingDateIdArgumentCaptor.capture());

        assertThat(hearingIdArgumentCaptor.getValue(), is(hearingId));
        assertThat(attendeeIdArgumentCaptor.getValue(), is(attendeeId));
        assertThat(hearingDateIdArgumentCaptor.getValue(), is(hearingSingleDay.getHearingDays().get(0).getId().getId()));
    }
}