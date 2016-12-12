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
        "code",
        "label",
        "type",
        "synonymMatch"
})
public class ResultChoice implements Comparable<ResultChoice> {

    /**
     * (Required)
     */
    @JsonProperty("code")
    private String code;
    /**
     * (Required)
     */
    @JsonProperty("label")
    private String label;
    /**
     * (Required)
     */
    @JsonProperty("type")
    private String type;

    @JsonProperty("synonymMatch")
    private String synonymMatch;

    @JsonIgnore
    private List<String> synonyms = new ArrayList<>();

    @JsonIgnore
    private String displayLebel;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * (Required)
     *
     * @return The code
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }


    /**
     * (Required)
     *
     * @return The label
     */
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public String getDisplayLebel(String text) {
        String displayLebelOveeride;
        if("alc".equalsIgnoreCase(text))
            displayLebelOveeride = "Alcohol";
        else if("abs".equalsIgnoreCase(text))
            displayLebelOveeride = "Abstinence";
        else if("mon".equalsIgnoreCase(text))
            displayLebelOveeride = "Monitoring";
        else if("req".equalsIgnoreCase(text))
            displayLebelOveeride = "Requirements";
        else if("tra".equalsIgnoreCase(text))
            displayLebelOveeride = "Treatment";
        else
            displayLebelOveeride  = displayLebel;
        return displayLebelOveeride;
    }

    /**
     * (Required)
     *
     * @return The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * (Required)
     *
     * @param type The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The synonyms
     */
    @JsonProperty("synonymMatch")
    public String getSynonymMatch() {
        return synonymMatch;
    }

    /**
     * @param synonymMatch The synonyms
     */
    @JsonProperty("synonymMatch")
    public void setSynonymMatch(String synonymMatch) {
        this.synonymMatch = synonymMatch;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public ResultChoice(String code, String label) {
        this.code = code;
        this.label = label;
    }
    public ResultChoice(String code, String label,String displayLebel) {
        this.code = code;
        this.label = label;
        this.displayLebel = displayLebel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultChoice that = (ResultChoice) o;

        return code.equals(that.code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public int compareTo(ResultChoice o) {
        return this.code.compareTo(o.getCode());
    }
}
