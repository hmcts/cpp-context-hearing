package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID personId;
    private String firstName;
    private String lastName;
    private String nationality;
    private String gender;
    private Address address;
    private LocalDate dateOfBirth;
    private String defenceOrganisation;
    private Interpreter interpreter;
    private List<DefendantCase> defendantCases;
    private List<Offence> offences;

    public Defendant() {
    }

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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

    public Defendant setId(UUID id) {
        this.id = id;
        return this;
    }

    public Defendant setPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    public Defendant setFirstName(final String firstName) {
        this.firstName = firstName;
        return this;
    }

    public Defendant setLastName(final String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Defendant setInterpreter(final Interpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public Defendant setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public Defendant setNationality(final String nationality) {
        this.nationality = nationality;
        return this;
    }

    public Defendant setGender(final String gender) {
        this.gender = gender;
        return this;
    }

    public Defendant setAddress(final Address address) {
        this.address = address;
        return this;
    }

    public Defendant setDefenceOrganisation(final String defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
        return this;
    }

    public Defendant setDefendantCases(List<DefendantCase> defendantCases) {
        this.defendantCases = new ArrayList<>(defendantCases);
        return this;
    }

    public Defendant setOffences(List<Offence> offences) {
        this.offences = new ArrayList<>(offences);
        return this;
    }

    public static Defendant defendant() {
        return new Defendant();
    }
}