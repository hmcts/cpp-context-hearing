package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCounselServiceTest {

    @InjectMocks
    ProsecutionCounselService prosecutionCounselService;

    @Mock
    ProsecutionCounselRepository prosecutionCounselRepository;

    @Mock
    List<ProsecutionCounsel> prosecutionCounselList;

    @Test
    public void shouldGetProsecutionCounselsByHearingId(){
        final UUID hearingId = randomUUID();
        when(prosecutionCounselRepository.findByHearingId(hearingId)).thenReturn(prosecutionCounselList);
        assertThat(prosecutionCounselService.getProsecutionCounselsByHearingId(hearingId), is(prosecutionCounselList));
    }

}