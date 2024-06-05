package uk.gov.moj.cpp.hearing.eventlog;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class HearingCaseDetail implements Serializable {

    private UUID caseId;
    private String caseUrn;
    private List<HearingDefendantDetail> defendantDetails;

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(final UUID caseId) {
        this.caseId = caseId;
    }

    public List<HearingDefendantDetail> getDefendantDetails() {
        return defendantDetails;
    }

    public void setDefendantDetails(final List<HearingDefendantDetail> defendantDetails) {
        this.defendantDetails = defendantDetails;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public void setCaseUrn(final String caseUrn) {
        this.caseUrn = caseUrn;
    }
}
