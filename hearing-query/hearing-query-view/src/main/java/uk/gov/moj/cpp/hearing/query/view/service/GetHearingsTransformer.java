package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.*;
import uk.gov.justice.hearing.courts.*;

import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;

public class GetHearingsTransformer {

    public HearingSummaries.Builder summary(final Hearing hearing) {
        return buildHearingSummary(hearing)
                .withHearingDays(hearing.getHearingDays())
                .withProsecutionCaseSummaries(
                        hearing.getProsecutionCases() == null ? emptyList() :
                                hearing.getProsecutionCases().stream().map(pc -> summary(pc).build())
                                        .collect(Collectors.toList())
                );
    }

    public HearingSummaries.Builder summaryForHearingsForToday(final Hearing hearing) {
        return buildHearingSummary(hearing)
                .withHearingDays(getHearingDaysForToday(hearing.getHearingDays()))
                .withCourtCentreId(hearing.getCourtCentre().getId())
                .withRoomId(hearing.getCourtCentre().getRoomId())
                .withProsecutionCaseSummaries(
                        hearing.getProsecutionCases() == null ? emptyList() :
                                hearing.getProsecutionCases().stream().map(pc -> summaryForToday(pc).build())
                                        .collect(Collectors.toList())
                );
    }

    private HearingSummaries.Builder buildHearingSummary(final Hearing hearing) {
        return HearingSummaries.hearingSummaries()
                .withType(hearing.getType())
                .withId(hearing.getId())
                .withCourtCentre(hearing.getCourtCentre())
                .withReportingRestrictionReason(hearing.getReportingRestrictionReason())
                .withHearingLanguage(hearing.getHearingLanguage().name())
                .withJurisdictionType(jurisdictionType(hearing.getJurisdictionType()))
                .withHasSharedResults(hearing.getHasSharedResults())
                .withCourtApplicationSummaries(
                        hearing.getCourtApplications() == null ? emptyList() :
                                hearing.getCourtApplications().stream().map(ca -> summary(ca).build())
                                        .collect(Collectors.toList())

                );
    }

    private List<HearingDay> getHearingDaysForToday(final List<HearingDay> hearingDays) {
        return hearingDays.stream().filter(hearingDay ->
                now().equals(hearingDay.getSittingDay().toLocalDate())
        ).collect(Collectors.toList());
    }

    private Defendants.Builder summary(final Defendant defendant) {
        final Defendants.Builder result = Defendants.defendants();
        result.withId(defendant.getId());
        result.withMasterDefendantId(defendant.getMasterDefendantId());
        result.withCourtProceedingsInitiated(defendant.getCourtProceedingsInitiated());
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

    private Defendants.Builder summaryForToday(final Defendant defendant) {
        final Defendants.Builder result = Defendants.defendants();
        result.withId(defendant.getId());
        if (defendant.getPersonDefendant() != null && defendant.getPersonDefendant().getPersonDetails() != null) {
            result.withFirstName(defendant.getPersonDefendant().getPersonDetails().getFirstName());
            result.withMiddleName(defendant.getPersonDefendant().getPersonDetails().getMiddleName());
            result.withLastName(defendant.getPersonDefendant().getPersonDetails().getLastName());
            result.withDateOfBirth(defendant.getPersonDefendant().getPersonDetails().getDateOfBirth());
        }
        if (defendant.getLegalEntityDefendant() != null && defendant.getLegalEntityDefendant().getOrganisation() != null) {
            result.withOrganisationName(defendant.getLegalEntityDefendant().getOrganisation().getName());
        }
        result.withSynonym("is this from aliases ?");
        result.withOffences(defendant.getOffences() == null ? emptyList() :
                defendant.getOffences().stream().map(o -> summaryForToday(o).build())
                        .collect(Collectors.toList()));
        return result;
    }

    private Offences.Builder summaryForToday(final Offence offence) {
        final Offences.Builder result = Offences.offences();

        result.withId(offence.getId());
        result.withOffenceTitle(offence.getOffenceTitle());
        result.withOffenceTitleWelsh(offence.getOffenceTitleWelsh());
        result.withWording(offence.getWording());
        result.withWordingWelsh(offence.getWordingWelsh());

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

    private ProsecutionCaseSummaries.Builder summaryForToday(final ProsecutionCase prosecutionCase) {
        return ProsecutionCaseSummaries.prosecutionCaseSummaries()
                .withId(prosecutionCase.getId())
                .withProsecutionCaseIdentifier(prosecutionCase.getProsecutionCaseIdentifier())
                .withDefendants(prosecutionCase.getDefendants() == null ? emptyList() :
                        prosecutionCase.getDefendants().stream().map(d -> summaryForToday(d).build())
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
