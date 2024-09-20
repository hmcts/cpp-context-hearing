package uk.gov.moj.cpp.hearing.mapping;

import org.junit.jupiter.api.Test;
import uk.gov.justice.core.courts.Ethnicity;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

public class EthnicityJPAMapperTest {

    private EthnicityJPAMapper ethnicityJPAMapper = new EthnicityJPAMapper();

    public static BeanMatcher<Ethnicity> whenEthnicity(final BeanMatcher<Ethnicity> m,
                                                   final uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity entity) {
        return m.with(Ethnicity::getObservedEthnicityId, is(entity.getObservedEthnicityId()))
                .with(Ethnicity::getObservedEthnicityCode, is(entity.getObservedEthnicityCode()))
                .with(Ethnicity::getSelfDefinedEthnicityCode, is(entity.getSelfDefinedEthnicityCode()))
                .with(Ethnicity::getSelfDefinedEthnicityId, is(entity.getSelfDefinedEthnicityId()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity> whenEthnicity(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity> m, final Ethnicity pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity::getObservedEthnicityId, is(pojo.getObservedEthnicityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity::getObservedEthnicityCode, is(pojo.getObservedEthnicityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity::getSelfDefinedEthnicityCode, is(pojo.getSelfDefinedEthnicityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity::getSelfDefinedEthnicityId, is(pojo.getSelfDefinedEthnicityId()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity entity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity.class);
        assertThat(ethnicityJPAMapper.fromJPA(entity), whenEthnicity(isBean(Ethnicity.class), entity));
    }

    @Test
    public void testToJPA() {
        final Ethnicity ethnicityPojo = aNewEnhancedRandom().nextObject(Ethnicity.class);
        assertThat(ethnicityJPAMapper.toJPA(ethnicityPojo), whenEthnicity(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity.class), ethnicityPojo));
    }
}