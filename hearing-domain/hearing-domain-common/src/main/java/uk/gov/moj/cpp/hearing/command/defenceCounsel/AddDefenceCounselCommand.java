package uk.gov.moj.cpp.hearing.command.defenceCounsel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddDefenceCounselCommand {

    private UUID personId;
    private UUID attendeeId;
    private UUID hearingId;
    private String status;
    private String firstName;
    private String lastName;
    private String title;

    private List<DefendantId> defendantIds;

    @JsonCreator
    public AddDefenceCounselCommand(
            @JsonProperty("personId") UUID personId,
            @JsonProperty("attendeeId") UUID attendeeId,
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("status") String status,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("title") String title,
            @JsonProperty("defendantIds") List<DefendantId> defendantIds) {
        this.personId = personId;
        this.attendeeId = attendeeId;
        this.hearingId = hearingId;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.defendantIds = defendantIds;
    }

    @JsonIgnore
    private AddDefenceCounselCommand(Builder builder) {
        this.personId = builder.personId;
        this.attendeeId = builder.attendeeId;
        this.hearingId = builder.hearingId;
        this.status = builder.status;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.title = builder.title;
        this.defendantIds = builder.defendantIds.stream().map(DefendantId.Builder::build).collect(Collectors.toList());
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getStatus() {
        return status;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public List<DefendantId> getDefendantIds() {
        return defendantIds;
    }

    public AddDefenceCounselCommand withStatus(String status) {
        this.status = status;
        return this;
    }

    public AddDefenceCounselCommand withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public AddDefenceCounselCommand withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public AddDefenceCounselCommand withTitle(String title) {
        this.title = title;
        return this;
    }

    public static class Builder {
        private UUID personId;
        private UUID attendeeId;
        private UUID hearingId;
        private String status;
        private String firstName;
        private String lastName;
        private String title;
        private List<DefendantId.Builder> defendantIds = new ArrayList<>();

        public Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withAttendeeId(UUID attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder addDefendantId(DefendantId.Builder defendantId) {
            this.defendantIds.add(defendantId);
            return this;
        }

        public AddDefenceCounselCommand build() {
            return new AddDefenceCounselCommand(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
