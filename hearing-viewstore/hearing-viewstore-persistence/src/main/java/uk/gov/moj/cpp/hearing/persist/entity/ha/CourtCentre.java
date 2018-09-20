package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@SuppressWarnings("squid:S1067")
@Embeddable
public class CourtCentre {

    @Column(name = "court_centre_id")
    private UUID id;

    @Column(name = "court_centre_name")
    private String name;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "welsh_name")
    private String welshName;

    @Column(name = "welsh_room_name")
    private String welshRoomName;

    public CourtCentre() {
        //For JPA
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getWelshName() {
        return welshName;
    }

    public void setWelshName(String welshName) {
        this.welshName = welshName;
    }

    public String getWelshRoomName() {
        return welshRoomName;
    }

    public void setWelshRoomName(String welshRoomName) {
        this.welshRoomName = welshRoomName;
    }
}
