package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ha_target")
public class Target {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "hearing_id")
    private Hearing hearing;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "draft_result")
    private String draftResult;

    @Column(name = "offence_id")
    private UUID offenceId;

    @Column(name = "application_id")
    private UUID applicationId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "target", orphanRemoval = true)
    private Set<ResultLine> resultLines = new HashSet<>();

    public Target() {
        //For JPA
    }

    public static Target target() {
        return new Target();
    }

    public UUID getId() {
        return id;
    }

    public Target setId(UUID id) {
        this.id = id;
        return this;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public Target setHearing(final Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Target setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public Target setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Target setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public Target setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public Set<ResultLine> getResultLines() {
        return resultLines;
    }

    public Target setResultLines(Set<ResultLine> resultLines) {

        this.resultLines = resultLines;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Target target = (Target) o;
        return Objects.equals(id, target.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}