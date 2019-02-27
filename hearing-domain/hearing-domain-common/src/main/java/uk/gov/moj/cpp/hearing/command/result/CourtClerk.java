package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CourtClerk implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String firstName;

    private String lastName;

    public CourtClerk() {
    }

    @JsonCreator
    private CourtClerk(@JsonProperty("id") final UUID id,
                       @JsonProperty("firstName") final String firstName,
                       @JsonProperty("lastName") final String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static final class Builder {

        private UUID id;

        private String firstName;

        private String lastName;

        public Builder withId(final UUID id) {
            this.id = id;
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

        public CourtClerk build() {
            return new CourtClerk(id, firstName, lastName);
        }
    }
}