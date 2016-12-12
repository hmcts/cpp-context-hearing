package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.room-booked")
public class RoomBooked {

    private UUID hearingId;

    private String roomName;

    public RoomBooked(UUID hearingId, String roomName) {
        this.hearingId = hearingId;
        this.roomName = roomName;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public String getRoomName() {
        return roomName;
    }

}
