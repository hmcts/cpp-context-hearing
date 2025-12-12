package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Address;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class AddressJPAMapperTest {

    private AddressJPAMapper addressJPAMapper = new AddressJPAMapper();

    public static BeanMatcher<Address> whenAddress(final BeanMatcher<Address> m,
                                                   final uk.gov.moj.cpp.hearing.persist.entity.ha.Address entity) {
        return m.with(Address::getAddress1, is(entity.getAddress1()))
                .with(Address::getAddress2, is(entity.getAddress2()))
                .with(Address::getAddress3, is(entity.getAddress3()))
                .with(Address::getAddress4, is(entity.getAddress4()))
                .with(Address::getAddress5, is(entity.getAddress5()))
                .with(Address::getPostcode, is(entity.getPostCode()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Address> whenAddress(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Address> m, final Address pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Address::getAddress1, is(pojo.getAddress1()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Address::getAddress2, is(pojo.getAddress2()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Address::getAddress3, is(pojo.getAddress3()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Address::getAddress4, is(pojo.getAddress4()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Address::getAddress5, is(pojo.getAddress5()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Address::getPostCode, is(pojo.getPostcode()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Address addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Address.class);
        assertThat(addressJPAMapper.fromJPA(addressEntity), whenAddress(isBean(Address.class), addressEntity));
    }

    @Test
    public void testToJPA() {
        final Address addressPojo = aNewEnhancedRandom().nextObject(Address.class);
        assertThat(addressJPAMapper.toJPA(addressPojo), whenAddress(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Address.class), addressPojo));
    }
}