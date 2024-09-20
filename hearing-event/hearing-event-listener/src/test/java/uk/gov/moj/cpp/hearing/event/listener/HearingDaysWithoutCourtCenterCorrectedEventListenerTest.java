package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.stream.Collectors.toSet;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingDaysWithoutCourtCenterCorrectedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private HearingDaysWithoutCourtCenterCorrectedEventListener hearingDaysWithoutCourtCenterCorrectedEventListener;


    @Test
    public void hearingDaysWithoutCourtCenterCorrected() {

        final HearingDaysWithoutCourtCentreCorrected hearingDaysWithoutCourtCentreCorrected = new HearingDaysWithoutCourtCentreCorrected();
        final UUID streamId = UUID.randomUUID();
        final HearingDay hd = new HearingDay(UUID.randomUUID(), UUID.randomUUID(), false, false, 30, 1, ZonedDateTime.now());
        hearingDaysWithoutCourtCentreCorrected.setId(streamId);
        hearingDaysWithoutCourtCentreCorrected.setHearingDays(asList(hd));

        final Hearing hearing = newHearingEntity();

        when(hearingRepository.findBy(streamId)).thenReturn(hearing);

        final Envelope<HearingDaysWithoutCourtCentreCorrected> hearingDaysWithoutCourtCenterCorrectedEnvelope =
                envelopeFrom(metadataWithDefaults(), hearingDaysWithoutCourtCentreCorrected);

        hearingDaysWithoutCourtCenterCorrectedEventListener.hearingDaysWithoutCourtCenterCorrected(hearingDaysWithoutCourtCenterCorrectedEnvelope);

        verify(hearingRepository).save(hearing);

        final Set<UUID> actualCourtRoomIds = hearing.getHearingDays().stream().map(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getCourtRoomId).collect(toSet());
        final Set<UUID> actualCourtCentreIds = hearing.getHearingDays().stream().map(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getCourtCentreId).collect(toSet());

        assertThat(actualCourtRoomIds, is(of(hd.getCourtRoomId())));
        assertThat(actualCourtCentreIds, is(of(hd.getCourtCentreId())));

    }

    private Hearing newHearingEntity() {
        final Hearing hearing = new Hearing();
        hearing.setId(UUID.randomUUID());
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay hd = new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();

        hearing.setHearingDays(of(hd));

        return hearing;
    }
}
