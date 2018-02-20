package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.getHearing;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private HearingJudgeRepository hearingJudgeRepository;

    @InjectMocks
    private HearingService caseHearingService;

    @Test
    public void shouldFindHearingByIdTest() throws IOException {

        final Optional<Hearing> hearing = getHearing();

        final HearingCase arbitraryCase1 = new HearingCase(randomUUID(), hearing.get().getHearingId(), randomUUID());
        final HearingCase arbitraryCase2 = new HearingCase(randomUUID(), hearing.get().getHearingId(), randomUUID());
        final HearingJudge hearingJudge = new HearingJudge(hearing.get().getHearingId(), STRING.next(),STRING.next(),STRING.next(),STRING.next());

        final List<HearingCase> cases = new ArrayList<>();
        cases.add(arbitraryCase1);
        cases.add(arbitraryCase2);

        when(this.hearingRepository.getByHearingId(hearing.get().getHearingId())).thenReturn(hearing);
        when(this.hearingCaseRepository.findByHearingId(hearing.get().getHearingId())).thenReturn(cases);
        when(this.hearingJudgeRepository.findBy(hearing.get().getHearingId())).thenReturn(hearingJudge);

        HearingView hearingView = this.caseHearingService.getHearingById(hearing.get().getHearingId());
        assertEquals(HearingTestUtils.startDate, hearingView.getStartDate());
        assertEquals(hearingJudge.getId(), hearingView.getJudge().getId());
        assertEquals(hearingJudge.getFirstName(), hearingView.getJudge().getFirstName());
        assertEquals(hearingJudge.getLastName(), hearingView.getJudge().getLastName());
        assertEquals(hearingJudge.getTitle(), hearingView.getJudge().getTitle());
    }
}
