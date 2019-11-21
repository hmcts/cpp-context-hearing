package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ProsecutionCaseDelegate implements Serializable {

    private static final long serialVersionUID = -6459704029050560450L;

    private final HearingAggregateMomento momento;

    public ProsecutionCaseDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleCaseMarkersUpdated(final CaseMarkersUpdated caseMarkersUpdated) {
        this.momento.getHearing().getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getId().equals(caseMarkersUpdated.getProsecutionCaseId()))
                .findFirst()
                .ifPresent(prosecutionCase -> setCaseMarkers(prosecutionCase, caseMarkersUpdated.getCaseMarkers()));

    }

    public Stream<Object> updateCaseMarkers(UUID hearingId, UUID prosecutionCaseId, List<Marker> markers) {
        if (!this.momento.isPublished()) {
            return Stream.of(CaseMarkersUpdated.caseMarkersUpdated()
                    .setHearingId(hearingId)
                    .setProsecutionCaseId(prosecutionCaseId)
                    .setCaseMarkers(markers)
            );
        }
        return Stream.empty();
    }

    private void setCaseMarkers(final ProsecutionCase prosecutionCase, final List<Marker> markers) {
        prosecutionCase.setCaseMarkers(markers);
    }
}