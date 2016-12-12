package uk.gov.moj.cpp.hearing.query.view.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the HearingServiceTest class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingCaseRepository hearingCaseRepository;

    @InjectMocks
    private HearingService caseHearingService;

    private final UUID hearingId = UUID.randomUUID();




    @Ignore
    public void findHearingsByStartDateTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(HearingTestUtils.getHearing().get(), HearingTestUtils.getHearing().get());

       // when(this.hearingRepository.findStartdatetimeWithNative("")).thenReturn(hearings);

      //  assertEquals(2, this.caseHearingService.getHearingsByStartDate(Optional.of("")).size());
    }


    @Test
    public void shouldFindHearingByIdTest() throws IOException {

        final Optional<Hearing> hearing = HearingTestUtils.getHearing();

        HearingCase arbitraryCase1 = new HearingCase();
        arbitraryCase1.setCaseId(UUID.randomUUID());
        arbitraryCase1.setHearingId(hearing.get().geHearingId());

        HearingCase arbitraryCase2 = new HearingCase();
        arbitraryCase2.setCaseId(UUID.randomUUID());
        arbitraryCase2.setHearingId(hearing.get().geHearingId());

        List<HearingCase> cases = new ArrayList<>();
        cases.add(arbitraryCase1);
        cases.add(arbitraryCase2);


        when(this.hearingRepository.getByHearingId(hearing.get().geHearingId())).thenReturn(hearing);
        when(this.hearingCaseRepository.findByHearingId(hearing.get().geHearingId())).thenReturn(cases);
        assertEquals(HearingTestUtils.startDate, this.caseHearingService.getHearingById(hearing.get().geHearingId()).getStartDate());
    }
}
