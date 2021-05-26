package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.Test;

public class ProsecutionCaseIdentifierJPAMapperTest {

    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper = new ProsecutionCaseIdentifierJPAMapper();

    public static BeanMatcher<ProsecutionCaseIdentifier> whenProsecutionCaseIdentifier(final BeanMatcher<ProsecutionCaseIdentifier> m,
                                                                                       final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier entity) {
        return m.with(ProsecutionCaseIdentifier::getCaseURN, is(entity.getCaseURN()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(entity.getProsecutionAuthorityCode()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(entity.getProsecutionAuthorityId()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(entity.getProsecutionAuthorityReference()))
                .with(ProsecutionCaseIdentifier::getAddress, nullValue())
                .with(ProsecutionCaseIdentifier::getContact, nullValue())
                .with(ProsecutionCaseIdentifier::getProsecutorCategory, nullValue())
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityName, nullValue());
    }

    public static BeanMatcher<ProsecutionCaseIdentifier> whenProsecutionCaseIdentifierWithNsp(final BeanMatcher<ProsecutionCaseIdentifier> m,
                                                                                              final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier entity) {

        return m.with(ProsecutionCaseIdentifier::getCaseURN, is(entity.getCaseURN()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(entity.getProsecutionAuthorityCode()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(entity.getProsecutionAuthorityId()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(entity.getProsecutionAuthorityReference()))
                .with(ProsecutionCaseIdentifier::getProsecutionAuthorityName, is(entity.getProsecutorAuthorityName()))
                .with(ProsecutionCaseIdentifier::getProsecutorCategory, is(entity.getProsecutorCategory()))
                .with(ProsecutionCaseIdentifier::getAddress, isBean(Address.class)
                        .with(Address::getAddress1, is(entity.getProsecutorAuthorityAddress1()))
                        .with(Address::getAddress2, is(entity.getProsecutorAuthorityAddress2()))
                        .with(Address::getAddress3, is(entity.getProsecutorAuthorityAddress3()))
                        .with(Address::getAddress4, is(entity.getProsecutorAuthorityAddress4()))
                        .with(Address::getAddress5, is(entity.getProsecutorAuthorityAddress5()))
                        .with(Address::getPostcode, is(entity.getProsecutorAuthorityPostCode())))
                .with(ProsecutionCaseIdentifier::getContact, isBean(ContactNumber.class)
                        .with(ContactNumber::getPrimaryEmail, is(entity.getProsecutorAuthorityEmailAddress())));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier> whenProsecutionCaseIdentifier(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier> m, final ProsecutionCaseIdentifier pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getCaseURN, is(pojo.getCaseURN()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(pojo.getProsecutionAuthorityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(pojo.getProsecutionAuthorityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(pojo.getProsecutionAuthorityReference()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier> whenProsecutionCaseIdentifierWithNsp(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier> m, final ProsecutionCaseIdentifier pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getCaseURN, is(pojo.getCaseURN()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(pojo.getProsecutionAuthorityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(pojo.getProsecutionAuthorityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityAddress1, is(pojo.getAddress().getAddress1()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityAddress2, is(pojo.getAddress().getAddress2()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityAddress3, is(pojo.getAddress().getAddress3()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityAddress4, is(pojo.getAddress().getAddress4()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityAddress5, is(pojo.getAddress().getAddress5()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityPostCode, is(pojo.getAddress().getPostcode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityEmailAddress, is(pojo.getContact().getPrimaryEmail()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorAuthorityName, is(pojo.getProsecutionAuthorityName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier::getProsecutorCategory, is(pojo.getProsecutorCategory()));
    }

    @Test
    public void testFromJPAForMajorCreditor() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier majorCreditorEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier.class);
        majorCreditorEntity.setProsecutorAuthorityName(null);
        majorCreditorEntity.setProsecutorAuthorityAddress1(null);
        majorCreditorEntity.setProsecutorAuthorityAddress2(null);
        majorCreditorEntity.setProsecutorAuthorityAddress3(null);
        majorCreditorEntity.setProsecutorAuthorityAddress4(null);
        majorCreditorEntity.setProsecutorAuthorityAddress5(null);
        majorCreditorEntity.setProsecutorAuthorityPostCode(null);
        majorCreditorEntity.setProsecutorAuthorityEmailAddress(null);
        majorCreditorEntity.setProsecutorCategory(null);

        assertThat(prosecutionCaseIdentifierJPAMapper.fromJPA(majorCreditorEntity), whenProsecutionCaseIdentifier(isBean(ProsecutionCaseIdentifier.class), majorCreditorEntity));
    }

    @Test
    public void testFromJPAForNsp() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier.class);
        assertThat(prosecutionCaseIdentifierJPAMapper.fromJPA(addressEntity), whenProsecutionCaseIdentifierWithNsp(isBean(ProsecutionCaseIdentifier.class), addressEntity));
    }

    @Test
    public void testToJPA() {
        final ProsecutionCaseIdentifier addressPojo = aNewEnhancedRandom().nextObject(ProsecutionCaseIdentifier.class);
        assertThat(prosecutionCaseIdentifierJPAMapper.toJPA(addressPojo), whenProsecutionCaseIdentifier(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier.class), addressPojo));
    }

    @Test
    public void testToJPAForNsp() {
        final ProsecutionCaseIdentifier nspPojo = aNewEnhancedRandom().nextObject(ProsecutionCaseIdentifier.class);
        nspPojo.setMajorCreditorCode(null);
        assertThat(prosecutionCaseIdentifierJPAMapper.toJPA(nspPojo), whenProsecutionCaseIdentifierWithNsp(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier.class), nspPojo));
    }
}