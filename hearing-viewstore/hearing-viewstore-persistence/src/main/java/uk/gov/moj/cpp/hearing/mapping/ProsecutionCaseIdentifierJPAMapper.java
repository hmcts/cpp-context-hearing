package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;

@ApplicationScoped
public class ProsecutionCaseIdentifierJPAMapper {

    public ProsecutionCaseIdentifier toJPA(final uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier pojo) {
        if (null == pojo ) {
            return null;
        }
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = new ProsecutionCaseIdentifier();
        prosecutionCaseIdentifier.setProsecutionAuthorityId(pojo.getProsecutionAuthorityId());
        prosecutionCaseIdentifier.setProsecutionAuthorityCode(pojo.getProsecutionAuthorityCode());
        prosecutionCaseIdentifier.setProsecutionAuthorityReference(pojo.getProsecutionAuthorityReference());
        prosecutionCaseIdentifier.setCaseURN(pojo.getCaseURN());
        return prosecutionCaseIdentifier;
    }

    public uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier fromJPA(final ProsecutionCaseIdentifier entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN(entity.getCaseURN())
                .withProsecutionAuthorityCode(entity.getProsecutionAuthorityCode())
                .withProsecutionAuthorityId(entity.getProsecutionAuthorityId())
                .withProsecutionAuthorityReference(entity.getProsecutionAuthorityReference())
                .build();
    }
}