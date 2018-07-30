package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private Address address;

    private String bailStatus;

    private LocalDate custodyTimeLimitDate;

    private LocalDate dateOfBirth;

    private String defenceOrganisation;

    private String firstName;

    private String gender;

    private UUID id;

    private Interpreter interpreter;

    private String lastName;

    private String nationality;

    private List<Offence> offences;

    private UUID personId;

    public Address getAddress() {
        return address;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public LocalDate getCustodyTimeLimitDate() {
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

    public List<Offence> getOffences() {
        return offences;
    }

    public UUID getPersonId() {
        return personId;
    }

    public static Builder defendants() {
        return new Defendant.Builder();
    }

    public static class Builder {
        private Address address;

        private String bailStatus;

        private LocalDate custodyTimeLimitDate;

        private LocalDate dateOfBirth;

        private String defenceOrganisation;

        private String firstName;

        private String gender;

        private UUID id;

        private Interpreter interpreter;

        private String lastName;

        private String nationality;

        private List<Offence> offences;

        private UUID personId;

        public Builder withAddress(final Address address) {
            this.address = address;
            return this;
        }

        public Builder withBailStatus(final String bailStatus) {
            this.bailStatus = bailStatus;
            return this;
        }

        public Builder withCustodyTimeLimitDate(final LocalDate custodyTimeLimitDate) {
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

        public Builder withInterpreter(final Interpreter interpreter) {
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

        public Builder withOffences(final List<Offence> offences) {
            this.offences = offences;
            return this;
        }

        public Builder withPersonId(final UUID personId) {
            this.personId = personId;
            return this;
        }

        public Defendant build() {
            final Defendant defendants = new Defendant();
            defendants.address = address;
            defendants.bailStatus = bailStatus;
            defendants.custodyTimeLimitDate = custodyTimeLimitDate;
            defendants.dateOfBirth = dateOfBirth;
            defendants.defenceOrganisation = defenceOrganisation;
            defendants.firstName = firstName;
            defendants.gender = gender;
            defendants.id = id;
            defendants.interpreter = interpreter;
            defendants.lastName = lastName;
            defendants.nationality = nationality;
            defendants.offences = offences;
            defendants.personId = personId;
            return defendants;
        }
    }
    public static Builder builder(){
        return new Builder();
    }
}
