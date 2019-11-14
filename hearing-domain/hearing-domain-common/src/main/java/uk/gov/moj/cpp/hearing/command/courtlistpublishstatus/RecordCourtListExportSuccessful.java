package uk.gov.moj.cpp.hearing.command.courtlistpublishstatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RecordCourtListExportSuccessful {
    private final UUID courtCentreId;

    private final UUID courtListFileId;

    private final String courtListFileName;

    private final ZonedDateTime createdTime;

    public RecordCourtListExportSuccessful(final UUID courtCentreId, final UUID courtListFileId, final String courtListFileName, final ZonedDateTime createdTime) {
        this.courtCentreId = courtCentreId;
        this.courtListFileId = courtListFileId;
        this.courtListFileName = courtListFileName;
        this.createdTime = createdTime;
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
                java.util.Objects.equals(this.courtListFileId, that.courtListFileId) &&
                java.util.Objects.equals(this.courtListFileName, that.courtListFileName) &&
                java.util.Objects.equals(this.createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, courtListFileId, courtListFileName, createdTime);
    }

    @Override
    public String toString() {
        return "RecordCourtListExportSuccessful{" +
                "courtCentreId='" + courtCentreId + "'," +
                "courtListFileId='" + courtListFileId + "'," +
                "courtListFileName='" + courtListFileName + "'," +
                "createdTime='" + createdTime + "'" +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private UUID courtListFileId;

        private String courtListFileName;

        private ZonedDateTime createdTime;

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

        public Builder withCreatedTime(final ZonedDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public RecordCourtListExportSuccessful build() {
            return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportSuccessful(courtCentreId, courtListFileId, courtListFileName, createdTime);
        }
    }
}
