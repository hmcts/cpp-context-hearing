package uk.gov.moj.cpp.hearing.persist;

import static java.time.ZonedDateTime.parse;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

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

    private static final UUID HEARING_ID_ONE = randomUUID();
    private static final UUID HEARING_ID_TWO = randomUUID();
    private static final ZonedDateTime ARBITRARY_DATE_1 = parse("2007-12-03T10:15:30Z");
    private static final ZonedDateTime ARBITRARY_DATE_2 = parse("2007-12-03T10:16:30Z");

    @Inject
    private HearingRepository hearingRepository;

    private final List<Hearing> hearings = new ArrayList<>();

    @Before
    public void setup() {
        final Hearing hearingOne = new Hearing(HEARING_ID_ONE, ARBITRARY_DATE_1.toLocalDate(),
                ARBITRARY_DATE_1.toLocalTime(), 1, null, null, null, null, null);
        hearings.add(hearingOne);
        hearingRepository.save(hearingOne);

        final Hearing hearingTwo = new Hearing(HEARING_ID_TWO, ARBITRARY_DATE_2.toLocalDate(),
                ARBITRARY_DATE_2.toLocalTime(), 1, null, null, null, null, null);
        hearings.add(hearingTwo);
        hearingRepository.save(hearingTwo);
    }

    @After
    public void teardown() {
        hearings.forEach(
                hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getHearingId())));
    }

    @Test
    public void shouldFindByStartDateTime() throws Exception {
        final List<Hearing> results = hearingRepository.findByStartdate(ARBITRARY_DATE_1.toLocalDate());

        assertEquals(results.size(), 2);

    }

    @Test
    public void shouldFindAll() throws Exception {
        final List<Hearing> results = hearingRepository.findAll();
        assertEquals(results.size(), 2);
        Hearing result = results.get(0);
        assertThat(result.getStartdate(), equalTo(ARBITRARY_DATE_1.toLocalDate()));
    }

    @Test
    public void shouldFindByHearingId() throws Exception {
        final Optional<Hearing> result = hearingRepository.getByHearingId(HEARING_ID_ONE);
        assertThat(result.get().getDuration(), equalTo(1));
        assertThat(result.get().getStartdate(), equalTo(ARBITRARY_DATE_1.toLocalDate()));
    }

    @Test
    public void shouldNotFindByHearingId() throws Exception {
        final Optional<Hearing> result = hearingRepository.getByHearingId(randomUUID());
        assertThat(result.isPresent(), equalTo(false));
    }

}
