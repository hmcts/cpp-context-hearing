package uk.gov.moj.cpp.hearing.command.verdict;


import static java.util.Collections.unmodifiableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final UUID personId;
    private final List<Offence> offences;

    @JsonCreator
    public Defendant(@JsonProperty("id") final UUID id,
                     @JsonProperty("personId") final UUID personId,
                     @JsonProperty("offences") final List<Offence> offences) {
        this.id = id;
        this.personId = personId;
        this.offences = (null == offences) ? new ArrayList<>() : new ArrayList<>(offences);

    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getId() {
        return id;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public static class Builder {

        private UUID id;
        private UUID personId;
        private List<Offence.Builder> offences = new ArrayList<>();

        private Builder() {

        }

        public UUID getId() {
            return id;
        }

        public UUID getPersonId() {
            return personId;
        }


        public List<Offence.Builder> getOffences() {
            return offences;
        }

        public Defendant.Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Defendant.Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Defendant.Builder addOffence(Offence.Builder offence) {
            this.offences.add(offence);
            return this;
        }

        public Defendant build() {
            return new Defendant(id, personId,
                    unmodifiableList(offences.stream().map(Offence.Builder::build).collect(Collectors.toList())));
        }
    }

    public static Defendant.Builder builder() {
        return new Defendant.Builder();
    }

    public static Defendant.Builder from(Defendant defendant) {
        Defendant.Builder builder = builder()
                .withId(defendant.getId())
                .withPersonId(defendant.getPersonId());

        defendant.getOffences().forEach(offence -> builder.addOffence(Offence.from(offence)));

        return builder;
    }
}
