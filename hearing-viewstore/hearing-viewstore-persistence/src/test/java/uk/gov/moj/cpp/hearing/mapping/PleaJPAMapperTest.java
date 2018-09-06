package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.DelegatedPowersJPAMapperTest.whenDelegatedPowers;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import java.util.UUID;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class PleaJPAMapperTest {

    private PleaJPAMapper pleaJPAMapper = JPACompositeMappers.PLEA_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final UUID offenceId = UUID.randomUUID();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Plea pleaEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea.class);
        assertThat(pleaJPAMapper.fromJPA(offenceId, pleaEntity), whenPlea(isBean(Plea.class), offenceId, pleaEntity));
    }

    @Test
    public void testToJPA() {
        final Plea pleaPojo = aNewEnhancedRandom().nextObject(Plea.class);
        assertThat(pleaJPAMapper.toJPA(pleaPojo), whenPlea(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea.class), pleaPojo));
    }

    public static BeanMatcher<Plea> whenPlea(final BeanMatcher<Plea> m, final UUID offenceId, 
            final uk.gov.moj.cpp.hearing.persist.entity.ha.Plea entity) {
        return m.with(Plea::getDelegatedPowers, whenDelegatedPowers(isBean(DelegatedPowers.class), entity.getDelegatedPowers()))
                .with(Plea::getOffenceId, is(offenceId))
                .with(Plea::getOriginatingHearingId, is(entity.getOriginatingHearingId()))
                .with(Plea::getPleaDate, is(entity.getPleaDate()))
                .with(Plea::getPleaValue, is(entity.getPleaValue()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Plea> whenPlea(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Plea> m, final Plea pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea::getDelegatedPowers, whenDelegatedPowers(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers.class), pojo.getDelegatedPowers()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea::getOriginatingHearingId, is(pojo.getOriginatingHearingId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea::getPleaDate, is(pojo.getPleaDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea::getPleaValue, is(pojo.getPleaValue()));
    }
}