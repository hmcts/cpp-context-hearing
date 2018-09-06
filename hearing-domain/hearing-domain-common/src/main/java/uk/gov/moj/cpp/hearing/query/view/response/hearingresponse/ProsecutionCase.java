package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier;

import java.util.List;
import java.util.UUID;

@JsonTypeName("prosecutionCase")
public class ProsecutionCase {

    private UUID id;
    private ProsecutionCaseIdentifier prosecutionCaseIdentifier;
    private List<HearingListResponseDefendant> defendants;

    @JsonCreator
    public ProsecutionCase(@JsonProperty("id") UUID id, @JsonProperty("prosecutionCaseIdentifier") ProsecutionCaseIdentifier prosecutionCaseIdentifier, @JsonProperty("defendants") List<HearingListResponseDefendant> defendants) {
        this.id = id;
        this.prosecutionCaseIdentifier = prosecutionCaseIdentifier;
        this.defendants = defendants;
    }

    public ProsecutionCase(Builder builder) {
        this.id = builder.id;
        this.prosecutionCaseIdentifier = builder.prosecutionCaseIdentifier;
        this.defendants = builder.defendants;
    }

    public UUID getId() {
        return id;
    }

    public ProsecutionCaseIdentifier getProsecutionCaseIdentifier() {
        return prosecutionCaseIdentifier;
    }

    public List<HearingListResponseDefendant> getDefendants() {
        return defendants;
    }

    public static class Builder {

        private UUID id;

        private ProsecutionCaseIdentifier prosecutionCaseIdentifier;

        private List<HearingListResponseDefendant> defendants;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withProsecutionCaseIdentifier(ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
            this.prosecutionCaseIdentifier = prosecutionCaseIdentifier;
            return this;
        }

        public Builder withDefendants(List<HearingListResponseDefendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public ProsecutionCase build() {
            return new ProsecutionCase(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
