package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class NowsMaterialRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    private static final UUID nowsId = randomUUID();
    private static final UUID hearingId = randomUUID();
    private static final UUID defendantId = randomUUID();
    private static final UUID nowsTypeId = randomUUID();
    private static final UUID materialId = randomUUID();
    private static final UUID sharedResultId = randomUUID();
    private static final String language = "wales";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private NowsRepository nowsRepository;
    private NowsMaterialRepository nowsMaterialRepository;

    @BeforeEach
    void openEntityManagerAndCreateRepositories() {
        nowsRepository = new NowsRepository();
        nowsMaterialRepository = new NowsMaterialRepository();

        hibernateTestEntityManagerProvider.injectEntityManagerInto(nowsRepository);
        hibernateTestEntityManagerProvider.injectEntityManagerInto(nowsMaterialRepository);

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

        nowsRepository.save(nows);
    }

    @Test
    void shouldUpdateNowsMaterialStatusToGenerated() {
        final int result = nowsMaterialRepository.updateStatus(materialId, "generated");
        assertThat(result, is(1));
    }

    @Test
    void shouldNotUpdateNowsMaterialStatusToGenerated() {
        final int result = nowsMaterialRepository.updateStatus(randomUUID(), "generated");
        assertThat(result, is(0));
    }
}
