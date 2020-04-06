package uk.gov.justice.ccr.notepad.view;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultLineId",
        "originalText",
        "resultCode",
        "resultLevel",
        "orderedDate",
        "parts",
        "childResultDefinitions",
        "excludedFromResults"

})
public class ResultDefinitionView {

    private final String resultLineId = UUID.randomUUID().toString();
    private String originalText;
    private String resultCode;

    private String resultLevel;

    private String orderedDate;

    private List<Part> parts = new ArrayList<>();

    private List<ChildResultDefinition> childResultDefinitions = new ArrayList<>();

    private Boolean excludedFromResults;

    public Boolean getExcludedFromResults() { return excludedFromResults; }

    public void setExcludedFromResults(final Boolean excludedFromResults) { this.excludedFromResults = excludedFromResults; }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(final String originalText) {
        this.originalText = originalText;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(final String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultLevel() {
        return resultLevel;
    }

    public void setResultLevel(final String resultLevel) {
        this.resultLevel = resultLevel;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(final List<Part> parts) {
        this.parts = parts;
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
        this.childResultDefinitions = childResultDefinitions;
    }

}
