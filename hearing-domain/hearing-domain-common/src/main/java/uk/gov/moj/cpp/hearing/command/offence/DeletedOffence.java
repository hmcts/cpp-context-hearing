package uk.gov.moj.cpp.hearing.command.offence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DeletedOffence {
    private UUID id;

    @JsonCreator
    public DeletedOffence(@JsonProperty("id") UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public static DeletedOffence.Builder builder() {
        return new DeletedOffence.Builder();
    }

    public static class Builder {
        private UUID id;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public DeletedOffence build() {
            return new DeletedOffence(this.id);
        }
    }
}
