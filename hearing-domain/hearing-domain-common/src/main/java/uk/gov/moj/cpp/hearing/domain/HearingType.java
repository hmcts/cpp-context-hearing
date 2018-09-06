package uk.gov.moj.cpp.hearing.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class HearingType implements Serializable {
    private static final long serialVersionUID = -8860386746131633120L;

    private final String description;

    private final UUID id;

    @JsonCreator
    public HearingType(@JsonProperty("description") final String description,
            @JsonProperty("id") final UUID id) {
        this.description = description;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public UUID getId() {
        return id;
    }

    public static Builder hearingType() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HearingType that = (HearingType) obj;

        return java.util.Objects.equals(this.description, that.description)
                && java.util.Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(description, id);
    }

    @Override
    public String toString() {
        return "HearingType{" + "description='" + description + "'," + "id='" + id + "'" + "}";
    }

    public static class Builder {
        private String description;

        private UUID id;

        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public HearingType build() {
            return new HearingType(description, id);
        }
    }
}