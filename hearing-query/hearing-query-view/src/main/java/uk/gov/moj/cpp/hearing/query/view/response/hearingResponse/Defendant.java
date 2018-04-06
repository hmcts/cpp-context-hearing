package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Defendant {

    private final String defendantId;
    private final String personId;
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String homeTelephone;
    private final String mobile;
    private final String fax;
    private final String email;
    private final Address address;
    private final String dateOfBirth;
    private final List<Offence> offences;

    @JsonCreator
    public Defendant(@JsonProperty("defendantId") final String defendantId, 
            @JsonProperty("personId") final String personId, 
            @JsonProperty("id") final String id, 
            @JsonProperty("firstName") final String firstName, 
            @JsonProperty("lastName") final String lastName,
            @JsonProperty("homeTelephone") final String homeTelephone, 
            @JsonProperty("mobile") final String mobile, 
            @JsonProperty("fax") final String fax, 
            @JsonProperty("email") final String email, 
            @JsonProperty("address") final Address address, 
            @JsonProperty("dateOfBirth") final String dateOfBirth,
            @JsonProperty("offences") final List<Offence> offences) {
        this.defendantId = defendantId;
        this.personId = personId;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.homeTelephone = homeTelephone;
        this.mobile = mobile;
        this.fax = fax;
        this.email = email;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.offences = offences;
    }
    
    @JsonIgnore
    private Defendant(final Builder builder) {
        this.defendantId = builder.defendantId;
        this.personId = builder.personId;
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.homeTelephone = builder.homeTelephone;
        this.mobile = builder.mobile;
        this.fax = builder.fax;
        this.email = builder.email;
        this.address = builder.address;
        this.dateOfBirth = builder.dateOfBirth;
        this.offences = builder.offences;
    }
    
    public String getDefendantId() {
        return defendantId;
    }

    public String getPersonId() {
        return personId;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHomeTelephone() {
        return homeTelephone;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        
        private String defendantId;
        private String personId;
        private String id;
        private String firstName;
        private String lastName;
        private String homeTelephone;
        private String mobile;
        private String fax;
        private String email;
        private Address address;
        private String dateOfBirth;
        private List<Offence> offences;
        
        public Builder withDefendantId(final String defendantId) {
            this.defendantId = defendantId;
            return this;
        }
        
        public Builder withPersonId(final String personId) {
            this.personId = personId;
            return this;
        }
        
        public Builder withId(final String id) {
            this.id = id;
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
        
        public Builder withHomeTelephone(final String homeTelephone) {
            this.homeTelephone = homeTelephone;
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
        
        public Builder withAddress(final Address address) {
            this.address = address;
            return this;
        }
        
        public Builder withDateOfBirth(final String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }
        
        public Builder withOffences(final List<Offence> offences) {
            this.offences = offences;
            return this;
        }

        public Defendant build() {
            return new Defendant(this);
        }
    }
}