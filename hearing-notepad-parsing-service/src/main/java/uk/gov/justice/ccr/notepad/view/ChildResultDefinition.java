package uk.gov.justice.ccr.notepad.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "shortCode",
        "label",
        "ruleType",
        "excludedFromResults"
})
public class ChildResultDefinition {

    private String code;
    private String shortCode;
    private String label;
    private String ruleType;
    private Boolean excludedFromResults;

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

}
