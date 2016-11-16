package uk.gov.moj.cpp.hearing.query.view.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.moj.cpp.hearing.domain.HearingStatusEnum;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;

/**
 * Unit tests for the HearingServiceTest class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private HearingService caseHearingService;

    private final UUID caseId = UUID.randomUUID();
    private final UUID hearingId = UUID.randomUUID();

    private final String PTP_HEARING = "PTP";

    private final String TRIAL_HEARING = "TRIAL";

    @Test
    public void emptyRepositoryReturnsNoAddresses() {
        final List<Hearing> hearings = new ArrayList<Hearing>();
        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);
        assertTrue(this.caseHearingService.getHearingsForCase(caseId, Optional.empty(), Optional.empty()).isEmpty());
    }

    @Test
    public void findHearingsByCaseIdTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing(caseId, PTP_HEARING), HearingTestUtils.getHearing(caseId, TRIAL_HEARING));

        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);

        assertEquals(2, this.caseHearingService.getHearingsForCase(caseId, Optional.empty(), Optional.empty()).size());
    }

    @Test
    public void findHearingsByCaseIdWithFromDateFilterTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing(caseId, PTP_HEARING));

        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);

        assertEquals(caseId.toString(), this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now()).toString()), Optional.empty()).stream()
                .findFirst().get().getCaseId());
        assertEquals(caseId.toString(), this.caseHearingService
                .getHearingsForCase(caseId, Optional.of((LocalDate.now().minusDays(1)).toString()), Optional.empty()).stream().findFirst().get().getCaseId());
        assertEquals(caseId.toString(), this.caseHearingService
                .getHearingsForCase(caseId, Optional.of((LocalDate.now().minusDays(2)).toString()), Optional.empty()).stream().findFirst().get().getCaseId());
        assertTrue(caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().plusDays(2)).toString()), Optional.empty()).isEmpty());
        assertTrue(caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().plusDays(1)).toString()), Optional.empty()).isEmpty());
    }

    @Test
    public void findHearingsByCaseIdWithFiltersTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing(caseId, PTP_HEARING));

        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);

        assertEquals(caseId.toString(), this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now()).toString()), Optional.of(PTP_HEARING))
                .stream().findFirst().get().getCaseId());
        assertEquals(caseId.toString(),
                this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().minusDays(1)).toString()), Optional.of(PTP_HEARING)).stream()
                        .findFirst().get().getCaseId());
        assertEquals(caseId.toString(),
                this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().minusDays(2)).toString()), Optional.of(PTP_HEARING)).stream()
                        .findFirst().get().getCaseId());
        assertTrue(caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().plusDays(2)).toString()), Optional.of(PTP_HEARING)).isEmpty());
        assertTrue(caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().plusDays(1)).toString()), Optional.of(PTP_HEARING)).isEmpty());
    }

    @Test
    public void findHearingsByCaseIdAndTypeTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing(caseId, PTP_HEARING));

        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);

        assertEquals(hearings.get(0).geHearingtId().toString(),
                this.caseHearingService.getHearingsForCase(caseId, Optional.empty(), Optional.of(PTP_HEARING)).stream().findFirst().get().getHearingId());
    }

    @Test
    public void failFindByCaseIdAndTypeTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing(caseId, PTP_HEARING));

        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);

        assertTrue(this.caseHearingService.getHearingsForCase(caseId, Optional.empty(), Optional.of(TRIAL_HEARING)).isEmpty());
    }

    @Test
    public void failfindHearingsByCaseIdWithFiltersTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing(caseId, PTP_HEARING));

        when(this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED)).thenReturn(hearings);

        assertTrue(this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now()).toString()), Optional.of(TRIAL_HEARING)).isEmpty());
        assertTrue(this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().minusDays(1)).toString()), Optional.of(TRIAL_HEARING))
                .isEmpty());
        assertTrue(this.caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().minusDays(2)).toString()), Optional.of(TRIAL_HEARING))
                .isEmpty());
        assertTrue(caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().plusDays(2)).toString()), Optional.of(TRIAL_HEARING)).isEmpty());
        assertTrue(caseHearingService.getHearingsForCase(caseId, Optional.of((LocalDate.now().plusDays(1)).toString()), Optional.of(TRIAL_HEARING)).isEmpty());
    }

    @Test
    public void shouldFindHearingByIdTest() throws IOException {

        final Hearing hearing = HearingTestUtils.getHearing(caseId, PTP_HEARING);
        when(this.hearingRepository.findByHearingId(hearing.geHearingtId())).thenReturn(hearing);
        assertEquals(caseId.toString(), this.caseHearingService.getHearingById(hearing.geHearingtId()).getCaseId());
    }
}
