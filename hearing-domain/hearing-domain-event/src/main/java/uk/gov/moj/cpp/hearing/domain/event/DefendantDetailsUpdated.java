package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.io.Serializable;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Event("hearing.defendant-details-updated")
public class DefendantDetailsUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final UUID caseId;

    private final Defendant defendant;

    @JsonCreator
    public DefendantDetailsUpdated(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("caseId") UUID caseId,
            @JsonProperty("defendant") Defendant defendant) {
        super();
        this.hearingId = hearingId;
        this.caseId = caseId;
        this.defendant = defendant;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public static class Builder {

        private UUID hearingId;

        private UUID caseId;

        private Defendant.Builder defendant;

        private Builder() {
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendant(Defendant.Builder defendant) {
            this.defendant = defendant;
            return this;
        }

        public DefendantDetailsUpdated build() {
            return new DefendantDetailsUpdated(hearingId, caseId,
                    ofNullable(defendant).map(Defendant.Builder::build).orElse(null));
        }
    }
}
