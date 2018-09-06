package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class ProsecutionCaseIdentifierJPAMapperTest {

    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper = new ProsecutionCaseIdentifierJPAMapper();

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier.class);
        assertThat(prosecutionCaseIdentifierJPAMapper.fromJPA(addressEntity), whenProsecutionCaseIdentifier(isBean(ProsecutionCaseIdentifier.class), addressEntity));
    }

    @Test
    public void testToJPA() {
        final ProsecutionCaseIdentifier addressPojo = aNewEnhancedRandom().nextObject(ProsecutionCaseIdentifier.class);
        assertThat(prosecutionCaseIdentifierJPAMapper.toJPA(addressPojo), whenProsecutionCaseIdentifier(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier.class), addressPojo));
    }

    public static BeanMatcher<ProsecutionCaseIdentifier> whenProsecutionCaseIdentifier(final BeanMatcher<ProsecutionCaseIdentifier> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier entity) {
        return m.with(ProsecutionCaseIdentifier::getCaseURN, is(entity.getCaseURN()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(entity.getProsecutionAuthorityCode()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(entity.getProsecutionAuthorityId()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(entity.getProsecutionAuthorityReference()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier> whenProsecutionCaseIdentifier(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier> m, final ProsecutionCaseIdentifier pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getCaseURN, is(pojo.getCaseURN()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(pojo.getProsecutionAuthorityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(pojo.getProsecutionAuthorityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(pojo.getProsecutionAuthorityReference()));
    }
}