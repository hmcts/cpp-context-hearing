package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationRespondent;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.hearing.courts.Applicant;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.Defendants;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.justice.hearing.courts.Respondents;

import java.util.stream.Collectors;

public class GetHearingsTransformer {

    public HearingSummaries.Builder summary(final Hearing hearing) {
        return HearingSummaries.hearingSummaries()
                .withType(hearing.getType())
                .withId(hearing.getId())
                .withCourtCentre(hearing.getCourtCentre())
                .withReportingRestrictionReason(hearing.getReportingRestrictionReason())
                .withHearingLanguage(hearing.getHearingLanguage().name())
                .withJurisdictionType(jurisdictionType(hearing.getJurisdictionType()))
                .withHearingDays(hearing.getHearingDays())
                .withHasSharedResults(hearing.getHasSharedResults())
                .withProsecutionCaseSummaries(
                        hearing.getProsecutionCases() == null ? emptyList() :
                                hearing.getProsecutionCases().stream().map(pc -> summary(pc).build())
                                        .collect(Collectors.toList())
                )
                .withCourtApplicationSummaries(
                        hearing.getCourtApplications() == null ? emptyList() :
                                hearing.getCourtApplications().stream().map(ca -> summary(ca).build())
                                        .collect(Collectors.toList())

                );
    }

    private Defendants.Builder summary(final Defendant defendant) {
        final Defendants.Builder result = Defendants.defendants();
        result.withId(defendant.getId());
        if (defendant.getPersonDefendant() != null && defendant.getPersonDefendant().getPersonDetails() != null) {
            result.withFirstName(defendant.getPersonDefendant().getPersonDetails().getFirstName());
            result.withMiddleName(defendant.getPersonDefendant().getPersonDetails().getMiddleName());
            result.withLastName(defendant.getPersonDefendant().getPersonDetails().getLastName());
        }
        if (defendant.getLegalEntityDefendant() != null && defendant.getLegalEntityDefendant().getOrganisation() != null) {
            result.withOrganisationName(defendant.getLegalEntityDefendant().getOrganisation().getName());
        }
        result.withSynonym("is this from aliases ?");
        return result;
    }

    private uk.gov.justice.hearing.courts.JurisdictionType jurisdictionType(final JurisdictionType jurisdictionType) {
        if (CROWN.equals(jurisdictionType)) {
            return uk.gov.justice.hearing.courts.JurisdictionType.CROWN;
        } else {
            return uk.gov.justice.hearing.courts.JurisdictionType.MAGISTRATES;
        }
    }

    private ProsecutionCaseSummaries.Builder summary(final ProsecutionCase prosecutionCase) {
        return ProsecutionCaseSummaries.prosecutionCaseSummaries()
                .withId(prosecutionCase.getId())
                .withProsecutionCaseIdentifier(prosecutionCase.getProsecutionCaseIdentifier())
                .withDefendants(prosecutionCase.getDefendants() == null ? emptyList() :
                        prosecutionCase.getDefendants().stream().map(d -> summary(d).build())
                                .collect(Collectors.toList()));
    }

    private Applicant.Builder summary(final CourtApplicationParty courtApplicationParty) {
        final Applicant.Builder result = Applicant.applicant();
        result.withId(courtApplicationParty.getId());
        result.withSynonym(courtApplicationParty.getSynonym());
        if (courtApplicationParty.getPersonDetails() != null) {
            result.withFirstName(courtApplicationParty.getPersonDetails().getFirstName());
            result.withLastName(courtApplicationParty.getPersonDetails().getLastName());
            result.withMiddleName(courtApplicationParty.getPersonDetails().getMiddleName());
        }
        if (courtApplicationParty.getOrganisation() != null) {
            result.withOrganisationName(courtApplicationParty.getOrganisation().getName());
        }
        return result;
    }

    private Respondents.Builder summary(final CourtApplicationRespondent courtApplicationRespondent) {
        final Respondents.Builder result = Respondents.respondents();
        result.withId(courtApplicationRespondent.getPartyDetails().getId());
        if (courtApplicationRespondent.getPartyDetails() != null && courtApplicationRespondent.getPartyDetails().getPersonDetails() != null) {
            result.withFirstName(courtApplicationRespondent.getPartyDetails().getPersonDetails().getFirstName());
            result.withLastName(courtApplicationRespondent.getPartyDetails().getPersonDetails().getLastName());
            result.withMiddleName(courtApplicationRespondent.getPartyDetails().getPersonDetails().getMiddleName());
        }
        if (courtApplicationRespondent.getPartyDetails() != null && courtApplicationRespondent.getPartyDetails().getOrganisation() != null) {
            result.withOrganisationName(courtApplicationRespondent.getPartyDetails().getOrganisation().getName());
        }
        return result;
    }

    private CourtApplicationSummaries.Builder summary(final CourtApplication courtApplication) {
        final CourtApplicationSummaries.Builder result = CourtApplicationSummaries.courtApplicationSummaries();
        result.withId(courtApplication.getId());
        result.withApplicationReference(courtApplication.getApplicationReference());
        result.withApplicant(summary(courtApplication.getApplicant()).build());
        result.withParentApplicationId(courtApplication.getParentApplicationId());
        result.withRespondents(courtApplication.getRespondents() == null ? emptyList() :
                courtApplication.getRespondents().stream().map(ca -> summary(ca).build())
                        .collect(Collectors.toList())
        );
        return result;
    }


}
