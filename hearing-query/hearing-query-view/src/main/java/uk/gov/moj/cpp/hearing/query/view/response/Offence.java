package uk.gov.moj.cpp.hearing.query.view.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "offenceId",
        "caseId",
        "defendantId",
        "personId",
        "plea",
        "verdicts"
})
public class Offence {

    @JsonProperty("offenceId")
    private String offenceId;
    @JsonProperty("caseId")
    private String caseId;
    @JsonProperty("defendantId")
    private String defendantId;
    @JsonProperty("personId")
    private String personId;
    @JsonProperty("plea")
    private Plea plea;
    @JsonProperty("verdicts")
    private Verdicts verdicts;

    @JsonProperty("offenceId")
    public String getOffenceId() {
        return offenceId;
    }

    @JsonProperty("offenceId")
    public void setOffenceId(String offenceId) {
        this.offenceId = offenceId;
    }

    @JsonProperty("caseId")
    public String getCaseId() {
        return caseId;
    }

    @JsonProperty("caseId")
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @JsonProperty("defendantId")
    public String getDefendantId() {
        return defendantId;
    }

    @JsonProperty("defendantId")
    public void setDefendantId(String defendantId) {
        this.defendantId = defendantId;
    }

    @JsonProperty("personId")
    public String getPersonId() {
        return personId;
    }

    @JsonProperty("personId")
    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @JsonProperty("plea")
    public Plea getPlea() {
        return plea;
    }

    @JsonProperty("plea")
    public void setPlea(Plea plea) {
        this.plea = plea;
    }

    @JsonProperty("verdicts")
    public Verdicts getVerdicts() {
        return verdicts;
    }

    @JsonProperty("verdicts")
    public void setVerdicts(Verdicts verdicts) {
        this.verdicts = verdicts;
    }

}
