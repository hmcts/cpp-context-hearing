package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class Verdict {

    @Column(name = "verdict_date")
    private LocalDate verdictDate;

    @Column(name = "verdict_originating_hearing_id")
    private UUID originatingHearingId;

    @Embedded
    private VerdictType verdictType;

    @Embedded
    private Jurors jurors;

    @Embedded
    private LesserOrAlternativeOffence lesserOrAlternativeOffence;

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public void setVerdictDate(LocalDate verdictDate) {
        this.verdictDate = verdictDate;
    }

    public VerdictType getVerdictType() {
        return verdictType;
    }

    public void setVerdictType(VerdictType verdictType) {
        this.verdictType = verdictType;
    }

    public Jurors getJurors() {
        return jurors;
    }

    public void setJurors(Jurors jurors) {
        this.jurors = jurors;
    }

    public LesserOrAlternativeOffence getLesserOrAlternativeOffence() {
        return lesserOrAlternativeOffence;
    }

    public void setLesserOrAlternativeOffence(LesserOrAlternativeOffence lesserOrAlternativeOffence) {
        this.lesserOrAlternativeOffence = lesserOrAlternativeOffence;
    }

    public UUID getOriginatingHearingId() {
        return originatingHearingId;
    }

    public void setOriginatingHearingId(UUID originatingHearingId) {
        this.originatingHearingId = originatingHearingId;
    }
}
