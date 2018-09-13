package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.json.schemas.core.Level;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ha_result_line")
public class ResultLine {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "target_id")
    private Target target;

    @Embedded
    private DelegatedPowers delegatedPowers;

    @Column(name = "is_complete")
    private Boolean isComplete;

    @Column(name = "is_modified")
    private Boolean isModified;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private Level level;

    @Column(name = "ordered_date")
    private LocalDate orderedDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "resultLine", orphanRemoval = true)
    private List<Prompt> prompts;

    @Column(name = "result_definition_id")
    private UUID resultDefinitionId;

    @Column(name = "result_label")
    private String resultLabel;

    @Column(name = "last_shared_date_time")
    private LocalDate sharedDate;

    public ResultLine() {
        //For JPA
    }

    public ResultLine(Builder builder) {
        this.id = builder.id;
        this.target = builder.target;
        this.delegatedPowers = builder.delegatedPowers;
        this.isComplete = builder.isComplete;
        this.isModified = builder.isModified;
        this.level = builder.level;
        this.orderedDate = builder.orderedDate;
        this.prompts = builder.prompts;
        this.resultDefinitionId = builder.resultDefinitionId;
        this.resultLabel = builder.resultLabel;
        this.sharedDate = builder.sharedDate;

    }

    public UUID getId() {
        return id;
    }

    public ResultLine setId(UUID id) {
        this.id = id;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public ResultLine setTarget(Target target) {
        this.target = target;
        return this;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public ResultLine setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
        return this;
    }

    public Boolean getComplete() {
        return isComplete;
    }

    public ResultLine setComplete(Boolean complete) {
        isComplete = complete;
        return this;
    }

    public Boolean getModified() {
        return isModified;
    }

    public ResultLine setModified(Boolean modified) {
        isModified = modified;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    public ResultLine setLevel(Level level) {
        this.level = level;
        return this;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public ResultLine setOrderedDate(LocalDate orderedDate) {
        this.orderedDate = orderedDate;
        return this;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public ResultLine setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public ResultLine setResultDefinitionId(UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
        return this;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public ResultLine setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
        return this;
    }

    public LocalDate getSharedDate() {
        return sharedDate;
    }

    public ResultLine setSharedDate(LocalDate sharedDate) {
        this.sharedDate = sharedDate;
        return this;
    }

    public static ResultLine resultLine() {
        return new ResultLine();
    }

    //TODO delete this Builder
    public static class Builder {
        private UUID id;
        private Target target;
        private DelegatedPowers delegatedPowers;
        private Boolean isComplete;
        private Boolean isModified;
        private Level level;
        private LocalDate orderedDate;
        private List<Prompt> prompts;
        private UUID resultDefinitionId;
        private String resultLabel;
        private LocalDate sharedDate;

        protected Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withTarget(Target target) {
            this.target = target;
            return this;
        }

        public Builder withDelegatedPowers(DelegatedPowers delegatedPowers) {
            this.delegatedPowers = delegatedPowers;
            return this;
        }

        public Builder withIsComplete(Boolean isComplete) {
            this.isComplete = isComplete;
            return this;
        }

        public Builder withisModified(Boolean isModified) {
            this.isModified = isModified;
            return this;
        }

        public Builder withLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder withOrderedDate(LocalDate orderedDate) {
            this.orderedDate = orderedDate;
            return this;
        }

        public Builder withPrompts(List<Prompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public Builder withResultDefinitionId(UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public Builder withResultLabel(String resultLabel) {
            this.resultLabel = resultLabel;
            return this;
        }

        public Builder withSharedDate(LocalDate sharedDate) {
            this.sharedDate = sharedDate;
            return this;
        }

        public ResultLine build() {
            return new ResultLine(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}