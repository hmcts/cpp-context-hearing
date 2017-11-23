package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.room-booked")
public class RoomBooked {

    private UUID hearingId;
    private UUID roomId;
    private final String roomName;

    public RoomBooked(final UUID hearingId, final String roomName) {
        this.hearingId = hearingId;
        this.roomName = roomName;
    }

    public RoomBooked(final UUID hearingId, final UUID roomId, final String roomName) {
        this.hearingId = hearingId;
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

}
