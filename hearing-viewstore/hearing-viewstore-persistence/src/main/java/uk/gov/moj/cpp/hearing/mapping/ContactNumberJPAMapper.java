package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;

@ApplicationScoped
public class ContactNumberJPAMapper {

    public Contact toJPA(final uk.gov.justice.json.schemas.core.ContactNumber pojo) {
        if (null == pojo) {
            return null;
        }
        final Contact contact = new Contact();
        contact.setFax(pojo.getFax());
        contact.setHome(pojo.getHome());
        contact.setMobile(pojo.getMobile());
        contact.setPrimaryEmail(pojo.getPrimaryEmail());
        contact.setSecondaryEmail(pojo.getSecondaryEmail());
        contact.setWork(pojo.getWork());
        return contact;
    }

    public uk.gov.justice.json.schemas.core.ContactNumber fromJPA(final Contact entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.ContactNumber.contactNumber()
                .withFax(entity.getFax())
                .withHome(entity.getHome())
                .withMobile(entity.getMobile())
                .withPrimaryEmail(entity.getPrimaryEmail())
                .withSecondaryEmail(entity.getSecondaryEmail())
                .withWork(entity.getWork())
                .build();
    }
}