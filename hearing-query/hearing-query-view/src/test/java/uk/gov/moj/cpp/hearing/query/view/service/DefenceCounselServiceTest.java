package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.DefenceCounselDefendantRepository;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefenceCounselServiceTest {

    @InjectMocks
    DefenceCounselService defenceCounselService;

    @Mock
    DefenceCounselRepository defenceCounselRepository;

    @Mock
    DefenceCounselDefendantRepository defenceCounselDefendantRepository;

    @Mock
    List<DefenceCounsel> defenceCounselList;

    @Mock
    List<DefenceCounselDefendant> defenceCounselDefendantList;

    @Test
    public void shouldGetDefenceCounselsByHearingId(){
        final UUID hearingId = randomUUID();
        when(defenceCounselRepository.findByHearingId(hearingId)).thenReturn(defenceCounselList);
        assertThat(defenceCounselService.getDefenceCounselsByHearingId(hearingId), is(defenceCounselList));
    }

    @Test
    public void shouldGetDefenceCounselDefendantsByDefenceCounselAttendeeId(){
        final UUID defenceCounselAttendeeId = randomUUID();
        when(defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(defenceCounselAttendeeId))
                .thenReturn(defenceCounselDefendantList);
        assertThat(defenceCounselService.getDefenceCounselDefendantsByDefenceCounselAttendeeId(defenceCounselAttendeeId),
                is(defenceCounselDefendantList));
    }

}