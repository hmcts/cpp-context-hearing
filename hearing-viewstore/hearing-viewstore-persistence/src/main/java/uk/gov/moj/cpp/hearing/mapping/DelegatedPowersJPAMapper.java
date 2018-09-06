package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers;

@ApplicationScoped
public class DelegatedPowersJPAMapper {

    public DelegatedPowers toJPA(uk.gov.justice.json.schemas.core.DelegatedPowers pojo) {
        if (null == pojo) {
            return null;
        }
        final DelegatedPowers delegatedPowers = new DelegatedPowers();
        delegatedPowers.setDelegatedPowersFirstName(pojo.getFirstName());
        delegatedPowers.setDelegatedPowersLastName(pojo.getLastName());
        delegatedPowers.setDelegatedPowersUserId(pojo.getUserId());
        return delegatedPowers;
    }


    public uk.gov.justice.json.schemas.core.DelegatedPowers fromJPA(final DelegatedPowers entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.DelegatedPowers.delegatedPowers()
                .withUserId(entity.getDelegatedPowersUserId())
                .withFirstName(entity.getDelegatedPowersFirstName())
                .withLastName(entity.getDelegatedPowersLastName())
                .build();
    }
}