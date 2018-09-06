package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.json.schemas.core.PleaValue;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class Plea {

    @Column(name = "plea_date")
    private LocalDate pleaDate;

    @Column(name = "plea_value")
    private PleaValue pleaValue;

    @Column(name = "originating_hearing_id")
    private UUID originatingHearingId;

    @Embedded
    private DelegatedPowers delegatedPowers;

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public void setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
    }

    public PleaValue getPleaValue() {
        return pleaValue;
    }

    public void setPleaValue(PleaValue pleaValue) {
        this.pleaValue = pleaValue;
    }

    public UUID getOriginatingHearingId() {
        return originatingHearingId;
    }

    public void setOriginatingHearingId(UUID originatingHearingId) {
        this.originatingHearingId = originatingHearingId;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public void setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Plea plea = (Plea) o;
        return Objects.equals(pleaDate, plea.pleaDate) &&
                Objects.equals(pleaValue, plea.pleaValue) &&
                Objects.equals(originatingHearingId, plea.originatingHearingId) &&
                Objects.equals(delegatedPowers, plea.delegatedPowers);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pleaDate, pleaValue, originatingHearingId, delegatedPowers);
    }
}
