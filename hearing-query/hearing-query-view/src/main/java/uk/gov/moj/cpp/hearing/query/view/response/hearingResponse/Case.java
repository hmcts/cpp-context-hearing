package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "caseId",
        "caseUrn",
        "defendants"
})
public class Case {
    private String caseId;
    private String caseUrn;
    private List<Defendant> defendants = null;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Case withCaseId(String caseId) {
        this.caseId = caseId;
        return this;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public void setCaseUrn(String caseUrn) {
        this.caseUrn = caseUrn;
    }

    public Case withCaseUrn(String caseUrn) {
        this.caseUrn = caseUrn;
        return this;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public void setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
    }

    public Case withDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }
}
