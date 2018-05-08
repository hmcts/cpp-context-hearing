package uk.gov.moj.cpp.hearing.query.view.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;
import uk.gov.moj.cpp.hearing.repository.HearingOutcomeRepository;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HearingOutcomeServiceTest {

    @InjectMocks
    HearingOutcomeService hearingOutcomeService;

    @Mock
    HearingOutcomeRepository hearingOutcomeRepository;

    @Mock
    List<HearingOutcome> hearingOutcomes;

    @Test
    public void shouldGetHearingOutcomeByHearingId(){
        final UUID hearingId = randomUUID();
        when(hearingOutcomeRepository.findByHearingId(hearingId)).thenReturn(hearingOutcomes);
        assertThat(hearingOutcomeService.getHearingOutcomeByHearingId(hearingId), is(hearingOutcomes));
    }

}