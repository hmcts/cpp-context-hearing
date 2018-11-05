package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Hearing implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String type;
    private UUID courtCentreId;
    private String courtCentreName;
    private UUID courtRoomId;
    private String courtRoomName;
    private Judge judge;
    private List<ZonedDateTime> hearingDays;
    private List<Defendant> defendants;


    public Hearing() {
    }

    @JsonCreator
    public Hearing(@JsonProperty("id") final UUID id,
                   @JsonProperty("type") final String type,
                   @JsonProperty("courtCentreId") final UUID courtCentreId,
                   @JsonProperty("courtCentreName") final String courtCentreName,
                   @JsonProperty("courtRoomId") final UUID courtRoomId,
                   @JsonProperty("courtRoomName") final String courtRoomName,
                   @JsonProperty("judge") final Judge judge,
                   @JsonProperty("hearingDays") final List<ZonedDateTime> hearingDays,
                    @JsonProperty("defendants") final List<Defendant> defendants) {
        this.id = id;
        this.type = type;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
        this.judge = judge;
        this.hearingDays = hearingDays;
        this.defendants = defendants;
    }


    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
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

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public Hearing setId(final UUID id) {
        this.id = id;
        return this;
    }

    public Hearing setType(final String type) {
        this.type = type;
        return this;
    }

    public Hearing setCourtCentreId(final UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public Hearing setCourtCentreName(final String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public Hearing setCourtRoomId(final UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public Hearing setCourtRoomName(final String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }

    public Hearing setJudge(final Judge judge) {
        this.judge = judge;
        return this;
    }

    public Hearing setHearingDays(final List<ZonedDateTime> hearingDays) {
        this.hearingDays = new ArrayList<>(hearingDays);
        return this;
    }

    public Hearing setDefendants(final List<Defendant> defendants) {
        this.defendants = new ArrayList<>(defendants);
        return this;
    }

    public static Hearing hearing() {
        return new Hearing();
    }
}



