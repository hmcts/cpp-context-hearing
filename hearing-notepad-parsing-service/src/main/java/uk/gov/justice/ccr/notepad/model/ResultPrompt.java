package uk.gov.justice.ccr.notepad.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "resultCode",
        "promptChoices"
})
public class ResultPrompt {


    /**
     * (Required)
     */
    @JsonProperty("resultCode")
    private String resultCode;
    /**
     * (Required)
     */
    @JsonProperty("promptChoices")
    private List<PromptChoice> promptChoices = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * (Required)
     *
     * @return The resultCode
     */
    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }


    /**
     * (Required)
     *
     * @param resultCode The resultCode
     */
    @JsonProperty("resultCode")
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * (Required)
     *
     * @return The promptChoices
     */
    @JsonProperty("promptChoices")
    public List<PromptChoice> getPromptChoices() {
        return promptChoices;
    }

    /**
     * (Required)
     *
     * @param promptChoices The promptChoices
     */
    @JsonProperty("promptChoices")
    public void setPromptChoices(List<PromptChoice> promptChoices) {
        this.promptChoices = promptChoices;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
