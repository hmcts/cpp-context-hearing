package uk.gov.moj.cpp.hearing.query.view.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "pleaId",
        "pleaDate",
        "value"
})
public class Plea {

    @JsonProperty("pleaId")
    private String pleaId;
    @JsonProperty("pleaDate")
    private String pleaDate;
    @JsonProperty("value")
    private String value;

    @JsonProperty("pleaId")
    public String getPleaId() {
        return pleaId;
    }

    @JsonProperty("pleaId")
    public void setPleaId(String pleaId) {
        this.pleaId = pleaId;
    }

    @JsonProperty("pleaDate")
    public String getPleaDate() {
        return pleaDate;
    }

    @JsonProperty("pleaDate")
    public void setPleaDate(String pleaDate) {
        this.pleaDate = pleaDate;
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
