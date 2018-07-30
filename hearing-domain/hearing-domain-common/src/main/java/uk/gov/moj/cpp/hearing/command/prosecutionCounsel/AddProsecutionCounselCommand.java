package uk.gov.moj.cpp.hearing.command.prosecutionCounsel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class AddProsecutionCounselCommand {

    private UUID personId;
    private UUID attendeeId;
    private UUID hearingId;
    private String status;
    private String firstName;
    private String lastName;
    private String title;
    private List<String> hearingDates;

    @JsonCreator
    public AddProsecutionCounselCommand(
            @JsonProperty("personId") UUID personId,
            @JsonProperty("attendeeId") UUID attendeeId,
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("status") String status,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("title") String title,
            @JsonProperty("hearingDates") List<String> hearingDates) {
        this.personId = personId;
        this.attendeeId = attendeeId;
        this.hearingId = hearingId;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.hearingDates = hearingDates;
    }

    @JsonIgnore
    private AddProsecutionCounselCommand(Builder builder) {
        this.personId = builder.personId;
        this.attendeeId = builder.attendeeId;
        this.hearingId = builder.hearingId;
        this.status = builder.status;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.title = builder.title;
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

    public List<String> getHearingDates() {
        return hearingDates;
    }

    public void setHearingDates(List<String> hearingDates) {
        this.hearingDates = hearingDates;
    }

    public AddProsecutionCounselCommand setStatus(String status) {
        this.status = status;
        return this;
    }

    public AddProsecutionCounselCommand setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public AddProsecutionCounselCommand setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public AddProsecutionCounselCommand setTitle(String title) {
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

        public AddProsecutionCounselCommand build() {
            return new AddProsecutionCounselCommand(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
