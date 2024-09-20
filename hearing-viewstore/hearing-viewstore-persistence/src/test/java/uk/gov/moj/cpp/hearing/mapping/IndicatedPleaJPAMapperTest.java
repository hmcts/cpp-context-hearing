package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class IndicatedPleaJPAMapperTest {

    private IndicatedPleaJPAMapper indicatedPleaJPAMapper = JPACompositeMappers.INDICATED_PLEA_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final  UUID offenceId = UUID.randomUUID();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea indicatedPleaEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea.class);
        assertThat(indicatedPleaJPAMapper.fromJPA(offenceId, indicatedPleaEntity), whenIndicatedPlea(isBean(IndicatedPlea.class), offenceId, indicatedPleaEntity));
    }

    @Test
    public void testToJPA() {
        final IndicatedPlea indicatedPleaPojo = aNewEnhancedRandom().nextObject(IndicatedPlea.class);
        assertThat(indicatedPleaJPAMapper.toJPA(indicatedPleaPojo), whenIndicatedPlea(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea.class), indicatedPleaPojo));
    }

    public static BeanMatcher<IndicatedPlea> whenIndicatedPlea(final BeanMatcher<IndicatedPlea> m, final UUID offenceId,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea entity) {
        return m.with(IndicatedPlea::getIndicatedPleaDate, is(entity.getIndicatedPleaDate()))
        .with(IndicatedPlea::getIndicatedPleaValue, is(entity.getIndicatedPleaValue()))
        .with(IndicatedPlea::getOffenceId, is(offenceId))
        .with(IndicatedPlea::getSource, is(entity.getIndicatedPleaSource()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea> whenIndicatedPlea(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea> m, final IndicatedPlea pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea::getIndicatedPleaDate, is(pojo.getIndicatedPleaDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea::getIndicatedPleaValue, is(pojo.getIndicatedPleaValue()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea::getIndicatedPleaSource, is(pojo.getSource()));
    }
}