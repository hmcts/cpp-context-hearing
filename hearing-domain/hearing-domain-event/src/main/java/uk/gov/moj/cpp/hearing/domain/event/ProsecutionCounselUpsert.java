package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.newprosecution-counsel-added")
public class ProsecutionCounselUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID personId;
    private UUID attendeeId;
    private UUID hearingId;
    private String status;
    private String firstName;
    private String lastName;
    private String title;

    @JsonCreator
    protected ProsecutionCounselUpsert(@JsonProperty("personId") final UUID personId,
                                       @JsonProperty("attendeeId") final UUID attendeeId,
                                       @JsonProperty("hearingId") final UUID hearingId,
                                       @JsonProperty("status") final String status,
                                       @JsonProperty("title") final String title,
                                       @JsonProperty("firstName") final String firstName,
                                       @JsonProperty("lastName") final String lastName){
        this.personId = personId;
        this.attendeeId = attendeeId;
        this.hearingId = hearingId;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
    }


    @JsonIgnore
    private ProsecutionCounselUpsert(Builder builder) {
        this.attendeeId = builder.attendeeId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.hearingId = builder.hearingId;
        this.personId = builder.personId;
        this.status = builder.status;
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

        public ProsecutionCounselUpsert build() {
            return new ProsecutionCounselUpsert(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
