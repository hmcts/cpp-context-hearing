package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final Person person;

    private final String bailStatus;

    private final LocalDate custodyTimeLimitDate;

    private final String defenceOrganisation;

    private final Interpreter interpreter;

    @JsonCreator
    public Defendant(@JsonProperty("id") final UUID id,
                     @JsonProperty("person") final Person person,
                     @JsonProperty("bailStatus") final String bailStatus,
                     @JsonProperty("custodyTimeLimitDate") final LocalDate custodyTimeLimitDate,
                     @JsonProperty("defenceOrganisation") final String defenceOrganisation,
                     @JsonProperty("interpreter") final Interpreter interpreter) {

        this.id = id;
        this.person = person;
        this.bailStatus = bailStatus;
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        this.defenceOrganisation = defenceOrganisation;
        this.interpreter = interpreter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Defendant defendant) {
        return Defendant.builder()
                .withId(defendant.getId())
                .withPerson(Person.builder(defendant.getPerson()))
                .withBailStatus(defendant.getBailStatus())
                .withCustodyTimeLimitDate(defendant.getCustodyTimeLimitDate())
                .withDefenceOrganisation(defendant.getDefenceOrganisation())
                .withInterpreter(Interpreter.builder(defendant.getInterpreter().getLanguage()));
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public LocalDate getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public String getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public UUID getId() {
        return id;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Person getPerson() {
        return person;
    }

    public static class Builder {

        private UUID id;

        private Person.Builder person;

        private String bailStatus;

        private LocalDate custodyTimeLimitDate;

        private String defenceOrganisation;

        private Interpreter.Builder interpreter;

        private Builder() {
        }

        public Builder withBailStatus(final String bailStatus) {
            this.bailStatus = bailStatus;
            return this;
        }

        public Builder withCustodyTimeLimitDate(final LocalDate custodyTimeLimitDate) {
            this.custodyTimeLimitDate = custodyTimeLimitDate;
            return this;
        }

        public Builder withDefenceOrganisation(final String defenceOrganisation) {
            this.defenceOrganisation = defenceOrganisation;
            return this;
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withPerson(final Person.Builder person) {
            this.person = person;
            return this;
        }

        public Builder withInterpreter(final Interpreter.Builder interpreter) {
            this.interpreter = interpreter;
            return this;
        }

        public Defendant build() {
            return new Defendant(id,
                    ofNullable(person).map(Person.Builder::build).orElse(null),
                    bailStatus,
                    custodyTimeLimitDate,
                    defenceOrganisation,
                    ofNullable(interpreter).map(Interpreter.Builder::build).orElse(null));
        }
    }
}
