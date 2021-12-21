package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_reporting_restriction")
public class ReportingRestriction {

    @EmbeddedId
    private HearingOffenceReportingRestrictionKey id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "offence_id", insertable = false, updatable = false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable = false, updatable = false, referencedColumnName = "hearing_id")})
    private Offence offence;

    @Column(name = "judicial_result_id")
    private UUID judicialResultId;

    @Column(name = "label")
    private String label;

    @Column(name = "ordered_date")
    private LocalDate orderedDate;

    public ReportingRestriction() {
        //For JPA
    }

    public HearingOffenceReportingRestrictionKey getId() {
        return id;
    }

    public void setId(final HearingOffenceReportingRestrictionKey id) {
        this.id = id;
    }

    public Offence getOffence() {
        return offence;
    }

    public void setOffence(final Offence offence) {
        this.offence = offence;
    }

    public UUID getJudicialResultId() {
        return judicialResultId;
    }

    public void setJudicialResultId(final UUID judicialResultId) {
        this.judicialResultId = judicialResultId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(final LocalDate orderedDate) {
        this.orderedDate = orderedDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((ReportingRestriction) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ReportingRestriction{" +
                "id=" + id +
                ", offence=" + offence +
                ", judicialResultId=" + judicialResultId +
                ", label='" + label + '\'' +
                ", orderedDate=" + orderedDate +
                '}';
    }
}