package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

import org.junit.Test;

public class JudicialRoleJPAMapperTest {

    private JudicialRoleJPAMapper judicialRoleJPAMapper = new JudicialRoleJPAMapper();

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstJudicialRole(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole entity) {
        return ElementAtListMatcher.first(whenJudicialRole((BeanMatcher<JudicialRole>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstJudicialRole(final BeanMatcher<?> m, final JudicialRole pojo) {
        return ElementAtListMatcher.first(whenJudicialRole((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole>) m, pojo));
    }

    public static BeanMatcher<JudicialRole> whenJudicialRole(final BeanMatcher<JudicialRole> m,
                                                             final uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole entity) {
        return m.with(JudicialRole::getFirstName, is(entity.getFirstName()))
                .with(JudicialRole::getIsBenchChairman, is(entity.getBenchChairman()))
                .with(JudicialRole::getIsDeputy, is(entity.getDeputy()))
                .with(JudicialRole::getJudicialId, is(entity.getJudicialId()))
                .with(jr->jr.getJudicialRoleType().getJudiciaryType(), is(entity.getJudicialRoleType()))
                .with(JudicialRole::getLastName, is(entity.getLastName()))
                .with(JudicialRole::getMiddleName, is(entity.getMiddleName()))
                .with(JudicialRole::getTitle, is(entity.getTitle()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole> whenJudicialRole(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole> m, final uk.gov.justice.core.courts.JudicialRole pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getFirstName, is(pojo.getFirstName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getBenchChairman, is(pojo.getIsBenchChairman()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getDeputy, is(pojo.getIsDeputy()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getJudicialId, is(pojo.getJudicialId()))
                .withValue(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getJudicialRoleType, pojo.getJudicialRoleType().getJudiciaryType())
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getLastName, is(pojo.getLastName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getMiddleName, is(pojo.getMiddleName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole::getTitle, is(pojo.getTitle()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole judicialRoleEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole.class);
        assertThat(judicialRoleJPAMapper.fromJPA(judicialRoleEntity),
                whenJudicialRole(isBean(JudicialRole.class), judicialRoleEntity));
    }

    @Test
    public void testToJPA() {
        final JudicialRole associatedPersonEntity = aNewEnhancedRandom().nextObject(JudicialRole.class);
        assertThat(judicialRoleJPAMapper.toJPA(aNewHearingJPADataTemplate().getHearing(), associatedPersonEntity),
                whenJudicialRole(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole.class), associatedPersonEntity));
    }
}