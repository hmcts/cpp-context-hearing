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
        "originalText",
        "resultCode",
        "parts"

})
public class ResultDefinition {

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("originalText")
    private String originalText;

    @JsonProperty("resultCode")
    private String resultCode;

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("parts")
    private List<Part> parts = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * (Required)
     *
     * @return
     * The originalText
     */
    @JsonProperty("originalText")
    public String getOriginalText() {
        return originalText;
    }

    /**
     *
     * (Required)
     *
     * @param originalText
     * The originalText
     */
    @JsonProperty("originalText")
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    @JsonProperty("resultCode")
    public String getResultCode() {
        return resultCode;
    }

    @JsonProperty("resultCode")
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    /**
     *
     * (Required)
     *
     * @return
     * The parts
     */
    @JsonProperty("parts")
    public List<Part> getParts() {
        return parts;
    }

    /**
     *
     * (Required)
     *
     * @param parts
     * The parts
     */
    @JsonProperty("parts")
    public void setParts(List<Part> parts) {
        this.parts = parts;
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
