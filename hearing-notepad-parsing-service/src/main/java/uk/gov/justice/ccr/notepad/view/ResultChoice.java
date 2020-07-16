package uk.gov.justice.ccr.notepad.view;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "shortCode",
        "label",
        "level",
        "type",
        "hidden"

})
public class ResultChoice implements Comparable<ResultChoice> {

    private String code;

    private String label;

    private String level;

    private ResultType type;

    private Boolean hidden;

    private String shortCode;

    public ResultChoice(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public ResultType getType() {
        return type;
    }

    public final void setType(ResultType value) {
        type = value;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(final Boolean hidden) {
        this.hidden = hidden;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(final String shortCode) {
        this.shortCode = shortCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultChoice)) {
            return false;
        }

        final ResultChoice that = (ResultChoice) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public int compareTo(ResultChoice o) {
        return this.label.compareToIgnoreCase(o.getLabel());
    }
}
