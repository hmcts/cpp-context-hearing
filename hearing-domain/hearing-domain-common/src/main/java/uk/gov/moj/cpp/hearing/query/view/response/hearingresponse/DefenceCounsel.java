package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.time.LocalDate;
import java.util.Set;

public final class DefenceCounsel {

    private final String attendeeId;
    private final String status;
    private final String defendantId;
    private final String title;
    private final String firstName;
    private final String lastName;
    private final Set<LocalDate> hearingDates;

    private DefenceCounsel(final Builder builder) {
        this.attendeeId = builder.attendeeId;
        this.status = builder.status;
        this.defendantId = builder.defendantId;
        this.title = builder.title;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.hearingDates = builder.hearingDates;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public String getStatus() {
        return status;
    }

    public String getDefendantId() {
        return defendantId;
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

    public Set<LocalDate> getHearingDates() {
        return hearingDates;
    }

    public static final class Builder {

        private String attendeeId;
        private String status;
        private String defendantId;
        private String title;
        private String firstName;
        private String lastName;
        private Set<LocalDate> hearingDates;

        public Builder withAttendeeId(final String attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }

        public Builder withStatus(final String status) {
            this.status = status;
            return this;
        }

        public Builder withDefendantId(final String defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withHearingDates(final Set<LocalDate> hearingDates) {
            this.hearingDates = hearingDates;
            return this;
        }

        public DefenceCounsel build() {
            return new DefenceCounsel(this);
        }
    }
}