package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.AddressJPAMapperTest.whenAddress;
import static uk.gov.moj.cpp.hearing.mapping.EthnicityJPAMapperTest.whenEthnicity;
import static uk.gov.moj.cpp.hearing.mapping.ContactNumberJPAMapperTest.whenContactNumber;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Ethnicity;
import uk.gov.justice.core.courts.Person;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.Test;

public class PersonJPAMapperTest {

    private PersonJPAMapper personJPAMapper = JPACompositeMappers.PERSON_JPA_MAPPER;

    public static BeanMatcher<Person> whenPerson(final BeanMatcher<Person> m,
                                                 final uk.gov.moj.cpp.hearing.persist.entity.ha.Person entity) {
        return m.with(Person::getAdditionalNationalityCode, is(entity.getAdditionalNationalityCode()))
                .with(Person::getAdditionalNationalityId, is(entity.getAdditionalNationalityId()))
                .with(Person::getAddress, whenAddress(isBean(Address.class), entity.getAddress()))
                .with(Person::getContact, whenContactNumber(isBean(ContactNumber.class), entity.getContact()))
                .with(Person::getDateOfBirth, is(entity.getDateOfBirth()))
                .with(Person::getDisabilityStatus, is(entity.getDisabilityStatus()))
                .with(Person::getDocumentationLanguageNeeds, is(entity.getDocumentationLanguageNeeds()))
                .with(Person::getFirstName, is(entity.getFirstName()))
                .with(Person::getGender, is(entity.getGender()))
                .with(Person::getInterpreterLanguageNeeds, is(entity.getInterpreterLanguageNeeds()))
                .with(Person::getLastName, is(entity.getLastName()))
                .with(Person::getMiddleName, is(entity.getMiddleName()))
                .with(Person::getNationalInsuranceNumber, is(entity.getNationalInsuranceNumber()))
                .with(Person::getNationalityCode, is(entity.getNationalityCode()))
                .with(Person::getNationalityId, is(entity.getNationalityId()))
                .with(Person::getOccupation, is(entity.getOccupation()))
                .with(Person::getOccupationCode, is(entity.getOccupationCode()))
                .with(Person::getEthnicity, whenEthnicity(isBean(Ethnicity.class), entity.getEthnicity()))
                .with(Person::getSpecificRequirements, is(entity.getSpecificRequirements()))
                .with(Person::getNationalityDescription, is(entity.getNationalityDescription()))
                .with(Person::getTitle, is(entity.getTitle()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Person> whenPerson(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Person> m, final Person pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getAdditionalNationalityCode, is(pojo.getAdditionalNationalityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getAdditionalNationalityId, is(pojo.getAdditionalNationalityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getAddress, whenAddress(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Address.class), pojo.getAddress()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getContact, whenContactNumber(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Contact.class), pojo.getContact()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getDateOfBirth, is(pojo.getDateOfBirth()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getDisabilityStatus, is(pojo.getDisabilityStatus()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getDocumentationLanguageNeeds, is(pojo.getDocumentationLanguageNeeds()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getFirstName, is(pojo.getFirstName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getGender, is(pojo.getGender()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getInterpreterLanguageNeeds, is(pojo.getInterpreterLanguageNeeds()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getLastName, is(pojo.getLastName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getMiddleName, is(pojo.getMiddleName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getNationalInsuranceNumber, is(pojo.getNationalInsuranceNumber()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getNationalityCode, is(pojo.getNationalityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getNationalityId, is(pojo.getNationalityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getOccupation, is(pojo.getOccupation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getOccupationCode, is(pojo.getOccupationCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getSpecificRequirements, is(pojo.getSpecificRequirements()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getTitle, is(pojo.getTitle()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Person::getEthnicity, whenEthnicity(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity.class), pojo.getEthnicity()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Person personEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Person.class);
        assertThat(personJPAMapper.fromJPA(personEntity), whenPerson(isBean(Person.class), personEntity));
    }

    @Test
    public void testToJPA() {
        final Person personPojo = aNewEnhancedRandom().nextObject(Person.class);
        assertThat(personJPAMapper.toJPA(personPojo), whenPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Person.class), personPojo));
    }
}