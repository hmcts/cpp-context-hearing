package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FilterHearingsBasedOnPermissions {

    public List<Hearing> filterHearings(final List<Hearing> hearings,
                                        final List<UUID> prosecutionCasesIdsWithAccess) {
        final List<Hearing> filteredHearings = new ArrayList<>();
        for (final Hearing hearing : hearings) {
            final Set<ProsecutionCase> prosecutionCaseSet = new HashSet();
            for (final ProsecutionCase prosecutionCase : hearing.getProsecutionCases()) {
                final UUID id = prosecutionCase.getId().getId();
                if (prosecutionCasesIdsWithAccess.contains(id)) {
                    prosecutionCaseSet.add(prosecutionCase);
                }
            }
            if (!prosecutionCaseSet.isEmpty()) {
                filteredHearings.add(copyHearing(hearing, prosecutionCaseSet));
            }
        }
        return filteredHearings;
    }

    public Hearing copyHearing(final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing originalHearing,
                               final Set<ProsecutionCase> prosecutionCaseSet) {
        if (null == originalHearing) {
            return originalHearing;
        }
        final Hearing hearing = new Hearing();
        hearing.setId(originalHearing.getId());
        hearing.setCourtCentre(originalHearing.getCourtCentre());
        hearing.setDefenceCounsels(originalHearing.getDefenceCounsels());
        hearing.setDefendantAttendance(originalHearing.getDefendantAttendance());
        hearing.setDefendantReferralReasons(originalHearing.getDefendantReferralReasons());
        hearing.setHasSharedResults(originalHearing.getHasSharedResults());
        hearing.setIsBoxHearing(originalHearing.getIsBoxHearing());
        hearing.setIsVacatedTrial(originalHearing.getIsVacatedTrial());
        hearing.setHearingCaseNotes(originalHearing.getHearingCaseNotes());
        hearing.setHearingDays(originalHearing.getHearingDays());
        hearing.setHearingLanguage(ofNullable(originalHearing.getHearingLanguage()).orElse(HearingLanguage.ENGLISH));
        hearing.setJudicialRoles(originalHearing.getJudicialRoles());
        hearing.setJurisdictionType(originalHearing.getJurisdictionType());
        hearing.setProsecutionCases(prosecutionCaseSet);
        hearing.setProsecutionCounsels(originalHearing.getProsecutionCounsels());
        hearing.setHearingInterpreterIntermediaries(originalHearing.getHearingInterpreterIntermediaries());
        hearing.setReportingRestrictionReason(originalHearing.getReportingRestrictionReason());
        hearing.setHearingType(originalHearing.getHearingType());
        hearing.setCourtApplicationsJson(originalHearing.getCourtApplicationsJson());
        hearing.setApprovalsRequested(originalHearing.getApprovalsRequested());
        return hearing;
    }
}
