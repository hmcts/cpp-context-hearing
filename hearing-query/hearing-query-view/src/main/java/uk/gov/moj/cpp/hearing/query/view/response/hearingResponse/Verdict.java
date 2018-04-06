package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "verdictId",
        "hearingId",
        "value",
        "verdictDate",
        "numberOfSplitJurors",
        "numberOfJurors",
        "unanimous"
})
public class Verdict {
    private String verdictId;
    private String hearingId;
    private Value value;
    private String verdictDate;
    private Integer numberOfSplitJurors;
    private Integer numberOfJurors;
    private Boolean unanimous;

    public String getVerdictId() {
        return verdictId;
    }

    public void setVerdictId(String verdictId) {
        this.verdictId = verdictId;
    }

    public Verdict withVerdictId(String verdictId) {
        this.verdictId = verdictId;
        return this;
    }

    public String getHearingId() {
        return hearingId;
    }

    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }

    public Verdict withHearingId(String hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Verdict withValue(Value value) {
        this.value = value;
        return this;
    }

    public String getVerdictDate() {
        return verdictDate;
    }

    public void setVerdictDate(String verdictDate) {
        this.verdictDate = verdictDate;
    }

    public Verdict withVerdictDate(String verdictDate) {
        this.verdictDate = verdictDate;
        return this;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public void setNumberOfSplitJurors(Integer numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
    }

    public Verdict withNumberOfSplitJurors(Integer numberOfSplitJurors) {
        this.numberOfSplitJurors = numberOfSplitJurors;
        return this;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public void setNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
    }

    public Verdict withNumberOfJurors(Integer numberOfJurors) {
        this.numberOfJurors = numberOfJurors;
        return this;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public void setUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
    }

    public Verdict withUnanimous(Boolean unanimous) {
        this.unanimous = unanimous;
        return this;
    }
}
