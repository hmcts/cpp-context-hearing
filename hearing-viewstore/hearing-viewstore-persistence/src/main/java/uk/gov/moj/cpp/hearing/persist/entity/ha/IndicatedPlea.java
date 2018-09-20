package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.json.schemas.core.IndicatedPleaValue;
import uk.gov.justice.json.schemas.core.Source;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
public class IndicatedPlea {

    @Column(name = "indicated_plea_date")
    private LocalDate indicatedPleaDate;

    @Column(name = "indicated_plea_value")
    @Enumerated(EnumType.STRING)
    private IndicatedPleaValue indicatedPleaValue;

    @Column(name = "indicated_plea_source")
    @Enumerated(EnumType.STRING)
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
}
