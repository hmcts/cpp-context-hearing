package uk.gov.justice.ccr.notepad.view;

import static com.google.common.collect.Lists.newArrayList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "label",
        "type",
        "required"
})
public class PromptChoice {

    private String code;

    private String label;

    private ResultType type;

    private Boolean required;

    private List<Children> children;

    private Set<String> fixedList;

    @JsonIgnore
    private boolean isVisible = true;

    @JsonIgnore
    private String durationElement;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ResultType getType() {
        return type;
    }

    public final void setType(ResultType value) {
        type = value;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public List<Children> getChildren() {
        return children;
    }

    public final void addChildren(Children value) {
        if (children == null) {
            children = newArrayList();
        }
        children.add(value);
    }

    @JsonIgnore
    public boolean getVisible() {
        return isVisible;
    }

    public final void setVisible(boolean value) {
        isVisible = value;
    }

    public String getDurationElement() {
        return durationElement;
    }

    public final void setDurationElement(String value) {
        durationElement = value;
    }


    public Set<String> getFixedList() {
        return fixedList;
    }

    public void setFixedList(Set<String> fixedList) {
        this.fixedList = fixedList;
    }
}
