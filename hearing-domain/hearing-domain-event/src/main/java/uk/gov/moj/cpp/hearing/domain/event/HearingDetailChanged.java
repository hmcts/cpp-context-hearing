package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.detail-changed")
public class HearingDetailChanged implements Serializable {

    private static final long serialVersionUID = 68006294540313163L;
    private final UUID id;
    private final String type;
    private final UUID courtRoomId;
    private final String courtRoomName;
    private final Judge judge;
    private final List<ZonedDateTime> hearingDays;

    @JsonCreator
    public HearingDetailChanged(@JsonProperty("id") final UUID id,
                                @JsonProperty("type") final String type,
                                @JsonProperty("courtRoomId") final UUID courtRoomId,
                                @JsonProperty("courtRoomName") final String courtRoomName,
                                @JsonProperty("judge") final Judge judge,
                                @JsonProperty("hearingDays") final List<ZonedDateTime> hearingDays) {
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

    public String getType() {
        return type;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public String getCourtRoomName() {
        return courtRoomName;
    }

    public Judge getJudge() {
        return judge;
    }

    public List<ZonedDateTime> getHearingDays() {
        return hearingDays;
    }
}
