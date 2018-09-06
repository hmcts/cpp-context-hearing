package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.AddressJPAMapperTest.whenAddress;
import static uk.gov.moj.cpp.hearing.mapping.ContactNumberJPAMapperTest.whenContactNumber;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.Address;
import uk.gov.justice.json.schemas.core.AssociatedPerson;
import uk.gov.justice.json.schemas.core.ContactNumber;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

public class AssociatedPersonJPAMapperTest {

    private AssociatedPersonJPAMapper associatedPersonJPAMapper = JPACompositeMappers.ASSOCIATED_PERSON_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson associatedPersonEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson.class);
        assertThat(associatedPersonJPAMapper.fromJPA(associatedPersonEntity), 
                whenAssociatedPerson(isBean(AssociatedPerson.class), associatedPersonEntity));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final Defendant defendantEntity = hearingEntity.getProsecutionCases().get(0).getDefendants().get(0);
        final AssociatedPerson associatedPersonPojo = aNewEnhancedRandom().nextObject(AssociatedPerson.class);
        assertThat(associatedPersonJPAMapper.toJPA(hearingEntity, defendantEntity, associatedPersonPojo), 
                whenAssociatedPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson.class), associatedPersonPojo));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstAssociatedPerson(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson entity) {
        return ElementAtListMatcher.first(whenAssociatedPerson((BeanMatcher<AssociatedPerson>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstAssociatedPerson(final BeanMatcher<?> m, final AssociatedPerson pojo) {
        return ElementAtListMatcher.first(whenAssociatedPerson((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson>) m, pojo));
    }

    public static BeanMatcher<AssociatedPerson> whenAssociatedPerson(final BeanMatcher<AssociatedPerson> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson entity) {
        return m.with(AssociatedPerson::getRole, is(entity.getRole()))
                .with(AssociatedPerson::getPerson, isBean(Person.class)
                .with(Person::getAdditionalNationalityCode, is(entity.getAdditionalNationalityCode()))
                .with(Person::getAdditionalNationalityId, is(entity.getAdditionalNationalityId()))

                .with(Person::getAddress, 
                        whenAddress(isBean(Address.class), entity.getAddress()))

                .with(Person::getContact, 
                        whenContactNumber(isBean(ContactNumber.class), entity.getContact()))

                .with(Person::getDateOfBirth, is(entity.getDateOfBirth()))
                .with(Person::getDisabilityStatus, is(entity.getDisabilityStatus()))
                .with(Person::getDocumentationLanguageNeeds, is(entity.getDocumentationLanguageNeeds()))
                .with(Person::getEthnicity, is(entity.getEthnicity()))
                .with(Person::getEthnicityId, is(entity.getEthnicityId()))
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
                .with(Person::getSpecificRequirements, is(entity.getSpecificRequirements()))
                .with(Person::getTitle, is(entity.getTitle())));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson> whenAssociatedPerson(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson> m, final uk.gov.justice.json.schemas.core.AssociatedPerson pojo) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getAdditionalNationalityCode, is(pojo.getPerson().getAdditionalNationalityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getAdditionalNationalityId, is(pojo.getPerson().getAdditionalNationalityId()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getAddress, 
                        whenAddress(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Address.class), pojo.getPerson().getAddress()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getContact, 
                        whenContactNumber(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Contact.class), pojo.getPerson().getContact()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getDateOfBirth, is(pojo.getPerson().getDateOfBirth()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getDisabilityStatus, is(pojo.getPerson().getDisabilityStatus()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getDocumentationLanguageNeeds, is(pojo.getPerson().getDocumentationLanguageNeeds()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getEthnicity, is(pojo.getPerson().getEthnicity()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getEthnicityId, is(pojo.getPerson().getEthnicityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getFirstName, is(pojo.getPerson().getFirstName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getGender, is(pojo.getPerson().getGender()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getInterpreterLanguageNeeds, is(pojo.getPerson().getInterpreterLanguageNeeds()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getLastName, is(pojo.getPerson().getLastName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getMiddleName, is(pojo.getPerson().getMiddleName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getNationalInsuranceNumber, is(pojo.getPerson().getNationalInsuranceNumber()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getNationalityCode, is(pojo.getPerson().getNationalityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getNationalityId, is(pojo.getPerson().getNationalityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getOccupation, is(pojo.getPerson().getOccupation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getOccupationCode, is(pojo.getPerson().getOccupationCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getSpecificRequirements, is(pojo.getPerson().getSpecificRequirements()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getTitle, is(pojo.getPerson().getTitle()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getRole, is(pojo.getRole()));
    }
}