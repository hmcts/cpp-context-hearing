package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class ProsecutionCounselRepositoryTest {

    private static final UUID ID_1 = randomUUID();
    private static final UUID HEARING_ID_1 = randomUUID();
    private static final UUID PERSON_ID_1 = randomUUID();
    private static final String STATUS_1 = STRING.next();
    private static final ProsecutionCounsel PROSECUTION_COUNSEL_1 =
            new ProsecutionCounsel(ID_1, HEARING_ID_1, PERSON_ID_1, STATUS_1);

    private static final UUID ID_2 = randomUUID();
    private static final UUID HEARING_ID_2 = randomUUID();
    private static final UUID PERSON_ID_2 = randomUUID();
    private static final String STATUS_2 = STRING.next();
    private static final ProsecutionCounsel PROSECUTION_COUNSEL_2 =
            new ProsecutionCounsel(ID_2, HEARING_ID_2, PERSON_ID_2, STATUS_2);

    @Inject
    private ProsecutionCounselRepository prosecutionCounselRepository;

    @Before
    public void setup() {
        prosecutionCounselRepository.save(PROSECUTION_COUNSEL_1);
        prosecutionCounselRepository.save(PROSECUTION_COUNSEL_2);
    }

    @Test
    public void testGetProsecutionCounselByHearingId() {
        final List<ProsecutionCounsel> prosecutionCounselList =
                prosecutionCounselRepository.findByHearingId(HEARING_ID_1);
        assertThat(prosecutionCounselList, hasSize(1));
        final ProsecutionCounsel prosecutionCounsel = prosecutionCounselList.get(0);
        assertEquals(ID_1, prosecutionCounsel.getId());
        assertEquals(HEARING_ID_1, prosecutionCounsel.getHearingId());
        assertEquals(PERSON_ID_1, prosecutionCounsel.getPersonId());
        assertEquals(STATUS_1, prosecutionCounsel.getStatus());
    }

    @After
    public void tearDown() {
        prosecutionCounselRepository.remove(PROSECUTION_COUNSEL_1);
        prosecutionCounselRepository.remove(PROSECUTION_COUNSEL_2);
    }

}