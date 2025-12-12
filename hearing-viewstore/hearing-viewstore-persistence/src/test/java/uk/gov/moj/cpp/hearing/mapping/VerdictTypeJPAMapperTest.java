package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class VerdictTypeJPAMapperTest {

    private VerdictTypeJPAMapper verdictTypeJPAMapper = new VerdictTypeJPAMapper();

    public static BeanMatcher<VerdictType> whenVerdictType(final BeanMatcher<VerdictType> m,
                                                           final uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType entity) {
        return m.with(VerdictType::getCategory, is(entity.getVerdictCategory()))
                .with(VerdictType::getCategoryType, is(entity.getVerdictCategoryType()))
                .with(VerdictType::getId, is(entity.getVerdictTypeId()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType> whenVerdictType(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType> m, final VerdictType pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType::getVerdictCategory, is(pojo.getCategory()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType::getVerdictCategoryType, is(pojo.getCategoryType()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType::getVerdictTypeId, is(pojo.getId()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType.class);
        assertThat(verdictTypeJPAMapper.fromJPA(addressEntity),
                whenVerdictType(isBean(VerdictType.class), addressEntity));
    }

    @Test
    public void testToJPA() {
        final VerdictType verdictTypePojo = aNewEnhancedRandom().nextObject(VerdictType.class);
        assertThat(verdictTypeJPAMapper.toJPA(verdictTypePojo),
                whenVerdictType(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType.class), verdictTypePojo));
    }
}