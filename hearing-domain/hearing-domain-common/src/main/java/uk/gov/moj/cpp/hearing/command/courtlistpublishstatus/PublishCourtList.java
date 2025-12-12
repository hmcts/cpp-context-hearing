package uk.gov.moj.cpp.hearing.command.courtlistpublishstatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class PublishCourtList {
    private final UUID courtCentreId;

    private final ZonedDateTime createdTime;


    public PublishCourtList(final UUID courtCentreId, final ZonedDateTime createdTime) {
        this.courtCentreId = courtCentreId;
        this.createdTime = createdTime;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }


    public static Builder publishCourtList() {
        return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtList.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtList that = (uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtList) obj;

        return java.util.Objects.equals(this.courtCentreId, that.courtCentreId) &&
                java.util.Objects.equals(this.createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, createdTime);
    }

    @Override
    public String toString() {
        return "PublishCourtList{" +
                "courtCentreId='" + courtCentreId + "'," +
                "createdTime='" + createdTime + "'," +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private ZonedDateTime createdTime;


        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }


        public Builder withRequestedTime(final ZonedDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public PublishCourtList build() {
            return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.PublishCourtList(courtCentreId, createdTime);
        }
    }
}
