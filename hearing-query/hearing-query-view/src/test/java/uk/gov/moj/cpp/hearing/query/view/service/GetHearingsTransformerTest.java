package uk.gov.moj.cpp.hearing.query.view.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationRespondent;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.hearing.courts.Applicant;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.Defendants;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.Offences;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.justice.hearing.courts.ReportingRestrictions;
import uk.gov.justice.hearing.courts.Respondents;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class GetHearingsTransformerTest {

    private final GetHearingsTransformer target = new GetHearingsTransformer();

    @Test
    public void test() {
        final Hearing hearing = CoreTestTemplates.hearing(CoreTestTemplates.defaultArguments()).build();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);
        courtApplication.setParentApplicationId(UUID.randomUUID());
        final HearingSummaries hearingSummary = target.summary(hearing).build();
        final CourtApplicationParty applicant = courtApplication.getApplicant();
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final ReportingRestriction reportingRestriction = defendant.getOffences().get(0).getReportingRestrictions().get(0);
        final CourtApplicationRespondent courtApplicationRespondent = courtApplication.getRespondents().get(0);
        final Person respondentPerson = courtApplicationRespondent.getPartyDetails().getPersonDetails();
        final CourtApplicationParty respondentParty = courtApplicationRespondent.getPartyDetails();

        assertThat(hearingSummary, isBean(HearingSummaries.class)
                .withValue(HearingSummaries::getId, hearing.getId())
                .withValue(HearingSummaries::getHearingLanguage, hearing.getHearingLanguage().name())
                .withValue(hs -> hs.getType().getId(), hearing.getType().getId())
                .withValue(hs -> hs.getJurisdictionType().name(), hearing.getJurisdictionType().name())
                .withValue(HearingSummaries::getHasSharedResults, hearing.getHasSharedResults())
                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                        .with(CourtApplicationSummaries::getApplicant, isBean(Applicant.class)
                                .withValue(Applicant::getFirstName, applicant.getPersonDetails().getFirstName())
                                .withValue(Applicant::getMiddleName, applicant.getPersonDetails().getMiddleName())
                                .withValue(Applicant::getLastName, applicant.getPersonDetails().getLastName())
                                .withValue(Applicant::getId, applicant.getId())
                                .withValue(Applicant::getOrganisationName, applicant.getOrganisation().getName())
                                .withValue(Applicant::getSynonym, applicant.getSynonym())
                        )
                        .with(CourtApplicationSummaries::getRespondents, first(isBean(Respondents.class)
                                .withValue(Respondents::getFirstName, respondentPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondentPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondentPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, respondentParty.getOrganisation().getName())
                        ))
                ))
                .with(HearingSummaries::getProsecutionCaseSummaries, first(isBean(ProsecutionCaseSummaries.class)
                        .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                        .withValue(ProsecutionCaseSummaries::getProsecutionCaseIdentifier, prosecutionCase.getProsecutionCaseIdentifier())
                            .with(ProsecutionCaseSummaries::getDefendants, first(isBean(Defendants.class)
                            .withValue(Defendants::getId, defendant.getId())
                            .withValue(Defendants::getMasterDefendantId, defendant.getMasterDefendantId())
                            .withValue(Defendants::getCourtProceedingsInitiated, defendant.getCourtProceedingsInitiated())))
                        .with(ProsecutionCaseSummaries::getDefendants, first(isBean(Defendants.class)
                                .with(Defendants::getOffences, first(isBean(Offences.class)
                                        .with(Offences::getReportingRestrictions, first(isBean(ReportingRestrictions.class)
                                                .withValue(ReportingRestrictions::getId, reportingRestriction.getId())
                                                .withValue(ReportingRestrictions::getJudicialResultId, reportingRestriction.getJudicialResultId())
                                                .withValue(ReportingRestrictions::getLabel, reportingRestriction.getLabel())
                                                .withValue(ReportingRestrictions::getOrderedDate, reportingRestriction.getOrderedDate())))))))
                ))
        );


    }
}
