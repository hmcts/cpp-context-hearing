package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequests;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequestsResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;

public class OutstandingFineRequestsService {

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    public DefendantOutstandingFineRequestsResult getDefendantOutstandingFineRequestsByHearingDate(final LocalDate hearingDate) {

        if (hearingDate == null) {
            return new DefendantOutstandingFineRequestsResult(null);
        }
        final List<Hearing> hearings = hearingRepository.findByHearingDate(hearingDate);
        if (CollectionUtils.isEmpty(hearings)) {
            return new DefendantOutstandingFineRequestsResult(null);
        }


        final List<DefendantOutstandingFineRequests> defendantDetails = new ArrayList<>();
        for (final Hearing hearing : hearings) {
            for (final ProsecutionCase pc : hearing.getProsecutionCases()) {
                final UUID courtCentreId = hearing.getCourtCentre().getId();
                final HearingDay hearingDay = hearing.getHearingDays().stream()
                        .filter(day -> Objects.equals(day.getDate(), hearingDate))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Hearing doesnt have Hearing Day " + hearingDate.toString()));

                defendantDetails.addAll(addDefendantDetailsToQueryResult(hearingDay, pc, courtCentreId));
            }
        }
        return new DefendantOutstandingFineRequestsResult(defendantDetails);
    }

    private List<DefendantOutstandingFineRequests> addDefendantDetailsToQueryResult(final HearingDay hearingDay, final ProsecutionCase pc, final UUID courtCentreId) {
        return pc.getDefendants().stream().map(defendant -> convertToDefendantRequestProfile(hearingDay, pc, courtCentreId, defendant)).collect(Collectors.toList());
    }

    private DefendantOutstandingFineRequests convertToDefendantRequestProfile(final HearingDay hearingDay, final ProsecutionCase pc, final UUID courtCentreId, final Defendant defendant) {
        if (defendant.getLegalEntityOrganisation() != null) {
            return DefendantOutstandingFineRequests.newBuilder()
                    .withDefendantId(defendant.getId().getId())
                    .withCaseId(pc.getId().getId())
                    .withCourtCentreId(courtCentreId)
                    .withDateOfHearing(hearingDay.getDate())
                    .withTimeOfHearing(hearingDay.getDateTime()).withLegalEntityDefendantName(defendant.getLegalEntityOrganisation().getName())
                    .build();
        } else {
            return DefendantOutstandingFineRequests.newBuilder()
                    .withDefendantId(defendant.getId().getId())
                    .withCaseId(pc.getId().getId())
                    .withCourtCentreId(courtCentreId)
                    .withDateOfHearing(hearingDay.getDate())
                    .withTimeOfHearing(hearingDay.getDateTime())
                    .withFirstName(defendant.getPersonDefendant().getPersonDetails().getFirstName())
                    .withLastName(defendant.getPersonDefendant().getPersonDetails().getLastName())
                    .withDateOfBirth(Objects.nonNull(defendant.getPersonDefendant().getPersonDetails().getDateOfBirth()) ? defendant.getPersonDefendant().getPersonDetails().getDateOfBirth().toString() : null)
                    .withNationalInsuranceNumber(defendant.getPersonDefendant().getPersonDetails().getNationalInsuranceNumber())
                    .build();
        }
    }
}
