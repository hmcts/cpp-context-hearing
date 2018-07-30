package uk.gov.justice.progression.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@SuppressWarnings({"squid:S1188"})
@Event("public.progression.case-defendant-changed")
public class CaseDefendantDetails {

    private final UUID caseId;

    private final List<Defendant> defendants;

    private CaseDefendantDetails(@JsonProperty("caseId") UUID caseId,
                                 @JsonProperty("defendants") final List<Defendant> defendants) {
        this.caseId = caseId;
        this.defendants = new ArrayList<>(defendants);
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<Defendant> getDefendants() {
        return new ArrayList<>(defendants);
    }

    public static class Builder {

        private UUID caseId;

        private List<Defendant> defendants = new ArrayList<>();

        private Builder() {
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder addDefendant(Defendant.Builder defendantBuilder) {
            final Defendant defendant = ofNullable(defendantBuilder).map(Defendant.Builder::build).orElse(null);

            if (Objects.nonNull(defendant)) {
                defendants.add(defendant);
            }

            return this;
        }

        public Builder addDefendants(List<Defendant> defendants) {

            defendants.forEach(defendant ->
                    addDefendant(Defendant.builder(defendant))
            );

            return this;
        }

        public CaseDefendantDetails build() {
            return new CaseDefendantDetails(caseId, defendants);
        }
    }

}