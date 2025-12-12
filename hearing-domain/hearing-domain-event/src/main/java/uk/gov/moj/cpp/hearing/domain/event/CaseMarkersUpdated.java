package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.case-markers-updated")
public class CaseMarkersUpdated implements Serializable {

    private static final long serialVersionUID = -8171932584498620216L;

    private UUID prosecutionCaseId;

    private UUID hearingId;

    private List<Marker> caseMarkers;

    public CaseMarkersUpdated() {
    }

    @JsonCreator
    public CaseMarkersUpdated(@JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
                              @JsonProperty("hearingId") final UUID hearingId,
                              @JsonProperty("caseMarkers") final List<Marker> caseMarkers) {
        this.prosecutionCaseId = prosecutionCaseId;
        this.hearingId = hearingId;
        this.caseMarkers = caseMarkers;
    }

    public static CaseMarkersUpdated caseMarkersUpdated() {
        return new CaseMarkersUpdated();
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public CaseMarkersUpdated setProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public CaseMarkersUpdated setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public List<Marker> getCaseMarkers() {
        return caseMarkers;
    }

    public CaseMarkersUpdated setCaseMarkers(List<Marker> caseMarkers) {
        this.caseMarkers = caseMarkers;
        return this;
    }
}