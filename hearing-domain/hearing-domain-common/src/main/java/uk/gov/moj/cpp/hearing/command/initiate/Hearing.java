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

    private final UUID id;
    private final String type;
    private final UUID courtCentreId;
    private final String courtCentreName;
    private final UUID courtRoomId;
    private final String courtRoomName;
    private final Judge judge;
    private final ZonedDateTime startDateTime;
    private final List<ZonedDateTime> hearingDays;
    private final Integer estimateMinutes;
    private final List<Defendant> defendants;
    private final List<Witness> witnesses;

    @JsonCreator
    public Hearing(@JsonProperty("id") final UUID id,
                   @JsonProperty("type") final String type,
                   @JsonProperty("courtCentreId") final UUID courtCentreId,
                   @JsonProperty("courtCentreName") final String courtCentreName,
                   @JsonProperty("courtRoomId") final UUID courtRoomId,
                   @JsonProperty("courtRoomName") final String courtRoomName,
                   @JsonProperty("judge") final Judge judge,
                   @JsonProperty("startDateTime") final ZonedDateTime startDateTime,
                   @JsonProperty("hearingDays") final List<ZonedDateTime> hearingDays,
                   @JsonProperty("estimateMinutes") final Integer estimateMinutes,
                   @JsonProperty("defendants") final List<Defendant> defendants,
                   @JsonProperty("witnesses") final List<Witness> witnesses) {
        this.id = id;
        this.type = type;
        this.courtCentreId = courtCentreId;
        this.courtCentreName = courtCentreName;
        this.courtRoomId = courtRoomId;
        this.courtRoomName = courtRoomName;
        this.judge = judge;
        this.startDateTime = startDateTime;
        this.hearingDays = hearingDays;
        this.estimateMinutes = estimateMinutes;
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

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public List<ZonedDateTime> getHearingDays() {
        return hearingDays;
    }

    public Integer getEstimateMinutes() {
        return estimateMinutes;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public  List<Witness> getWitnesses(){
        return witnesses;
    }


    public static class Builder {

        private UUID id;
        private String type;
        private UUID courtCentreId;
        private String courtCentreName;
        private UUID courtRoomId;
        private String courtRoomName;
        private Judge.Builder judge;
        private ZonedDateTime startDateTime;
        private List<ZonedDateTime> hearingDays;
        private Integer estimateMinutes;
        private List<Defendant.Builder> defendants = new ArrayList<>();
        private List<Witness.Builder> witnesses = new ArrayList<>();

        public Builder() {

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

        public Judge.Builder getJudge() {
            return judge;
        }

        public ZonedDateTime getStartDateTime() {
            return startDateTime;
        }

        public Integer getEstimateMinutes() {
            return estimateMinutes;
        }

        public List<Defendant.Builder> getDefendants() {
            return defendants;
        }

        public List<Witness.Builder> getWitnesses(){
            return witnesses;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withCourtCentreId(UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withCourtRoomId(UUID courtRoomId) {
            this.courtRoomId = courtRoomId;
            return this;
        }

        public Builder withCourtRoomName(String courtRoomName) {
            this.courtRoomName = courtRoomName;
            return this;
        }

        public Builder withJudge(Judge.Builder judge) {
            this.judge = judge;
            return this;
        }

        public Builder withStartDateTime(ZonedDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public Builder withHearingDays(List<ZonedDateTime> hearingDays) {
            this.hearingDays = hearingDays;
            return this;
        }

        public Builder withEstimateMinutes(Integer estimateMinutes) {
            this.estimateMinutes = estimateMinutes;
            return this;
        }

        public Builder addDefendant(Defendant.Builder defendant) {
            this.defendants.add(defendant);
            return this;
        }

        public Builder addWitness(Witness.Builder witness){
            this.witnesses.add(witness);
            return this;
        }

        public Hearing build() {
            return new Hearing(id, type, courtCentreId, courtCentreName, courtRoomId, courtRoomName,
                    ofNullable(judge).map(Judge.Builder::build).orElse(null),
                    startDateTime, hearingDays, estimateMinutes,
                    unmodifiableList(defendants.stream().map(Defendant.Builder::build).collect(Collectors.toList())),
                    unmodifiableList(witnesses.stream().map(Witness.Builder::build).collect(Collectors.toList())));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Hearing hearing) {
        Builder builder = builder()
                .withId(hearing.getId())
                .withType(hearing.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withCourtRoomId(hearing.getCourtRoomId())
                .withCourtRoomName(hearing.getCourtRoomName())
                .withJudge(Judge.from(hearing.getJudge()))
                .withStartDateTime(hearing.getStartDateTime())
                .withHearingDays(hearing.getHearingDays())
                .withEstimateMinutes(hearing.getEstimateMinutes());

        hearing.getDefendants().forEach(defendant -> builder.addDefendant(Defendant.from(defendant)));

        hearing.getWitnesses().forEach( witness -> builder.addWitness(Witness.from(witness)));

        return builder;
    }
}



