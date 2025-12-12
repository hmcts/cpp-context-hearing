package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Event("hearing.case-defendants-updated")
public class CaseDefendantsUpdated  implements Serializable {

    private static final long serialVersionUID = 1L;

    private ProsecutionCase prosecutionCase;

    private List<UUID> hearingIds;

    @JsonCreator
    public CaseDefendantsUpdated(@JsonProperty("prosecutionCase") ProsecutionCase prosecutionCase, @JsonProperty("hearingIds") List<UUID> hearingIds) {
        this.prosecutionCase = prosecutionCase;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public ProsecutionCase getProsecutionCase() {
        return prosecutionCase;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }
    public static CaseDefendantsUpdated.CaseDefendantsUpdatedBuilder caseDefendantsUpdatd() {
        return new uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdated.CaseDefendantsUpdatedBuilder();
    }
    public static final class CaseDefendantsUpdatedBuilder {
        private ProsecutionCase prosecutionCase;
        private List<UUID> hearingIds;

        private CaseDefendantsUpdatedBuilder() {
        }

        public CaseDefendantsUpdated.CaseDefendantsUpdatedBuilder withProsecutionCase(ProsecutionCase prosecutionCase) {
            this.prosecutionCase = prosecutionCase;
            return this;
        }

        public CaseDefendantsUpdated.CaseDefendantsUpdatedBuilder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public CaseDefendantsUpdated build() {
            return new CaseDefendantsUpdated(prosecutionCase, hearingIds);
        }
    }

}
