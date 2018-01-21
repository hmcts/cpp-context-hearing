package uk.gov.moj.cpp.hearing.persist;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class HearingJudgeRepositoryTest extends BaseTransactionalTest {
    final UUID hearingId = randomUUID();
    final String id = STRING.next();
    final String firstName = STRING.next();
    final String lastName = STRING.next();
    final String title = STRING.next();

    @Inject
    private HearingJudgeRepository hearingJudgeRepository;

    @Test
    public void findAllTest() {
        final HearingJudge hearingJudge = new HearingJudge(this.hearingId, this.id,this.firstName,this.lastName,this.title);

        this.hearingJudgeRepository.save(hearingJudge);

        final List<HearingJudge> hearingJudges = this.hearingJudgeRepository.findAll();

        assertThat(hearingJudges.get(0).getHearingId(), is(this.hearingId));
        assertThat(hearingJudges.get(0).getId(), is(this.id));
        assertThat(hearingJudges.get(0).getFirstName(), is(this.firstName));
        assertThat(hearingJudges.get(0).getLastName(), is(this.lastName));
        assertThat(hearingJudges.get(0).getTitle(), is(this.title));
    }

    @Test
    public void findWithPK() {
        final HearingJudge hearingJudge = new HearingJudge(this.hearingId, this.id,this.firstName,this.lastName,this.title);

        this.hearingJudgeRepository.save(hearingJudge);

        final HearingJudge result = this.hearingJudgeRepository.findBy(hearingId);

        assertThat(result.getHearingId(), is(this.hearingId));
        assertThat(result.getId(), is(this.id));
        assertThat(result.getFirstName(), is(this.firstName));
        assertThat(result.getLastName(), is(this.lastName));
        assertThat(result.getTitle(), is(this.title));
    }

}
