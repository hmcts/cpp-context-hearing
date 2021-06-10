package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.hearing.courts.Applicant;
import uk.gov.justice.hearing.courts.CaseSummaries;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.Defendants;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.Offences;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.justice.hearing.courts.ReportingRestrictions;
import uk.gov.justice.hearing.courts.Respondents;
import uk.gov.justice.hearing.courts.Subject;
import uk.gov.justice.hearing.courts.Type;

import java.util.List;
import java.util.stream.Collectors;

public class GetHearingsTransformer {

    public HearingSummaries.Builder summary(final Hearing hearing) {
        return buildHearingSummary(hearing)
                .withHearingDays(hearing.getHearingDays())
                .withCourtApplicationSummaries(isEmpty(hearing.getCourtApplications()) ? emptyList() :
                        hearing.getCourtApplications().stream().map(courtApplication -> summary(courtApplication).build())
                                .collect(toList()))
                .withProsecutionCaseSummaries(
                        isEmpty(hearing.getProsecutionCases()) ? emptyList() :
                                hearing.getProsecutionCases().stream().map(pc -> summary(pc).build())
                                        .collect(toList())
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
                                        .collect(toList())
                );
    }

    private HearingSummaries.Builder buildHearingSummary(final Hearing hearing) {
        return HearingSummaries.hearingSummaries()
                .withType(hearing.getType())
                .withId(hearing.getId())
                .withCourtCentre(hearing.getCourtCentre())
                .withReportingRestrictionReason(hearing.getReportingRestrictionReason())
                .withHearingLanguage(hearing.getHearingLanguage().name())
                .withJurisdictionType(hearing.getJurisdictionType())
                .withHasSharedResults(hearing.getHasSharedResults())
                .withCourtApplicationSummaries(
                        hearing.getCourtApplications() == null ? emptyList() :
                                hearing.getCourtApplications().stream().map(ca -> summary(ca).build())
                                        .collect(toList())

                );
    }

    private List<HearingDay> getHearingDaysForToday(final List<HearingDay> hearingDays) {
        return hearingDays.stream().filter(hearingDay ->
                now().equals(hearingDay.getSittingDay().toLocalDate())
        ).collect(toList());
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
        if (defendant.getOffences() != null) {
            result.withOffences(defendant.getOffences().stream().map(o -> summaryWithReportingRestrictions(o).build())
                    .collect(toList()));
        }
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
                        .collect(toList()));
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

    private Offences.Builder summaryWithReportingRestrictions(final Offence offence) {
        final Offences.Builder result = Offences.offences();

        result.withId(offence.getId());
        result.withOffenceTitle(offence.getOffenceTitle());
        result.withOffenceTitleWelsh(offence.getOffenceTitleWelsh());
        result.withWording(offence.getWording());
        result.withWordingWelsh(offence.getWordingWelsh());
        if (offence.getReportingRestrictions() != null) {
            result.withReportingRestrictions(offence.getReportingRestrictions().stream()
                    .map(reportingRestriction -> summaryReportingRestriction(reportingRestriction).build())
                    .collect(toList()));
        }

        return result;
    }

    private ReportingRestrictions.Builder summaryReportingRestriction(final ReportingRestriction reportingRestriction) {
        final ReportingRestrictions.Builder result = ReportingRestrictions.reportingRestrictions();
        result.withId(reportingRestriction.getId());
        result.withJudicialResultId(reportingRestriction.getJudicialResultId());
        result.withLabel(reportingRestriction.getLabel());
        result.withOrderedDate(reportingRestriction.getOrderedDate());

        return result;
    }


    private ProsecutionCaseSummaries.Builder summary(final ProsecutionCase prosecutionCase) {
        return ProsecutionCaseSummaries.prosecutionCaseSummaries()
                .withId(prosecutionCase.getId())
                .withProsecutionCaseIdentifier(prosecutionCase.getProsecutionCaseIdentifier())
                .withDefendants(prosecutionCase.getDefendants() == null ? emptyList() :
                        prosecutionCase.getDefendants().stream().map(d -> summary(d).build())
                                .collect(toList()));
    }

    private ProsecutionCaseSummaries.Builder summaryForToday(final ProsecutionCase prosecutionCase) {
        return ProsecutionCaseSummaries.prosecutionCaseSummaries()
                .withId(prosecutionCase.getId())
                .withProsecutionCaseIdentifier(prosecutionCase.getProsecutionCaseIdentifier())
                .withDefendants(prosecutionCase.getDefendants() == null ? emptyList() :
                        prosecutionCase.getDefendants().stream().map(d -> summaryForToday(d).build())
                                .collect(toList()));
    }

