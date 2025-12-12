package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AllocationDecisionJPAMapper {

    private CourtIndicatedSentenceJPAMapper courtIndicatedSentenceJPAMapper;

    //To keep cditester happy
    public AllocationDecisionJPAMapper() { }

    @Inject
    public AllocationDecisionJPAMapper(final CourtIndicatedSentenceJPAMapper courtIndicatedSentenceJPAMapper) {
        this.courtIndicatedSentenceJPAMapper = courtIndicatedSentenceJPAMapper;
    }

     public AllocationDecision toJPA(final uk.gov.justice.core.courts.AllocationDecision pojo) {
        if (null == pojo) {
            return null;
        }
        final AllocationDecision allocationDecision = new AllocationDecision();
        allocationDecision.setOriginatingHearingId(pojo.getOriginatingHearingId());
        allocationDecision.setMotReasonId(pojo.getMotReasonId());
        allocationDecision.setMotReasonDescription(pojo.getMotReasonDescription());
        allocationDecision.setMotReasonCode(pojo.getMotReasonCode());
        allocationDecision.setAllocationDecisionDate(pojo.getAllocationDecisionDate());
        allocationDecision.setSequenceNumber(pojo.getSequenceNumber());
        allocationDecision.setCourtIndicatedSentence(pojo.getCourtIndicatedSentence()!=null?courtIndicatedSentenceJPAMapper.toJPA(pojo.getCourtIndicatedSentence()):null);
        return allocationDecision;
    }

    public uk.gov.justice.core.courts.AllocationDecision fromJPA(final UUID offenceId, final AllocationDecision entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.AllocationDecision.allocationDecision()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(entity.getOriginatingHearingId())
                .withMotReasonId(entity.getMotReasonId())
                .withMotReasonDescription(entity.getMotReasonDescription())
                .withMotReasonCode(entity.getMotReasonCode())
                .withAllocationDecisionDate(entity.getAllocationDecisionDate())
                .withSequenceNumber(entity.getSequenceNumber())
                .withCourtIndicatedSentence(courtIndicatedSentenceJPAMapper.fromJPA(entity.getCourtIndicatedSentence()))
                .build();
    }
}