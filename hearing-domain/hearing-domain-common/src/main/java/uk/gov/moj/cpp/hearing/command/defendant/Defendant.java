package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Address address;

    private final String bailStatus;

    private final ZonedDateTime custodyTimeLimitDate;

    private final LocalDate dateOfBirth;

    private final String defenceOrganisation;

    private final String firstName;

    private final String gender;

    private final UUID id;

    private final Interpreter interpreter;

    private final String lastName;

    private final String nationality;

    private final UUID personId;

    @JsonCreator
    public Defendant(@JsonProperty("id") final UUID id,
                     @JsonProperty("personId") final UUID personId,
                     @JsonProperty("firstName") final String firstName,
                     @JsonProperty("lastName") final String lastName,
                     @JsonProperty("nationality") final String nationality,
                     @JsonProperty("gender") final String gender,
                     @JsonProperty("address") final Address address,
                     @JsonProperty("dateOfBirth") final LocalDate dateOfBirth,
                     @JsonProperty("bailStatus") final String bailStatus,
                     @JsonProperty("custodyTimeLimitDate") final ZonedDateTime custodyTimeLimitDate,
                     @JsonProperty("defenceOrganisation") final String defenceOrganisation,
                     @JsonProperty("interpreter") final Interpreter interpreter) {

        this.id = id;
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.gender = gender;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.bailStatus = bailStatus;
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        this.defenceOrganisation = defenceOrganisation;
        this.interpreter = interpreter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Address getAddress() {
        return address;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public ZonedDateTime getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getGender() {
        return gender;
    }

    public UUID getId() {
        return id;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public UUID getPersonId() {
        return personId;
    }

    public static class Builder {

        private UUID id;

        private UUID personId;

        private String firstName;

        private String lastName;

        private String nationality;

        private String gender;

        private Address.Builder address;

        private LocalDate dateOfBirth;

        private String bailStatus;

        private ZonedDateTime custodyTimeLimitDate;

        private String defenceOrganisation;

        private Interpreter.Builder interpreter;

        private Builder() {
        }

        public Builder withAddress(final Address.Builder address) {
            this.address = address;
            return this;
        }

        public Builder withBailStatus(final String bailStatus) {
            this.bailStatus = bailStatus;
            return this;
        }

        public Builder withCustodyTimeLimitDate(final ZonedDateTime custodyTimeLimitDate) {
            this.custodyTimeLimitDate = custodyTimeLimitDate;
            return this;
        }

        public Builder withDateOfBirth(final LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withDefenceOrganisation(final String defenceOrganisation) {
            this.defenceOrganisation = defenceOrganisation;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withGender(final String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withInterpreter(final Interpreter.Builder interpreter) {
            this.interpreter = interpreter;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withNationality(final String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withPersonId(final UUID personId) {
            this.personId = personId;
            return this;
        }

        public Defendant build() {
            return new Defendant(id,
                    personId,
                    firstName,
                    lastName,
                    nationality,
                    gender,
                    ofNullable(address).map(Address.Builder::build).orElse(null),
                    dateOfBirth,
                    bailStatus,
                    custodyTimeLimitDate,
                    defenceOrganisation,
                    ofNullable(interpreter).map(Interpreter.Builder::build).orElse(null));
        }
    }
}
