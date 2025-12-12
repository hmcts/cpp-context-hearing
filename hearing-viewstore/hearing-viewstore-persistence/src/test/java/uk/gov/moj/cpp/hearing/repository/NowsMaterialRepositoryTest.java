package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class NowsMaterialRepositoryTest {

    private static final UUID nowsId = randomUUID();
    private static final UUID hearingId = randomUUID();
    private static final UUID defendantId = randomUUID();
    private static final UUID nowsTypeId = randomUUID();
    private static final UUID materialId = randomUUID();
    private static final UUID sharedResultId = randomUUID();
    private static final String language = "wales";

    @Inject
    private NowsRepository nowsRepository;
    @Inject
    private NowsMaterialRepository nowsMaterialRepository;

    @Before
    public void setup() {
        final Nows nows = Nows.builder()
                .withId(nowsId)
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withNowsTypeId(nowsTypeId)
                .withMaterial(asSet(NowsMaterial.builder()
                        .withId(materialId)
                        .withStatus("requested")
                        .withUserGroups(asSet("LO", "GA"))
                        .withLanguage(language)
                        .withNowResult(asSet(NowsResult.builder()
                                .withId(randomUUID())
                                .withSequence(1)
                                .withSharedResultId(sharedResultId)
                                .build()))
                        .build()))

                .build();

        nows.getMaterial().iterator().next().setNows(nows);
        nows.getMaterial().iterator().next().getNowResult().iterator().next().setNowsMaterial(nows.getMaterial().iterator().next());

        this.nowsRepository.save(nows);
    }

    @Test
    public void shouldUpdateNowsMaterialStatusToGenerated() {
        final int result = this.nowsMaterialRepository.updateStatus(materialId, "generated");
        assertThat(result, is(1));
    }

    @Test
    public void shouldNotUpdateNowsMaterialStatusToGenerated() {
        final int result = this.nowsMaterialRepository.updateStatus(randomUUID(), "generated");
        assertThat(result, is(0));
    }
}