package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;

import uk.gov.justice.core.courts.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DefenceOrganisationJPAMapper {
    private AddressJPAMapper addressJPAMapper;
    private ContactNumberJPAMapper contactNumberJPAMapper;

    @Inject
    public DefenceOrganisationJPAMapper(final AddressJPAMapper addressJPAMapper, final ContactNumberJPAMapper contactNumberJPAMapper) {
        this.addressJPAMapper = addressJPAMapper;
        this.contactNumberJPAMapper = contactNumberJPAMapper;
    }

    public DefenceOrganisationJPAMapper() {

    }

    public DefenceOrganisation toJPA(final uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisationPojo) {
        if (isNull(defenceOrganisationPojo)) {
            return null;
        }
        final DefenceOrganisation defenceOrganisation = new DefenceOrganisation();
        defenceOrganisation.setLaaContractNumber(defenceOrganisationPojo.getLaaContractNumber());
        defenceOrganisation.setName(defenceOrganisationPojo.getOrganisation().getName());
        defenceOrganisation.setIncorporationNumber(defenceOrganisationPojo.getOrganisation().getIncorporationNumber());
        defenceOrganisation.setRegisteredCharityNumber(defenceOrganisationPojo.getOrganisation().getRegisteredCharityNumber());
        defenceOrganisation.setAddress(addressJPAMapper.toJPA(defenceOrganisationPojo.getOrganisation().getAddress()));
        defenceOrganisation.setContact(contactNumberJPAMapper.toJPA(defenceOrganisationPojo.getOrganisation().getContact()));
        return defenceOrganisation;
    }

    public uk.gov.justice.core.courts.DefenceOrganisation fromJPA(final DefenceOrganisation defenceOrganisation) {
        if (isNull(defenceOrganisation)) {
            return null;
        }
        //TODO set correct values for id & isProbationBreach when rebase with main
        final Organisation organisation = new Organisation(
                addressJPAMapper.fromJPA(defenceOrganisation.getAddress()),
                contactNumberJPAMapper.fromJPA(defenceOrganisation.getContact()), null,
                defenceOrganisation.getIncorporationNumber(), null, defenceOrganisation.getName(),
                defenceOrganisation.getRegisteredCharityNumber());
        return new uk.gov.justice.core.courts.DefenceOrganisation(defenceOrganisation.getLaaContractNumber(),
                organisation);

    }
}
