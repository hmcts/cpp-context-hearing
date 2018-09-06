package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class LesserOrAlternativeOffenceJPAMapperTest {

    private LesserOrAlternativeOffenceJPAMapper lesserOrAlternativeOffenceJPAMapper = new LesserOrAlternativeOffenceJPAMapper();

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence.class);
        assertThat(lesserOrAlternativeOffenceJPAMapper.fromJPA(addressEntity), whenLesserOrAlternativeOffence(isBean(LesserOrAlternativeOffence.class), addressEntity));
    }

    @Test
    public void testToJPA() {
        final LesserOrAlternativeOffence addressPojo = aNewEnhancedRandom().nextObject(LesserOrAlternativeOffence.class);
        assertThat(lesserOrAlternativeOffenceJPAMapper.toJPA(addressPojo), whenLesserOrAlternativeOffence(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence.class), addressPojo));
    }

    public static BeanMatcher<LesserOrAlternativeOffence> whenLesserOrAlternativeOffence(final BeanMatcher<LesserOrAlternativeOffence> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence entity) {
        return m.with(LesserOrAlternativeOffence::getDescription, is(entity.getLesserOffenceTitle()))
                .with(LesserOrAlternativeOffence::getLegislation, is(entity.getLesserOffenceLegislation()))
                .with(LesserOrAlternativeOffence::getOffenceCode, is(entity.getLesserOffenceCode()))
                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(entity.getLesserOffenceDefinitionId()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence> whenLesserOrAlternativeOffence(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence> m, final LesserOrAlternativeOffence pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceTitle, is(pojo.getDescription()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceLegislation, is(pojo.getLegislation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceCode, is(pojo.getOffenceCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceDefinitionId, is(pojo.getOffenceDefinitionId()));
    }
}