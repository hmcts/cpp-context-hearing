package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCaseTest.buildLegalCase1;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class HearingRepositoryTest {

    private static final List<Hearing> hearings = new ArrayList<>();
    private static final LegalCase legalCase1 = buildLegalCase1();

    @Inject
    private HearingRepository hearingRepository;
    @Inject
    private LegalCaseRepository legalCaseRepository;
    
    @BeforeClass
    public static void create() {
        hearings.add(HearingRepositoryTestUtils.buildHearing(legalCase1));
    }

    @Before
    public void setup() {
        legalCaseRepository.save(legalCase1);
        hearings.forEach(hearing -> hearingRepository.save(hearing));
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
        legalCaseRepository.attachAndRemove(legalCaseRepository.findBy(legalCase1.getId()));
    }


    private ZonedDateTime atStartOfDay(ZonedDateTime zdt) {
        return zdt.toLocalDate().atStartOfDay(zdt.getZone());
    }

    @Test
    public void shouldFindByStartDate() throws Exception {
        final ZonedDateTime localTime = atStartOfDay(HearingRepositoryTestUtils.START_DATE_1);
        assertEquals(1, hearingRepository.findByDate(localTime.toLocalDate()).size());

    }

    @Test
    public void shouldFindAll() throws Exception {
        assertEquals(hearings.size(), hearingRepository.findAll().size());
    }

    @Test
    public void shouldFindByHearingId() throws Exception {
        final Hearing hearing = hearingRepository.findById(HearingRepositoryTestUtils.HEARING_ID_1);
        assertNotNull(hearing);
        assertEquals(HearingRepositoryTestUtils.HEARING_ID_1, hearing.getId());
        assertEquals(HearingRepositoryTestUtils.START_DATE_1, (hearing.getHearingDays().get(0)).getDateTime());
    }

    @Test
    public void shouldNotFindByHearingId() throws Exception {
        assertNull(hearingRepository.findById(randomUUID()));
    }
}
