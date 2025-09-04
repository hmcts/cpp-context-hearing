package uk.gov.moj.cpp.hearing.query.view.model;

public class ApplicationWithStatus {
    private final String applicationId;
    private final String applicationStatus;

    public ApplicationWithStatus(final String applicationId, final String applicationStatus) {
        this.applicationId = applicationId;
        this.applicationStatus = applicationStatus;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }
}
