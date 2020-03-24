package uk.gov.moj.cpp.hearing.command.courtlistpublishstatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RecordCourtListExportSuccessful {
    private final UUID courtCentreId;

    private final String courtListFileName;

    private final ZonedDateTime createdTime;

    public RecordCourtListExportSuccessful(final UUID courtCentreId, final String courtListFileName, final ZonedDateTime createdTime) {
        this.courtCentreId = courtCentreId;
        this.courtListFileName = courtListFileName;
        this.createdTime = createdTime;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtListFileName() {
        return courtListFileName;
    }


    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public static Builder recordCourtListExportSuccessful() {
        return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful that = (uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful) obj;

        return java.util.Objects.equals(this.courtCentreId, that.courtCentreId) &&
                java.util.Objects.equals(this.courtListFileName, that.courtListFileName) &&
                java.util.Objects.equals(this.createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, courtListFileName, createdTime);
    }

    @Override
    public String toString() {
        return "RecordCourtListExportSuccessful{" +
                "courtCentreId='" + courtCentreId + "'," +
                "courtListFileName='" + courtListFileName + "'," +
                "createdTime='" + createdTime + "'" +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private String courtListFileName;

        private ZonedDateTime createdTime;

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtListFileName(final String courtListFileName) {
            this.courtListFileName = courtListFileName;
            return this;
        }

        public Builder withCreatedTime(final ZonedDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public RecordCourtListExportSuccessful build() {
            return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful(courtCentreId, courtListFileName, createdTime);
        }
    }
}
