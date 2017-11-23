package uk.gov.moj.cpp.hearing.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingDetails {

    private final UUID hearingId;
    private final ZonedDateTime startDateTime;
    private final int duration;
    private final String hearingType;
    private final UUID courtCentreId;
    private final String courtCentreName;
    private final UUID roomId;
    private final String roomName;
    private final UUID caseId;

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public int getDuration() {
        return duration;
    }

    public String getHearingType() {
        return hearingType;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public UUID getCaseId() {
        return caseId;
    }

    private HearingDetails(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.startDateTime = builder.startDateTime;
        this.duration = builder.duration;
        this.hearingType = builder.hearingType;
        this.courtCentreId = builder.courtCentreId;
        this.courtCentreName = builder.courtCentreName;
        this.roomId = builder.roomId;
        this.roomName = builder.roomName;
        this.caseId = builder.caseId;
    }


    public static class Builder {
        private UUID hearingId;
        private ZonedDateTime startDateTime;
        private int duration;
        private String hearingType;
        private UUID courtCentreId;
        private String courtCentreName;
        private UUID roomId;
        private String roomName;
        private UUID caseId;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withStartDateTime(final ZonedDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public Builder withDuration(final int duration) {
            this.duration = duration;
            return this;
        }

        public Builder withHearingType(final String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtCentreName(final String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withRoomId(final UUID roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder withRoomName(final String roomName) {
            this.roomName = roomName;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public HearingDetails build() {
            return new HearingDetails(this);
        }
    }
}
