package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.DelegatedPowers;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SharedResultsCommandResultLine implements Serializable {
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
    private LocalDate amendmentDate;
    private UUID amendmentReasonId;
    private String amendmentReason;
    private DelegatedPowers fourEyesApproval;
    private LocalDate approvedDate;
    private boolean isDeleted;
    private CourtApplicationOutcome courtApplicationOutcome;


    @SuppressWarnings({"squid:S2384"})
    @JsonCreator
    public SharedResultsCommandResultLine(
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
            @JsonProperty("amendmentDate") final LocalDate amendmentDate,
            @JsonProperty("fourEyesApproval") final DelegatedPowers fourEyesApproval,
            @JsonProperty("approvedDate") final LocalDate approvedDate,
            @JsonProperty("isDeleted") final boolean isDeleted,
            @JsonProperty("applicationOutcome") final CourtApplicationOutcome courtApplicationOutcome
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
        this.courtApplicationOutcome = courtApplicationOutcome;

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

    public LocalDate getAmendmentDate() {
        return amendmentDate;
    }

    public void setAmendmentDate(final LocalDate amendmentDate) {
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

    public CourtApplicationOutcome getApplicationOutcome() {
        return courtApplicationOutcome;
    }

    public void setApplicationOutcome(final CourtApplicationOutcome courtApplicationOutcome) {
        this.courtApplicationOutcome = courtApplicationOutcome;
    }
}
