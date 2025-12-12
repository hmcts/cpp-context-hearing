package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class OffenceFactsJPAMapperTest {

    private OffenceFactsJPAMapper offenceFactsJPAMapper = new OffenceFactsJPAMapper();

    public static BeanMatcher<OffenceFacts> whenOffenceFacts(final BeanMatcher<OffenceFacts> m,
                                                             final uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts entity) {
        return m.with(OffenceFacts::getAlcoholReadingAmount, is(Integer.parseInt(entity.getAlcoholReadingAmount())))
                .with(OffenceFacts::getAlcoholReadingMethodCode, is(entity.getAlcoholReadingMethodCode()))
                .with(OffenceFacts::getVehicleRegistration, is(entity.getVehicleRegistration()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts> whenOffenceFacts(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts> m, final OffenceFacts pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts::getAlcoholReadingAmount, is(pojo.getAlcoholReadingAmount().toString()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts::getAlcoholReadingMethodCode, is(pojo.getAlcoholReadingMethodCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts::getVehicleRegistration, is(pojo.getVehicleRegistration()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts offenceFactsEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts.class);
        offenceFactsEntity.setAlcoholReadingAmount("123");
        assertThat(offenceFactsJPAMapper.fromJPA(offenceFactsEntity), whenOffenceFacts(isBean(OffenceFacts.class), offenceFactsEntity));
    }

    @Test
    public void testToJPA() {
        final OffenceFacts offenceFactsPojo = aNewEnhancedRandom().nextObject(OffenceFacts.class);
        assertThat(offenceFactsJPAMapper.toJPA(offenceFactsPojo), whenOffenceFacts(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts.class), offenceFactsPojo));
    }
}