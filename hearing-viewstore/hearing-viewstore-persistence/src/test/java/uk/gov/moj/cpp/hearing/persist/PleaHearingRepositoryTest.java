package uk.gov.moj.cpp.hearing.persist;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class PleaHearingRepositoryTest extends BaseTransactionalTest {
    final UUID pleaId = randomUUID();
    final UUID hearingId = randomUUID();
    final UUID caseId = randomUUID();
    final UUID defendantId = randomUUID();
    final UUID personId = randomUUID();
    final UUID offenceId = randomUUID();
    final LocalDate pleaDate = FUTURE_LOCAL_DATE.next();
    final String value = STRING.next();

    @Inject
    private PleaHearingRepository pleaHearingRepository;

    @Test
    public void findAllTest() {
        final PleaHearing pleaHearingRandom = new PleaHearing(this.pleaId, this.hearingId, this.caseId, this.defendantId, this.offenceId, this.pleaDate, this.value,this.personId);
        this.pleaHearingRepository.save(pleaHearingRandom);

        final List<PleaHearing> pleaHearings = this.pleaHearingRepository.findAll();

        assertThat(pleaHearings.get(0).getPleaId(), is(this.pleaId));
        assertThat(pleaHearings.get(0).getHearingId(), is(this.hearingId));
        assertThat(pleaHearings.get(0).getCaseId(), is(this.caseId));
        assertThat(pleaHearings.get(0).getDefendantId(), is(this.defendantId));
        assertThat(pleaHearings.get(0).getPersonId(), is(this.personId));
        assertThat(pleaHearings.get(0).getOffenceId(), is(this.offenceId));
        assertThat(pleaHearings.get(0).getPleaDate(), is(this.pleaDate));
        assertThat(pleaHearings.get(0).getValue(), is(this.value));
    }

    @Test
    public void findWithPK() {
        final PleaHearing pleaHearingRandom = new PleaHearing(this.pleaId, this.hearingId, this.caseId, this.defendantId, this.offenceId, this.pleaDate, this.value,this.personId);

        this.pleaHearingRepository.save(pleaHearingRandom);

        final PleaHearing pleaHearing = this.pleaHearingRepository.findBy(this.pleaId);

        assertThat(pleaHearing.getPleaId(), is(this.pleaId));
        assertThat(pleaHearing.getHearingId(), is(this.hearingId));
        assertThat(pleaHearing.getCaseId(), is(this.caseId));
        assertThat(pleaHearing.getDefendantId(), is(this.defendantId));
        assertThat(pleaHearing.getPersonId(), is(this.personId));
        assertThat(pleaHearing.getOffenceId(), is(this.offenceId));
        assertThat(pleaHearing.getPleaDate(), is(this.pleaDate));
        assertThat(pleaHearing.getValue(), is(this.value));
    }

    @Test
    public void findWithCaseId() {
        final PleaHearing pleaHearingRandom = new PleaHearing(this.pleaId, this.hearingId, this.caseId, this.defendantId, this.offenceId, this.pleaDate, this.value,this.personId);
        this.pleaHearingRepository.save(pleaHearingRandom);

        final List<PleaHearing> pleaHearings = this.pleaHearingRepository.findByCaseId(this.caseId);

        assertThat(pleaHearings.get(0).getPleaId(), is(this.pleaId));
        assertThat(pleaHearings.get(0).getHearingId(), is(this.hearingId));
        assertThat(pleaHearings.get(0).getCaseId(), is(this.caseId));
        assertThat(pleaHearings.get(0).getDefendantId(), is(this.defendantId));
        assertThat(pleaHearings.get(0).getPersonId(), is(this.personId));
        assertThat(pleaHearings.get(0).getOffenceId(), is(this.offenceId));
        assertThat(pleaHearings.get(0).getPleaDate(), is(this.pleaDate));
        assertThat(pleaHearings.get(0).getValue(), is(this.value));
    }

}
