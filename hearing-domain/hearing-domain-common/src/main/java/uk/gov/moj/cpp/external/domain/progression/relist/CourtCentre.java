package uk.gov.moj.cpp.external.domain.progression.relist;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CourtCentre implements Serializable {

    private static final long serialVersionUID = -5659002390777022184L;

    private UUID id;
    private UUID roomId;

    public CourtCentre() {
    }

    public CourtCentre(@JsonProperty(value = "id") final UUID id, @JsonProperty(value = "roomId") final UUID roomId) {
        this.id = id;
        this.roomId = roomId;
    }

    public UUID getId() {
        return id;
    }

    public CourtCentre setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public CourtCentre setRoomId(UUID roomId) {
        this.roomId = roomId;
        return this;
    }

    public static CourtCentre courtCentre() {
        return new CourtCentre();
    }
}
