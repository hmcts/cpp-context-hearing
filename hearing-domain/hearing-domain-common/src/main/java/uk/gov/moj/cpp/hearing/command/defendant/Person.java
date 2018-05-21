package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final String title;

    private final String firstName;

    private final String lastName;

    private final LocalDate dateOfBirth;

    private final String nationality;

    private final String gender;

    private final String homeTelephone;

    private final String workTelephone;

    private final String mobile;

    private final String fax;

    private final String email;

    private final Address address;

    @JsonCreator
    public Person(@JsonProperty("id") final UUID id,
                  @JsonProperty("title") final String title,
                  @JsonProperty("firstName") final String firstName,
                  @JsonProperty("lastName") final String lastName,
                  @JsonProperty("dateOfBirth") final LocalDate dateOfBirth,
                  @JsonProperty("nationality") final String nationality,
                  @JsonProperty("gender") final String gender,
                  @JsonProperty("homeTelephone") final String homeTelephone,
                  @JsonProperty("workTelephone") final String workTelephone,
                  @JsonProperty("mobile") final String mobile,
                  @JsonProperty("fax") final String fax,
                  @JsonProperty("email") final String email,
                  @JsonProperty("address") final Address address) {
        this.id = id;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.gender = gender;
        this.homeTelephone = homeTelephone;
        this.workTelephone = workTelephone;
        this.mobile = mobile;
        this.fax = fax;
        this.email = email;
        this.address = address;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getFax() {
        return fax;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Person person) {
        return Person.builder()
                .withId(person.getId())
                .withTitle(person.getTitle())
                .withFirstName(person.getFirstName())
                .withLastName(person.getLastName())
                .withDateOfBirth(person.getDateOfBirth())
                .withNationality(person.getNationality())
                .withGender(person.getGender())
                .withHomeTelephone(person.getHomeTelephone())
                .withWorkTelephone(person.getWorkTelephone())
                .withMobile(person.getMobile())
                .withFax(person.getFax())
                .withEmail(person.getEmail())
                .withAddress(Address.builder(person.getAddress()));
    }

    public static class Builder {

        private UUID id;

        private String title;

        private String firstName;

        private String lastName;

        private LocalDate dateOfBirth;

        private String nationality;

        private String gender;

        private String homeTelephone;

        private String workTelephone;

        private String mobile;

        private String fax;

        private String email;

        private Address.Builder address;

        private Builder() {}

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }
        public Builder withDateOfBirth(final LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withNationality(final String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withGender(final String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withHomeTelephone(final String homeTelephone) {
            this.homeTelephone = homeTelephone;
            return this;
        }

        public Builder withWorkTelephone(final String workTelephone) {
            this.workTelephone = workTelephone;
            return this;
        }

        public Builder withMobile(final String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder withFax(final String fax) {
            this.fax = fax;
            return this;
        }

        public Builder withEmail(final String email) {
            this.email = email;
            return this;
        }

        public Builder withAddress(final Address.Builder address) {
            this.address = address;
            return this;
        }

        public Person build() {
            return new Person(id, title, firstName, lastName, dateOfBirth, nationality, gender, homeTelephone, workTelephone, mobile, fax, email, ofNullable(address).map(Address.Builder::build).orElse(null));
        }
    }
}
