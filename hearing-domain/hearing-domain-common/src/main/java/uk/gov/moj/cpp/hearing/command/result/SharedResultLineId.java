package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S1700"})
public class SharedResultLineId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID sharedResultLineId;

    @JsonCreator
    public SharedResultLineId(@JsonProperty("sharedResultLineId") UUID sharedResultLineId) {
        this.sharedResultLineId = sharedResultLineId;
    }

    @JsonIgnore
    private SharedResultLineId(Builder builder) {
        this.sharedResultLineId = builder.sharedResultLineId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getSharedResultLineId() {
        return sharedResultLineId;
    }

    public static class Builder {
        private UUID sharedResultLineId;

        public Builder withSharedResultLineId(UUID sharedResultLineId) {
            this.sharedResultLineId = sharedResultLineId;
            return this;
        }

        public SharedResultLineId build() {
            return new SharedResultLineId(this);
        }
    }
}
