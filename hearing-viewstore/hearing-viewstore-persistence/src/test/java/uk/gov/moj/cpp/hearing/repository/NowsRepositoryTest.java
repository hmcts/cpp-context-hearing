package uk.gov.moj.cpp.hearing.repository;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class NowsRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";
    private static final String language = "wales";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    UUID id;
    UUID hearingId;
    UUID defendantId;
    UUID nowsTypeId;
    UUID nowMaterialId;
    UUID sharedResultId;
    private NowsMaterial nowsMaterial;

    private NowsRepository nowsRepository;

    @BeforeEach
    void openEntityManagerAndCreateRepository() {
        nowsRepository = new NowsRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(nowsRepository);

        id = randomUUID();
        hearingId = randomUUID();
        defendantId = randomUUID();
        nowsTypeId = randomUUID();
        nowMaterialId = randomUUID();
        sharedResultId = randomUUID();

        Nows nows = new Nows();
        nows.setId(id);
        nows.setDefendantId(defendantId);
        nows.setHearingId(hearingId);
        nows.setNowsTypeId(nowsTypeId);

        nowsMaterial = new NowsMaterial();
        nowsMaterial.setId(nowMaterialId);
        nowsMaterial.setNows(nows);
        nowsMaterial.setStatus("requested");
        nowsMaterial.setUserGroups(asSet("LO", "GA"));
        nowsMaterial.setLanguage(language);
        nows.getMaterial().add(nowsMaterial);

        NowsResult nowsResult = new NowsResult();
        nowsResult.setId(randomUUID());
        nowsResult.setSequence(1);
        nowsResult.setSharedResultId(sharedResultId);
        nowsResult.setNowsMaterial(nowsMaterial);
        nowsMaterial.getNowResult().add(nowsResult);

        nowsRepository.save(nows);
    }


    @Test
    void findAllTest() {
        final Nows nows = nowsRepository.findAll().stream().filter(n -> n.getId().equals(id)).findFirst().get();
        assertThat(nows.getDefendantId(), is(this.defendantId));
        assertThat(nows.getHearingId(), is(this.hearingId));
        assertThat(nows.getNowsTypeId(), is(this.nowsTypeId));

        assertThat(nows.getMaterial().iterator().next().getId(), is(this.nowsMaterial.getId()));
        assertThat(nows.getMaterial().iterator().next().getStatus(), is("requested"));
        assertThat(nows.getMaterial().iterator().next().getUserGroups(), containsInAnyOrder("LO", "GA"));
        assertThat(nows.getMaterial().iterator().next().getLanguage(), is(language));

        assertThat(nows.getMaterial().iterator().next().getNowResult().iterator().next().getSharedResultId(), is(sharedResultId));
        assertThat(nows.getMaterial().iterator().next().getNowResult().iterator().next().getSequence(), is(1));
    }

    @Test
    void findByHearingIdTest() {
        final List<Nows> nows = nowsRepository.findByHearingId(hearingId);
        assertThat(nows.get(0).getId(), is(this.id));
        assertThat(nows.get(0).getDefendantId(), is(this.defendantId));
        assertThat(nows.get(0).getHearingId(), is(this.hearingId));
        assertThat(nows.get(0).getNowsTypeId(), is(this.nowsTypeId));

        assertThat(nows.get(0).getMaterial().iterator().next().getId(), is(this.nowsMaterial.getId()));
        assertThat(nows.get(0).getMaterial().iterator().next().getStatus(), is("requested"));
        assertThat(nows.get(0).getMaterial().iterator().next().getUserGroups(), containsInAnyOrder("LO", "GA"));
        assertThat(nows.get(0).getMaterial().iterator().next().getLanguage(), is(language));

        assertThat(nows.get(0).getMaterial().iterator().next().getNowResult().iterator().next().getSharedResultId(), is(sharedResultId));
        assertThat(nows.get(0).getMaterial().iterator().next().getNowResult().iterator().next().getSequence(), is(1));
    }
}
