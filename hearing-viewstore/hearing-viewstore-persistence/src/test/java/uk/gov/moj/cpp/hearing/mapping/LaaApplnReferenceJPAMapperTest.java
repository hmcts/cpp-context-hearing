package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.LaaReference;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class LaaApplnReferenceJPAMapperTest {

    private LaaApplnReferenceJPAMapper laaApplnReferenceJPAMapper = new LaaApplnReferenceJPAMapper();

    public static BeanMatcher<LaaReference> whenLaa(final BeanMatcher<LaaReference> m,
                                                        final uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference entity) {
        return m.with(LaaReference::getApplicationReference, is(entity.getApplicationReference()))
                .with(LaaReference::getEffectiveEndDate, is(entity.getEffectiveEndDate()))
                .with(LaaReference::getEffectiveStartDate, is(entity.getEffectiveStartDate()))
                .with(LaaReference::getStatusCode, is(entity.getStatusCode()))
                .with(LaaReference::getStatusDescription, is(entity.getStatusDescription()))
                .with(LaaReference::getStatusId, is(entity.getStatusId()))
                .with(LaaReference::getStatusDate, is(entity.getStatusDate()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference> whenLaa(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference> m, final LaaReference pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getApplicationReference, is(pojo.getApplicationReference()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getEffectiveEndDate, is(pojo.getEffectiveEndDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getEffectiveStartDate, is(pojo.getEffectiveStartDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getStatusCode, is(pojo.getStatusCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getStatusDescription, is(pojo.getStatusDescription()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getStatusId, is(pojo.getStatusId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference::getStatusDate, is(pojo.getStatusDate()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference laaEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference.class);
        assertThat(laaApplnReferenceJPAMapper.fromJpa(laaEntity), whenLaa(isBean(LaaReference.class), laaEntity));
    }

    @Test
    public void testToJPA() {
        final LaaReference laaPojo = aNewEnhancedRandom().nextObject(LaaReference.class);
        assertThat(laaApplnReferenceJPAMapper.toJpa(laaPojo), whenLaa(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference.class), laaPojo));
    }
}
