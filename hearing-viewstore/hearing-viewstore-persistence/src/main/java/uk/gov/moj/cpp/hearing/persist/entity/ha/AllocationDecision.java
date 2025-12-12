package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class AllocationDecision {

    @Column(name = "ad_originating_hearing_id")
    private UUID originatingHearingId;

    @Column(name = "mot_reason_id")
    private UUID motReasonId;

    @Column(name = "mot_reason_description")
    private String motReasonDescription;

    @Column(name = "mot_reason_code")
    private String motReasonCode;

    @Column(name = "allocation_decision_date")
    private LocalDate allocationDecisionDate;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Embedded
    private CourtIndicatedSentence courtIndicatedSentence;

    public UUID getOriginatingHearingId() {
        return originatingHearingId;
    }

    public void setOriginatingHearingId(final UUID originatingHearingId) {
        this.originatingHearingId = originatingHearingId;
    }

    public UUID getMotReasonId() {
        return motReasonId;
    }

    public void setMotReasonId(final UUID motReasonId) {
        this.motReasonId = motReasonId;
    }

    public String getMotReasonDescription() {
        return motReasonDescription;
    }

    public void setMotReasonDescription(final String motReasonDescription) {
        this.motReasonDescription = motReasonDescription;
    }

    public String getMotReasonCode() {
        return motReasonCode;
    }

    public void setMotReasonCode(final String motReasonCode) {
        this.motReasonCode = motReasonCode;
    }

    public LocalDate getAllocationDecisionDate() {
        return allocationDecisionDate;
    }

    public void setAllocationDecisionDate(final LocalDate allocationDecisionDate) {
        this.allocationDecisionDate = allocationDecisionDate;
    }


    public CourtIndicatedSentence getCourtIndicatedSentence() {
        return courtIndicatedSentence;
    }

    public void setCourtIndicatedSentence(final CourtIndicatedSentence courtIndicatedSentence) {
        this.courtIndicatedSentence = courtIndicatedSentence;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
