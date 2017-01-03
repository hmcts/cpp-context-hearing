package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingOutcomeServiceTest {

    @InjectMocks
    HearingOutcomeService hearingOutcomeService;

    @Mock
    HearingOutcomeRepository hearingOutcomeRepository;

    @Mock
    List<HearingOutcome> hearingOutcomes;

    @Mock
    List<DefenceCounselDefendant> defenceCounselDefendantList;

    @Test
    public void shouldGetHearingOutcomeByHearingId(){
        final UUID hearingId = randomUUID();
        when(hearingOutcomeRepository.findByHearingId(hearingId)).thenReturn(hearingOutcomes);
        assertThat(hearingOutcomeService.getHearingOutcomeByHearingId(hearingId), is(hearingOutcomes));
    }

}