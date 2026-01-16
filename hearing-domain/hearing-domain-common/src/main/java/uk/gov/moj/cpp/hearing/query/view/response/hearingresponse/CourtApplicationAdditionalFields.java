package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

public class CourtApplicationAdditionalFields {

    private Boolean amendmentAllowed;
    public CourtApplicationAdditionalFields() {
    }

    public CourtApplicationAdditionalFields(final Boolean amendmentAllowed) {
        this.amendmentAllowed = amendmentAllowed;
    }

    public Boolean getAmendmentAllowed() {
        return amendmentAllowed;
    }

    public void setAmendmentAllowed(final Boolean amendmentAllowed) {
        this.amendmentAllowed = amendmentAllowed;
    }
}
