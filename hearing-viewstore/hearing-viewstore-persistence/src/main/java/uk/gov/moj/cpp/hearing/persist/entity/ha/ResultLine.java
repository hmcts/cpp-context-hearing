package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.Level;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    private Set<Prompt> prompts;

    @Column(name = "result_definition_id")
    private UUID resultDefinitionId;

    @Column(name = "result_label")
    private String resultLabel;

    @Column(name = "last_shared_date_time")
    private LocalDate sharedDate;

    public ResultLine() {
        //For JPA
    }

    public static ResultLine resultLine() {
        return new ResultLine();
    }

    public UUID getId() {
        return id;
    }

    public ResultLine setId(final UUID id) {
        this.id = id;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public ResultLine setTarget(final Target target) {
        this.target = target;
        return this;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public ResultLine setDelegatedPowers(final DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
        return this;
    }

    public Boolean getComplete() {
        return isComplete;
    }

    public ResultLine setComplete(final Boolean complete) {
        isComplete = complete;
        return this;
    }

    public Boolean getModified() {
        return isModified;
    }

    public ResultLine setModified(final Boolean modified) {
        isModified = modified;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    public ResultLine setLevel(final Level level) {
        this.level = level;
        return this;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public ResultLine setOrderedDate(final LocalDate orderedDate) {
        this.orderedDate = orderedDate;
        return this;
    }

    public Set<Prompt> getPrompts() {
        return prompts;
    }

    public ResultLine setPrompts(Set<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public ResultLine setResultDefinitionId(final UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
        return this;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public ResultLine setResultLabel(final String resultLabel) {
        this.resultLabel = resultLabel;
        return this;
    }

    public LocalDate getSharedDate() {
        return sharedDate;
    }

    public ResultLine setSharedDate(final LocalDate sharedDate) {
        this.sharedDate = sharedDate;
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
        ResultLine that = (ResultLine) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}