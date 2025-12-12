package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.case-ejected")
@SuppressWarnings("squid:S2384")
public class CaseEjected implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID prosecutionCaseId;

    private List<UUID> hearingIds;

    @JsonCreator
    public CaseEjected(@JsonProperty("prosecutionCaseId") UUID prosecutionCaseId, @JsonProperty("hearingIds") List<UUID> hearingIds) {
        this.prosecutionCaseId = prosecutionCaseId;
        this.hearingIds = hearingIds;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }
    public static CaseEjectedBuilder aCaseEjected() {
        return new uk.gov.moj.cpp.hearing.domain.event.CaseEjected.CaseEjectedBuilder();
    }
    public static final class CaseEjectedBuilder {
        private UUID prosecutionCaseId;
        private List<UUID> hearingIds;

        private CaseEjectedBuilder() {
        }

        public CaseEjectedBuilder withProsecutionCaseId(UUID prosecutionCaseId) {
            this.prosecutionCaseId = prosecutionCaseId;
            return this;
        }

        public CaseEjectedBuilder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = hearingIds;
            return this;
        }

        public CaseEjected build() {
            return new CaseEjected(prosecutionCaseId, hearingIds);
        }
    }
}
