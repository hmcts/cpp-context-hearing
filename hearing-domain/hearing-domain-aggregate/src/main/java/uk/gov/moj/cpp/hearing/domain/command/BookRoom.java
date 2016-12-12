package uk.gov.moj.cpp.hearing.domain.command;

import java.util.UUID;


public class BookRoom {
    private UUID hearingId;
    private String roomName;

    public BookRoom(UUID hearingId, String roomName) {
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
