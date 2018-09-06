package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.AddressJPAMapperTest.whenAddress;
import static uk.gov.moj.cpp.hearing.mapping.ContactNumberJPAMapperTest.whenContactNumber;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.Address;
import uk.gov.justice.json.schemas.core.ContactNumber;
import uk.gov.justice.json.schemas.core.Organisation;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class OrganisationJPAMapperTest {

    private OrganisationJPAMapper organisationJPAMapper = JPACompositeMappers.ORGANISATION_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation organizationEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class);
        assertThat(organisationJPAMapper.fromJPA(organizationEntity), whenOrganization(isBean(Organisation.class), organizationEntity));
    }

    @Test
    public void testToJPA() {
        final Organisation organisationPojo = aNewEnhancedRandom().nextObject(Organisation.class);
        assertThat(organisationJPAMapper.toJPA(organisationPojo), whenOrganization(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class), organisationPojo));
    }

    public static BeanMatcher<Organisation> whenOrganization(final BeanMatcher<Organisation> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation entity) {
        return m.with(Organisation::getAddress, whenAddress(isBean(Address.class), entity.getAddress()))
                .with(Organisation::getContact, whenContactNumber(isBean(ContactNumber.class), entity.getContact()))
                .with(Organisation::getId, is(entity.getId()))
                .with(Organisation::getIncorporationNumber, is(entity.getIncorporationNumber()))
                .with(Organisation::getName, is(entity.getName()))
                .with(Organisation::getRegisteredCharityNumber, is(entity.getRegisteredCharityNumber()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation> whenOrganization(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation> m, final Organisation pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation::getAddress, whenAddress(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Address.class), pojo.getAddress()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation::getContact, whenContactNumber(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Contact.class), pojo.getContact()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation::getId, is(pojo.getId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation::getIncorporationNumber, is(pojo.getIncorporationNumber()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation::getName, is(pojo.getName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation::getRegisteredCharityNumber, is(pojo.getRegisteredCharityNumber()));
    }
}