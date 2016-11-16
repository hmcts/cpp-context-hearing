package uk.gov.moj.cpp.hearing.persist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.gov.moj.cpp.hearing.domain.HearingStatusEnum;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

@RunWith(CdiTestRunner.class)
public class HearingRepositoryTest {

    private static final String COURT_CENTER_NAME = "Liverpool";
    private static final UUID HEARING_ID_ONE = UUID.randomUUID();
    private static final UUID HEARING_ID_TWO = UUID.randomUUID();
    private static final UUID CASE_ID = UUID.randomUUID();
    private List<Hearing> hearings = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    private static LocalDate now;

    @Before
    public void setup() {
        now = LocalDate.now();
        Hearing hearingOne = new Hearing();
        hearings.add(hearingOne);
        hearingOne.setCaseId(CASE_ID);
        hearingOne.setHearingId(HEARING_ID_ONE);
        hearingOne.setStartDate(now);
        hearingOne.setCourtCentreName(COURT_CENTER_NAME);
        hearingOne.setHearingType(HearingTypeEnum.TRIAL);
        hearingOne.setStatus(HearingStatusEnum.BOOKED);
        hearingOne.setDuration(1);
        hearingRepository.save(hearingOne);

        Hearing hearingTwo = new Hearing();
        hearings.add(hearingTwo);
        hearingTwo.setCaseId(CASE_ID);
        hearingTwo.setHearingId(HEARING_ID_TWO);
        hearingTwo.setStartDate(now.minusDays(2));
        hearingTwo.setCourtCentreName(COURT_CENTER_NAME);
        hearingTwo.setHearingType(HearingTypeEnum.PTP);
        hearingTwo.setStatus(HearingStatusEnum.VACATED);
        hearingTwo.setDuration(1);
        hearingRepository.save(hearingTwo);
    }

    @After
    public void teardown() {
        hearings.forEach(
                hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.geHearingtId())));
    }

    @Test
    public void shouldFindByCaseIdAndStartDateGreaterThanEquals() throws Exception {
        List<Hearing> results = hearingRepository.findByCaseIdAndStartDateGreaterThanEquals(CASE_ID, now);
        assertEquals(results.size(), 1);
        Hearing result = results.get(0);
        assertThat(result.getCaseId(), equalTo(CASE_ID));
    }

    @Test
    public void shouldFindByCaseIdAndStatusEqual() throws Exception {
        List<Hearing> results = hearingRepository.findByCaseIdAndStatusEqual(CASE_ID, HearingStatusEnum.BOOKED);
        assertEquals(results.size(), 1);
        Hearing result = results.get(0);
        assertThat(result.getCaseId(), equalTo(CASE_ID));
    }

    @Test
    public void shouldFindAll() throws Exception {
        List<Hearing> results = hearingRepository.findAll();
        assertEquals(results.size(), 2);
        Hearing result = results.get(0);
        assertThat(result.getCaseId(), equalTo(CASE_ID));
    }

    @Test
    public void shouldFindByHearingId() throws Exception {
        Hearing result = hearingRepository.findByHearingId(HEARING_ID_ONE);
        assertThat(result.getCaseId(), equalTo(CASE_ID));
        assertThat(result.getCourtCentreName(), equalTo(COURT_CENTER_NAME));
        assertThat(result.getDuration(), equalTo(1));
        assertThat(result.getHearingType(), equalTo(HearingTypeEnum.TRIAL));
        assertThat(result.getStartDate(), equalTo(now));
        assertThat(result.getStatus(), equalTo(HearingStatusEnum.BOOKED));
    }

}
