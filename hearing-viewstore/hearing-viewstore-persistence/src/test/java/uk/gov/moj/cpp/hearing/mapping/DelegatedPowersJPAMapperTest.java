package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class DelegatedPowersJPAMapperTest {

    private DelegatedPowersJPAMapper delegatedPowersJPAMapper = new DelegatedPowersJPAMapper();

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers delegatedPowersEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers.class);
        assertThat(delegatedPowersJPAMapper.fromJPA(delegatedPowersEntity), whenDelegatedPowers(isBean(DelegatedPowers.class), delegatedPowersEntity));
    }

    @Test
    public void testToJPA() {
        final DelegatedPowers delegatedPowersPojo = aNewEnhancedRandom().nextObject(DelegatedPowers.class);
        assertThat(delegatedPowersJPAMapper.toJPA(delegatedPowersPojo), whenDelegatedPowers(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers.class), delegatedPowersPojo));
    }

    public static BeanMatcher<DelegatedPowers> whenDelegatedPowers(final BeanMatcher<DelegatedPowers> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers entity) {
        return m.with(DelegatedPowers::getFirstName, is(entity.getDelegatedPowersFirstName()))
                .with(DelegatedPowers::getLastName, is(entity.getDelegatedPowersLastName()))
                .with(DelegatedPowers::getUserId, is(entity.getDelegatedPowersUserId()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers> whenDelegatedPowers(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers> m, final DelegatedPowers pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers::getDelegatedPowersFirstName, is(pojo.getFirstName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers::getDelegatedPowersLastName, is(pojo.getLastName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers::getDelegatedPowersUserId, is(pojo.getUserId()));
    }
}