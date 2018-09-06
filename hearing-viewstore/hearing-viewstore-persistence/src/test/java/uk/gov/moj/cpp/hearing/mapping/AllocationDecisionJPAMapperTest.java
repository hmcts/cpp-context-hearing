package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.AllocationDecision;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class AllocationDecisionJPAMapperTest  {

    private AllocationDecisionJPAMapper allocationDecisionJPAMapper = new AllocationDecisionJPAMapper();

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision allocationDecisionEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision.class);
        assertThat(allocationDecisionJPAMapper.fromJPA(allocationDecisionEntity), whenAllocationDecision(isBean(AllocationDecision.class), allocationDecisionEntity));
    }

    @Test
    public void testToJPA() {
        final AllocationDecision allocationDecisionPojo = aNewEnhancedRandom().nextObject(AllocationDecision.class);
        assertThat(allocationDecisionJPAMapper.toJPA(allocationDecisionPojo), whenAllocationDecision(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision.class), allocationDecisionPojo));
    }

    public static BeanMatcher<AllocationDecision> whenAllocationDecision(final BeanMatcher<AllocationDecision> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision entity) {
        return m.with(AllocationDecision::getCourtDecision, is(entity.getCourtDecision()))
                .with(AllocationDecision::getDefendantRepresentation, is(entity.getDefendantRepresentation()))
                .with(AllocationDecision::getIndicationOfSentence, is(entity.getIndicationOfSentence()))
                .with(AllocationDecision::getProsecutionRepresentation, is(entity.getProsecutionRepresentation()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision> whenAllocationDecision(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision> m, final AllocationDecision pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getCourtDecision, is(pojo.getCourtDecision()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getDefendantRepresentation, is(pojo.getDefendantRepresentation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getIndicationOfSentence, is(pojo.getIndicationOfSentence()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getProsecutionRepresentation, is(pojo.getProsecutionRepresentation()));
    }
}