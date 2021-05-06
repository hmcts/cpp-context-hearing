package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.CpsProsecutorJPAMapperTest.whenProsecutor;
import static uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapperTest.whenFirstDefendant;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Prosecutor;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

import org.junit.Test;

public class ProsecutionCaseJPAMapperTest {

    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper = JPACompositeMappers.PROSECUTION_CASE_JPA_MAPPER;

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstProsecutionCase(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase entity) {
        return ElementAtListMatcher.first(whenProsecutionCase((BeanMatcher<ProsecutionCase>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstProsecutionCase(final BeanMatcher<?> m, final ProsecutionCase pojo) {
        return ElementAtListMatcher.first(whenProsecutionCase((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase>) m, pojo));
    }

    public static BeanMatcher<ProsecutionCase> whenProsecutionCase(final BeanMatcher<ProsecutionCase> m,
                                                                   final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase entity) {
        return m.with(ProsecutionCase::getCaseStatus, is(entity.getCaseStatus()))

                .with(ProsecutionCase::getDefendants, whenFirstDefendant(isBean(Defendant.class), entity.getDefendants().iterator().next()))

                .with(ProsecutionCase::getId, is(entity.getId().getId()))
                .with(ProsecutionCase::getInitiationCode, is(entity.getInitiationCode()))
                .with(ProsecutionCase::getOriginatingOrganisation, is(entity.getOriginatingOrganisation()))
                .with(ProsecutionCase::getStatementOfFacts, is(entity.getStatementOfFacts()))
                .with(ProsecutionCase::getStatementOfFactsWelsh, is(entity.getStatementOfFactsWelsh()))

                .with(ProsecutionCase::getProsecutor, CpsProsecutorJPAMapperTest.whenProsecutor(
                        isBean(Prosecutor.class), entity.getCpsProsecutor()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> whenProsecutionCase(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> m, final ProsecutionCase pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getCaseStatus, is(pojo.getCaseStatus()))

                /*
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getDefendants, whenFirstDefendant(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant.class), pojo.getDefendants().get(0)))
*/
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getId, isBean(HearingSnapshotKey.class)
                        .with(HearingSnapshotKey::getId, is(pojo.getId())))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getInitiationCode, is(pojo.getInitiationCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getOriginatingOrganisation, is(pojo.getOriginatingOrganisation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getStatementOfFacts, is(pojo.getStatementOfFacts()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getStatementOfFactsWelsh, is(pojo.getStatementOfFactsWelsh()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase::getCpsProsecutor, whenProsecutor(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor.class), pojo.getProsecutor()));
    }

    @Test
    public void testFromJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntity = hearingEntity.getProsecutionCases().iterator().next();
        assertThat(prosecutionCaseJPAMapper.fromJPA(prosecutionCaseEntity), whenProsecutionCase(isBean(ProsecutionCase.class), prosecutionCaseEntity));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final ProsecutionCase prosecutionCasePojo = prosecutionCaseJPAMapper.fromJPA(hearingEntity.getProsecutionCases().iterator().next());
        assertThat(prosecutionCaseJPAMapper.toJPA(hearingEntity, prosecutionCasePojo), whenProsecutionCase(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase.class), prosecutionCasePojo));
    }
}