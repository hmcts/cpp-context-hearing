package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Event("hearing.update-case-defendant-details-enriched-with-hearing-ids")
public class CaseDefendantDetailsWithHearings implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID caseId;

    private final String caseUrn;

    private final Defendant defendant;

    private final List<UUID> hearingIds;

    public CaseDefendantDetailsWithHearings(
            @JsonProperty("caseId") UUID caseId,
            @JsonProperty("caseUrn") String caseUrn,
            @JsonProperty("defendant") final Defendant defendant,
            @JsonProperty("hearingIds") final List<UUID> hearingIds) {

        this.caseId = caseId;
        this.caseUrn = caseUrn;
        this.defendant = defendant;
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

    public Defendant getDefendant() {
        return defendant;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }

    public static class Builder {

        private UUID caseId;

        private String caseUrn;

        private Defendant.Builder defendant;

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

        public Builder withDefendants(Defendant.Builder defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public CaseDefendantDetailsWithHearings build() {
            return new CaseDefendantDetailsWithHearings(caseId, caseUrn, ofNullable(defendant).map(Defendant.Builder::build).orElse(null), hearingIds);
        }
    }
}
