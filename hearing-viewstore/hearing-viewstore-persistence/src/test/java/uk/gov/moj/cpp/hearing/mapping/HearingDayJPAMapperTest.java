package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingDayJPAMapperTest {

    private HearingDayJPAMapper hearingDayJPAMapper = new HearingDayJPAMapper();

    public static BeanMatcher<HearingDay> whenHearingDay(final BeanMatcher<HearingDay> m,
                                                         final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay entity) {
        return m.with(HearingDay::getListedDurationMinutes, is(entity.getListedDurationMinutes()))
                .with(HearingDay::getHasSharedResults, is(entity.getHasSharedResults()))
                .with(HearingDay::getListingSequence, is(entity.getListingSequence()))
                .with(HearingDay::getSittingDay, is(entity.getSittingDay()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay> whenHearingDay(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay> m, final uk.gov.justice.core.courts.HearingDay pojo) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getListedDurationMinutes, is(pojo.getListedDurationMinutes()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getHasSharedResults, is(pojo.getHasSharedResults()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getListingSequence, is(pojo.getListingSequence()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getSittingDay, is(pojo.getSittingDay()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay associatedPersonEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class);
        assertThat(hearingDayJPAMapper.fromJPA(associatedPersonEntity),
                whenHearingDay(isBean(HearingDay.class), associatedPersonEntity));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing.class);
        final HearingDay associatedPersonEntity = aNewEnhancedRandom().nextObject(HearingDay.class);
        assertThat(hearingDayJPAMapper.toJPA(hearingEntity, associatedPersonEntity),
                whenHearingDay(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class), associatedPersonEntity));
    }
}
