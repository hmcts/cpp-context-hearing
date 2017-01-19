package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DefenceCounselRepositoryTest {

    private static final UUID ID_1 = randomUUID();
    private static final UUID HEARING_ID_1 = randomUUID();
    private static final UUID PERSON_ID_1 = randomUUID();
    private static final String STATUS_1 = STRING.next();
    private static final DefenceCounsel DEFENCE_COUNSEL_1 =
            new DefenceCounsel(ID_1, HEARING_ID_1, PERSON_ID_1, STATUS_1);

    private static final UUID ID_2 = randomUUID();
    private static final UUID HEARING_ID_2 = randomUUID();
    private static final UUID PERSON_ID_2 = randomUUID();
    private static final String STATUS_2 = STRING.next();
    private static final DefenceCounsel DEFENCE_COUNSEL_2 =
            new DefenceCounsel(ID_2, HEARING_ID_2, PERSON_ID_2, STATUS_2);

    @Inject
    private DefenceCounselRepository defenceCounselRepository;

    @Before
    public void setup() {
        defenceCounselRepository.save(DEFENCE_COUNSEL_1);
        defenceCounselRepository.save(DEFENCE_COUNSEL_2);
    }

    @Test
    public void testGetProsecutionCounselByHearingId() {
        final List<DefenceCounsel> defenceCounselList =
                defenceCounselRepository.findByHearingId(HEARING_ID_1);
        assertThat(defenceCounselList, hasSize(1));
        final DefenceCounsel defenceCounsel = defenceCounselList.get(0);
        assertEquals(ID_1, defenceCounsel.getId());
        assertEquals(HEARING_ID_1, defenceCounsel.getHearingId());
        assertEquals(PERSON_ID_1, defenceCounsel.getPersonId());
        assertEquals(STATUS_1, defenceCounsel.getStatus());
    }

    @After
    public void tearDown() {
        defenceCounselRepository.remove(DEFENCE_COUNSEL_1);
        defenceCounselRepository.remove(DEFENCE_COUNSEL_2);
    }

}