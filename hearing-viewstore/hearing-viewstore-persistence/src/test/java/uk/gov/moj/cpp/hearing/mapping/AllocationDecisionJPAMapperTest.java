package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.CourtIndicatedSentenceJPAMapperTest.whenCourtIndicatedSentence;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.CourtIndicatedSentence;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.util.UUID;

import org.junit.Test;

public class AllocationDecisionJPAMapperTest {

    private AllocationDecisionJPAMapper allocationDecisionJPAMapper = JPACompositeMappers.ALLOCATION_DECISION_JPA_MAPPER;

    public static BeanMatcher<AllocationDecision> whenAllocationDecision(final BeanMatcher<AllocationDecision> m,
                                                                         final uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision entity) {
        return m.with(AllocationDecision::getOriginatingHearingId, is(entity.getOriginatingHearingId()))
                .with(AllocationDecision::getMotReasonId, is(entity.getMotReasonId()))
                .with(AllocationDecision::getMotReasonDescription, is(entity.getMotReasonDescription()))
                .with(AllocationDecision::getMotReasonCode, is(entity.getMotReasonCode()))
                .with(AllocationDecision::getAllocationDecisionDate, is(entity.getAllocationDecisionDate()))
                .with(AllocationDecision::getSequenceNumber, is(entity.getSequenceNumber()))
                .with(AllocationDecision::getCourtIndicatedSentence, whenCourtIndicatedSentence(isBean(CourtIndicatedSentence.class), entity.getCourtIndicatedSentence()));
    }


    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision> whenAllocationDecision(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision> m, final AllocationDecision pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getCourtIndicatedSentence, whenCourtIndicatedSentence(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence.class), pojo.getCourtIndicatedSentence()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getOriginatingHearingId, is(pojo.getOriginatingHearingId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getMotReasonId, is(pojo.getMotReasonId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getMotReasonDescription, is(pojo.getMotReasonDescription()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getMotReasonCode, is(pojo.getMotReasonCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getSequenceNumber, is(pojo.getSequenceNumber()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision::getAllocationDecisionDate, is(pojo.getAllocationDecisionDate()));

    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision allocationDecisionEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision.class);
        assertThat(allocationDecisionJPAMapper.fromJPA(UUID.randomUUID(),allocationDecisionEntity), whenAllocationDecision(isBean(AllocationDecision.class), allocationDecisionEntity));
    }

    @Test
    public void testToJPA() {
        final AllocationDecision allocationDecisionPojo = aNewEnhancedRandom().nextObject(AllocationDecision.class);
        assertThat(allocationDecisionJPAMapper.toJPA(allocationDecisionPojo), whenAllocationDecision(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision.class), allocationDecisionPojo));
    }
}