package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VerdictServiceTest {

    @Mock
    VerdictHearingRepository verdictHearingRepository;

    @InjectMocks
    VerdictService verdictService;

    @Test
    public void shouldGetVerdictsByCaseId(){
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        final UUID caseId = randomUUID();
        final VerdictHearing verdict = new VerdictHearing(randomUUID(),randomUUID(),caseId,randomUUID(),randomUUID(),randomUUID(),"GUILTY", LocalDate.now());
        verdicts.add(verdict);
        when(verdictHearingRepository.findByCaseId(caseId)).thenReturn(verdicts);
        final List<VerdictHearing> verdictsReturned = verdictService.getVerdictHearingByCaseId(caseId);
        verify(verdictHearingRepository).findByCaseId(caseId);
        assertEquals(1,verdictsReturned.size());
    }

}
