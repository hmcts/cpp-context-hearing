package uk.gov.moj.cpp.hearing.command.plea;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Optional.ofNullable;

public class Offence implements Serializable {
    private final UUID id;
    private final Plea plea;

    @JsonCreator
    public Offence(@JsonProperty("id") final UUID id,
                   @JsonProperty("plea") final Plea plea) {
        this.id = id;
        this.plea = plea;

    }

    public UUID getId() {
        return id;
    }

    public Plea getPlea() {
        return plea;
    }

    public static class Builder {

        private UUID id;

        private Plea.Builder plea;

        private Builder() {

        }

        public UUID getId() {
            return id;
        }

        public Offence.Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Offence.Builder withPlea(Plea.Builder plea) {
            this.plea = plea;
            return this;
        }

        public Offence build() {
            return new Offence(id, ofNullable(plea).map(Plea.Builder::build).orElse(null)
            );
        }
    }

    public static Offence.Builder builder() {
        return new Offence.Builder();
    }

    public static Offence.Builder from(Offence offence) {
        return builder()
                .withId(offence.getId())
                .withPlea(Plea.from(offence.getPlea()));
    }
}
