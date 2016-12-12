package uk.gov.moj.cpp.hearing.domain.command;


import java.util.UUID;

public class AllocateCourt {
    private UUID hearingId;
    private String courtCentreName;

    public AllocateCourt(UUID hearingId, String courtCentreName) {
        this.hearingId = hearingId;
        this.courtCentreName = courtCentreName;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }
}
