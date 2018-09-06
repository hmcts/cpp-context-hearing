package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision;

@ApplicationScoped
public class AllocationDecisionJPAMapper {

    public AllocationDecision toJPA(final uk.gov.justice.json.schemas.core.AllocationDecision pojo) {
        if (null == pojo) {
            return null;
        }
        final AllocationDecision allocationDecision = new AllocationDecision();
        allocationDecision.setCourtDecision(pojo.getCourtDecision());
        allocationDecision.setDefendantRepresentation(pojo.getDefendantRepresentation());
        allocationDecision.setIndicationOfSentence(pojo.getIndicationOfSentence());
        allocationDecision.setProsecutionRepresentation(pojo.getProsecutionRepresentation());
        return allocationDecision;
    }

    public uk.gov.justice.json.schemas.core.AllocationDecision fromJPA(final AllocationDecision entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.AllocationDecision.allocationDecision()
                .withCourtDecision(entity.getCourtDecision())
                .withDefendantRepresentation(entity.getDefendantRepresentation())
                .withIndicationOfSentence(entity.getIndicationOfSentence())
                .withProsecutionRepresentation(entity.getProsecutionRepresentation())
                .build();
    }
}