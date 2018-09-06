package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ha_target")
public class Target {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "hearing_id")
    private Hearing hearing;

    //TODO determine whether this reference should be resolved
    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "draft_result")
    private String draftResult;

    //TODO determine whether this reference should be resolved
    @Column(name = "offence_id")
    private UUID offenceId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "target", orphanRemoval = true)
    private List<ResultLine> resultLines = new ArrayList<>();

    public Target() {
        //For JPA
    }

    public Target(Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.defendantId = builder.defendantId;
        this.offenceId = builder.offenceId;
        this.draftResult = builder.draftResult;
    }

    public UUID getId() {
        return id;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public Target setId(UUID id) {
        this.id = id;
        return this;
    }

    public Target setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Target setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public Target setDraftResult(String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Target setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public Target setResultLines(List<ResultLine> resultLines) {
        this.resultLines = resultLines;
        return this;
    }

    public static Target target() {
        return new Target();
    }

    //TODO remove this builder
    public static class Builder {
        private UUID id;
        private Hearing hearing;
        private UUID defendantId;
        private String draftResult;
        private UUID offenceId;

        protected Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withHearing(Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withOffenceId(UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withDraftResult(String draftResult) {
            this.draftResult = draftResult;
            return this;
        }

        public Target build() {
            return new Target(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }
}