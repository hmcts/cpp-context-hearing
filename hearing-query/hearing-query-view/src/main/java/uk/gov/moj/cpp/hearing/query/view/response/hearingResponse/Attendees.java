package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "prosecutionCounsels",
        "defenceCounsels"
})
public class Attendees {
    private List<ProsecutionCounsel> prosecutionCounsels = null;
    private List<DefenceCounsel> defenceCounsels = null;

    public List<ProsecutionCounsel> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public void setProsecutionCounsels(List<ProsecutionCounsel> prosecutionCounsels) {
        this.prosecutionCounsels = prosecutionCounsels;
    }

    public Attendees withProsecutionCounsels(List<ProsecutionCounsel> prosecutionCounsels) {
        this.prosecutionCounsels = prosecutionCounsels;
        return this;
    }

    public List<DefenceCounsel> getDefenceCounsels() {
        return defenceCounsels;
    }

    public void setDefenceCounsels(List<DefenceCounsel> defenceCounsels) {
        this.defenceCounsels = defenceCounsels;
    }

    public Attendees withDefenceCounsels(List<DefenceCounsel> defenceCounsels) {
        this.defenceCounsels = defenceCounsels;
        return this;
    }
}
