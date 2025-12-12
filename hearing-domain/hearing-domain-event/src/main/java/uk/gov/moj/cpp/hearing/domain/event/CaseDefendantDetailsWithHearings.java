package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.update-case-defendant-details-enriched-with-hearing-ids")
public class CaseDefendantDetailsWithHearings implements Serializable {

    private static final long serialVersionUID = 1L;

    private Defendant defendant;

    private List<UUID> hearingIds;

    public CaseDefendantDetailsWithHearings() {
    }

    @JsonCreator
    private CaseDefendantDetailsWithHearings(
            @JsonProperty("defendant") final Defendant defendant,
            @JsonProperty("hearingIds") final List<UUID> hearingIds) {

        this.defendant = defendant;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public static CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings() {
        return new CaseDefendantDetailsWithHearings();
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public CaseDefendantDetailsWithHearings setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }

    public CaseDefendantDetailsWithHearings setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }
}
