package uk.gov.justice.ccr.notepad.view;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "value",
        "state",
        "type",
        "code",
        "label",
        "resultLevel",
        "resultChoices"
})
public class Part {

    private ResultType type;

    private String label;

    private Object value;

    private String code;

    private String resultLevel;

    private String originalText;

    private State state;//resolved,unresolved

    private TreeSet<ResultChoice> resultChoices;


    @JsonIgnore
    private boolean isVisible = true;

    public TreeSet<ResultChoice> getResultChoices() {
        return resultChoices;
    }

    public void setResultChoices(TreeSet<ResultChoice> resultChoices) {
        this.resultChoices = resultChoices;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ResultType getType() {
        return type;
    }

    public final void setType(ResultType value) {
        type = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @JsonIgnore
    public String getValueAsString() {
        return value.toString();
    }

    public String getCode() {
        return code;
    }

    public final void setCode(String value) {
        code = value;
    }

    public String getResultLevel() {
        return resultLevel;
    }

    public final void setResultLevel(String value) {
        resultLevel = value;
    }

    public String getLabel() {
        return label;
    }

    public final void setLabel(String value) {
        label = value;
    }

    @JsonIgnore
    public boolean getVisible() {
        return isVisible;
    }

    public final void setVisible(boolean value) {
        isVisible = value;
    }

    public String getOriginalText() {
        return originalText;
    }

    public final void setOriginalText(String value) {
        originalText = value;
    }

    public enum State {
        UNRESOLVED, RESOLVED
    }
}
