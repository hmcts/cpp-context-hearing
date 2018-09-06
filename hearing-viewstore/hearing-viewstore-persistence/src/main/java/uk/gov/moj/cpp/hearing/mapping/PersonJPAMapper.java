package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;

@ApplicationScoped
public class PersonJPAMapper {

    private AddressJPAMapper addressJPAMapper;
    private ContactNumberJPAMapper contactNumberJPAMapper;

    @Inject
    public PersonJPAMapper(final AddressJPAMapper addressJPAMapper, final ContactNumberJPAMapper contactNumberJPAMapper) {
        super();
        this.addressJPAMapper = addressJPAMapper;
        this.contactNumberJPAMapper = contactNumberJPAMapper;
    }

    //To keep cditester happy
    public PersonJPAMapper() {
    }

    public Person toJPA(final uk.gov.justice.json.schemas.core.Person pojo) {
        if (null == pojo) {
            return null;
        }
        final Person person = new Person();
        person.setAdditionalNationalityCode(pojo.getAdditionalNationalityCode());
        person.setAdditionalNationalityId(pojo.getAdditionalNationalityId());
        person.setAddress(addressJPAMapper.toJPA(pojo.getAddress()));
        person.setContact(contactNumberJPAMapper.toJPA(pojo.getContact()));
        person.setDateOfBirth(pojo.getDateOfBirth());
        person.setDisabilityStatus(pojo.getDisabilityStatus());
        person.setDocumentationLanguageNeeds(pojo.getDocumentationLanguageNeeds());
        person.setEthnicity(pojo.getEthnicity());
        person.setEthnicityId(pojo.getEthnicityId());
        person.setFirstName(pojo.getFirstName());
        person.setGender(pojo.getGender());
        person.setInterpreterLanguageNeeds(pojo.getInterpreterLanguageNeeds());
        person.setLastName(pojo.getLastName());
        person.setMiddleName(pojo.getMiddleName());
        person.setNationalInsuranceNumber(pojo.getNationalInsuranceNumber());
        person.setNationalityCode(pojo.getNationalityCode());
        person.setNationalityId(pojo.getNationalityId());
        person.setOccupation(pojo.getOccupation());
        person.setOccupationCode(pojo.getOccupationCode());
        person.setSpecificRequirements(pojo.getSpecificRequirements());
        person.setTitle(pojo.getTitle());
        return person;
    }

    public uk.gov.justice.json.schemas.core.Person fromJPA(final Person entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.Person.person()
                .withAdditionalNationalityCode(entity.getAdditionalNationalityCode())
                .withAdditionalNationalityId(entity.getAdditionalNationalityId())
                .withAddress(addressJPAMapper.fromJPA(entity.getAddress()))
                .withContact(contactNumberJPAMapper.fromJPA(entity.getContact()))
                .withDateOfBirth(entity.getDateOfBirth())
                .withDisabilityStatus(entity.getDisabilityStatus())
                .withDocumentationLanguageNeeds(entity.getDocumentationLanguageNeeds())
                .withEthnicity(entity.getEthnicity())
                .withEthnicityId(entity.getEthnicityId())
                .withFirstName(entity.getFirstName())
                .withGender(entity.getGender())
                .withInterpreterLanguageNeeds(entity.getInterpreterLanguageNeeds())
                .withLastName(entity.getLastName())
                .withMiddleName(entity.getMiddleName())
                .withNationalInsuranceNumber(entity.getNationalInsuranceNumber())
                .withNationalityCode(entity.getNationalityCode())
                .withNationalityId(entity.getNationalityId())
                .withOccupation(entity.getOccupation())
                .withOccupationCode(entity.getOccupationCode())
                .withSpecificRequirements(entity.getSpecificRequirements())
                .withTitle(entity.getTitle())
                .build();
    }
}