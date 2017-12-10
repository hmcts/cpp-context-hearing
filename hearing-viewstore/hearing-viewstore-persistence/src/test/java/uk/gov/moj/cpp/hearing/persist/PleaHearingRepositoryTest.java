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
        final PleaHearing pleaHearingRandom = new PleaHearing(pleaId, hearingId, caseId, defendantId, offenceId, pleaDate, value);
        pleaHearingRandom.setPersonId(personId);
        pleaHearingRepository.save(pleaHearingRandom);

        final List<PleaHearing> pleaHearings = pleaHearingRepository.findAll();

        assertThat(pleaHearings.get(0).getPleaId(), is(pleaId));
        assertThat(pleaHearings.get(0).getHearingId(), is(hearingId));
        assertThat(pleaHearings.get(0).getCaseId(), is(caseId));
        assertThat(pleaHearings.get(0).getDefendantId(), is(defendantId));
        assertThat(pleaHearings.get(0).getPersonId(), is(personId));
        assertThat(pleaHearings.get(0).getOffenceId(), is(offenceId));
        assertThat(pleaHearings.get(0).getPleaDate(), is(pleaDate));
        assertThat(pleaHearings.get(0).getValue(), is(value));
    }

    @Test
    public void findWithPK() {
        final PleaHearing pleaHearingRandom = new PleaHearing(pleaId, hearingId, caseId, defendantId,  offenceId, pleaDate, value);

        pleaHearingRepository.save(pleaHearingRandom);

        final PleaHearing pleaHearing = pleaHearingRepository.findBy(pleaId);

        assertThat(pleaHearing.getPleaId(), is(pleaId));
        assertThat(pleaHearing.getHearingId(), is(hearingId));
        assertThat(pleaHearing.getCaseId(), is(caseId));
        assertThat(pleaHearing.getDefendantId(), is(defendantId));
        assertThat(pleaHearing.getPersonId()==null, is(true));
        assertThat(pleaHearing.getOffenceId(), is(offenceId));
        assertThat(pleaHearing.getPleaDate(), is(pleaDate));
        assertThat(pleaHearing.getValue(), is(value));
    }

    @Test
    public void findWithCaseId() {
        final PleaHearing pleaHearingRandom = new PleaHearing(pleaId, hearingId, caseId, defendantId, offenceId, pleaDate, value);
        pleaHearingRandom.setPersonId(personId);
        pleaHearingRepository.save(pleaHearingRandom);

        final List<PleaHearing> pleaHearings = pleaHearingRepository.findByCaseId(caseId);

        assertThat(pleaHearings.get(0).getPleaId(), is(pleaId));
        assertThat(pleaHearings.get(0).getHearingId(), is(hearingId));
        assertThat(pleaHearings.get(0).getCaseId(), is(caseId));
        assertThat(pleaHearings.get(0).getDefendantId(), is(defendantId));
        assertThat(pleaHearings.get(0).getPersonId(), is(personId));
        assertThat(pleaHearings.get(0).getOffenceId(), is(offenceId));
        assertThat(pleaHearings.get(0).getPleaDate(), is(pleaDate));
        assertThat(pleaHearings.get(0).getValue(), is(value));
    }
}
