package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class VerdictHearingRepositoryTest {

    final UUID verdictId = randomUUID();
    final UUID hearingId = randomUUID();
    final UUID caseId = randomUUID();
    final UUID defendantId = randomUUID();
    final UUID personId = randomUUID();
    final UUID offenceId = randomUUID();
    final String value = STRING.next();

    @Inject
    private VerdictHearingRepository verdictHearingRepository;

    @Test
    public void shouldFindAllVerdicts() {
        final VerdictHearing verdictHearingToSave = new VerdictHearing(this.verdictId, this.hearingId, this.caseId, this.personId, this.defendantId, this.offenceId, value);
        verdictHearingRepository.save(verdictHearingToSave);

        final List<VerdictHearing> verdictsRetrieved = verdictHearingRepository.findAll();

        assertThat(verdictsRetrieved.get(0).getVerdictId(), is(this.verdictId));
        assertThat(verdictsRetrieved.get(0).getHearingId(), is(this.hearingId));
        assertThat(verdictsRetrieved.get(0).getCaseId(), is(this.caseId));
        assertThat(verdictsRetrieved.get(0).getDefendantId(), is(this.defendantId));
        assertThat(verdictsRetrieved.get(0).getPersonId(), is(this.personId));
        assertThat(verdictsRetrieved.get(0).getOffenceId(), is(this.offenceId));
        assertThat(verdictsRetrieved.get(0).getValue(), is(this.value));
    }

    @Test
    public void shouldFindVerdictByCaseId() {
        final VerdictHearing verdictHearingToSave = new VerdictHearing(this.verdictId, this.hearingId, this.caseId, this.personId, this.defendantId, this.offenceId, value);
        verdictHearingRepository.save(verdictHearingToSave);

        final List<VerdictHearing> verdictsRetrieved = verdictHearingRepository.findByCaseId(this.caseId);

        assertThat(verdictsRetrieved.get(0).getVerdictId(), is(this.verdictId));
        assertThat(verdictsRetrieved.get(0).getHearingId(), is(this.hearingId));
        assertThat(verdictsRetrieved.get(0).getCaseId(), is(this.caseId));
        assertThat(verdictsRetrieved.get(0).getDefendantId(), is(this.defendantId));
        assertThat(verdictsRetrieved.get(0).getPersonId(), is(this.personId));
        assertThat(verdictsRetrieved.get(0).getOffenceId(), is(this.offenceId));
        assertThat(verdictsRetrieved.get(0).getValue(), is(this.value));

    }
    @Test
    public void shouldFindVerdictByPrimaryKey() {
        final VerdictHearing verdictHearingToSave = new VerdictHearing(this.verdictId, this.hearingId, this.caseId, this.personId, this.defendantId, this.offenceId, value);
        verdictHearingRepository.save(verdictHearingToSave);

        final VerdictHearing verdictsRetrieved = verdictHearingRepository.findBy(this.verdictId);

        assertThat(verdictsRetrieved.getVerdictId(), is(this.verdictId));
        assertThat(verdictsRetrieved.getHearingId(), is(this.hearingId));
        assertThat(verdictsRetrieved.getCaseId(), is(this.caseId));
        assertThat(verdictsRetrieved.getDefendantId(), is(this.defendantId));
        assertThat(verdictsRetrieved.getPersonId(), is(this.personId));
        assertThat(verdictsRetrieved.getOffenceId(), is(this.offenceId));
        assertThat(verdictsRetrieved.getValue(), is(this.value));
    }
}
