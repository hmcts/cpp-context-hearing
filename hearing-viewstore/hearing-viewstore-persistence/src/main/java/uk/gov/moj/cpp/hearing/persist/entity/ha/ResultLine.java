package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.Level;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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



public class ResultLine {

    private UUID id;


    private Target target;

    private String shortCode;

    private ZonedDateTime amendmentDate;

    private UUID amendmentReasonId;

    private String amendmentReason;

    private String childResultLineIds;

    private String parentResultLineIds;

    private UUID offenceId;

    private UUID applicationId;

    private UUID caseId;

    private UUID defendantId;

    private UUID masterDefendantId;

    private Boolean shadowListed;

    @Embedded
    private DelegatedPowers delegatedPowers;

    private Boolean isComplete;

    private Boolean isModified;


    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private Level level;

    private LocalDate orderedDate;

    private Set<Prompt> prompts;

    private UUID resultDefinitionId;

    private String resultLabel;

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

    public String getShortCode() {
        return shortCode;
    }

    public ResultLine setShortCode(final String shortCode) {
        this.shortCode = shortCode;
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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public ResultLine setDeleted(final Boolean deleted) {
        isDeleted = deleted;
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

    public ZonedDateTime getAmendmentDate() {
        return amendmentDate;
    }

    public UUID getAmendmentReasonId() {
        return amendmentReasonId;
    }

    public String getAmendmentReason() {
        return amendmentReason;
    }


    public String getChildResultLineIds() {
        return childResultLineIds;
    }

    public String getParentResultLineIds() {
        return parentResultLineIds;
    }

    public ResultLine setAmendmentDate(final ZonedDateTime amendmentDate) {
        this.amendmentDate = amendmentDate;
        return this;
    }

    public ResultLine setAmendmentReasonId(final UUID amendmentReasonId) {
        this.amendmentReasonId = amendmentReasonId;
        return this;
    }

    public ResultLine setAmendmentReason(final String amendmentReason) {
        this.amendmentReason = amendmentReason;
        return this;
    }


    public ResultLine setChildResultLineIds(final String childResultLineIds) {
        this.childResultLineIds = childResultLineIds;
        return this;
    }

    public ResultLine setParentResultLineIds(final String parentResultLineIds) {
        this.parentResultLineIds = parentResultLineIds;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public ResultLine setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ResultLine setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public ResultLine setCaseId(final UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public ResultLine setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public ResultLine setMasterDefendantId(final UUID masterDefendantId) {
        this.masterDefendantId = masterDefendantId;
        return this;
    }

    public Boolean getShadowListed() {
        return shadowListed;
    }

    public ResultLine setShadowListed(final Boolean shadowListed) {
        this.shadowListed = shadowListed;
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