package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ha_defendant_attendance")
public class DefendantAttendance {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "defendant_id", nullable = false)
    private UUID defendantId;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "is_in_attendance")
    private Boolean isInAttendance;

    public DefendantAttendance() {
        //For JPA
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(HearingSnapshotKey id) {
        this.id = id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Boolean getInAttendance() {
        return isInAttendance;
    }

    public void setInAttendance(Boolean inAttendance) {
        isInAttendance = inAttendance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((DefendantAttendance)o).id);
    }
}
