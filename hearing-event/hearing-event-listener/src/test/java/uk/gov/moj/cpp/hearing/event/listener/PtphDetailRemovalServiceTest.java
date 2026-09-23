package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PtphDetailRemovalServiceTest {

    @Mock
    private PtphDetailRepository ptphDetailRepository;

    @InjectMocks
    private PtphDetailRemovalService ptphDetailRemovalService;

    @Test
    public void shouldRemoveTheRecordForTheHearing() {
        final UUID hearingId = randomUUID();
        final PtphDetail ptphDetail = new PtphDetail();

        when(ptphDetailRepository.findBy(hearingId)).thenReturn(ptphDetail);

        ptphDetailRemovalService.removeFor(hearingId);

        verify(ptphDetailRepository).removeAndFlush(ptphDetail);
    }

    /** Most hearings have no tier or list type, so removal must be a no-op for them. */
    @Test
    public void shouldDoNothingWhenTheHearingHasNoRecord() {
        final UUID hearingId = randomUUID();

        when(ptphDetailRepository.findBy(hearingId)).thenReturn(null);

        ptphDetailRemovalService.removeFor(hearingId);

        verify(ptphDetailRepository, never()).removeAndFlush(any());
    }
}
