package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class Plea {

    @Column(name = "plea_date")
    private LocalDate pleaDate;

    @Column(name = "plea_value")
    private String pleaValue;

    @Column(name = "originating_hearing_id")
    private UUID originatingHearingId;

    @Embedded
    private DelegatedPowers delegatedPowers;

    @Embedded
    private LesserOrAlternativeOffenceForPlea lesserOrAlternativeOffence;

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public void setPleaDate(final LocalDate pleaDate) {
        this.pleaDate = pleaDate;
    }

    public String getPleaValue() {
        return pleaValue;
    }

    public void setPleaValue(final String pleaValue) {
        this.pleaValue = pleaValue;
    }

    public UUID getOriginatingHearingId() {
        return originatingHearingId;
    }

    public void setOriginatingHearingId(final UUID originatingHearingId) {
        this.originatingHearingId = originatingHearingId;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public void setDelegatedPowers(final DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
    }

    public LesserOrAlternativeOffenceForPlea getLesserOrAlternativeOffence() {
        return lesserOrAlternativeOffence;
    }

    public void setLesserOrAlternativeOffence(final LesserOrAlternativeOffenceForPlea lesserOrAlternativeOffence) {
        this.lesserOrAlternativeOffence = lesserOrAlternativeOffence;
    }
}
