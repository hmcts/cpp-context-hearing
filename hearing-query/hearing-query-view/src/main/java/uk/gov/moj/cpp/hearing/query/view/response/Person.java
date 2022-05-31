package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class Person {

    private final UUID id;

    private final String name;

    private final UUID masterDefendantId;

    @JsonCreator
    public Person(@JsonProperty(value = "id") final UUID id,
                  @JsonProperty(value = "name") final String name,
                  @JsonProperty(value = "masterDefendantId") final UUID masterDefendantId) {
        this.id = id;
        this.name = name;
        this.masterDefendantId = masterDefendantId;
    }

    public Person(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.masterDefendantId = builder.masterDefendantId;
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

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public static class Builder {

        private UUID id;

        private String name;

        private UUID masterDefendantId;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withMasterDefendantId(final UUID masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }
}
