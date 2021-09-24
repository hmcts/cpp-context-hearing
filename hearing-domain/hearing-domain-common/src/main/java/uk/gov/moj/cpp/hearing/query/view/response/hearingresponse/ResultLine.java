package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Prompt;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S00107", "squid:S2384"})
public class ResultLine {

    private final LocalDate orderedDate;
    private final LocalDate sharedDate;
    private final UUID resultLineId;
    private final UUID offenceId;
    private final UUID defendantId;
    private final UUID masterDefendantId;
    private final UUID resultDefinitionId;
    private final List<Prompt> prompts;
    private final Level level;
    private final DelegatedPowers delegatedPowers;
    private final String resultLabel;
    private final Boolean isModified;
    private final Boolean isComplete;
    private final Boolean shadowListed;
    private final String draftResult;
    private final UUID applicationId;
    private final UUID caseId;
    private final ZonedDateTime amendmentDate;
    private final UUID amendmentReasonId;
    private final String shortCode;
    private final DelegatedPowers fourEyesApproval;
    private final LocalDate approvedDate;
    private final Boolean isDeleted;
    private final List<UUID> childResultLineIds;
    private final List<UUID> parentResultLineIds;
    private final String amendmentReason;

    public ResultLine(final LocalDate orderedDate, final LocalDate sharedDate, final UUID resultLineId, final UUID offenceId,
                      final UUID defendantId, final UUID masterDefendantId, final UUID resultDefinitionId, final List<Prompt> prompts, final Level level,
                      final DelegatedPowers delegatedPowers, final String resultLabel, final Boolean isModified, final Boolean isComplete,
                      final Boolean shadowListed, final String draftResult, final UUID applicationId, final UUID caseId, final ZonedDateTime amendmentDate,
                      final UUID amendmentReasonId, final String shortCode, final DelegatedPowers fourEyesApproval,
                      final LocalDate approvedDate, final Boolean isDeleted, final List<UUID> childResultLineIds,
                      final List<UUID> parentResultLineIds, final String amendmentReason) {
        this.orderedDate = orderedDate;
        this.sharedDate = sharedDate;
        this.resultLineId = resultLineId;
        this.offenceId = offenceId;
        this.defendantId = defendantId;
        this.masterDefendantId = masterDefendantId;
        this.resultDefinitionId = resultDefinitionId;
        this.prompts = prompts;
        this.level = level;
        this.delegatedPowers = delegatedPowers;
        this.resultLabel = resultLabel;
        this.isModified = isModified;
        this.isComplete = isComplete;
        this.shadowListed = shadowListed;
        this.draftResult = draftResult;
        this.applicationId = applicationId;
        this.caseId = caseId;
        this.amendmentDate = amendmentDate;
        this.amendmentReasonId = amendmentReasonId;
        this.shortCode = shortCode;
        this.fourEyesApproval = fourEyesApproval;
        this.approvedDate = approvedDate;
        this.isDeleted = isDeleted;
        this.childResultLineIds = childResultLineIds;
        this.parentResultLineIds = parentResultLineIds;
        this.amendmentReason = amendmentReason;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public LocalDate getSharedDate() {
        return sharedDate;
    }

    public UUID getResultLineId() {
        return resultLineId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public List<Prompt> getPrompts() {
        return unmodifiableList(prompts);
    }

    public Level getLevel() {
        return level;
    }

    public String getShortCode() {
        return shortCode;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public Boolean getModified() {
        return isModified;
    }

    public Boolean getComplete() {
        return isComplete;
    }

    public Boolean getShadowListed() {
        return shadowListed;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ZonedDateTime getAmendmentDate() {
        return amendmentDate;
    }

    public UUID getAmendmentReasonId() {
        return amendmentReasonId;
    }

    public DelegatedPowers getFourEyesApproval() {
        return fourEyesApproval;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public List<UUID> getChildResultLineIds() {
        return nonNull(childResultLineIds)?childResultLineIds:Collections.emptyList();
    }

    public List<UUID> getParentResultLineIds() {
        return nonNull(parentResultLineIds)?parentResultLineIds:Collections.emptyList();
    }

    public String getAmendmentReason() {
        return amendmentReason;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public static class Builder {

        private LocalDate orderedDate;
        private LocalDate sharedDate;
        private UUID resultLineId;
        private UUID offenceId;
        private UUID defendantId;
        private UUID masterDefendantId;
        private UUID resultDefinitionId;
        private List<Prompt> prompts;
        private Level level;
        private String shortCode;
        private DelegatedPowers delegatedPowers;
        private String resultLabel;
        private Boolean isModified;
        private Boolean isComplete;
        private Boolean shadowListed;
        private String draftResult;
        private UUID applicationId;
        private UUID caseId;
        private ZonedDateTime amendmentDate;
        private UUID amendmentReasonId;
        private String amendmentReason;
        private DelegatedPowers fourEyesApproval;
        private LocalDate approvedDate;
        private Boolean isDeleted;
        private List<UUID> childResultLineIds;
        private List<UUID> parentResultLineIds;

        public ResultLine build() {
            return new ResultLine(orderedDate, sharedDate, resultLineId, offenceId, defendantId, masterDefendantId,resultDefinitionId,
                    prompts, level, delegatedPowers, resultLabel, isModified, isComplete, shadowListed, draftResult,
                    applicationId, caseId, amendmentDate, amendmentReasonId, shortCode, fourEyesApproval, approvedDate,
                    isDeleted, childResultLineIds, parentResultLineIds, amendmentReason);
        }

        public ResultLine.Builder withOrderedDate(final LocalDate orderedDate) {
            this.orderedDate = orderedDate;
            return this;
        }

        public ResultLine.Builder withSharedDate(final LocalDate sharedDate) {
            this.sharedDate = sharedDate;
            return this;
        }

        public ResultLine.Builder withResultLineId(final UUID resultLineId) {
            this.resultLineId = resultLineId;
            return this;
        }

        public ResultLine.Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public ResultLine.Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public ResultLine.Builder withMasterDefendantId(final UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public ResultLine.Builder withResultDefinitionId(final UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public ResultLine.Builder withPrompts(final List<Prompt> prompts) {
            this.prompts = prompts;
            return this;
        }

        public ResultLine.Builder withLevel(final Level level) {
            this.level = level;
            return this;
        }

        public Builder withShortCode(final String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public Builder withDelegatedPowers(final DelegatedPowers delegatedPowers) {
            this.delegatedPowers = delegatedPowers;
            return this;
        }

        public Builder withResultLabel(final String resultLabel) {
            this.resultLabel = resultLabel;
            return this;
        }

        public Builder withIsModified(final Boolean isModified) {
            this.isModified = isModified;
            return this;
        }

        public Builder withIsComplete(final Boolean isComplete) {
            this.isComplete = isComplete;
            return this;
        }

        public Builder withShadowListed(final Boolean shadowListed) {
            this.shadowListed = shadowListed;
            return this;
        }

        public Builder withDraftResult(final String draftResult) {
            this.draftResult = draftResult;
            return this;
        }

        public Builder withApplicationId(final UUID applicationId) {
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

        public Builder withIsDeleted(final Boolean isDeleted) {
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
    }
}
