package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCaseTest.buildLegalCase1;

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

import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;

@RunWith(CdiTestRunner.class)
public class AhearingRepositoryTest {

    private static final List<Ahearing> hearings = new ArrayList<>();
    private static final LegalCase legalCase1 = buildLegalCase1();

    @Inject
    private AhearingRepository ahearingRepository;
    @Inject
    private LegalCaseRepository legalCaseRepository;
    
    @BeforeClass
    public static void create() {
        hearings.add(AhearingRepositoryTestUtils.buildHearing(legalCase1));
    }

    @Before
    public void setup() {
        legalCaseRepository.save(legalCase1);
        hearings.forEach(hearing -> ahearingRepository.save(hearing));
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> ahearingRepository.attachAndRemove(ahearingRepository.findBy(hearing.getId())));
        legalCaseRepository.attachAndRemove(legalCaseRepository.findBy(legalCase1.getId()));
    }


    private ZonedDateTime atStartOfDay(ZonedDateTime zdt) {
        return zdt.toLocalDate().atStartOfDay(zdt.getZone());
    }

    @Ignore // GPE-3032
    @Test
    public void shouldFindByStartDate() throws Exception {
        final ZonedDateTime localTime = atStartOfDay(AhearingRepositoryTestUtils.START_DATE_1);
        assertEquals(1, ahearingRepository.findByStartDate(localTime).size());

    }

    @Test
    public void shouldFindAll() throws Exception {
        assertEquals(hearings.size(), ahearingRepository.findAll().size());
    }

    @Test
    public void shouldFindByHearingId() throws Exception {
        final Ahearing ahearing = ahearingRepository.findById(AhearingRepositoryTestUtils.HEARING_ID_1);
        assertNotNull(ahearing);
        assertEquals(AhearingRepositoryTestUtils.HEARING_ID_1, ahearing.getId());
        assertEquals(AhearingRepositoryTestUtils.START_DATE_1, ahearing.getStartDateTime());
    }

    @Test
    public void shouldNotFindByHearingId() throws Exception {
        assertNull(ahearingRepository.findById(randomUUID()));
    }
}
