package uk.gov.moj.cpp.hearing.repository;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCaseTest.buildLegalCase1;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;

@RunWith(CdiTestRunner.class)
public class LegalCaseRepositoryTest {

    private static final List<Hearing> hearings = new ArrayList<>();
    private static final LegalCase legalCase1 = buildLegalCase1();
    
    @Inject
    private LegalCaseRepository legalCaseRepository;
    
    @BeforeClass
    public static void create() {
        hearings.add(HearingRepositoryTestUtils.buildHearing(legalCase1));
    }

    @Before
    public void setup() {
        legalCaseRepository.save(legalCase1);
    }

    @After
    public void teardown() {
        legalCaseRepository.attachAndRemove(legalCaseRepository.findBy(legalCase1.getId()));
    }

    @Test
    public void shouldFindAll() throws Exception {
        final List<LegalCase> legalCases = legalCaseRepository.findAll();
        assertNotNull(legalCases);
        assertEquals(1, legalCases.size());
        assertEquals(legalCase1.getId(), legalCases.get(0).getId());
        assertEquals(legalCase1.getCaseUrn(), legalCases.get(0).getCaseUrn());
    }
    
    @Test
    public void shouldFindAllByIds() throws Exception {
        final List<LegalCase> legalCases = legalCaseRepository.findByIds(asList(legalCase1.getId()));
        assertNotNull(legalCases);
        assertEquals(1, legalCases.size());
        assertEquals(legalCase1.getId(), legalCases.get(0).getId());
        assertEquals(legalCase1.getCaseUrn(), legalCases.get(0).getCaseUrn());
    }
    
    @Test
    public void shouldNotFindByHearingId() throws Exception {
        assertNull(legalCaseRepository.findById(randomUUID()));
    }
}