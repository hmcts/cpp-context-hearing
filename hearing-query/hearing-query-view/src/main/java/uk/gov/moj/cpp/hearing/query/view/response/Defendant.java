package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class Defendant {

    private UUID id;

    private String name;

    @JsonCreator
    public Defendant(@JsonProperty(value = "id") final UUID id,
                     @JsonProperty(value = "name") final String name) {
        this.id = id;
        this.name = name;
    }

    public Defendant(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static class Builder {

        private UUID id;

        private String name;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Defendant build() {
            return new Defendant(this);
        }
    }
}
