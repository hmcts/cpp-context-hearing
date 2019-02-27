package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import uk.gov.justice.core.courts.ReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

import org.junit.Test;

public class DefendantReferralReasonJPAMapperTest {

    private DefendantReferralReasonJPAMapper defendantReferralReasonJPAMapper = new DefendantReferralReasonJPAMapper();

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstReferralReason(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason entity) {
        return ElementAtListMatcher.first(whenReferralReason((BeanMatcher<ReferralReason>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstReferralReason(final BeanMatcher<?> m, final ReferralReason pojo) {
        return ElementAtListMatcher.first(whenReferralReason((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason>) m, pojo));
    }

    public static BeanMatcher<ReferralReason> whenReferralReason(final BeanMatcher<ReferralReason> m,
                                                                 final uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason entity) {
        return m.with(ReferralReason::getDefendantId, is(entity.getDefendantId()))
                .with(ReferralReason::getDescription, is(entity.getDescription()))
                .with(ReferralReason::getId, is(entity.getId().getId()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason> whenReferralReason(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason> m, final uk.gov.justice.core.courts.ReferralReason pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason::getDefendantId, is(pojo.getDefendantId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason::getDescription, is(pojo.getDescription()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason::getId, isBean(HearingSnapshotKey.class)
                        .with(HearingSnapshotKey::getId, is(pojo.getId())));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason associatedPersonEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason.class);
        assertThat(defendantReferralReasonJPAMapper.fromJPA(associatedPersonEntity),
                whenReferralReason(isBean(ReferralReason.class), associatedPersonEntity));
    }

    @Test
    public void testToJPA() {
        final ReferralReason associatedPersonEntity = aNewEnhancedRandom().nextObject(ReferralReason.class);
        assertThat(defendantReferralReasonJPAMapper.toJPA(aNewHearingJPADataTemplate().getHearing(), associatedPersonEntity),
                whenReferralReason(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason.class), associatedPersonEntity));
    }
}