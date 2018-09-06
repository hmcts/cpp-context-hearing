package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.ContactNumber;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class ContactNumberJPAMapperTest {

    private ContactNumberJPAMapper contactNumberJPAMapper = new ContactNumberJPAMapper();

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Contact contactEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Contact.class);
        assertThat(contactNumberJPAMapper.fromJPA(contactEntity), whenContactNumber(isBean(ContactNumber.class), contactEntity));
    }

    @Test
    public void testToJPA() {
        final ContactNumber contactNumberPojo = aNewEnhancedRandom().nextObject(ContactNumber.class);
        assertThat(contactNumberJPAMapper.toJPA(contactNumberPojo), whenContactNumber(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Contact.class), contactNumberPojo));
    }

    public static BeanMatcher<ContactNumber> whenContactNumber(final BeanMatcher<ContactNumber> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.Contact entity) {
        return m.with(ContactNumber::getFax, is(entity.getFax()))
                .with(ContactNumber::getHome, is(entity.getHome()))
                .with(ContactNumber::getMobile, is(entity.getMobile()))
                .with(ContactNumber::getPrimaryEmail, is(entity.getPrimaryEmail()))
                .with(ContactNumber::getSecondaryEmail, is(entity.getSecondaryEmail()))
                .with(ContactNumber::getWork, is(entity.getWork()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Contact> whenContactNumber(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Contact> m, final ContactNumber pojo) {
        return m.with(Contact::getFax, is(pojo.getFax()))
                .with(Contact::getHome, is(pojo.getHome()))
                .with(Contact::getMobile, is(pojo.getMobile()))
                .with(Contact::getPrimaryEmail, is(pojo.getPrimaryEmail()))
                .with(Contact::getSecondaryEmail, is(pojo.getSecondaryEmail()))
                .with(Contact::getWork, is(pojo.getWork()));
    }
}