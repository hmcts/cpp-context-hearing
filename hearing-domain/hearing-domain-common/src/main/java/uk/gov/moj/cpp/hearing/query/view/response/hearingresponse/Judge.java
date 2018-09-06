package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Judge {

    private String id;
    private String title;
    private String firstName;
    private String lastName;

    @JsonCreator
    public Judge(@JsonProperty("id") final String id,
                 @JsonProperty("title") final String title,
                 @JsonProperty("firstName") final String firstName,
                 @JsonProperty("lastName") final String lastName) {
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Judge() {
    }

    private Judge(final Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
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

    public static final class Builder {

        private String id;
        private String title;
        private String firstName;
        private String lastName;

        public Builder withId(final String id) {
            this.id = id;
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

        public Judge build() {
            return new Judge(this);
        }
    }
}
