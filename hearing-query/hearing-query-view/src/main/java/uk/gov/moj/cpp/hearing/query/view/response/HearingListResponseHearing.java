package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("hearing")
public final class HearingListResponseHearing {

    private final String hearingId;
    private final String hearingType;
    private final List<LegalCase> cases;
    private final List<HearingListResponseDefendant> hearingListResponseDefendants;
    
    @JsonCreator
    public HearingListResponseHearing(@JsonProperty("hearingId") String hearingId, 
            @JsonProperty("hearingType") String hearingType, 
            @JsonProperty("cases") List<LegalCase> cases,
            @JsonProperty("defendants") List<HearingListResponseDefendant> hearingListResponseDefendants) {
        super();
        this.hearingId = hearingId;
        this.hearingType = hearingType;
        this.cases = cases;
        this.hearingListResponseDefendants = hearingListResponseDefendants;
    }

    private HearingListResponseHearing(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.hearingType = builder.hearingType;
        this.cases = builder.cases;
        this.hearingListResponseDefendants = builder.hearingListResponseDefendants;
    }

    public String getHearingId() {
        return hearingId;
    }

    public String getHearingType() {
        return hearingType;
    }

    public List<LegalCase> getCases() {
        return cases;
    }

    public List<HearingListResponseDefendant> getDefendants() {
        return hearingListResponseDefendants;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
 
        private String hearingId;
        private String hearingType;
        private List<LegalCase> cases;
        private List<HearingListResponseDefendant> hearingListResponseDefendants;

        public Builder withHearingId(final String hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingType(final String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withCases(final List<LegalCase> cases) {
            this.cases = cases;
            return this;
        }

        public Builder withDefendants(final List<HearingListResponseDefendant> hearingListResponseDefendants) {
            this.hearingListResponseDefendants = hearingListResponseDefendants;
            return this;
        }

        public HearingListResponseHearing build() {
            return new HearingListResponseHearing(this);
        }
    }
}