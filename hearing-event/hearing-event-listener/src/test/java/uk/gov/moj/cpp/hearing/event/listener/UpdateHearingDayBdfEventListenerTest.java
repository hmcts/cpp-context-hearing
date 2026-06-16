package uk.gov.moj.cpp.hearing.event.listener;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingUpdatedHearingDayBdf;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateHearingDayBdfEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private UpdateHearingDayBdfEventListener listener;

    @Test
    public void shouldUpdateHearingDayFieldsAndSaveWhenHearingAndDaysExist() {
        final UUID hearingId = randomUUID();
        final ZonedDateTime sittingDay = ZonedDateTime.parse("2024-06-01T09:00:00Z");
        final HearingDay hearingDay = new HearingDay(randomUUID(), randomUUID(), false, false, 60, 1, sittingDay);
        final HearingUpdatedHearingDayBdf payload = new HearingUpdatedHearingDayBdf(hearingId, hearingDay);

        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay entityDay =
                new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();
        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay> hearingDays = new HashSet<>();
        hearingDays.add(entityDay);

        final Hearing hearing = new Hearing();
        hearing.setHearingDays(hearingDays);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        final Envelope<HearingUpdatedHearingDayBdf> envelope = envelopeFrom(metadataWithDefaults(), payload);

        listener.hearingUpdatedHearingDayBdf(envelope);

        verify(hearingRepository).save(hearing);
        assertThat(entityDay.getSittingDay(), is(sittingDay));
        assertThat(entityDay.getDate(), is(LocalDate.parse("2024-06-01")));
        assertThat(entityDay.getDateTime(), is(sittingDay));
    }

    @Test
    public void shouldNotSaveWhenHearingNotFound() {
        final UUID hearingId = randomUUID();
        final HearingDay hearingDay = new HearingDay(randomUUID(), randomUUID(), false, false, 60, 1, now());
        final HearingUpdatedHearingDayBdf payload = new HearingUpdatedHearingDayBdf(hearingId, hearingDay);

        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        final Envelope<HearingUpdatedHearingDayBdf> envelope = envelopeFrom(metadataWithDefaults(), payload);

        listener.hearingUpdatedHearingDayBdf(envelope);

        verify(hearingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void shouldNotSaveWhenHearingHasNullHearingDays() {
        final UUID hearingId = randomUUID();
        final HearingDay hearingDay = new HearingDay(randomUUID(), randomUUID(), false, false, 60, 1, now());
        final HearingUpdatedHearingDayBdf payload = new HearingUpdatedHearingDayBdf(hearingId, hearingDay);

        final Hearing hearing = new Hearing();
        hearing.setHearingDays(null);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        final Envelope<HearingUpdatedHearingDayBdf> envelope = envelopeFrom(metadataWithDefaults(), payload);

        listener.hearingUpdatedHearingDayBdf(envelope);

        verify(hearingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void shouldNotSaveWhenHearingHasEmptyHearingDays() {
        final UUID hearingId = randomUUID();
        final HearingDay hearingDay = new HearingDay(randomUUID(), randomUUID(), false, false, 60, 1, now());
        final HearingUpdatedHearingDayBdf payload = new HearingUpdatedHearingDayBdf(hearingId, hearingDay);

        final Hearing hearing = new Hearing();
        hearing.setHearingDays(new HashSet<>());

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        final Envelope<HearingUpdatedHearingDayBdf> envelope = envelopeFrom(metadataWithDefaults(), payload);

        listener.hearingUpdatedHearingDayBdf(envelope);

        verify(hearingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
