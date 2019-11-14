package uk.gov.moj.cpp.hearing.publishing.events;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings({"squid:S1067"})
@Event("hearing.event.publish-court-list-export-failed")
public class PublishCourtListExportFailed {
    private final UUID courtCentreId;

    private final UUID courtListFileId;

    private final String courtListFileName;

    private final String errorMessage;

    private final ZonedDateTime createdTime;

    private final PublishStatus publishStatus;

    public PublishCourtListExportFailed(final UUID courtCentreId, final UUID courtListFileId, final String courtListFileName, final String errorMessage, final ZonedDateTime createdTime, final PublishStatus publishStatus) {
        this.courtCentreId = courtCentreId;
        this.courtListFileId = courtListFileId;
        this.courtListFileName = courtListFileName;
        this.errorMessage = errorMessage;
        this.createdTime = createdTime;
        this.publishStatus = publishStatus;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public UUID getCourtListFileId() {
        return courtListFileId;
    }

    public String getCourtListFileName() {
        return courtListFileName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public PublishStatus getPublishStatus() {
        return publishStatus;
    }

    public static Builder publishCourtListExportFailed() {
        return new PublishCourtListExportFailed.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PublishCourtListExportFailed that = (PublishCourtListExportFailed) obj;

        return java.util.Objects.equals(this.courtCentreId, that.courtCentreId) &&
                java.util.Objects.equals(this.courtListFileId, that.courtListFileId) &&
                java.util.Objects.equals(this.courtListFileName, that.courtListFileName) &&
                java.util.Objects.equals(this.errorMessage, that.errorMessage) &&
                java.util.Objects.equals(this.createdTime, that.createdTime) &&
                java.util.Objects.equals(this.publishStatus, that.publishStatus);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, courtListFileId, courtListFileName, errorMessage, createdTime, publishStatus);
    }

    @Override
    public String toString() {
        return "PublishCourtListExportFailed{" +
                "courtCentreId='" + courtCentreId + "'," +
                "courtListFileId='" + courtListFileId + "'," +
                "courtListFileName='" + courtListFileName + "'," +
                "errorMessage='" + errorMessage + "'," +
                "createdTime='" + createdTime + "'," +
                "publishStatus='" + publishStatus + "'" +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private UUID courtListFileId;

        private String courtListFileName;

        private String errorMessage;

        private ZonedDateTime createdTime;

        private PublishStatus publishStatus;

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtListFileId(final UUID courtListFileId) {
            this.courtListFileId = courtListFileId;
            return this;
        }

        public Builder withCourtListFileName(final String courtListFileName) {
            this.courtListFileName = courtListFileName;
            return this;
        }

        public Builder withErrorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withCreatedTime(final ZonedDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder withPublishStatus(final PublishStatus publishStatus) {
            this.publishStatus = publishStatus;
            return this;
        }

        public PublishCourtListExportFailed build() {
            return new PublishCourtListExportFailed(courtCentreId, courtListFileId, courtListFileName, errorMessage, createdTime, publishStatus);
        }
    }
}
