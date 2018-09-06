package uk.gov.moj.cpp.hearing.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import uk.gov.justice.json.schemas.core.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

@ApplicationScoped
public class AssociatedPersonJPAMapper {

    private AddressJPAMapper addressJPAMapper;
    private ContactNumberJPAMapper contactNumberJPAMapper;

    @Inject
    public AssociatedPersonJPAMapper(AddressJPAMapper addressJPAMapper,
            ContactNumberJPAMapper contactNumberJPAMapper) {
        this.addressJPAMapper = addressJPAMapper;
        this.contactNumberJPAMapper = contactNumberJPAMapper;
    }

    public AssociatedPersonJPAMapper() {
        //TO keep cdi tester happy
    }

    AssociatedPerson toJPA(final Hearing hearing, final Defendant defendant, final uk.gov.justice.json.schemas.core.AssociatedPerson pojo) {
        final AssociatedPerson associatedPerson = new AssociatedPerson();
        associatedPerson.setAdditionalNationalityCode(pojo.getPerson().getAdditionalNationalityCode());
        associatedPerson.setAdditionalNationalityId(pojo.getPerson().getAdditionalNationalityId());
        associatedPerson.setAddress(addressJPAMapper.toJPA(pojo.getPerson().getAddress()));
        associatedPerson.setContact(contactNumberJPAMapper.toJPA(pojo.getPerson().getContact()));
        associatedPerson.setDateOfBirth(pojo.getPerson().getDateOfBirth());
        associatedPerson.setDisabilityStatus(pojo.getPerson().getDisabilityStatus());
        associatedPerson.setDocumentationLanguageNeeds(pojo.getPerson().getDocumentationLanguageNeeds());
        associatedPerson.setEthnicity(pojo.getPerson().getEthnicity());
        associatedPerson.setEthnicityId(pojo.getPerson().getEthnicityId());
        associatedPerson.setFirstName(pojo.getPerson().getFirstName());
        associatedPerson.setGender(pojo.getPerson().getGender());
        associatedPerson.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        associatedPerson.setInterpreterLanguageNeeds(pojo.getPerson().getInterpreterLanguageNeeds());
        associatedPerson.setLastName(pojo.getPerson().getLastName());
        associatedPerson.setMiddleName(pojo.getPerson().getMiddleName());
        associatedPerson.setNationalInsuranceNumber(pojo.getPerson().getNationalInsuranceNumber());
        associatedPerson.setNationalityCode(pojo.getPerson().getNationalityCode());
        associatedPerson.setNationalityId(pojo.getPerson().getNationalityId());
        associatedPerson.setOccupation(pojo.getPerson().getOccupation());
        associatedPerson.setOccupationCode(pojo.getPerson().getOccupationCode());
        associatedPerson.setRole(pojo.getRole());
        associatedPerson.setSpecificRequirements(pojo.getPerson().getSpecificRequirements());
        associatedPerson.setTitle(pojo.getPerson().getTitle());
        associatedPerson.setDefendant(defendant);
        associatedPerson.setDefendantId(defendant.getId().getId());
        return associatedPerson;
    }

    public List<AssociatedPerson> toJPA(final Hearing hearing, final Defendant defendant, final List<uk.gov.justice.json.schemas.core.AssociatedPerson> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, defendant, pojo)).collect(Collectors.toList());
    }

    uk.gov.justice.json.schemas.core.AssociatedPerson fromJPA(final AssociatedPerson entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.AssociatedPerson.associatedPerson()
                .withPerson(Person.person()
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
                        .build())
                .withRole(entity.getRole())
                .build();
    }

    public List<uk.gov.justice.json.schemas.core.AssociatedPerson> fromJPA(List<AssociatedPerson> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}