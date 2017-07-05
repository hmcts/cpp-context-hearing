package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselToDefendant;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DefenceCounselRepositoryTest extends BaseTransactionalTest {

    private static final UUID ATTENDEE_ID = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID PERSON_ID = randomUUID();
    private static final String STATUS = STRING.next();
    private static final DefenceCounsel DEFENCE_COUNSEL =
            new DefenceCounsel(ATTENDEE_ID, HEARING_ID, PERSON_ID, STATUS);

    private static final UUID DEFENDANT_ID = randomUUID();
    private static final DefenceCounselDefendant DEFENCE_COUNSEL_DEFENDANT =
            new DefenceCounselDefendant(ATTENDEE_ID, DEFENDANT_ID);

    private static final UUID ATTENDEE_ID_2 = randomUUID();
    private static final UUID HEARING_ID_2 = randomUUID();
    private static final UUID PERSON_ID_2 = randomUUID();
    private static final String STATUS_2 = STRING.next();
    private static final DefenceCounsel DEFENCE_COUNSEL_2 =
            new DefenceCounsel(ATTENDEE_ID_2, HEARING_ID_2, PERSON_ID_2, STATUS_2);

    @Inject
    private DefenceCounselDefendantRepository defenceCounselDefendantRepository;

    @Inject
    private DefenceCounselRepository defenceCounselRepository;

    @Test
    public void shouldGetDefenceCounselByHearingId() {
        defenceCounselRepository.save(DEFENCE_COUNSEL);
        defenceCounselRepository.save(DEFENCE_COUNSEL_2);

        final List<DefenceCounsel> defenceCounselList = defenceCounselRepository.findByHearingId(HEARING_ID);

        assertThat(defenceCounselList, hasSize(1));
        final DefenceCounsel defenceCounsel = defenceCounselList.get(0);

        assertThat(defenceCounsel.getAttendeeId(), is(ATTENDEE_ID));
        assertThat(defenceCounsel.getHearingId(), is(HEARING_ID));
        assertThat(defenceCounsel.getPersonId(), is(PERSON_ID));
        assertThat(defenceCounsel.getStatus(), is(STATUS));
    }

    @Test
    public void shouldGetDefenceCounselAndDefendantByHearingId() {
        defenceCounselRepository.save(DEFENCE_COUNSEL);
        defenceCounselDefendantRepository.save(DEFENCE_COUNSEL_DEFENDANT);

        final List<DefenceCounselToDefendant> defenceCounselAndDefendants = defenceCounselRepository.findDefenceCounselAndDefendantByHearingId(HEARING_ID);

        assertThat(defenceCounselAndDefendants, hasSize(1));
        assertThat(defenceCounselAndDefendants.get(0).getPersonId(), is(PERSON_ID));
        assertThat(defenceCounselAndDefendants.get(0).getDefendantId(), is(DEFENDANT_ID));
    }

    @Test
    public void shouldReturnEmptyListWhenDefendantIsMissingForDefenceCounselInAHearing() {
        defenceCounselRepository.save(DEFENCE_COUNSEL);

        final List<DefenceCounselToDefendant> defenceCounselAndDefendants = defenceCounselRepository.findDefenceCounselAndDefendantByHearingId(HEARING_ID);

        assertThat(defenceCounselAndDefendants, hasSize(0));
    }

    @Test
    public void shouldReturnEmptyListWhenDefenceCounselIsMissingForDefendantInAHearing() {
        defenceCounselDefendantRepository.save(DEFENCE_COUNSEL_DEFENDANT);

        final List<DefenceCounselToDefendant> defenceCounselAndDefendants = defenceCounselRepository.findDefenceCounselAndDefendantByHearingId(HEARING_ID);

        assertThat(defenceCounselAndDefendants, hasSize(0));
    }

}