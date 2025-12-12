package uk.gov.moj.cpp.hearing.publishing.events;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings({"squid:S1067"})
@Event("hearing.event.publish-court-list-export-successful")
public class PublishCourtListExportSuccessful {
    private final UUID courtCentreId;

    private final String courtListFileName;

    private final PublishStatus publishStatus;

    private final ZonedDateTime createdTime;

    public PublishCourtListExportSuccessful(final UUID courtCentreId, final String courtListFileName, final PublishStatus publishStatus, final ZonedDateTime createdTime) {
        this.courtCentreId = courtCentreId;
        this.courtListFileName = courtListFileName;
        this.publishStatus = publishStatus;
        this.createdTime = createdTime;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtListFileName() {
        return courtListFileName;
    }

    public PublishStatus getPublishStatus() {
        return publishStatus;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public static Builder publishCourtListExportSuccessful() {
        return new PublishCourtListExportSuccessful.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PublishCourtListExportSuccessful that = (PublishCourtListExportSuccessful) obj;

        return java.util.Objects.equals(this.courtCentreId, that.courtCentreId) &&
                java.util.Objects.equals(this.courtListFileName, that.courtListFileName) &&
                java.util.Objects.equals(this.publishStatus, that.publishStatus) &&
                java.util.Objects.equals(this.createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, courtListFileName, publishStatus, createdTime);
    }

    @Override
    public String toString() {
        return "PublishCourtListExportSuccessful{" +
                "courtCentreId='" + courtCentreId + "'," +
                "courtListFileName='" + courtListFileName + "'," +
                "publishStatus='" + publishStatus + "'," +
                "createdTime='" + createdTime + "'" +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private String courtListFileName;

        private PublishStatus publishStatus;

        private ZonedDateTime createdTime;

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtListFileName(final String courtListFileName) {
            this.courtListFileName = courtListFileName;
            return this;
        }

        public Builder withPublishStatus(final PublishStatus publishStatus) {
            this.publishStatus = publishStatus;
            return this;
        }

        public Builder withCreatedTime(final ZonedDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public PublishCourtListExportSuccessful build() {
            return new PublishCourtListExportSuccessful(courtCentreId, courtListFileName, publishStatus, createdTime);
        }
    }
}