    private Applicant.Builder applicantSummary(final CourtApplicationParty courtApplicationParty) {
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
        } else if (courtApplicationParty.getProsecutingAuthority() != null) {
            result.withOrganisationName(courtApplicationParty.getProsecutingAuthority().getName());
        }


        return result;
    }

    private Subject.Builder subjectSummary(final CourtApplicationParty courtApplicationParty) {
        final Subject.Builder result = Subject.subject();
        result.withId(courtApplicationParty.getId());
        result.withSynonym(courtApplicationParty.getSynonym());

        if (nonNull(courtApplicationParty.getMasterDefendant())) {
            final MasterDefendant masterDefendant = courtApplicationParty.getMasterDefendant();
            result.withMasterDefendantId(masterDefendant.getMasterDefendantId());
            if (nonNull(masterDefendant.getPersonDefendant())) {
                result.withFirstName(masterDefendant.getPersonDefendant().getPersonDetails().getFirstName());
                result.withMiddleName(masterDefendant.getPersonDefendant().getPersonDetails().getMiddleName());
                result.withLastName(masterDefendant.getPersonDefendant().getPersonDetails().getLastName());
            } else if (nonNull(masterDefendant.getLegalEntityDefendant())) {
                result.withOrganisationName(masterDefendant.getLegalEntityDefendant().getOrganisation().getName());
            }
        }

        if (courtApplicationParty.getPersonDetails() != null) {
            result.withFirstName(courtApplicationParty.getPersonDetails().getFirstName());
            result.withMiddleName(courtApplicationParty.getPersonDetails().getMiddleName());
            result.withLastName(courtApplicationParty.getPersonDetails().getLastName());
        }

        if (courtApplicationParty.getOrganisation() != null) {
            result.withOrganisationName(courtApplicationParty.getOrganisation().getName());
        } else if (courtApplicationParty.getProsecutingAuthority() != null) {
            result.withOrganisationName(courtApplicationParty.getProsecutingAuthority().getName());
        }

        return result;
    }

    private Respondents.Builder respondentSummary(final CourtApplicationParty courtApplicationParty) {
        final Respondents.Builder result = Respondents.respondents();
        result.withId(courtApplicationParty.getId());
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

    private CourtApplicationSummaries.Builder summary(final CourtApplication courtApplication) {
        final CourtApplicationType type = courtApplication.getType();
        final CourtApplicationSummaries.Builder result = CourtApplicationSummaries.courtApplicationSummaries();
        result.withId(courtApplication.getId());
        result.withApplicationReference(courtApplication.getApplicationReference());
        result.withApplicant(applicantSummary(courtApplication.getApplicant()).build());
        result.withSubject(subjectSummary(courtApplication.getSubject()).build());
        result.withParentApplicationId(courtApplication.getParentApplicationId());
        result.withType(courtApplication.getType() != null ?
                Type.type().withType(type.getType())
                        .withLegislation(type.getLegislation())
                        .withTypeWelsh(type.getTypeWelsh())
                        .build()
                : null);
        result.withRespondents(courtApplication.getRespondents() == null ? emptyList() :
                courtApplication.getRespondents().stream().map(ca -> respondentSummary(ca).build())
                        .collect(toList())
        );

        if (isNotEmpty(courtApplication.getCourtApplicationCases())) {
            result.withCaseSummaries(courtApplication.getCourtApplicationCases().stream()
                    .map(cac -> CaseSummaries.caseSummaries()
                            .withId(cac.getProsecutionCaseId())
                            .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                    .withCaseURN(cac.getProsecutionCaseIdentifier().getCaseURN())
                                    .withProsecutionAuthorityReference(cac.getProsecutionCaseIdentifier().getProsecutionAuthorityReference())
                                    .withProsecutionAuthorityCode(cac.getProsecutionCaseIdentifier().getProsecutionAuthorityCode())
                                    .withProsecutionAuthorityId(cac.getProsecutionCaseIdentifier().getProsecutionAuthorityId())
                                    .build())
                            .build())
                    .collect(Collectors.toList()));
        } else if (nonNull(courtApplication.getCourtOrder())) {
            result.withCaseSummaries(courtApplication.getCourtOrder().getCourtOrderOffences().stream()
                    .map(coo -> CaseSummaries.caseSummaries()
                            .withId(coo.getProsecutionCaseId())
                            .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                    .withCaseURN(coo.getProsecutionCaseIdentifier().getCaseURN())
                                    .withProsecutionAuthorityReference(coo.getProsecutionCaseIdentifier().getProsecutionAuthorityReference())
                                    .withProsecutionAuthorityCode(coo.getProsecutionCaseIdentifier().getProsecutionAuthorityCode())
                                    .withProsecutionAuthorityId(coo.getProsecutionCaseIdentifier().getProsecutionAuthorityId())
                                    .build())
                            .build())
                    .collect(toList()));
        }
        return result;
    }
}
