package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Judge {

    private final UUID id;
    private final String title;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public Judge(@JsonProperty("id") final UUID id,
                 @JsonProperty("title") final String title,
                 @JsonProperty("firstName") final String firstName,
                 @JsonProperty("lastName") final String lastName) {
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
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

        private UUID id;
        private String title;
        private String firstName;
        private String lastName;

        private Builder() {

        }

        public UUID getId() {
            return id;
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

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
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

        public Judge build() {
            return new Judge(id, title, firstName, lastName);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Judge judge) {
        return builder()
                .withId(judge.getId())
                .withFirstName(judge.getFirstName())
                .withLastName(judge.getLastName())
                .withTitle(judge.getTitle());

    }
}
