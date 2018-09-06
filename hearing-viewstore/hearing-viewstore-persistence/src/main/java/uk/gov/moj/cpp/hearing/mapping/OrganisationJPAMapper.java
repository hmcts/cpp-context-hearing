package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;

@ApplicationScoped
public class OrganisationJPAMapper {

    private AddressJPAMapper addressJPAMapper;
    private ContactNumberJPAMapper contactNumberJPAMapper;

    @Inject
    public OrganisationJPAMapper(final AddressJPAMapper addressJPAMapper, final ContactNumberJPAMapper contactNumberJPAMapper) {
        this.addressJPAMapper = addressJPAMapper;
        this.contactNumberJPAMapper = contactNumberJPAMapper;
    }

    //to keep cdi tester happy
    public OrganisationJPAMapper() {
    }

    public Organisation toJPA(final uk.gov.justice.json.schemas.core.Organisation pojo) {
        if (null == pojo) {
            return null;
        }
        final Organisation organisation = new Organisation();
        organisation.setAddress(addressJPAMapper.toJPA(pojo.getAddress()));
        organisation.setContact(contactNumberJPAMapper.toJPA(pojo.getContact()));
        organisation.setId(pojo.getId());
        organisation.setIncorporationNumber(pojo.getIncorporationNumber());
        organisation.setName(pojo.getName());
        organisation.setRegisteredCharityNumber(pojo.getRegisteredCharityNumber());
        return organisation;
    }

    public uk.gov.justice.json.schemas.core.Organisation fromJPA(final Organisation entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.Organisation.organisation()
                .withAddress(addressJPAMapper.fromJPA(entity.getAddress()))
                .withContact(contactNumberJPAMapper.fromJPA(entity.getContact()))
                .withId(entity.getId())
                .withIncorporationNumber(entity.getIncorporationNumber())
                .withName(entity.getName())
                .withRegisteredCharityNumber(entity.getRegisteredCharityNumber())
                .build();
    }
}