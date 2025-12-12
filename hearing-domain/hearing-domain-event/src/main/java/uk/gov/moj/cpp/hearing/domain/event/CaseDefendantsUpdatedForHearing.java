package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.case-defendants-updated-for-hearing")
public class CaseDefendantsUpdatedForHearing implements Serializable {
    private static final long serialVersionUID = 1L;

    private ProsecutionCase prosecutionCase;

    private UUID hearingId;

    @JsonCreator
    public CaseDefendantsUpdatedForHearing(@JsonProperty("prosecutionCase") ProsecutionCase prosecutionCase, @JsonProperty("hearingId") UUID hearingId) {
        this.prosecutionCase = prosecutionCase;
        this.hearingId = hearingId;
    }

    public ProsecutionCase getProsecutionCase() {
        return prosecutionCase;
    }

    public UUID getHearingId() {
        return hearingId;
    }
    public static CaseDefendantsUpdatedForHearing.CaseDefendantsUpdatedForHearingBuilder caseDefendantsUpdatedForHearing() {
        return new uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing.CaseDefendantsUpdatedForHearingBuilder();
    }
    public static final class CaseDefendantsUpdatedForHearingBuilder {
        private ProsecutionCase prosecutionCase;
        private UUID hearingId;

        private CaseDefendantsUpdatedForHearingBuilder() {
        }

        public CaseDefendantsUpdatedForHearing.CaseDefendantsUpdatedForHearingBuilder withProsecutionCase(ProsecutionCase prosecutionCase) {
            this.prosecutionCase = prosecutionCase;
            return this;
        }

        public CaseDefendantsUpdatedForHearing.CaseDefendantsUpdatedForHearingBuilder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public CaseDefendantsUpdatedForHearing build() {
            return new CaseDefendantsUpdatedForHearing(prosecutionCase, hearingId);
        }
    }
}
