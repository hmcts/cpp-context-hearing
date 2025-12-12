package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.HearingType;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class HearingTypeJPAMapperTest {

    private HearingTypeJPAMapper hearingTypeJPAMapper = new HearingTypeJPAMapper();

    public static BeanMatcher<HearingType> whenHearingType(final BeanMatcher<HearingType> m,
                                                           final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType entity) {
        return m.with(HearingType::getId, is(entity.getId()))
                .with(HearingType::getDescription, is(entity.getDescription()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType> whenHearingType(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType> m, final HearingType pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType::getId, is(pojo.getId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType::getDescription, is(pojo.getDescription()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType hearingTypeEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class);
        assertThat(hearingTypeJPAMapper.fromJPA(hearingTypeEntity),
                whenHearingType(isBean(HearingType.class), hearingTypeEntity));
    }

    @Test
    public void testToJPA() {
        final HearingType hearingTypePojo = aNewEnhancedRandom().nextObject(HearingType.class);
        assertThat(hearingTypeJPAMapper.toJPA(hearingTypePojo),
                whenHearingType(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class), hearingTypePojo));
    }
}