package uk.gov.moj.cpp.hearing.query.view.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("defendant")
public final class HearingListResponseDefendant {

    private final String firstName;
    private final String lastName;

    public HearingListResponseDefendant() {
        this.firstName = null;
        this.lastName = null;
    }

    @JsonCreator
    public HearingListResponseDefendant(@JsonProperty("firstName") final String firstName, 
            @JsonProperty("lastName") String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    private HearingListResponseDefendant(final Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
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

        private String firstName;
        private String lastName;

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public HearingListResponseDefendant build() {
            return new HearingListResponseDefendant(this);
        }

    }

}