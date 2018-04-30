package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

public class CaseDefendantDetailsWithHearingCommand {

    private final UUID caseId;

    private final String caseUrn;

    private final Defendant defendants;

    private final List<UUID> hearingIds;

    public CaseDefendantDetailsWithHearingCommand(
            @JsonProperty("caseId") UUID caseId,
            @JsonProperty("caseUrn") String caseUrn,
            @JsonProperty("defendants") final Defendant defendants,
            @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.caseId = caseId;
        this.caseUrn = caseUrn;
        this.defendants = defendants;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public Defendant getDefendants() {
        return defendants;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }

    public static class Builder {

        private UUID caseId;

        private String caseUrn;

        private Defendant.Builder defendants;

        private List<UUID> hearingIds;

        private Builder() {

        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public Builder withDefendants(Defendant.Builder defendants) {
            this.defendants = defendants;
            return this;
        }

        public Builder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public CaseDefendantDetailsWithHearingCommand build() {
            return new CaseDefendantDetailsWithHearingCommand(caseId, caseUrn, ofNullable(defendants).map(Defendant.Builder::build).orElse(null), hearingIds);
        }
    }

}
