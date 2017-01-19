package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.getHearing;

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

    @Ignore
    public void findHearingsByStartDateTest() throws IOException {

        final List<Hearing> hearings = Arrays.asList(getHearing().get(), getHearing().get());

       // when(this.hearingRepository.findStartdatetimeWithNative("")).thenReturn(hearings);

      //  assertEquals(2, this.caseHearingService.getHearingsByStartDate(Optional.of("")).size());
    }

    @Test
    public void shouldFindHearingByIdTest() throws IOException {

        final Optional<Hearing> hearing = getHearing();

        final HearingCase arbitraryCase1 = new HearingCase(randomUUID(), hearing.get().getHearingId(), randomUUID());
        final HearingCase arbitraryCase2 = new HearingCase(randomUUID(), hearing.get().getHearingId(), randomUUID());

        final List<HearingCase> cases = new ArrayList<>();
        cases.add(arbitraryCase1);
        cases.add(arbitraryCase2);

        when(this.hearingRepository.getByHearingId(hearing.get().getHearingId())).thenReturn(hearing);
        when(this.hearingCaseRepository.findByHearingId(hearing.get().getHearingId())).thenReturn(cases);

        assertEquals(HearingTestUtils.startDate, this.caseHearingService.getHearingById(hearing.get().getHearingId()).getStartDate());
    }
}
