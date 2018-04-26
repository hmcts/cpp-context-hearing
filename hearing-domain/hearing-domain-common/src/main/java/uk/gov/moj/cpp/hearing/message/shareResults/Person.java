package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.LocalDate;
import java.util.UUID;

public class Person {

    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Address address;

    private String nationality;
    private String gender;
    private String homeTelephone;
    private String workTelephone;
    private String mobile;
    private String fax;

    private String email;


    public UUID getId() {
        return id;
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

    public Address getAddress() {
        return address;
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

    public Person setId(UUID id) {
        this.id = id;
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

    public Person setAddress(Address address) {
        this.address = address;
        return this;
    }

    public Person setNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public Person setGender(String gender) {
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

    public static Person person() {
        return new Person();
    }
}
