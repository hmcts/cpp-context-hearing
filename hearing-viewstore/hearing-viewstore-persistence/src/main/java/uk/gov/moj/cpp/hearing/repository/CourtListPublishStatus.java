package uk.gov.moj.cpp.hearing.repository;


import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class CourtListPublishStatus {

    private UUID courtCentreId;
    private ZonedDateTime lastUpdated;
    private PublishStatus publishStatus;
    private String errorMessage;

    public CourtListPublishStatus(final UUID courtCentreId,
                                  final ZonedDateTime lastUpdated,
                                  final PublishStatus publishStatus) {
        this.courtCentreId = courtCentreId;
        this.lastUpdated = lastUpdated;
        this.publishStatus = publishStatus;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public PublishStatus getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(PublishStatus publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getFailureMessage() {
        return errorMessage;
    }

    public void setFailureMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}