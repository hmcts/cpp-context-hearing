package uk.gov.justice.ccr.notepad.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "label",
        "state",
        "type",
        "resultChoices"
})
public class Part {

    @JsonProperty("type")
    private String type;

    @JsonIgnore
    private String text;

    @JsonProperty("label")
    private String label;

    @JsonProperty("state")
    private State state;//resolved,unresolved

    @JsonProperty("resultChoices")
    private Set<ResultChoice> resultChoices = new HashSet<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * (Required)
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * (Required)
     *
     * @param text The text
     */

    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return The resultChoices
     */
    @JsonProperty("resultChoices")
    public Set<ResultChoice> getResultChoices() {
        return resultChoices;
    }

    /**
     * @param resultChoices The resultChoices
     */
    @JsonProperty("resultChoices")
    public void setResultChoices(Set<ResultChoice> resultChoices) {
        this.resultChoices = resultChoices;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("state")
    public State getState() {
        return state;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("state")
    public void setState(State state) {
        this.state = state;
    }
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }
    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    public static enum State {
        UNRESOLVED, RESOLVED;
    }

    public static enum Type {
        RESULT, PROMPT;
    }



}
