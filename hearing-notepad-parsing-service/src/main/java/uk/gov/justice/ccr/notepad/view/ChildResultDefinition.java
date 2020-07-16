package uk.gov.justice.ccr.notepad.view;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "shortCode",
        "label",
        "ruleType",
        "excludedFromResults",
        "childResultCodes"
})
public class ChildResultDefinition {

    private String code;
    private String shortCode;
    private String label;
    private String ruleType;
    private Boolean excludedFromResults;
    private List<UUID> childResultCodes;

    public Boolean getExcludedFromResults() { return excludedFromResults; }

    public void setExcludedFromResults(final Boolean excludedFromResults) { this.excludedFromResults = excludedFromResults; }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(final String shortCode) {
        this.shortCode = shortCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(final String ruleType) {
        this.ruleType = ruleType;
    }

    public List<UUID> getChildResultCodes() {
        return childResultCodes;
    }

    public void setChildResultCodes(final List<UUID> childResultCodes) {
        this.childResultCodes = childResultCodes;
    }
}
