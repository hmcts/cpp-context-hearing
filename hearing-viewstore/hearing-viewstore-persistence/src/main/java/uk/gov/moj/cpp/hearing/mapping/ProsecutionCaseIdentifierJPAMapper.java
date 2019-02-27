package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProsecutionCaseIdentifierJPAMapper {

    public ProsecutionCaseIdentifier toJPA(final uk.gov.justice.core.courts.ProsecutionCaseIdentifier pojo) {
        if (null == pojo) {
            return null;
        }
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = new ProsecutionCaseIdentifier();
        prosecutionCaseIdentifier.setProsecutionAuthorityId(pojo.getProsecutionAuthorityId());
        prosecutionCaseIdentifier.setProsecutionAuthorityCode(pojo.getProsecutionAuthorityCode());
        prosecutionCaseIdentifier.setProsecutionAuthorityReference(pojo.getProsecutionAuthorityReference());
        prosecutionCaseIdentifier.setCaseURN(pojo.getCaseURN());
        return prosecutionCaseIdentifier;
    }

    public uk.gov.justice.core.courts.ProsecutionCaseIdentifier fromJPA(final ProsecutionCaseIdentifier entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN(entity.getCaseURN())
                .withProsecutionAuthorityCode(entity.getProsecutionAuthorityCode())
                .withProsecutionAuthorityId(entity.getProsecutionAuthorityId())
                .withProsecutionAuthorityReference(entity.getProsecutionAuthorityReference())
                .build();
    }
}