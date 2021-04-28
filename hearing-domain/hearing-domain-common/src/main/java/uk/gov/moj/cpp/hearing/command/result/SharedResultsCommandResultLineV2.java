package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.DelegatedPowers;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384"})
public class SharedResultsCommandResultLineV2 implements Serializable {
    private DelegatedPowers delegatedPowers;
    private LocalDate orderedDate;
    private LocalDate sharedDate;
    private UUID resultLineId;
    private UUID targetId;
    private UUID offenceId;
    private UUID defendantId;
    private UUID resultDefinitionId;
    @SuppressWarnings({"squid:S1948"})
    private List<SharedResultsCommandPrompt> prompts;
    private String resultLabel;
    private String level;
    private boolean isModified;
    private boolean isComplete;
    private UUID applicationId;
    private ZonedDateTime amendmentDate;
    private UUID amendmentReasonId;
    private String amendmentReason;
    private DelegatedPowers fourEyesApproval;
    private LocalDate approvedDate;
    private boolean isDeleted;
    private List<UUID> childResultLineIds;
    private List<UUID> parentResultLineIds;

    @SuppressWarnings({"squid:S2384"})
    @JsonCreator
    public SharedResultsCommandResultLineV2(
            @JsonProperty("delegatedPowers") final DelegatedPowers delegatedPowers,
            @JsonProperty("orderedDate") final LocalDate orderedDate,
            @JsonProperty("sharedDate") final LocalDate sharedDate,
            @JsonProperty("resultLineId") final UUID resultLineId,
            @JsonProperty("targetId") final UUID targetId,
            @JsonProperty("offenceId") final UUID offenceId,
            @JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
            @JsonProperty("prompts") final List<SharedResultsCommandPrompt> prompts,
            @JsonProperty("resultLabel") final String resultLabel,
            @JsonProperty("level") final String level,
            @JsonProperty("isModified") final boolean isModified,
            @JsonProperty("isComplete") final boolean isComplete,
            @JsonProperty("applicationId") final UUID applicationId,
            @JsonProperty("amendmentReasonId") final UUID amendmentReasonId,
            @JsonProperty("amendmentReason") final String amendmentReason,
            @JsonProperty("amendmentDate") final ZonedDateTime amendmentDate,
            @JsonProperty("fourEyesApproval") final DelegatedPowers fourEyesApproval,
            @JsonProperty("approvedDate") final LocalDate approvedDate,
            @JsonProperty("isDeleted") final boolean isDeleted,
            @JsonProperty("childResultLineIds") final List<UUID> childResultLineIds,
            @JsonProperty("parentResultLineIds") final List<UUID> parentResultLineIds
    ) {
        this.delegatedPowers = delegatedPowers;
        this.orderedDate = orderedDate;
        this.sharedDate = sharedDate;
        this.resultLineId = resultLineId;
        this.targetId = targetId;
        this.offenceId = offenceId;
        this.defendantId = defendantId;
        this.resultDefinitionId = resultDefinitionId;
        this.resultLabel = resultLabel;
        this.prompts = prompts;
        this.level = level;
        this.isModified = isModified;
        this.isComplete = isComplete;
        this.applicationId = applicationId;
        this.amendmentReasonId = amendmentReasonId;
        this.amendmentReason = amendmentReason;
        this.amendmentDate = amendmentDate;
        this.fourEyesApproval = fourEyesApproval;
        this.approvedDate = approvedDate;
        this.isDeleted = isDeleted;
        this.childResultLineIds = childResultLineIds;
        this.parentResultLineIds = parentResultLineIds;
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

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
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

    public static Builder sharedResultsCommandResultLine(){
        return new Builder();
    }

    public static final class Builder {
        private DelegatedPowers delegatedPowers;
        private LocalDate orderedDate;
        private LocalDate sharedDate;
        private UUID resultLineId;
        private UUID targetId;
        private UUID offenceId;
        private UUID defendantId;
        private UUID resultDefinitionId;
        private List<SharedResultsCommandPrompt> prompts;
        private String resultLabel;
        private String level;
        private boolean isModified;
        private boolean isComplete;
        private UUID applicationId;
        private ZonedDateTime amendmentDate;
        private UUID amendmentReasonId;
        private String amendmentReason;
        private DelegatedPowers fourEyesApproval;
        private LocalDate approvedDate;
        private boolean isDeleted;
        private List<UUID> childResultLineIds;
        private List<UUID> parentResultLineIds;

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

        public Builder withTargetId(final UUID targetId) {
            this.targetId = targetId;
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

        public SharedResultsCommandResultLineV2 build() {
            return new SharedResultsCommandResultLineV2(
                    delegatedPowers,
                    orderedDate,
                    sharedDate,
                    resultLineId,
                    targetId,
                    offenceId,
                    defendantId,
                    resultDefinitionId,
                    prompts,
                    resultLabel,
                    level,
                    isModified,
                    isComplete,
                    applicationId,
                    amendmentReasonId,
                    amendmentReason,
                    amendmentDate,
                    fourEyesApproval,
                    approvedDate,
                    isDeleted,
                    childResultLineIds,
                    parentResultLineIds
            );
        }
    }

   

}
