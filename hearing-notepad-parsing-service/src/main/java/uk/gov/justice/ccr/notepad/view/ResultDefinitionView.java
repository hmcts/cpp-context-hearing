package uk.gov.justice.ccr.notepad.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultLineId",
        "originalText",
        "resultDefinitionId",
        "shortCode",
        "resultLevel",
        "orderedDate",
        "parts",
        "childResultDefinitions",
        "label",
        "conditionalMandatory",
        "excludedFromResults",
        "resultDefinitionGroup"

})
public class ResultDefinitionView {

    private final String resultLineId = UUID.randomUUID().toString();

    private String originalText;

    private String resultDefinitionId;

    private String resultLevel;

    private String orderedDate;

    private String shortCode;

    private List<Part> parts = new ArrayList<>();

    private List<ChildResultDefinition> childResultDefinitions = new ArrayList<>();

    private String label;

    private Boolean conditionalMandatory;

    private List<PromptChoice> promptChoices = new ArrayList<>();

    private Boolean excludedFromResults;

    private String resultDefinitionGroup;

    public Boolean getExcludedFromResults() { return excludedFromResults; }

    public void setExcludedFromResults(final Boolean excludedFromResults) { this.excludedFromResults = excludedFromResults; }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(final String originalText) {
        this.originalText = originalText;
    }

    public String getResultDefinitionId() {
        return resultDefinitionId;
    }

    public void setResultDefinitionId(final String resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    public String getResultLevel() {
        return resultLevel;
    }

    public void setResultLevel(final String resultLevel) {
        this.resultLevel = resultLevel;
    }

    public List<Part> getParts() {
         return Optional.ofNullable(parts).orElse(new ArrayList<>(parts));
    }

    public void setParts(final List<Part> parts) {
        if(!parts.isEmpty()) {
            this.parts = new ArrayList<>(parts);
        } else {
            this.parts = null;
        }
    }

    public String getResultLineId() {
        return resultLineId;
    }

    public String getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(final String orderedDate) {
        this.orderedDate = orderedDate;
    }

    public List<ChildResultDefinition> getChildResultDefinitions() {
        return childResultDefinitions;
    }

    public void setChildResultDefinitions(final List<ChildResultDefinition> childResultDefinitions) {
        if(childResultDefinitions !=null) {
            this.childResultDefinitions = new ArrayList<>(childResultDefinitions);
        } else {
            this.childResultDefinitions = null;
        }
    }

    public List<PromptChoice> getPromptChoices() {
        return promptChoices;
    }

    public void setPromptChoices(final List<PromptChoice> promptChoices) {
            this.promptChoices = promptChoices;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean isConditionalMandatory() {
        return conditionalMandatory;
    }

    public void setConditionalMandatory(Boolean conditionalMandatory) {
        this.conditionalMandatory = conditionalMandatory;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }


    public String getResultDefinitionGroup() {
        return resultDefinitionGroup;
    }

    public void setResultDefinitionGroup(final String resultDefinitionGroup) {
        this.resultDefinitionGroup = resultDefinitionGroup;
    }
}
