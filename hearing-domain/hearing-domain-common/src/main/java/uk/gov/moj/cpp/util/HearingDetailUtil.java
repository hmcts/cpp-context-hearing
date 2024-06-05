package uk.gov.moj.cpp.util;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.eventlog.HearingApplicationDetail;
import uk.gov.moj.cpp.hearing.eventlog.HearingCaseDetail;
import uk.gov.moj.cpp.hearing.eventlog.HearingDefendantDetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HearingDetailUtil implements Serializable {

    public static List<HearingCaseDetail> getCaseDetails(final Hearing hearing) {
        final List<HearingCaseDetail> caseDetailsList = new ArrayList<>();
        final List<ProsecutionCase> prosecutionCases = hearing.getProsecutionCases();
        if (Objects.nonNull(prosecutionCases)) {
            prosecutionCases.stream().forEach(prosecutionCase -> {
                final HearingCaseDetail caseDetails = new HearingCaseDetail();
                final List<HearingDefendantDetail> defendantDetailsList = new ArrayList<>();
                caseDetails.setCaseId(prosecutionCase.getId());
                caseDetails.setCaseUrn(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN());
                final List<Defendant> defendants = prosecutionCase.getDefendants();
                defendants.stream().forEach(defendant -> {
                    final HearingDefendantDetail defendantDetails = getHearingDefendantDetail(defendant);
                    defendantDetailsList.add(defendantDetails);

                });
                caseDetails.setDefendantDetails(defendantDetailsList);
                caseDetailsList.add(caseDetails);
            });

        }
        return caseDetailsList;
    }

    public static HearingDefendantDetail getHearingDefendantDetail(final Defendant defendant) {
        final HearingDefendantDetail defendantDetails = new HearingDefendantDetail();
        defendantDetails.setDefendantId(defendant.getId());
        if (Objects.nonNull(defendant.getPersonDefendant()) && Objects.nonNull(defendant.getPersonDefendant().getPersonDetails())) {
            defendantDetails.setDefendantFirstName(defendant.getPersonDefendant().getPersonDetails().getFirstName());
            defendantDetails.setDefendantLastName(defendant.getPersonDefendant().getPersonDetails().getLastName());
            defendantDetails.setInterpreterLanguageNeeds(defendant.getPersonDefendant().getPersonDetails()
                    .getInterpreterLanguageNeeds());
        }
        if (Objects.nonNull(defendant.getPersonDefendant()) && Objects.nonNull(defendant.getPersonDefendant().getBailStatus())) {
            defendantDetails.setDefendantRemandStatus(defendant.getPersonDefendant().getBailStatus().getDescription());
        }

        return defendantDetails;
    }


    public static List<HearingApplicationDetail> getApplicationDetails(final Hearing hearing) {
        final List<HearingApplicationDetail> applicationDetails = new ArrayList<>();
        final List<CourtApplication> courtApplications = hearing.getCourtApplications();

        if (Objects.nonNull(courtApplications)) {
            courtApplications.stream().forEach(courtApplication -> {
                final HearingApplicationDetail applicationDetail = new HearingApplicationDetail();
                applicationDetail.setApplicationId(courtApplication.getId());
                applicationDetail.setApplicationReference(courtApplication.getApplicationReference());
                final CourtApplicationParty subject = courtApplication.getSubject();
                final HearingDefendantDetail defendantDetails = getApplicationSubject(subject);
                applicationDetail.setSubject(defendantDetails);
                applicationDetails.add(applicationDetail);

            });
        }
        return applicationDetails;
    }

    public static HearingDefendantDetail getApplicationSubject(final CourtApplicationParty subject) {
        final HearingDefendantDetail defendantDetails = new HearingDefendantDetail();
        defendantDetails.setDefendantId(subject.getId());
        if (Objects.nonNull(subject.getPersonDetails())) {
            defendantDetails.setDefendantFirstName(subject.getPersonDetails().getFirstName());
            defendantDetails.setDefendantLastName(subject.getPersonDetails().getLastName());
        }
        if (Objects.nonNull(subject.getMasterDefendant()) && Objects.nonNull(subject.getMasterDefendant().getPersonDefendant()) && Objects.nonNull(subject.getMasterDefendant().getPersonDefendant().getBailStatus())) {
            defendantDetails.setDefendantRemandStatus(subject.getMasterDefendant().getPersonDefendant().getBailStatus().getDescription());
        }
        if(Objects.nonNull(subject.getPersonDetails())) {
            defendantDetails.setInterpreterLanguageNeeds(subject.getPersonDetails().getInterpreterLanguageNeeds());
        }
        return defendantDetails;
    }
}