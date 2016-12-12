package uk.gov.moj.cpp.hearing.persist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class HearingRepositoryTest {

    private static final UUID HEARING_ID_ONE = UUID.randomUUID();
    private static final UUID HEARING_ID_TWO = UUID.randomUUID();
    private List<Hearing> hearings = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    ZonedDateTime ARBITRARY_DATE_1 = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]");

    ZonedDateTime ARBITRARY_DATE_2 = ZonedDateTime.parse("2007-12-03T10:16:30+01:00[Europe/Paris]");

    @Before
    public void setup() {

        Hearing hearingOne = new Hearing();
        hearings.add(hearingOne);
        hearingOne.setHearingId(HEARING_ID_ONE);
        hearingOne.setStartTime(ARBITRARY_DATE_1.toLocalTime());
        hearingOne.setStartdate(ARBITRARY_DATE_1.toLocalDate());
        hearingOne.setDuration(1);
        hearingRepository.save(hearingOne);

        Hearing hearingTwo = new Hearing();
        hearings.add(hearingTwo);
        hearingTwo.setHearingId(HEARING_ID_TWO);
        hearingTwo.setStartdate(ARBITRARY_DATE_2.toLocalDate());
        hearingTwo.setStartTime(ARBITRARY_DATE_2.toLocalTime());
        hearingTwo.setDuration(1);
        hearingRepository.save(hearingTwo);

    }

    @After
    public void teardown() {
        hearings.forEach(
                hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.geHearingId())));
    }

    @Test
    public void shouldFindByStartDateTime() throws Exception {
        List<Hearing> results = hearingRepository.findByStartdate(ARBITRARY_DATE_1.toLocalDate());

        assertEquals(results.size(), 2);

    }

    @Test
    public void shouldFindAll() throws Exception {
        List<Hearing> results = hearingRepository.findAll();
        assertEquals(results.size(), 2);
        Hearing result = results.get(0);
        assertThat(result.getStartdate(), equalTo(ARBITRARY_DATE_1.toLocalDate()));
    }

    @Test
    public void shouldFindByHearingId() throws Exception {

        Optional<Hearing> result = hearingRepository.getByHearingId(HEARING_ID_ONE);
        assertThat(result.get().getDuration(), equalTo(1));
        assertThat(result.get().getStartdate(), equalTo(ARBITRARY_DATE_1.toLocalDate()));
    }

    @Test
    public void shouldNotFindByHearingId() throws Exception {
        Optional<Hearing> result = hearingRepository.getByHearingId(UUID.randomUUID());
        assertThat(result.isPresent(), equalTo(false));
    }

  }
