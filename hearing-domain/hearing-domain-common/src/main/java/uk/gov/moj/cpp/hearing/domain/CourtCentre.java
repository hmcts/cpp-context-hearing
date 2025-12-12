package uk.gov.moj.cpp.hearing.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S1067"})
public class CourtCentre implements Serializable {

    private static final long serialVersionUID = 6636124711374673263L;

    private final UUID id;
    private final String name;
    private final UUID roomId;
    private final String roomName;
    private final String welshName;
    private final String welshRoomName;

    @JsonCreator
    public CourtCentre(@JsonProperty("id") final UUID id,
                       @JsonProperty("name") final String name,
                       @JsonProperty("roomId") final UUID roomId,
                       @JsonProperty("roomName") final String roomName,
                       @JsonProperty("welshName") final String welshName,
                       @JsonProperty("welshRoomName") final String welshRoomName) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
        this.roomName = roomName;
        this.welshName = welshName;
        this.welshRoomName = welshRoomName;
    }

    public static Builder courtCentre() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getWelshName() {
        return welshName;
    }

    public String getWelshRoomName() {
        return welshRoomName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CourtCentre that = (CourtCentre) obj;

        return Objects.equals(this.id, that.id)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.roomId, that.roomId)
                && Objects.equals(this.roomName, that.roomName)
                && Objects.equals(this.welshName, that.welshName)
                && Objects.equals(this.welshRoomName, that.welshRoomName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, roomId, roomName, welshName, welshRoomName);
    }

    @Override
    public String toString() {
        return "CourtCentre{" + "id='" + id + "'," + "name='" + name + "'," + "roomId='" + roomId + "'," + "roomName='"
                + roomName + "'," + "welshName='" + welshName + "'," + "welshRoomName='" + welshRoomName + "'" + "}";
    }

    public static class Builder {

        private UUID id;
        private String name;
        private UUID roomId;
        private String roomName;
        private String welshName;
        private String welshRoomName;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
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

        public Builder withWelshName(final String welshName) {
            this.welshName = welshName;
            return this;
        }

        public Builder withWelshRoomName(final String welshRoomName) {
            this.welshRoomName = welshRoomName;
            return this;
        }

        public CourtCentre build() {
            return new CourtCentre(id, name, roomId, roomName, welshName, welshRoomName);
        }
    }
}
