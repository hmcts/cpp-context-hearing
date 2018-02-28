package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictValue;

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
        final UUID verdictValueId = randomUUID();
        final String verdictValueCategory = STRING.next();
        final String verdictValueCode = STRING.next();
        final String verdictValueDescription = STRING.next();
        final LocalDate verdictDate = LocalDate.now();
        final Integer numberOfSplitJurors = 2;
        final Integer numberOfJurors = 11;
        final Boolean unanimous = false;
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdict = new VerdictHearing.Builder()
                .withVerdictId(randomUUID())
                .withHearingId(randomUUID())
                .withCaseId(caseId)
                .withPersonId(randomUUID())
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withValue(verdictValue)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();
        verdicts.add(verdict);
        when(verdictHearingRepository.findByCaseId(caseId)).thenReturn(verdicts);
        final List<VerdictHearing> verdictsReturned = verdictService.getVerdictHearingByCaseId(caseId);
        verify(verdictHearingRepository).findByCaseId(caseId);
        assertEquals(1, verdictsReturned.size());
    }

}
