package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

//TODO GPE-6313 remove
@SuppressWarnings({"squid:S1135"})
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String title;

    private String firstName;

    private String lastName;

    private String dateOfBirth;

    private String nationality;

    private String gender;

    private String homeTelephone;

    private String workTelephone;

    private String mobile;

    private String fax;

    private String email;

    private Address address;

    public static Person person() {
        return new Person();
    }

    public UUID getId() {
        return this.id;
    }

    public Person setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public Person setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public Person setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return this.lastName;
    }

    public Person setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public Person setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getNationality() {
        return this.nationality;
    }

    public Person setNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public String getGender() {
        return this.gender;
    }

    public Person setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getHomeTelephone() {
        return this.homeTelephone;
    }

    public Person setHomeTelephone(String homeTelephone) {
        this.homeTelephone = homeTelephone;
        return this;
    }

    public String getWorkTelephone() {
        return this.workTelephone;
    }

    public Person setWorkTelephone(String workTelephone) {
        this.workTelephone = workTelephone;
        return this;
    }

    public String getMobile() {
        return this.mobile;
    }

    public Person setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getFax() {
        return this.fax;
    }

    public Person setFax(String fax) {
        this.fax = fax;
        return this;
    }

    public String getEmail() {
        return this.email;
    }

    public Person setEmail(String email) {
        this.email = email;
        return this;
    }

    public Address getAddress() {
        return this.address;
    }

    public Person setAddress(Address address) {
        this.address = address;
        return this;
    }
}
