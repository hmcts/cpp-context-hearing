package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

//TODO replace hearing.prosecution-counsel-added
@Event("hearing.newprosecution-counsel-added")
public class NewProsecutionCounselAdded {
    private UUID personId;
    private UUID attendeeId;
    private UUID hearingId;
    private String status;
    private String firstName;
    private String lastName;
    private String title;


    public NewProsecutionCounselAdded() {
    }


    private static NewProsecutionCounselAdded create(final Builder builder) {
        NewProsecutionCounselAdded thus = new NewProsecutionCounselAdded();
        thus.attendeeId=builder.attendeeId;
        thus.firstName=builder.firstName;
        thus.lastName=builder.lastName;
        thus.hearingId=builder.hearingId;
        thus.personId=builder.personId;
        thus.status=builder.status;
        thus.title=builder.title;
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
            this.personId=personId;
            return this;
        }
        public Builder withAttendeeId(UUID attendeeId) {
            this.attendeeId=attendeeId;
            return this;
        }
        public Builder withHearingId(UUID hearingId) {
            this.hearingId=hearingId;
            return this;
        }
        public Builder withStatus(String status) {
            this.status=status;
            return this;
        }
        public Builder withFirstName(String firstName) {
            this.firstName=firstName;
            return this;
        }
        public Builder withLastName(String lastName) {
            this.lastName=lastName;
            return this;
        }
        public Builder withTitle(String title) {
            this.title=title;
            return this;
        }

        public NewProsecutionCounselAdded build() {
            return NewProsecutionCounselAdded.create(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
