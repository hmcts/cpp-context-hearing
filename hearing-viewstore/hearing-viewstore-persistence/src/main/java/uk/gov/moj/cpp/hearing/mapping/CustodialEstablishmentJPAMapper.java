package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.CustodialEstablishment;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustodialEstablishmentJPAMapper {

    public CustodialEstablishment toJPA(final uk.gov.justice.core.courts.CustodialEstablishment pojo) {
        if (null == pojo) {
            return null;
        }
        final CustodialEstablishment custodialEstablishment = new CustodialEstablishment();
        custodialEstablishment.setCustody(pojo.getCustody());
        custodialEstablishment.setId(pojo.getId());
        custodialEstablishment.setName(pojo.getName());
        return custodialEstablishment;
    }

    public uk.gov.justice.core.courts.CustodialEstablishment fromJPA(final CustodialEstablishment entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.CustodialEstablishment.custodialEstablishment()
                .withCustody(entity.getCustody())
                .withId(entity.getId())
                .withName(entity.getName())
                .build();
    }
}