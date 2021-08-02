package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.Prosecutor;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;

import java.io.Serializable;
import java.util.Collection;
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
        if (momento.getHearing() != null) {
            ofNullable(momento.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                    .filter(prosecutionCase -> prosecutionCase.getId().equals(caseMarkersUpdated.getProsecutionCaseId()))
                    .findFirst()
                    .ifPresent(prosecutionCase -> setCaseMarkers(prosecutionCase, caseMarkersUpdated.getCaseMarkers()));
        }
    }

    public void handleProsecutorUpdated(final CpsProsecutorUpdated cpsProsecutorUpdated) {
        if (momento.getHearing() != null) {
            ofNullable(momento.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                    .filter(prosecutionCase -> prosecutionCase.getId().equals(cpsProsecutorUpdated.getProsecutionCaseId()))
                    .findFirst()
                    .ifPresent(prosecutionCase -> setProsecutor(prosecutionCase, cpsProsecutorUpdated));
        }
    }

    public Stream<Object> updateCaseMarkers(final UUID hearingId, final UUID prosecutionCaseId, List<Marker> markers) {
        if (!this.momento.isPublished()) {
            return Stream.of(CaseMarkersUpdated.caseMarkersUpdated()
                    .setHearingId(hearingId)
                    .setProsecutionCaseId(prosecutionCaseId)
                    .setCaseMarkers(markers == null || markers.isEmpty() ? null : markers)
            );
        }
        return Stream.empty();
    }

    public Stream<Object> updateProsecutor(final UUID hearingId, final UUID prosecutionCaseId, final ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
        if (momento.getHearing() != null && !this.momento.isPublished()) {
            return Stream.of(CpsProsecutorUpdated.cpsProsecutorUpdated()
                    .setHearingId(hearingId)
                    .setProsecutionCaseId(prosecutionCaseId)
                    .setProsecutionAuthorityId(prosecutionCaseIdentifier.getProsecutionAuthorityId())
                    .setProsecutionAuthorityCode(prosecutionCaseIdentifier.getProsecutionAuthorityCode())
                    .setProsecutionAuthorityName(prosecutionCaseIdentifier.getProsecutionAuthorityName())
                    .setProsecutionAuthorityReference(prosecutionCaseIdentifier.getProsecutionAuthorityReference())
                    .setCaseURN(prosecutionCaseIdentifier.getCaseURN())
                    .setAddress(prosecutionCaseIdentifier.getAddress()));
        }
        return Stream.empty();
    }

    public void onDefendantLegalaidStatusTobeUpdatedForHearing(final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdatedForHearing) {
        if (momento.getHearing() != null) {
            ofNullable(momento.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .filter(defendant -> defendant.getId().equals(defendantLegalAidStatusUpdatedForHearing.getDefendantId()))
                    .findFirst().ifPresent(defendant ->
                    defendant.setLegalAidStatus(defendantLegalAidStatusUpdatedForHearing.getLegalAidStatus()));
        }
    }

    public void onCaseDefendantUpdatedForHearing(final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing) {
        final ProsecutionCase updatedProsecutionCase = caseDefendantsUpdatedForHearing.getProsecutionCase();
        ofNullable(momento.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(prosecutionCase -> prosecutionCase.getId().equals(updatedProsecutionCase.getId()))
                .map(prosecutionCase -> {
                    prosecutionCase.setCaseStatus(updatedProsecutionCase.getCaseStatus());
                    return prosecutionCase;
                })
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant ->
                        updatedProsecutionCase.getDefendants().stream()
                                .filter(updatedDefendant -> defendant.getId().equals(updatedDefendant.getId()))
                                .findFirst().ifPresent(updatedDefendant ->
                                defendant.setProceedingsConcluded(updatedDefendant.getProceedingsConcluded())
                        ));

    }

    private void setCaseMarkers(final ProsecutionCase prosecutionCase, final List<Marker> markers) {
        prosecutionCase.setCaseMarkers(markers == null || markers.isEmpty() ? null : markers);
    }

    private void setProsecutor(final ProsecutionCase prosecutionCase, final CpsProsecutorUpdated cpsProsecutorUpdated) {
        prosecutionCase.setProsecutor(Prosecutor.prosecutor().withProsecutorCode(cpsProsecutorUpdated.getProsecutionAuthorityCode()).withProsecutorId(cpsProsecutorUpdated.getProsecutionCaseId()).withProsecutorName(cpsProsecutorUpdated.getProsecutionAuthorityName()).withAddress(cpsProsecutorUpdated.getAddress()).build());
    }
}