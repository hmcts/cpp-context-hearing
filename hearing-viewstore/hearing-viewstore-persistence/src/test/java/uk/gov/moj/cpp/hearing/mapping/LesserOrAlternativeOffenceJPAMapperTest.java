package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.Test;

public class LesserOrAlternativeOffenceJPAMapperTest {

    private LesserOrAlternativeOffenceJPAMapper lesserOrAlternativeOffenceJPAMapper = new LesserOrAlternativeOffenceJPAMapper();

    public static BeanMatcher<LesserOrAlternativeOffence> whenLesserOrAlternativeOffence(final BeanMatcher<LesserOrAlternativeOffence> m,
                                                                                         final uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence entity) {
        return m.with(LesserOrAlternativeOffence::getOffenceTitle, is(entity.getLesserOffenceTitle()))
                .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(entity.getLesserOffenceTitleWelsh()))
                .with(LesserOrAlternativeOffence::getOffenceLegislation, is(entity.getLesserOffenceLegislation()))
                .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(entity.getLesserOffenceLegislationWelsh()))
                .with(LesserOrAlternativeOffence::getOffenceCode, is(entity.getLesserOffenceCode()))
                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(entity.getLesserOffenceDefinitionId()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence> whenLesserOrAlternativeOffence(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence> m, final LesserOrAlternativeOffence pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceTitle, is(pojo.getOffenceTitle()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceTitleWelsh, is(pojo.getOffenceTitleWelsh()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceLegislation, is(pojo.getOffenceLegislation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceLegislationWelsh, is(pojo.getOffenceLegislationWelsh()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceCode, is(pojo.getOffenceCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence::getLesserOffenceDefinitionId, is(pojo.getOffenceDefinitionId()));
    }

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
}