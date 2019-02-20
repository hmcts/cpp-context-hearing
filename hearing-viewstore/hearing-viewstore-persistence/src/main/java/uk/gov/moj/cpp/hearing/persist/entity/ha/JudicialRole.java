package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_judicial_role")
public class JudicialRole {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "judicial_id")
    private UUID judicialId;

    @Column(name = "title")
    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "judicial_role_type")
    private String judicialRoleType;

    @Column(name = "is_deputy")
    private Boolean isDeputy;

    @Column(name = "is_bench_chairman")
    private Boolean isBenchChairman;

    public JudicialRole() {
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

    public UUID getJudicialId() {
        return judicialId;
    }

    public void setJudicialId(UUID judicialId) {
        this.judicialId = judicialId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getDeputy() {
        return isDeputy;
    }

    public void setDeputy(Boolean deputy) {
        isDeputy = deputy;
    }

    public Boolean getBenchChairman() {
        return isBenchChairman;
    }

    public void setBenchChairman(Boolean benchChairman) {
        isBenchChairman = benchChairman;
    }

    public String getJudicialRoleType() {
        return judicialRoleType;
    }

    public void setJudicialRoleType(String judicialRoleType) {
        this.judicialRoleType = judicialRoleType;
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
        return Objects.equals(this.id, ((JudicialRole) o).id);
    }
}