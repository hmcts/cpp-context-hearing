package uk.gov.moj.cpp.hearing.command.initiate;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private List<Witness> witnesses;

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
                   @JsonProperty("defendants") final List<Defendant> defendants,
                   @JsonProperty("witnesses") final List<Witness> witnesses) {
        this.id = id;
        this.type = type;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
        this.judge = judge;
        this.hearingDays = hearingDays;
        this.defendants = defendants;
        this.witnesses = witnesses;
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

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public Hearing setType(String type) {
        this.type = type;
        return this;
    }

    public Hearing setCourtCentreId(UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
        return this;
    }

    public Hearing setCourtCentreName(String courtCentreName) {
        this.courtCentreName = courtCentreName;
        return this;
    }

    public Hearing setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public Hearing setCourtRoomName(String courtRoomName) {
        this.courtRoomName = courtRoomName;
        return this;
    }

    public Hearing setJudge(Judge judge) {
        this.judge = judge;
        return this;
    }

    public Hearing setHearingDays(List<ZonedDateTime> hearingDays) {
        this.hearingDays = new ArrayList<>(hearingDays);
        return this;
    }

    public Hearing setWitnesses(List<Witness> witnesses) {
        this.witnesses = new ArrayList<>(witnesses);
        return this;
    }

    public Hearing setDefendants(List<Defendant> defendants) {
        this.defendants = new ArrayList<>(defendants);
        return this;
    }

    public static Hearing hearing() {
        return new Hearing();
    }
}



