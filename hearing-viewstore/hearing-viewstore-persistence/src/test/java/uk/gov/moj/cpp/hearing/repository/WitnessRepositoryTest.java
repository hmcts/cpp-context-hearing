package uk.gov.moj.cpp.hearing.repository;


import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.moj.cpp.hearing.persist.WitnessRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Witness;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCaseTest.buildLegalCase1;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class WitnessRepositoryTest {
    public static final String DEFENCE = "defence";
    public static final String EXPERT = "expert";
    private static Ahearing hearing = null;
    private static LegalCase legalCase = null;
    final String firstName = STRING.next();
    final String lastName = STRING.next();
    final String title = STRING.next();
    UUID id = null;
    UUID hearingId = null;
    UUID personId = randomUUID();
    HearingSnapshotKey hearingSnapshotKey = null;
    @Inject
    private AhearingRepository ahearingRepository;
    @Inject
    private LegalCaseRepository legalCaseRepository;
    @Inject
    private WitnessRepository witnessRepository;

    @Before
    public void setup() {
        id = randomUUID();
        legalCase = buildLegalCase1();
        legalCaseRepository.save(legalCase);
        hearing = AhearingRepositoryTestUtils.buildHearing(legalCase);
        hearingId = hearing.getId();
        ahearingRepository.save(hearing);
        hearingSnapshotKey = new HearingSnapshotKey(id, hearingId);
        Witness witness = Witness.builder().withId(hearingSnapshotKey).withHearing(hearing).withLegalCase(legalCase).withFirstName(firstName)
                .withLastName(lastName).withType(DEFENCE).withClassification(EXPERT).build();

        this.witnessRepository.save(witness);
    }


    @Test
    public void findAllTest() {
        final List<Witness> witnesses = this.witnessRepository.findAll();
        assertThat(witnesses.get(0).getHearing().getId(), is(this.hearingId));
        assertThat(witnesses.get(0).getId().getId(), is(this.id));
        assertThat(witnesses.get(0).getType(), is(this.DEFENCE));
        assertThat(witnesses.get(0).getClassification(), is(this.EXPERT));
        assertThat(witnesses.get(0).getFirstName(), is(this.firstName));
        assertThat(witnesses.get(0).getLastName(), is(this.lastName));
    }

    @Test
    public void findWithPK() {
        final Witness witnesses = this.witnessRepository.findBy(hearingSnapshotKey);
        assertThat(witnesses.getHearing().getId(), is(this.hearingId));
        assertThat(witnesses.getId().getId(), is(this.id));
        assertThat(witnesses.getType(), is(this.DEFENCE));
        assertThat(witnesses.getClassification(), is(this.EXPERT));
        assertThat(witnesses.getFirstName(), is(this.firstName));
        assertThat(witnesses.getLastName(), is(this.lastName));
    }

}
