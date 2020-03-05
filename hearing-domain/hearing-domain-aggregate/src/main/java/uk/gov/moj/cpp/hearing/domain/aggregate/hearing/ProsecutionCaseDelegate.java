package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;

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

    public void onDefendantLegalaidStatusTobeUpdatedForHearing(final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdatedForHearing) {
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant ->defendant.getId().equals(defendantLegalAidStatusUpdatedForHearing.getDefendantId()))
                .findFirst().ifPresent(defendant ->
                defendant.setLegalAidStatus(defendantLegalAidStatusUpdatedForHearing.getLegalAidStatus()));
    }

    public void onCaseDefendantUpdatedForHearing(final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing) {
        final ProsecutionCase updatedProsecutionCase  = caseDefendantsUpdatedForHearing.getProsecutionCase();
        this.momento.getHearing().getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getId().equals(updatedProsecutionCase.getId()))
                .map(prosecutionCase-> {
                    prosecutionCase.setCaseStatus(updatedProsecutionCase.getCaseStatus());
                    return prosecutionCase;
                })
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant ->
                        updatedProsecutionCase.getDefendants().stream()
                                .filter(updatedDefendant ->defendant.getId().equals(updatedDefendant.getId()))
                                .findFirst().ifPresent(updatedDefendant->
                            defendant.setProceedingsConcluded(updatedDefendant.getProceedingsConcluded())
                        ));

    }

    private void setCaseMarkers(final ProsecutionCase prosecutionCase, final List<Marker> markers) {
        prosecutionCase.setCaseMarkers(markers);
    }
}