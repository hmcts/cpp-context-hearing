package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DefenceCounselDefendantRepositoryTest {

    private static final UUID DEFENCE_COUNSEL_ATTENDEE_ID_1 = randomUUID();
    private static final UUID DEFENCE_COUNSEL_ATTENDEE_ID_2 = randomUUID();
    private static final UUID DEFENDANT_ID_1A = randomUUID();
    private static final UUID DEFENDANT_ID_1B = randomUUID();
    private static final UUID DEFENDANT_ID_2A = randomUUID();
    private static final UUID DEFENDANT_ID_2B = randomUUID();

    private static final DefenceCounselDefendant DEFENCE_COUNSEL_DEFENDANT_1A =
            new DefenceCounselDefendant(DEFENCE_COUNSEL_ATTENDEE_ID_1, DEFENDANT_ID_1A);

    private static final DefenceCounselDefendant DEFENCE_COUNSEL_DEFENDANT_1B =
            new DefenceCounselDefendant(DEFENCE_COUNSEL_ATTENDEE_ID_1, DEFENDANT_ID_1B);

    private static final DefenceCounselDefendant DEFENCE_COUNSEL_DEFENDANT_2A =
            new DefenceCounselDefendant(DEFENCE_COUNSEL_ATTENDEE_ID_2, DEFENDANT_ID_2A);

    private static final DefenceCounselDefendant DEFENCE_COUNSEL_DEFENDANT_2B =
            new DefenceCounselDefendant(DEFENCE_COUNSEL_ATTENDEE_ID_2, DEFENDANT_ID_2B);

    @Inject
    private DefenceCounselDefendantRepository defenceCounselDefendantRepository;

    @Test
    public void testGetProsecutionCounselByHearingId() {
        defenceCounselDefendantRepository.save(DEFENCE_COUNSEL_DEFENDANT_1A);
        defenceCounselDefendantRepository.save(DEFENCE_COUNSEL_DEFENDANT_1B);
        defenceCounselDefendantRepository.save(DEFENCE_COUNSEL_DEFENDANT_2A);
        defenceCounselDefendantRepository.save(DEFENCE_COUNSEL_DEFENDANT_2B);

        assertThat(defenceCounselDefendantRepository.findAll(), hasSize(4));

        final List<DefenceCounselDefendant> defenceCounselList1 =
                defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(
                        DEFENCE_COUNSEL_ATTENDEE_ID_1);

        assertThat(defenceCounselList1, hasSize(2));

        final List<DefenceCounselDefendant> defenceCounselList2 =
                defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(
                        DEFENCE_COUNSEL_ATTENDEE_ID_2);

        assertThat(defenceCounselList2, hasSize(2));

        defenceCounselDefendantRepository.attachAndRemove(DEFENCE_COUNSEL_DEFENDANT_1A);
        defenceCounselDefendantRepository.attachAndRemove(DEFENCE_COUNSEL_DEFENDANT_1B);
        defenceCounselDefendantRepository.attachAndRemove(DEFENCE_COUNSEL_DEFENDANT_2A);
        defenceCounselDefendantRepository.attachAndRemove(DEFENCE_COUNSEL_DEFENDANT_2B);

        assertThat(defenceCounselDefendantRepository.findAll(), hasSize(0));
    }
}