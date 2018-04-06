package uk.gov.moj.cpp.hearing.query.view.response.hearingResponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class DefenceCounsel {

    private final String attendeeId;
    private final String status;
    private final String defendantId;
    private final String title;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public DefenceCounsel(@JsonProperty("attendeeId") final String attendeeId, 
            @JsonProperty("status") final String status, 
            @JsonProperty("defendantId") final String defendantId, 
            @JsonProperty("title") final String title, 
            @JsonProperty("firstName") final String firstName,
            @JsonProperty("lastName") final String lastName) {
        this.attendeeId = attendeeId;
        this.status = status;
        this.defendantId = defendantId;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @JsonIgnore
    private DefenceCounsel(final Builder builder) {
        this.attendeeId = builder.attendeeId;
        this.status = builder.status;
        this.defendantId = builder.defendantId;
        this.title = builder.title;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }
    
    public String getAttendeeId() {
        return attendeeId;
    }

    public String getStatus() {
        return status;
    }

    public String getDefendantId() {
        return defendantId;
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

    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private String attendeeId;
        private String status;
        private String defendantId;
        private String title;
        private String firstName;
        private String lastName;
        
        public Builder withAttendeeId(final String attendeeId) {
            this.attendeeId = attendeeId;
            return this;
        }
        
        public Builder withStatus(final String status) {
            this.status = status;
            return this;
        }
        
        public Builder withDefendantId(final String defendantId) {
            this.defendantId = defendantId;
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
        
        public DefenceCounsel build() {
            return new DefenceCounsel(this);
        }
    }
}