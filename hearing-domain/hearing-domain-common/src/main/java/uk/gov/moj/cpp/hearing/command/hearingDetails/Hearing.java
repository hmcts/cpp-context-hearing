package uk.gov.moj.cpp.hearing.command.hearingDetails;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Hearing {

    private UUID id;

    private String type;

    private UUID courtRoomId;

    private String courtRoomName;

    private Judge judge;

    private List<ZonedDateTime> hearingDays;

    public Hearing() {
    }

    @JsonCreator
    public Hearing(UUID id, String type, UUID courtRoomId, String courtRoomName, Judge judge, List<ZonedDateTime> hearingDays) {
        this.id = id;
        this.type = type;
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
        this.judge = judge;
        this.hearingDays = hearingDays;
    }

    public UUID getId() {
        return id;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public Hearing setType(String type) {
        this.type = type;
        return this;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public Hearing setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public Hearing setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }

    public Judge getJudge() {
        return judge;
    }

    public Hearing setJudge(Judge judge) {
        this.judge = judge;
        return this;
    }

    public List<ZonedDateTime> getHearingDays() {
        return hearingDays;
    }

    public Hearing setHearingDays(List<ZonedDateTime> hearingDays) {
        this.hearingDays = hearingDays;
        return this;
    }

    public static Hearing hearing(){
        return new Hearing();
    }
}


