package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.justice.domain.annotation.Event;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.unmodifiableList;

@Event("hearing.newdefence-counsel-added")
public class NewDefenceCounselAdded {

    private UUID personId;
    private UUID attendeeId;
    private List<UUID> defendantIds;
    private UUID hearingId;
    private String status;
    private String title;
    private String firstName;
    private String lastName;

    @JsonCreator
    public NewDefenceCounselAdded() {
        // default constructor for Jackson serialisation
    }

    private static NewDefenceCounselAdded build(Builder builder) {
        NewDefenceCounselAdded thus = new NewDefenceCounselAdded();
        thus.attendeeId = builder.attendeeId;
        thus.hearingId = builder.hearingId;
        thus.status = builder.status;
        thus.defendantIds = builder.defendantIds;
        thus.firstName = builder.firstName;
        thus.lastName = builder.lastName;
        thus.title = builder.title;
        thus.personId = builder.personId;
        return thus;
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

    public List<UUID> getDefendantIds() {
        return unmodifiableList(defendantIds);
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static class Builder {
        private UUID personId;
        private UUID attendeeId;
        private List<UUID> defendantIds;
        private UUID hearingId;
        private String status;
        private String title;
        private String firstName;
        private String lastName;

        public Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withAttendeeId(UUID attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public Builder withDefendantIds(List<UUID> defendantIds) {
            this.defendantIds = defendantIds;
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

        public NewDefenceCounselAdded build() {
            return NewDefenceCounselAdded.build(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}

