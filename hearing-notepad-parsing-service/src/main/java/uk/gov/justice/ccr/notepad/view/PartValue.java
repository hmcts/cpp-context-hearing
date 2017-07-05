package uk.gov.justice.ccr.notepad.view;


import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "value",
        "type",
        "label"
})
public class PartValue {

    private ResultType type;

    private String label;

    private Integer value;

    public ResultType getType() {
        return type;
    }

    public final void setType(ResultType value) {
        type = value;
    }

    public String getLabel() {
        return label;
    }

    public final void setLabel(String value) {
        label = value;
    }

    public Integer getValue() {
        return value;
    }

    public final void setValue(Integer value) {
        this.value = value;
    }
}
