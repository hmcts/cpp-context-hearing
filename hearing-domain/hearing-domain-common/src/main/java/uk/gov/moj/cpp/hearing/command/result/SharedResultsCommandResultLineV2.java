package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.DelegatedPowers;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public class SharedResultsCommandResultLineV2 implements Serializable {

    private static final long serialVersionUID = 2942565459149667125L;

    private String shortCode;
    private DelegatedPowers delegatedPowers;
    private LocalDate orderedDate;
    private LocalDate sharedDate;
    private UUID resultLineId;
    private UUID offenceId;
    private UUID defendantId;
    private UUID masterDefendantId;
    private UUID resultDefinitionId;
    @SuppressWarnings({"squid:S1948"})
    private List<SharedResultsCommandPrompt> prompts;
    private String resultLabel;
    private String level;
    private boolean isModified;
    private boolean isComplete;
    private UUID applicationId;
    private UUID caseId;
    private ZonedDateTime amendmentDate;
    private UUID amendmentReasonId;
    private String amendmentReason;
    private DelegatedPowers fourEyesApproval;
    private LocalDate approvedDate;
    private boolean isDeleted;
    private List<UUID> childResultLineIds;
    private List<UUID> parentResultLineIds;
    private boolean shadowListed;
    private String draftResult;
    private String amendmentsLog;

    @SuppressWarnings({"squid:S2384", "squid:S1067"})
    @JsonCreator
    public SharedResultsCommandResultLineV2(
            @JsonProperty("shortCode") final String shortCode,
            @JsonProperty("delegatedPowers") final DelegatedPowers delegatedPowers,
            @JsonProperty("orderedDate") final LocalDate orderedDate,
            @JsonProperty("sharedDate") final LocalDate sharedDate,
            @JsonProperty("resultLineId") final UUID resultLineId,
            @JsonProperty("offenceId") final UUID offenceId,
            @JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("masterDefendantId") final UUID masterDefendantId,
            @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
            @JsonProperty("prompts") final List<SharedResultsCommandPrompt> prompts,
            @JsonProperty("resultLabel") final String resultLabel,
            @JsonProperty("level") final String level,
            @JsonProperty("isModified") final boolean isModified,
            @JsonProperty("isComplete") final boolean isComplete,
            @JsonProperty("applicationId") final UUID applicationId,
            @JsonProperty("caseId") final UUID caseId,
            @JsonProperty("amendmentReasonId") final UUID amendmentReasonId,
            @JsonProperty("amendmentReason") final String amendmentReason,
            @JsonProperty("amendmentDate") final ZonedDateTime amendmentDate,
            @JsonProperty("fourEyesApproval") final DelegatedPowers fourEyesApproval,
            @JsonProperty("approvedDate") final LocalDate approvedDate,
            @JsonProperty("isDeleted") final boolean isDeleted,
            @JsonProperty("childResultLineIds") final List<UUID> childResultLineIds,
            @JsonProperty("parentResultLineIds") final List<UUID> parentResultLineIds,
            @JsonProperty("shadowListed") final boolean shadowListed,
            @JsonProperty("draftResult") final String draftResult,
            @JsonProperty("amendmentsLog") final String amendmentsLog
    ) {
        this.shortCode = shortCode;
        this.delegatedPowers = delegatedPowers;
        this.orderedDate = orderedDate;
        this.sharedDate = sharedDate;
        this.resultLineId = resultLineId;
        this.offenceId = offenceId;
        this.defendantId = defendantId;
        this.masterDefendantId = masterDefendantId;
        this.resultDefinitionId = resultDefinitionId;
        this.resultLabel = resultLabel;
        this.prompts = prompts;
        this.level = level;
        this.isModified = isModified;
        this.isComplete = isComplete;
        this.applicationId = applicationId;
        this.caseId = caseId;
        this.amendmentReasonId = amendmentReasonId;
        this.amendmentReason = amendmentReason;
        this.amendmentDate = amendmentDate;
        this.fourEyesApproval = fourEyesApproval;
        this.approvedDate = approvedDate;
        this.isDeleted = isDeleted;
        this.childResultLineIds = childResultLineIds;
        this.parentResultLineIds = parentResultLineIds;
        this.shadowListed = shadowListed;
        this.draftResult = draftResult;
        this.amendmentsLog = amendmentsLog;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(final String shortCode) {
        this.shortCode = shortCode;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public void setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(LocalDate orderedDate) {
        this.orderedDate = orderedDate;
    }

    public LocalDate getSharedDate() {
        return sharedDate;
    }

    public void setSharedDate(LocalDate sharedDate) {
        this.sharedDate = sharedDate;
    }

    public UUID getResultLineId() {
        return resultLineId;
    }

    public void setResultLineId(UUID resultLineId) {
        this.resultLineId = resultLineId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public void setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public void setResultDefinitionId(UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    @SuppressWarnings({"squid:S2384"})
    public List<SharedResultsCommandPrompt> getPrompts() {
        return prompts;
    }

    @SuppressWarnings({"squid:S2384"})
    public void setPrompts(List<SharedResultsCommandPrompt> prompts) {
        this.prompts = prompts;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean getIsModified() {
        return isModified;
    }

    public void setIsModified(boolean modified) {
        isModified = modified;
    }

    public boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean complete) {
        isComplete = complete;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
    }

    public ZonedDateTime getAmendmentDate() {
        return amendmentDate;
    }

    public void setAmendmentDate(final ZonedDateTime amendmentDate) {
        this.amendmentDate = amendmentDate;
    }

    public UUID getAmendmentReasonId() {
        return amendmentReasonId;
    }

    public void setAmendmentReasonId(final UUID amendmentReasonId) {
        this.amendmentReasonId = amendmentReasonId;
    }

    public String getAmendmentReason() {
        return this.amendmentReason;
    }

    public void setAmendmentReason(final String amendmentReason) {
        this.amendmentReason = amendmentReason;
    }

    public DelegatedPowers getFourEyesApproval() {
        return fourEyesApproval;
    }

    public void setFourEyesApproval(final DelegatedPowers fourEyesApproval) {
        this.fourEyesApproval = fourEyesApproval;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(final LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(final boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<UUID> getChildResultLineIds() {
        return childResultLineIds;
    }

    public void setChildResultLineIds(final List<UUID> childResultLineIds) {
        this.childResultLineIds = childResultLineIds;
    }

    public List<UUID> getParentResultLineIds() {
        return parentResultLineIds;
    }

    public void setParentResultLineIds(final List<UUID> parentResultLineIds) {
        this.parentResultLineIds = parentResultLineIds;
    }


    public boolean isShadowListed() {
        return shadowListed;
    }

    public void setShadowListed(final boolean shadowListed) {
        this.shadowListed = shadowListed;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public void setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public void setMasterDefendantId(final UUID masterDefendantId) {
        this.masterDefendantId = masterDefendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(final UUID caseId) {
        this.caseId = caseId;
    }

    public String getAmendmentsLog() {
        return amendmentsLog;
    }

    public void setAmendmentsLog(String amendmentsLog) {
        this.amendmentsLog = amendmentsLog;
    }

    public static Builder sharedResultsCommandResultLine(){
        return new Builder();
    }

    public static final class Builder {
        private String shortCode;
        private DelegatedPowers delegatedPowers;
        private LocalDate orderedDate;
        private LocalDate sharedDate;
        private UUID resultLineId;
        private UUID offenceId;
        private UUID defendantId;
        private UUID masterDefendantId;
        private UUID resultDefinitionId;
        private List<SharedResultsCommandPrompt> prompts;
        private String resultLabel;
        private String level;
        private boolean isModified;
        private boolean isComplete;
        private UUID applicationId;
        private UUID caseId;
        private ZonedDateTime amendmentDate;
        private UUID amendmentReasonId;
        private String amendmentReason;
        private DelegatedPowers fourEyesApproval;
        private LocalDate approvedDate;
        private boolean isDeleted;
        private List<UUID> childResultLineIds;
        private List<UUID> parentResultLineIds;
        private boolean shadowListed;
        private String draftResult;
        private String amendmentsLog;

        public Builder withShortCode(final String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public Builder withDelegatedPowers(final DelegatedPowers delegatedPowers) {
            this.delegatedPowers = delegatedPowers;
            return this;
        }

        public Builder withOrderedDate(final LocalDate orderedDate) {
            this.orderedDate = orderedDate;
            return this;
        }

        public Builder withSharedDate(final LocalDate sharedDate) {
            this.sharedDate = sharedDate;
            return this;
        }

        public Builder withResultLineId(final UUID resultLineId) {
            this.resultLineId = resultLineId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withMasterDefendantId(final UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public Builder withResultDefinitionId(final UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public Builder withPrompts(final List<SharedResultsCommandPrompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public Builder withResultLabel(final String resultLabel) {
            this.resultLabel = resultLabel;
            return this;
        }

        public Builder withLevel(final String level) {
            this.level = level;
            return this;
        }

        public Builder withIsModified(final boolean isModified) {
            this.isModified = isModified;
            return this;
        }

        public Builder withIsComplete(final boolean isComplete) {
            this.isComplete = isComplete;
            return this;
        }

        public Builder withApplicationIds(final UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }
        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }
        public Builder withAmendmentDate(final ZonedDateTime amendmentDate) {
            this.amendmentDate = amendmentDate;
            return this;
        }

        public Builder withAmendmentReasonId(final UUID amendmentReasonId) {
            this.amendmentReasonId = amendmentReasonId;
            return this;
        }

        public Builder withAmendmentReason(final String amendmentReason) {
            this.amendmentReason = amendmentReason;
            return this;
        }

        public Builder withFourEyesApproval(final DelegatedPowers fourEyesApproval) {
            this.fourEyesApproval = fourEyesApproval;
            return this;
        }

        public Builder withApprovedDate(final LocalDate approvedDate) {
            this.approvedDate = approvedDate;
            return this;
        }

        public Builder withIsDeleted(final boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder withChildResultLineIds(final List<UUID> childResultLineIds) {
            this.childResultLineIds = childResultLineIds;
            return this;
        }

        public Builder withParentResultLineIds(final List<UUID> parentResultLineIds) {
            this.parentResultLineIds = parentResultLineIds;
            return this;
        }

        public Builder withShadowListed(final boolean shadowListed) {
            this.shadowListed = shadowListed;
            return this;
        }

        public Builder withDraftResult(final String draftResult) {
            this.draftResult = draftResult;
            return this;
        }

        public Builder withamendmentsLog(final String amendmentsLog) {
            this.amendmentsLog = amendmentsLog;
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
            final Builder builder = (Builder) o;
            return isModified == builder.isModified &&
                    isComplete == builder.isComplete &&
                    isDeleted == builder.isDeleted &&
                    shadowListed == builder.shadowListed &&
                    Objects.equals(shortCode, builder.shortCode) &&
                    Objects.equals(delegatedPowers, builder.delegatedPowers) &&
                    Objects.equals(orderedDate, builder.orderedDate) &&
                    Objects.equals(sharedDate, builder.sharedDate) &&
                    Objects.equals(resultLineId, builder.resultLineId) &&
                    Objects.equals(offenceId, builder.offenceId) &&
                    Objects.equals(defendantId, builder.defendantId) &&
                    Objects.equals(masterDefendantId, builder.masterDefendantId) &&
                    Objects.equals(resultDefinitionId, builder.resultDefinitionId) &&
                    Objects.equals(prompts, builder.prompts) &&
                    Objects.equals(resultLabel, builder.resultLabel) &&
                    Objects.equals(level, builder.level) &&
                    Objects.equals(applicationId, builder.applicationId) &&
                    Objects.equals(caseId, builder.caseId) &&
                    Objects.equals(amendmentDate, builder.amendmentDate) &&
                    Objects.equals(amendmentReasonId, builder.amendmentReasonId) &&
                    Objects.equals(amendmentReason, builder.amendmentReason) &&
                    Objects.equals(fourEyesApproval, builder.fourEyesApproval) &&
                    Objects.equals(approvedDate, builder.approvedDate) &&
                    Objects.equals(childResultLineIds, builder.childResultLineIds) &&
                    Objects.equals(parentResultLineIds, builder.parentResultLineIds) &&
                    Objects.equals(draftResult, builder.draftResult) &&
                    Objects.equals(amendmentsLog, builder.amendmentsLog);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shortCode, delegatedPowers, orderedDate, sharedDate, resultLineId, offenceId, defendantId, masterDefendantId, resultDefinitionId, prompts, resultLabel, level, isModified, isComplete, applicationId, caseId, amendmentDate, amendmentReasonId, amendmentReason, fourEyesApproval, approvedDate, isDeleted, childResultLineIds, parentResultLineIds, shadowListed, draftResult, amendmentsLog);
        }

        public SharedResultsCommandResultLineV2 build() {
            return new SharedResultsCommandResultLineV2(
                    shortCode,
                    delegatedPowers,
                    orderedDate,
                    sharedDate,
                    resultLineId,
                    offenceId,
                    defendantId,
                    masterDefendantId,
                    resultDefinitionId,
                    prompts,
                    resultLabel,
                    level,
                    isModified,
                    isComplete,
                    applicationId,
                    caseId,
                    amendmentReasonId,
                    amendmentReason,
                    amendmentDate,
                    fourEyesApproval,
                    approvedDate,
                    isDeleted,
                    childResultLineIds,
                    parentResultLineIds,
                    shadowListed,

                    draftResult,
                    amendmentsLog
            );
        }
    }

   

}
