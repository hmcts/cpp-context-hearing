package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.add-case-defendants-for-hearing")
public class AddCaseDefendantsForHearing implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID caseId;

    private UUID hearingId;

    private List<Defendant> defendants;

    @JsonCreator
    public AddCaseDefendantsForHearing(@JsonProperty("caseId") UUID caseId, @JsonProperty("hearingId") UUID hearingId,
                                       @JsonProperty("defendants") List<Defendant> defendants) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.defendants = defendants;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public static AddCaseDefendantsForHearing.AddCaseDefendantsForHearingBuilder addCaseDefendantsForHearing() {
        return new AddCaseDefendantsForHearing.AddCaseDefendantsForHearingBuilder();
    }
    public static final class AddCaseDefendantsForHearingBuilder {
        private UUID caseId;
        private UUID hearingId;
        private List<Defendant> defendants;

        private AddCaseDefendantsForHearingBuilder() {
        }

        public AddCaseDefendantsForHearing.AddCaseDefendantsForHearingBuilder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public AddCaseDefendantsForHearing.AddCaseDefendantsForHearingBuilder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public AddCaseDefendantsForHearing.AddCaseDefendantsForHearingBuilder withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public AddCaseDefendantsForHearing build() {
            return new AddCaseDefendantsForHearing(caseId, hearingId, defendants);
        }
    }
}
