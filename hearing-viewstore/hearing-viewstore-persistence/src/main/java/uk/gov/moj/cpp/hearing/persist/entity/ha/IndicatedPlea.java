package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import uk.gov.justice.json.schemas.core.IndicatedPleaValue;
import uk.gov.justice.json.schemas.core.Source;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class IndicatedPlea {

    @Column(name = "indicated_plea_date")
    private LocalDate indicatedPleaDate;

    @Column(name = "indicated_plea_value")
    private IndicatedPleaValue indicatedPleaValue;

    @Column(name = "indicated_plea_source")
    private Source indicatedPleaSource;

    @Embedded
    private AllocationDecision allocationDecision;

    public AllocationDecision getAllocationDecision() {
        return allocationDecision;
    }

    public void setAllocationDecision(AllocationDecision allocationDecision) {
        this.allocationDecision = allocationDecision;
    }

    public LocalDate getIndicatedPleaDate() {
        return indicatedPleaDate;
    }

    public void setIndicatedPleaDate(LocalDate indicatedPleaDate) {
        this.indicatedPleaDate = indicatedPleaDate;
    }

    public IndicatedPleaValue getIndicatedPleaValue() {
        return indicatedPleaValue;
    }

    public void setIndicatedPleaValue(IndicatedPleaValue indicatedPleaValue2) {
        this.indicatedPleaValue = indicatedPleaValue2;
    }

    public Source getIndicatedPleaSource() {
        return indicatedPleaSource;
    }

    public void setIndicatedPleaSource(Source source) {
        this.indicatedPleaSource = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IndicatedPlea that = (IndicatedPlea) o;
        return Objects.equals(indicatedPleaDate, that.indicatedPleaDate) &&
                Objects.equals(indicatedPleaValue, that.indicatedPleaValue) &&
                Objects.equals(indicatedPleaSource, that.indicatedPleaSource) &&
                Objects.equals(allocationDecision, that.allocationDecision);
    }

    @Override
    public int hashCode() {

        return Objects.hash(indicatedPleaDate, indicatedPleaValue, indicatedPleaSource, allocationDecision);
    }
}
