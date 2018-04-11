package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

public class Defendant {

    private final UUID id;
    private final UUID personId;
    private final String firstName;
    private final String lastName;
    private final String nationality;
    private final String gender;
    private final Address address;
    private final LocalDate dateOfBirth;
    private final String defenceOrganisation;
    private final Interpreter interpreter;
    private final List<DefendantCase> defendantCases;
    private final List<Offence> offences;

    @JsonCreator
    public Defendant(@JsonProperty("id") final UUID id,
                     @JsonProperty("personId") final UUID personId,
                     @JsonProperty("firstName") final String firstName,
                     @JsonProperty("lastName") final String lastName,
                     @JsonProperty("nationality") final String nationality,
                     @JsonProperty("gender") final String gender,
                     @JsonProperty("address") final Address address,
                     @JsonProperty("dateOfBirth") final LocalDate dateOfBirth,
                     @JsonProperty("defenceOrganisation") final String defenceOrganisation,
                     @JsonProperty("interpreter") final Interpreter interpreter,
                     @JsonProperty("defendantCases") final List<DefendantCase> defendantCases,
                     @JsonProperty("offences") final List<Offence> offences) {
        this.id = id;
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.gender = gender;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.defenceOrganisation = defenceOrganisation;
        this.interpreter = interpreter;
        this.defendantCases = defendantCases;
        this.offences = offences;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public List<DefendantCase> getDefendantCases() {
        return defendantCases;
    }

    public List<Offence> getOffences() {
        return offences;
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
        private String defenceOrganisation;
        private Interpreter.Builder interpreter;
        private List<DefendantCase.Builder> defendantCases = new ArrayList<>();
        private List<Offence.Builder> offences = new ArrayList<>();

        private Builder() {

        }

        public UUID getId() {
            return id;
        }

        public UUID getPersonId() {
            return personId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getNationality() {
            return nationality;
        }

        public String getGender() {
            return gender;
        }

        public Address.Builder getAddress() {
            return address;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public String getDefenceOrganisation() {
            return defenceOrganisation;
        }

        public Interpreter.Builder getInterpreter() {
            return interpreter;
        }

        public List<Offence.Builder> getOffences() {
            return offences;
        }

        public List<DefendantCase.Builder> getDefendantCases() {
            return defendantCases;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withNationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withAddress(Address.Builder address) {
            this.address = address;
            return this;
        }

        public Builder withDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withDefenceOrganisation(String defenceOrganisation) {
            this.defenceOrganisation = defenceOrganisation;
            return this;
        }

        public Builder withInterpreter(Interpreter.Builder interpreter) {
            this.interpreter = interpreter;
            return this;
        }

        public Builder addDefendantCase(DefendantCase.Builder defendantCase) {
            this.defendantCases.add(defendantCase);
            return this;
        }

        public Builder addOffence(Offence.Builder offence) {
            this.offences.add(offence);
            return this;
        }

        public Defendant build() {
            return new Defendant(id, personId, firstName, lastName, nationality, gender,
                    ofNullable(address).map(Address.Builder::build).orElse(null),
                    dateOfBirth, defenceOrganisation,
                    ofNullable(interpreter).map(Interpreter.Builder::build).orElse(null),
                    unmodifiableList(defendantCases.stream().map(DefendantCase.Builder::build).collect(Collectors.toList())),
                    unmodifiableList(offences.stream().map(Offence.Builder::build).collect(Collectors.toList())));
        }
    }

    public static Defendant.Builder builder() {
        return new Builder();
    }

    public static Builder from(Defendant defendant) {
        Builder builder = builder()
                .withId(defendant.getId())
                .withPersonId(defendant.getPersonId())
                .withFirstName(defendant.getFirstName())
                .withLastName(defendant.getLastName())
                .withNationality(defendant.getNationality())
                .withGender(defendant.getGender())
                .withAddress(Address.from(defendant.getAddress()))
                .withDateOfBirth(defendant.getDateOfBirth())
                .withDefenceOrganisation(defendant.getDefenceOrganisation())
                .withInterpreter(Interpreter.from(defendant.getInterpreter()));

        defendant.getDefendantCases().forEach(defendantCase -> builder.addDefendantCase(DefendantCase.from(defendantCase)));

        defendant.getOffences().forEach(offence -> builder.addOffence(Offence.from(offence)));


        return builder;
    }
}