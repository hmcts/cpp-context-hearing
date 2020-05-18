package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.hearing-extended")
public class HearingExtended implements Serializable {

    private static final long serialVersionUID = 2L;

    private final UUID hearingId;
    private final CourtApplication courtApplication;
    private final List<ProsecutionCase> prosecutionCases;

    @JsonCreator
    public HearingExtended(@JsonProperty("hearingId") final UUID hearingId, @JsonProperty("courtApplication") final CourtApplication courtApplication,
                           @JsonProperty("prosecutionCases") final List<ProsecutionCase> prosecutionCases) {
        this.hearingId = hearingId;
        this.courtApplication = courtApplication;
        this.prosecutionCases = prosecutionCases;
    }

    public CourtApplication getCourtApplication() {
        return courtApplication;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }
}
