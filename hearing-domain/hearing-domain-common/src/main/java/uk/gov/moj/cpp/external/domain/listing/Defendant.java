package uk.gov.moj.cpp.external.domain.listing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
@SuppressWarnings("squid:S1067")
public class Defendant implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String id;
    private final String personId;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String bailStatus;
    private final LocalDate custodyTimeLimit;
    private final String defenceOrganisation;
    private final List<Offence> offences;

    @JsonCreator
    public Defendant(@JsonProperty(value = "id") final String id,
                     @JsonProperty(value = "personId") final String personId,
                     @JsonProperty(value = "firstName") final String firstName,
                     @JsonProperty(value = "lastName") final String lastName,
                     @JsonProperty(value = "dateOfBirth") final LocalDate dateOfBirth,
                     @JsonProperty(value = "bailStatus") final String bailStatus,
                     @JsonProperty(value = "custodyTimeLimit") final LocalDate custodyTimeLimit,
                     @JsonProperty(value = "defenceOrganisation") final String defenceOrganisation,
                     @JsonProperty(value = "offences") final List<Offence> offences) {
        this.id = id;
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.bailStatus = bailStatus;
        this.custodyTimeLimit = custodyTimeLimit;
        this.defenceOrganisation = defenceOrganisation;
        this.offences = offences;
    }

    public String getId() {
        return id;
    }

    public String getPersonId() {
        return personId;
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

    public String getBailStatus() {
        return bailStatus;
    }

    public LocalDate getCustodyTimeLimit() {
        return custodyTimeLimit;
    }

    public String getDefenceOrganisation() {
        return defenceOrganisation;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Defendant defendant = (Defendant) o;
        return Objects.equals(id, defendant.id) &&
                Objects.equals(personId, defendant.personId) &&
                Objects.equals(firstName, defendant.firstName) &&
                Objects.equals(lastName, defendant.lastName) &&
                Objects.equals(dateOfBirth, defendant.dateOfBirth) &&
                Objects.equals(bailStatus, defendant.bailStatus) &&
                Objects.equals(custodyTimeLimit, defendant.custodyTimeLimit) &&
                Objects.equals(defenceOrganisation, defendant.defenceOrganisation) &&
                Objects.equals(offences, defendant.offences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personId, firstName, lastName,
                dateOfBirth, bailStatus, custodyTimeLimit, defenceOrganisation, offences);
    }
}
