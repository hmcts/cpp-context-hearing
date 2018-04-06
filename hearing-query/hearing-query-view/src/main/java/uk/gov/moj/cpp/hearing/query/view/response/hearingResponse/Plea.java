package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "pleaId",
        "pleaDate",
})
public class Plea {
    private String pleaId;
    private String pleaDate;

    public String getPleaId() {
        return pleaId;
    }

    public void setPleaId(String pleaId) {
        this.pleaId = pleaId;
    }

    public Plea withPleaId(String pleaId) {
        this.pleaId = pleaId;
        return this;
    }

    public String getPleaDate() {
        return pleaDate;
    }

    public void setPleaDate(String pleaDate) {
        this.pleaDate = pleaDate;
    }

    public Plea withPleaDate(String pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }
}
