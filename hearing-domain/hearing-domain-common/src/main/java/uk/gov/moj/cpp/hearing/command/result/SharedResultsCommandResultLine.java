package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.DelegatedPowers;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
            @JsonProperty("isComplete") final boolean isComplete
    ) {
        this.delegatedPowers = delegatedPowers;
        this.orderedDate = orderedDate;
        this.sharedDate = sharedDate;
        this.resultLineId = resultLineId;
        this.targetId = targetId;
        this.offenceId = offenceId;
        this.defendantId = defendantId;
        this.resultDefinitionId = resultDefinitionId;
        this.prompts = prompts;
        this.level = level;
        this.isModified = isModified;
        this.isComplete = isComplete;
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

    public void setISComplete(boolean complete) {
        isComplete = complete;
    }
}
