package uk.gov.moj.cpp.hearing.query.view.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "verdictId",
        "value"
})
public class Verdicts {

    @JsonProperty("verdictId")
    private String verdictId;
    @JsonProperty("value")
    private String value;

    @JsonProperty("verdictId")
    public String getVerdictId() {
        return verdictId;
    }

    @JsonProperty("verdictId")
    public void setVerdictId(String verdictId) {
        this.verdictId = verdictId;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

}
