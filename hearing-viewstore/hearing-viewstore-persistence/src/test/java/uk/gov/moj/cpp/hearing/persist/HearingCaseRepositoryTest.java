package uk.gov.moj.cpp.hearing.persist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class HearingCaseRepositoryTest {
    @Inject
    private HearingCaseRepository hearingCaseRepository;

    List<UUID> hearingIds = new ArrayList<>();
    UUID hearingId = UUID.randomUUID();
    UUID caseId_1 = UUID.randomUUID();
    UUID caseId_2 = UUID.randomUUID();

    @Before
    public void setup() {
        hearingIds.add(hearingId);

        HearingCase hearingCase = new HearingCase();
        hearingCase.setId(UUID.randomUUID());
        hearingCase.setHearingId(hearingId);
        hearingCase.setCaseId(caseId_1);
        hearingCaseRepository.save(hearingCase);

        hearingCase = new HearingCase();
        hearingCase.setId(UUID.randomUUID());
        hearingCase.setHearingId(hearingId);
        hearingCase.setCaseId(caseId_2);
        hearingCaseRepository.save(hearingCase);

        hearingCase = new HearingCase();
        hearingCase.setId(UUID.randomUUID());
        hearingCase.setHearingId(hearingId);
        hearingCase.setCaseId(UUID.randomUUID());
        hearingCaseRepository.save(hearingCase);

        hearingCase = new HearingCase();
        hearingCase.setId(UUID.randomUUID());
        hearingCase.setHearingId(UUID.randomUUID());
        hearingCase.setCaseId(UUID.randomUUID());
        hearingCaseRepository.save(hearingCase);

        hearingIds.add(hearingCase.getHearingId());
    }

    @Test
    public void testWithMutipleCasesPerHearing() throws Exception {
        List<HearingCase> hearingCase = hearingCaseRepository.findByHearingId(hearingId);

        assertThat(hearingCase.size(), equalTo(3));

        long count =  hearingCase.stream().map(hearingCase1 -> hearingCase1.getCaseId()).filter(caseId -> caseId.equals(caseId_1)).count();
        assertThat(1l,equalTo(count));

    }

    @Test
    public void testWithMutipleCasesForMutipleHearings() throws Exception {
        List<HearingCase> hearingCase = hearingCaseRepository.findByHearingIds(hearingIds);

        assertThat(hearingCase.size(), equalTo(4));

        long count =  hearingCase.stream().map(hearingCase1 -> hearingCase1.getCaseId()).filter(caseId -> caseId.equals(caseId_1)).count();
        assertThat(1l,equalTo(count));

    }

    @Test
    public void testWithZeroCases() throws Exception {
        List<HearingCase> hearingCase = hearingCaseRepository.findByHearingId(UUID.randomUUID());

        assertThat(hearingCase.size(), equalTo(0));

        long count =  hearingCase.stream().map(hearingCase1 -> hearingCase1.getCaseId()).filter(caseId -> caseId.equals(caseId_1)).count();
        assertThat(0l,equalTo(count));

    }

}