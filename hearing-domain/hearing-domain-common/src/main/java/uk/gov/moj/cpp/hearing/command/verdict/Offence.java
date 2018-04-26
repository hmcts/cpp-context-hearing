package uk.gov.moj.cpp.hearing.command.verdict;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Offence implements Serializable {
    private final UUID id;
    private final Verdict verdict;

    @JsonCreator
    public Offence(@JsonProperty("id") final UUID id,
                   @JsonProperty("verdict") final Verdict verdict) {
        this.id = id;
        this.verdict = verdict;
    }

    public UUID getId() {
        return id;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public static class Builder {

        private UUID id;

        private Verdict.Builder verdict;

        private Builder() {

        }

        public UUID getId() {
            return id;
        }

        public Offence.Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Offence.Builder withVerdict(Verdict.Builder verdict) {
            this.verdict = verdict;
            return this;
        }

        public Verdict.Builder getVerdict() {
            return verdict;
        }

        public Offence build() {
            return new Offence(id, ofNullable(verdict).map(Verdict.Builder::build).orElse(null)
            );
        }
    }

    public static Offence.Builder builder() {
        return new Offence.Builder();
    }

    public static Offence.Builder from(Offence offence) {
        return builder()
                .withId(offence.getId())
                .withVerdict(Verdict.from(offence.getVerdict()));
    }
}
