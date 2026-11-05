package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class PtphDetailRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    private static final String TIER = "TIER_1";
    private static final String AMENDED_TIER = "TIER_3";
    private static final String LIST_TYPE = "TYPE_1_FIXED";
    private static final String KEY_REASON = "Awaiting expert evidence";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private PtphDetailRepository ptphDetailRepository;

    @BeforeEach
    void openEntityManagerAndCreateRepository() {
        ptphDetailRepository = new PtphDetailRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(ptphDetailRepository);
    }

    @Test
    void shouldSaveAndFindByHearingId() {

        final PtphDetail ptphDetail = ptphDetail(randomUUID());

        ptphDetailRepository.save(ptphDetail);

        final PtphDetail found = ptphDetailRepository.findBy(ptphDetail.getHearingId());

        assertThat(found.getHearingId(), is(ptphDetail.getHearingId()));
        assertThat(found.getTier(), is(TIER));
        assertThat(found.getListType(), is(LIST_TYPE));
        assertThat(found.getKeyReason(), is(KEY_REASON));
        assertThat(found.isFinalised(), is(true));
    }

    @Test
    void shouldReturnNullWhenNoPtphDetailExistsForTheHearing() {
        assertThat(ptphDetailRepository.findBy(randomUUID()), is(nullValue()));
    }

    @Test
    void shouldOverwriteAnExistingPtphDetailOnSave() {

        final UUID hearingId = randomUUID();
        ptphDetailRepository.save(ptphDetail(hearingId));

        final PtphDetail amended = ptphDetail(hearingId);
        amended.setTier(AMENDED_TIER);
        amended.setFinalised(false);

        ptphDetailRepository.save(amended);

        final PtphDetail found = ptphDetailRepository.findBy(hearingId);

        assertThat(found.getTier(), is(AMENDED_TIER));
        assertThat(found.isFinalised(), is(false));
    }

    @Test
    void shouldRemoveAndFlushADetachedPtphDetail() {

        final PtphDetail ptphDetail = ptphDetail(randomUUID());
        ptphDetailRepository.save(ptphDetail);

        // passed detached so removeAndFlush has to merge it back before removing
        ptphDetailRepository.removeAndFlush(ptphDetail);

        assertThat(ptphDetailRepository.findBy(ptphDetail.getHearingId()), is(nullValue()));
    }

    private PtphDetail ptphDetail(final UUID hearingId) {
        final PtphDetail ptphDetail = new PtphDetail();
        ptphDetail.setHearingId(hearingId);
        ptphDetail.setTier(TIER);
        ptphDetail.setListType(LIST_TYPE);
        ptphDetail.setKeyReason(KEY_REASON);
        ptphDetail.setFinalised(true);
        return ptphDetail;
    }
}
