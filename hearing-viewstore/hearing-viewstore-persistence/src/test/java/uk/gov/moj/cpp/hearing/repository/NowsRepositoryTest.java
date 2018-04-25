package uk.gov.moj.cpp.hearing.repository;


import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.moj.cpp.hearing.persist.NowsMaterialRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterialStatus;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class NowsRepositoryTest {
    UUID id;
    UUID hearingId;
    UUID defendantId;

    @Inject
    private NowsMaterialRepository nowsMaterialRepository;

    @Before
    public void setup() {
        id = randomUUID();
        hearingId = randomUUID();
        defendantId = randomUUID();

        NowsMaterial nowsMaterial = new NowsMaterial();
        nowsMaterial.setId(id);
        nowsMaterial.setDefendantId(defendantId);
        nowsMaterial.setHearingId(hearingId);
        nowsMaterial.setStatus(NowsMaterialStatus.REQUESTED);
        nowsMaterial.setUserGroups(Arrays.asList("LO", "GA"));

        this.nowsMaterialRepository.save(nowsMaterial);
    }


    @Test
    public void findAllTest() {
        final NowsMaterial nowsMaterials = this.nowsMaterialRepository.findAll().stream().filter(nowsMaterial -> nowsMaterial.getId().equals(id)).findFirst().get();
        assertThat(nowsMaterials.getDefendantId(), is(this.defendantId));
        assertThat(nowsMaterials.getHearingId(), is(this.hearingId));
        assertThat(nowsMaterials.getStatus(), is(NowsMaterialStatus.REQUESTED));
        assertThat(nowsMaterials.getUserGroups(), containsInAnyOrder("LO", "GA"));

    }

    @Test
    public void findByHearingIdTest() {
        final List<NowsMaterial> nowsMaterials = this.nowsMaterialRepository.findByHearingId(hearingId);
        assertThat(nowsMaterials.get(0).getId(), is(this.id));
        assertThat(nowsMaterials.get(0).getDefendantId(), is(this.defendantId));
        assertThat(nowsMaterials.get(0).getHearingId(), is(this.hearingId));
        assertThat(nowsMaterials.get(0).getStatus(), is(NowsMaterialStatus.REQUESTED));
        assertThat(nowsMaterials.get(0).getUserGroups(), containsInAnyOrder("LO", "GA"));

    }

}
