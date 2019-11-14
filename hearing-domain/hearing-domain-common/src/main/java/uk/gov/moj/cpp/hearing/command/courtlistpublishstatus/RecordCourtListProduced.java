package uk.gov.moj.cpp.hearing.command.courtlistpublishstatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RecordCourtListProduced {

    private UUID courtCentreId;
    private UUID courtListFileId;
    private String courtListFileName;
    private ZonedDateTime createdTime;


    public RecordCourtListProduced(final UUID courtCentreId, final UUID courtListFileId, final String courtListFileName, final ZonedDateTime createdTime) {
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

    @Override
    public String toString() {
        return "RecordCourtListProduced{" +
                "courtCentreId='" + courtCentreId + "'," +
                "courtListFileId='" + courtListFileId + "'," +
                "courtListFileName='" + courtListFileName + "'," +
                "createdTime='" + createdTime + "'," +
                "}";
    }
}
