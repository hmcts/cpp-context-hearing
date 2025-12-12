package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContactNumberJPAMapper {

    public Contact toJPA(final uk.gov.justice.core.courts.ContactNumber pojo) {
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

    public uk.gov.justice.core.courts.ContactNumber fromJPA(final Contact entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ContactNumber.contactNumber()
                .withFax(entity.getFax())
                .withHome(entity.getHome())
                .withMobile(entity.getMobile())
                .withPrimaryEmail(entity.getPrimaryEmail())
                .withSecondaryEmail(entity.getSecondaryEmail())
                .withWork(entity.getWork())
                .build();
    }
}