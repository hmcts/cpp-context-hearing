package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PersonJPAMapper {

    private AddressJPAMapper addressJPAMapper;
    private ContactNumberJPAMapper contactNumberJPAMapper;
    private EthnicityJPAMapper ethnicityJPAMapper;

    @Inject
    public PersonJPAMapper(final AddressJPAMapper addressJPAMapper,
                           final ContactNumberJPAMapper contactNumberJPAMapper,
                           final EthnicityJPAMapper ethnicityJPAMapper) {
        super();
        this.addressJPAMapper = addressJPAMapper;
        this.contactNumberJPAMapper = contactNumberJPAMapper;
        this.ethnicityJPAMapper =ethnicityJPAMapper;
    }

    //To keep cditester happy
    public PersonJPAMapper() {
    }

    public Person toJPA(final uk.gov.justice.core.courts.Person pojo) {
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
        person.setNationalityDescription(pojo.getNationalityDescription());
        person.setEthnicity(ethnicityJPAMapper.toJPA(pojo.getEthnicity()));
        return person;
    }

    public uk.gov.justice.core.courts.Person fromJPA(final Person entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Person.person()
                .withAdditionalNationalityCode(entity.getAdditionalNationalityCode())
                .withAdditionalNationalityId(entity.getAdditionalNationalityId())
                .withAddress(addressJPAMapper.fromJPA(entity.getAddress()))
                .withContact(contactNumberJPAMapper.fromJPA(entity.getContact()))
                .withDateOfBirth(entity.getDateOfBirth())
                .withDisabilityStatus(entity.getDisabilityStatus())
                .withDocumentationLanguageNeeds(entity.getDocumentationLanguageNeeds())
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
                .withNationalityDescription(entity.getNationalityDescription())
                .withEthnicity(ethnicityJPAMapper.fromJPA(entity.getEthnicity()))
                .build();
    }
}