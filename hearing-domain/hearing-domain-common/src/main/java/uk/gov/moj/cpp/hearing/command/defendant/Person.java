package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.json.schemas.core.Gender;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String title;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String nationality;

    private Gender gender;

    private String homeTelephone;

    private String workTelephone;

    private String mobile;

    private String fax;

    private String email;

    private Address address;

    public Person() {
    }

    @JsonCreator
    public Person(@JsonProperty("id") final UUID id,
                  @JsonProperty("title") final String title,
                  @JsonProperty("firstName") final String firstName,
                  @JsonProperty("lastName") final String lastName,
                  @JsonProperty("dateOfBirth") final LocalDate dateOfBirth,
                  @JsonProperty("nationality") final String nationality,
                  @JsonProperty("gender") final Gender gender,
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

    public Gender getGender() {
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

    public Person setId(UUID id) {
        this.id = id;
        return this;
    }

    public Person setTitle(String title) {
        this.title = title;
        return this;
    }

    public Person setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Person setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Person setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public Person setNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public Person setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public Person setHomeTelephone(String homeTelephone) {
        this.homeTelephone = homeTelephone;
        return this;
    }

    public Person setWorkTelephone(String workTelephone) {
        this.workTelephone = workTelephone;
        return this;
    }

    public Person setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public Person setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public Person setEmail(String email) {
        this.email = email;
        return this;
    }

    public Person setAddress(Address address) {
        this.address = address;
        return this;
    }

    public static Person person() {
        return new Person();
    }
}
