package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.Prosecutor;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class CpsProsecutorJPAMapperTest {

    private CpsProsecutorJPAMapper cpsProsecutorJPAMapper = new CpsProsecutorJPAMapper();

    public static BeanMatcher<Prosecutor> whenProsecutor(final BeanMatcher<Prosecutor> m,
                                                                           final uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor entity) {
        return m.with(Prosecutor::getProsecutorCode, is(entity.getCpsProsecutorCode()))
                .with(Prosecutor::getProsecutorId, is(entity.getCpsProsecutorId()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor> whenProsecutor(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor> m, final Prosecutor pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor::getCpsProsecutorId, is(pojo.getProsecutorId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor::getCpsProsecutorCode, is(pojo.getProsecutorCode()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor cpsProsecutor = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor.class);
        assertThat(cpsProsecutorJPAMapper.fromJPA(cpsProsecutor), whenProsecutor(isBean(Prosecutor.class), cpsProsecutor));
    }

    @Test
    public void testToJPA() {
        final Prosecutor prosecutorPojo = aNewEnhancedRandom().nextObject(Prosecutor.class);
        assertThat(cpsProsecutorJPAMapper.toJPA(prosecutorPojo), whenProsecutor(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor.class), prosecutorPojo));
    }
}