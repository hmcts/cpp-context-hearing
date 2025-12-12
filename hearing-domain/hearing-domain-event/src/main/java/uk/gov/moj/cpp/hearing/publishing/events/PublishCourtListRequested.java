package uk.gov.moj.cpp.hearing.publishing.events;

import uk.gov.justice.domain.annotation.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

@Event("hearing.event.publish-court-list-requested")
public class PublishCourtListRequested {
    private final UUID courtCentreId;

    private final PublishStatus publishStatus;

    private final ZonedDateTime createdTime;


    public PublishCourtListRequested(final UUID courtCentreId, final PublishStatus publishStatus, final ZonedDateTime createdTime) {
        this.courtCentreId = courtCentreId;
        this.publishStatus = publishStatus;
        this.createdTime = createdTime;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }


    public PublishStatus getPublishStatus() {
        return publishStatus;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public static Builder publishCourtListRequested() {
        return new PublishCourtListRequested.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PublishCourtListRequested that = (PublishCourtListRequested) obj;

        return java.util.Objects.equals(this.courtCentreId, that.courtCentreId) &&
                java.util.Objects.equals(this.publishStatus, that.publishStatus) &&
                java.util.Objects.equals(this.createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, publishStatus, createdTime);
    }

    @Override
    public String toString() {
        return "PublishCourtListRequested{" +
                "courtCentreId='" + courtCentreId + "'," +
                "publishStatus='" + publishStatus + "'," +
                "createdTime='" + createdTime + "'," +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private PublishStatus publishStatus;

        private ZonedDateTime createdTime;

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
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

        public PublishCourtListRequested build() {
            return new PublishCourtListRequested(courtCentreId, publishStatus, createdTime);
        }
    }
}
