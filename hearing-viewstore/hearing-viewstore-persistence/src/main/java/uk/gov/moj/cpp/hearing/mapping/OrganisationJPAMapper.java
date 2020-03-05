package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    public Organisation toJPA(final uk.gov.justice.core.courts.Organisation pojo) {
        if (null == pojo) {
            return null;
        }
        final Organisation organisation = new Organisation();
        organisation.setAddress(addressJPAMapper.toJPA(pojo.getAddress()));
        organisation.setContact(contactNumberJPAMapper.toJPA(pojo.getContact()));
        organisation.setId(UUID.randomUUID());
        organisation.setIncorporationNumber(pojo.getIncorporationNumber());
        organisation.setName(pojo.getName());
        organisation.setRegisteredCharityNumber(pojo.getRegisteredCharityNumber());
        return organisation;
    }

    public uk.gov.justice.core.courts.Organisation fromJPA(final Organisation entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Organisation.organisation()
                .withAddress(addressJPAMapper.fromJPA(entity.getAddress()))
                .withContact(contactNumberJPAMapper.fromJPA(entity.getContact()))
                .withIncorporationNumber(entity.getIncorporationNumber())
                .withName(entity.getName())
                .withRegisteredCharityNumber(entity.getRegisteredCharityNumber())
                .build();
    }
}