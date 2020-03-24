package uk.gov.moj.cpp.hearing.command.courtlistpublishstatus;

import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings({"squid:S1067"})
public class RecordCourtListExportFailed {
    private final UUID courtCentreId;

    private final String courtListFileName;

    private final String errorMessage;

    private final ZonedDateTime createdTime;


    public RecordCourtListExportFailed(final UUID courtCentreId, final String courtListFileName, final String errorMessage, final ZonedDateTime createdTime) {
        this.courtCentreId = courtCentreId;
        this.courtListFileName = courtListFileName;
        this.errorMessage = errorMessage;
        this.createdTime = createdTime;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
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

    public static Builder recordCourtListExportFailed() {
        return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportFailed.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportFailed that = (uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportFailed) obj;

        return java.util.Objects.equals(this.courtCentreId, that.courtCentreId) &&
                java.util.Objects.equals(this.courtListFileName, that.courtListFileName) &&
                java.util.Objects.equals(this.errorMessage, that.errorMessage) &&
                java.util.Objects.equals(this.createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(courtCentreId, courtListFileName, errorMessage, createdTime);
    }

    @Override
    public String toString() {
        return "RecordCourtListExportFailed{" +
                "courtCentreId='" + courtCentreId + "'," +
                "courtListFileName='" + courtListFileName + "'," +
                "errorMessage='" + errorMessage + "'," +
                "createdTime='" + createdTime + "'," +
                "}";
    }

    public static class Builder {
        private UUID courtCentreId;

        private String courtListFileName;

        private String errorMessage;

        private ZonedDateTime createdTime;


        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
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

        public RecordCourtListExportFailed build() {
            return new uk.gov.moj.cpp.hearing.command.courtlistpublishstatus.RecordCourtListExportFailed(courtCentreId, courtListFileName, errorMessage, createdTime);
        }
    }
}
