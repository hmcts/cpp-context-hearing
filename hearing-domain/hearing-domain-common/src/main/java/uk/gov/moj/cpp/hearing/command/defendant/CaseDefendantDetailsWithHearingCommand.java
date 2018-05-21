package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class CaseDefendantDetailsWithHearingCommand {

    private final UUID caseId;

    private final Defendant defendant;

    private final List<UUID> hearingIds;

    private CaseDefendantDetailsWithHearingCommand(
            @JsonProperty("caseId") UUID caseId,
            @JsonProperty("defendant") final Defendant defendant,
            @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.caseId = caseId;
        this.defendant = defendant;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }

    public static class Builder {

        private UUID caseId;

        private Defendant.Builder defendant;

        private List<UUID> hearingIds;

        private Builder() {

        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendants(Defendant.Builder defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public CaseDefendantDetailsWithHearingCommand build() {
            return new CaseDefendantDetailsWithHearingCommand(caseId, ofNullable(defendant).map(Defendant.Builder::build).orElse(null), hearingIds);
        }
    }
}
