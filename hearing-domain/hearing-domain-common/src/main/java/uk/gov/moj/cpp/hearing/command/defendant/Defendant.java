package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private Person person;

    private String bailStatus;

    private LocalDate custodyTimeLimitDate;

    private String defenceOrganisation;

    private Interpreter interpreter;

    public Defendant() {
    }

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

    public UUID getId() {
        return id;
    }

    public Person getPerson() {
        return person;
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

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Defendant setId(UUID id) {
        this.id = id;
        return this;
    }

    public Defendant setPerson(Person person) {
        this.person = person;
        return this;
    }

    public Defendant setBailStatus(String bailStatus) {
        this.bailStatus = bailStatus;
        return this;
    }

    public Defendant setCustodyTimeLimitDate(LocalDate custodyTimeLimitDate) {
        this.custodyTimeLimitDate = custodyTimeLimitDate;
        return this;
    }

    public Defendant setDefenceOrganisation(String defenceOrganisation) {
        this.defenceOrganisation = defenceOrganisation;
        return this;
    }

    public Defendant setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
        return this;
    }

    public static Defendant defendant(){
        return new Defendant();
    }
}
