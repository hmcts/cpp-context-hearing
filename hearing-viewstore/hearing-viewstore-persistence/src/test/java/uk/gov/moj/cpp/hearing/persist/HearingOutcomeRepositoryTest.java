package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class HearingOutcomeRepositoryTest extends BaseTransactionalTest {
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID TARGET_ID = randomUUID();
    private static final String DRAFT_RESULT = STRING.next();

    @Inject
    private HearingOutcomeRepository hearingOutcomeRepository;

    @Test
    public void findById() throws Exception {
        final List<HearingOutcome> hearingEvents = hearingOutcomeRepository.findAll();
        assertThat(hearingEvents.size(), is(0));

        hearingOutcomeRepository.save(new HearingOutcome(OFFENCE_ID, HEARING_ID,DEFENDANT_ID,TARGET_ID,DRAFT_RESULT));

        assertThat(hearingOutcomeRepository.findAll().size(), is(1));

        HearingOutcome hearingOutcome = hearingOutcomeRepository.findBy(TARGET_ID);

        assertThat(hearingOutcome.getDraftResult(), is(DRAFT_RESULT));
        assertThat(hearingOutcome.getHearingId(), is(HEARING_ID));
        assertThat(hearingOutcome.getId(), is(TARGET_ID));
        assertThat(hearingOutcome.getOffenceId(), is(OFFENCE_ID));
        assertThat(hearingOutcome.getDefendantId(), is(DEFENDANT_ID));
    }

    @Test
    public void findByHearingId() throws Exception {
        final List<HearingOutcome> hearingEvents = hearingOutcomeRepository.findAll();
        assertThat(hearingEvents.size(), is(0));

        hearingOutcomeRepository.save(new HearingOutcome(OFFENCE_ID, HEARING_ID,DEFENDANT_ID,randomUUID(),DRAFT_RESULT));

        hearingOutcomeRepository.save(new HearingOutcome(OFFENCE_ID, HEARING_ID,DEFENDANT_ID,randomUUID(),DRAFT_RESULT));

        assertThat(hearingOutcomeRepository.findAll().size(), is(2));

        List<HearingOutcome> hearingOutcomes = hearingOutcomeRepository.findByHearingId(HEARING_ID);

        HearingOutcome hearingOutcome = hearingOutcomes.get(0);

        assertThat(hearingOutcomes.size(), is(2));
        assertThat(hearingOutcome.getDraftResult(), is(DRAFT_RESULT));
        assertThat(hearingOutcome.getHearingId(), is(HEARING_ID));
        assertThat(hearingOutcome.getOffenceId(), is(OFFENCE_ID));
        assertThat(hearingOutcome.getDefendantId(), is(DEFENDANT_ID));
    }

}