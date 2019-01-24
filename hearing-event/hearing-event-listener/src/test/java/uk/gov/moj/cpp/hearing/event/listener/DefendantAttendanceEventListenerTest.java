package uk.gov.moj.cpp.hearing.event.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantAttendanceRepository;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefendantAttendanceEventListenerTest {

    @Mock
    private DefendantAttendanceRepository defendantAttendanceRepository;

    @InjectMocks
    private DefendantAttendanceEventListener defendantAttendanceEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldUpdateDefendantAttendanceToNotPresent() throws Exception {

        final UUID hearingId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final LocalDate attendanceDay = LocalDate.of(2018, 12, 12);

        final DefendantAttendanceUpdated defendantAttendanceUpdated = DefendantAttendanceUpdated.defendantAttendanceUpdated()
                .setHearingId(hearingId)
                .setDefendantId(defendantId)
                .setAttendanceDay(AttendanceDay.attendanceDay()
                        .withDay(attendanceDay)
                        .withIsInAttendance(false)
                        .build());

        final DefendantAttendance defendantAttendance = new DefendantAttendance();
        defendantAttendance.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingId));
        defendantAttendance.setDefendantId(defendantId);
        defendantAttendance.setDay(attendanceDay);
        defendantAttendance.setInAttendance(false);

        when(defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, attendanceDay)).thenReturn(null);
        when(defendantAttendanceRepository.saveAndFlush(any(DefendantAttendance.class))).thenReturn(any(DefendantAttendance.class));

        defendantAttendanceEventListener.updateDefendantAttendance(envelopeFrom(metadataWithRandomUUID("hearing.defendant-attendance-updated"),
                objectToJsonObjectConverter.convert(defendantAttendanceUpdated)));

        final ArgumentCaptor<UUID> hearingIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> defendantIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<LocalDate> attendanceDateArgumentCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(this.defendantAttendanceRepository).findByHearingIdDefendantIdAndDate(hearingIdArgumentCaptor.capture(), defendantIdArgumentCaptor.capture(), attendanceDateArgumentCaptor.capture());

        assertThat(hearingIdArgumentCaptor.getValue(), is(hearingId));
        assertThat(defendantIdArgumentCaptor.getValue(), is(defendantId));
        assertThat(attendanceDateArgumentCaptor.getValue(), is(attendanceDay));
        assertThat(defendantAttendance.getInAttendance(), is(false));
    }


    @Test
    public void shouldUpdateDefendantAttendanceFromNotPresentToPresent() throws Exception {

        final UUID hearingId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final LocalDate attendanceDay = LocalDate.of(2018, 12, 12);

        final DefendantAttendanceUpdated defendantAttendanceUpdated = DefendantAttendanceUpdated.defendantAttendanceUpdated()
                .setHearingId(hearingId)
                .setDefendantId(defendantId)
                .setAttendanceDay(AttendanceDay.attendanceDay()
                        .withDay(attendanceDay)
                        .withIsInAttendance(true)
                        .build());

        final DefendantAttendance defendantAttendance = new DefendantAttendance();
        defendantAttendance.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingId));
        defendantAttendance.setDefendantId(defendantId);
        defendantAttendance.setDay(attendanceDay);
        defendantAttendance.setInAttendance(false);

        when(defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, attendanceDay)).thenReturn(defendantAttendance);
        when(defendantAttendanceRepository.saveAndFlush(defendantAttendance)).thenReturn(defendantAttendance);

        defendantAttendanceEventListener.updateDefendantAttendance(envelopeFrom(metadataWithRandomUUID("hearing.defendant-attendance-updated"),
                objectToJsonObjectConverter.convert(defendantAttendanceUpdated)));

        final ArgumentCaptor<UUID> hearingIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> defendantIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<LocalDate> attendanceDateArgumentCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(this.defendantAttendanceRepository).findByHearingIdDefendantIdAndDate(hearingIdArgumentCaptor.capture(), defendantIdArgumentCaptor.capture(), attendanceDateArgumentCaptor.capture());

        assertThat(hearingIdArgumentCaptor.getValue(), is(hearingId));
        assertThat(defendantIdArgumentCaptor.getValue(), is(defendantId));
        assertThat(attendanceDateArgumentCaptor.getValue(), is(attendanceDay));
        assertThat(defendantAttendance.getInAttendance(), is(true));
    }
}
