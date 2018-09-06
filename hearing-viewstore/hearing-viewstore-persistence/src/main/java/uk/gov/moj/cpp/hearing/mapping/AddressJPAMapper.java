package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;

@ApplicationScoped
public class AddressJPAMapper {

    public Address toJPA(final uk.gov.justice.json.schemas.core.Address pojo) {
        if (null == pojo) {
            return null;
        }
        final Address address = new Address();
        address.setAddress1(pojo.getAddress1());
        address.setAddress2(pojo.getAddress2());
        address.setAddress3(pojo.getAddress3());
        address.setAddress4(pojo.getAddress4());
        address.setAddress5(pojo.getAddress5());
        address.setPostCode(pojo.getPostcode());
        return address;
    }

    public uk.gov.justice.json.schemas.core.Address fromJPA(final Address entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.Address.address()
                .withAddress1(entity.getAddress1())
                .withAddress2(entity.getAddress2())
                .withAddress3(entity.getAddress3())
                .withAddress4(entity.getAddress4())
                .withAddress5(entity.getAddress5())
                .withPostcode(entity.getPostCode())
                .build();
    }
}