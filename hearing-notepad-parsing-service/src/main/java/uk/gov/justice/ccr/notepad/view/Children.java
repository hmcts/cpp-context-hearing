package uk.gov.justice.ccr.notepad.view;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "label",
        "type"
})
public class Children {
    private String label;
    private ResultType type;

    public Children(String label, ResultType type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }


    public ResultType getType() {
        return type;
    }
}
