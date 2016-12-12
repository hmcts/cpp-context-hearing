package uk.gov.justice.ccr.notepad.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "label",
        "type",
        "required",
        "synonyms"
})
public class PromptChoice {

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("code")
    private String code;
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("label")
    private String label;

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("required")
    private String required;
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("type")
    private String type;

    @JsonProperty("synonyms")
    private Map<String,String> synonyms = new HashMap<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * (Required)
     *
     * @return
     * The code
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     *
     * (Required)
     *
     * @param code
     * The code
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * (Required)
     *
     * @return
     * The label
     */
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    /**
     *
     * (Required)
     *
     * @param label
     * The label
     */
    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * (Required)
     *
     * @return
     * The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     *
     * (Required)
     *
     * @param type
     * The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The synonyms
     */
    @JsonProperty("synonyms")
    public Map<String,String> getSynonyms() {
        return synonyms;
    }

    /**
     *
     * @param synonyms
     * The synonyms
     */
    @JsonProperty("synonyms")
    public void setSynonyms(Map<String,String> synonyms) {
        this.synonyms = synonyms;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }
}
