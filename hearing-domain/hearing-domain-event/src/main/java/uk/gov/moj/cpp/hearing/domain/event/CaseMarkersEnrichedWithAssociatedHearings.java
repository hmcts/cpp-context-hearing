package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.case-markers-enriched-with-associated-hearings")
public class CaseMarkersEnrichedWithAssociatedHearings implements Serializable {

    private static final long serialVersionUID = -8171932584498620216L;

    private UUID prosecutionCaseId;

    private List<UUID> hearingIds;

    private List<Marker> caseMarkers;

    public CaseMarkersEnrichedWithAssociatedHearings() {
    }

    @JsonCreator
    public CaseMarkersEnrichedWithAssociatedHearings(@JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
                                                     @JsonProperty("hearingIds") final List<UUID> hearingIds,
                                                     @JsonProperty("caseMarkers") final List<Marker> caseMarkers) {
        this.prosecutionCaseId = prosecutionCaseId;
        this.hearingIds = new ArrayList<>(hearingIds);
        this.caseMarkers = caseMarkers;
    }

    public static CaseMarkersEnrichedWithAssociatedHearings caseMarkersEnrichedWithAssociatedHearings() {
        return new CaseMarkersEnrichedWithAssociatedHearings();
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public CaseMarkersEnrichedWithAssociatedHearings setProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public CaseMarkersEnrichedWithAssociatedHearings setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }

    public List<Marker> getCaseMarkers() {
        return caseMarkers;
    }

    public CaseMarkersEnrichedWithAssociatedHearings setMarkers(List<Marker> caseMarkers) {
        this.caseMarkers = caseMarkers;
        return this;
    }
}