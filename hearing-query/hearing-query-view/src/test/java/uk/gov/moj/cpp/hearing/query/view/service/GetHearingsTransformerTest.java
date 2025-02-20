package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.Boolean.TRUE;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.NUMBER_OF_GROUP_CASES;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Person;
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
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class GetHearingsTransformerTest {

    private final GetHearingsTransformer target = new GetHearingsTransformer();

    @Test
    public void shouldTransformHearing() {
        final Hearing hearing = CoreTestTemplates.hearing(CoreTestTemplates.defaultArguments()).build();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);
        courtApplication.setParentApplicationId(UUID.randomUUID());
        final HearingSummaries hearingSummary = target.summary(hearing).build();
        final CourtApplicationParty applicant = courtApplication.getApplicant();
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final ReportingRestriction reportingRestriction = defendant.getOffences().get(0).getReportingRestrictions().get(0);
        final CourtApplicationParty courtApplicationRespondent = courtApplication.getRespondents().get(0);
        final Person respondentPerson = courtApplicationRespondent.getPersonDetails();
        final CourtApplicationParty courtApplicationParty = courtApplication.getRespondents().get(0);
        final Person respondantPerson = courtApplicationParty.getPersonDetails();

        assertThat(hearingSummary, isBean(HearingSummaries.class)
                .withValue(HearingSummaries::getId, hearing.getId())
                .withValue(HearingSummaries::getHearingLanguage, hearing.getHearingLanguage().name())
                .withValue(HearingSummaries::getIsGroupProceedings, hearing.getIsGroupProceedings())
                .withValue(HearingSummaries::getNumberOfGroupCases, hearing.getNumberOfGroupCases())
                .withValue(hs -> hs.getType().getId(), hearing.getType().getId())
                .withValue(HearingSummaries::getHearingDays, hearing.getHearingDays())
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
                                .withValue(Respondents::getFirstName, respondantPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondantPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondantPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationParty.getOrganisation().getName())
                                .withValue(Respondents::getFirstName, respondentPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondentPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondentPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationRespondent.getOrganisation().getName())
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

    @Test
    public void shouldGetSummaryForHearingsForToday() {
        final Hearing hearing = CoreTestTemplates.hearing(CoreTestTemplates.defaultArguments()).build();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);
        courtApplication.setParentApplicationId(UUID.randomUUID());
        final HearingSummaries hearingSummary = target.summaryForHearingsForToday(hearing).build();
        final CourtApplicationParty applicant = courtApplication.getApplicant();
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final ReportingRestriction reportingRestriction = defendant.getOffences().get(0).getReportingRestrictions().get(0);
        final CourtApplicationParty courtApplicationRespondent = courtApplication.getRespondents().get(0);
        final Person respondentPerson = courtApplicationRespondent.getPersonDetails();
        final CourtApplicationParty courtApplicationParty = courtApplication.getRespondents().get(0);
        final Person respondantPerson = courtApplicationParty.getPersonDetails();

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
                                .withValue(Respondents::getFirstName, respondantPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondantPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondantPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationParty.getOrganisation().getName())
                                .withValue(Respondents::getFirstName, respondentPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondentPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondentPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationRespondent.getOrganisation().getName())
                        ))
                ))
                .with(HearingSummaries::getProsecutionCaseSummaries, first(isBean(ProsecutionCaseSummaries.class)
                        .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                        .withValue(ProsecutionCaseSummaries::getProsecutionCaseIdentifier, prosecutionCase.getProsecutionCaseIdentifier())
                        .with(ProsecutionCaseSummaries::getDefendants, first(isBean(Defendants.class)
                                .withValue(Defendants::getId, defendant.getId())))
                ))
        );
    }

    @Test
    public void shouldGetSummaryForHearingsForFuture() {
        final Hearing hearing = CoreTestTemplates.hearing(CoreTestTemplates.defaultArguments()).build();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);
        courtApplication.setParentApplicationId(UUID.randomUUID());
        final HearingSummaries hearingSummary = target.summaryForHearingsForFuture(hearing).build();
        final CourtApplicationParty applicant = courtApplication.getApplicant();
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final ReportingRestriction reportingRestriction = defendant.getOffences().get(0).getReportingRestrictions().get(0);
        final CourtApplicationParty courtApplicationRespondent = courtApplication.getRespondents().get(0);
        final Person respondentPerson = courtApplicationRespondent.getPersonDetails();
        final CourtApplicationParty courtApplicationParty = courtApplication.getRespondents().get(0);
        final Person respondantPerson = courtApplicationParty.getPersonDetails();

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
                                .withValue(Respondents::getFirstName, respondantPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondantPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondantPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationParty.getOrganisation().getName())
                                .withValue(Respondents::getFirstName, respondentPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondentPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondentPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationRespondent.getOrganisation().getName())
                        ))
                ))
                .with(HearingSummaries::getProsecutionCaseSummaries, first(isBean(ProsecutionCaseSummaries.class)
                        .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                        .withValue(ProsecutionCaseSummaries::getProsecutionCaseIdentifier, prosecutionCase.getProsecutionCaseIdentifier())
                        .with(ProsecutionCaseSummaries::getDefendants, first(isBean(Defendants.class)
                                .withValue(Defendants::getId, defendant.getId())))
                ))
        );
    }

    @Test
    public void shouldTransformHearingWithCourtApplicationCases() throws Exception {
        ZonedDateTime localDate = now().withZoneSameLocal(ZoneId.of("UTC"));
        final Hearing hearing = CoreTestTemplates.hearingWithParam(defaultArguments()
                        .setDefendantType(PERSON)
                        .setHearingLanguage(ENGLISH)
                        .setJurisdictionType(CROWN)
                        .setMinimumAssociatedPerson(true)
                        .setMinimumDefenceOrganisation(true), fromString(CoreTestTemplates.COURT_CENTRE_ID), randomUUID(), "CourtRoom 1",
                localDate.toLocalDate(), randomUUID(), randomUUID(), Optional.of(fromString("9cc41e45-b594-4ba6-906e-1a4626b08fed"))).build();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);
        final CourtApplicationParty applicant = courtApplication.getApplicant();
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final CourtApplicationParty courtApplicationParty = courtApplication.getRespondents().get(0);
        final Person respondantPerson = courtApplicationParty.getPersonDetails();
        final CourtApplicationCase applicationCase = courtApplication.getCourtApplicationCases().get(0);

        final HearingSummaries hearingSummary = target.summary(hearing).build();


        assertThat(hearingSummary, isBean(HearingSummaries.class)
                .with(HearingSummaries::getProsecutionCaseSummaries, first(isBean(ProsecutionCaseSummaries.class)
                        .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                        .withValue(ProsecutionCaseSummaries::getProsecutionCaseIdentifier, prosecutionCase.getProsecutionCaseIdentifier())
                        .with(ProsecutionCaseSummaries::getDefendants, first(isBean(Defendants.class)
                                .withValue(Defendants::getId, defendant.getId())
                                .withValue(Defendants::getMasterDefendantId, defendant.getMasterDefendantId())
                                .withValue(Defendants::getCourtProceedingsInitiated, defendant.getCourtProceedingsInitiated())))
                ))
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
                                .withValue(Respondents::getFirstName, respondantPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondantPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondantPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationParty.getOrganisation().getName())
                        ))
                        .with(CourtApplicationSummaries::getCaseSummaries, first(isBean(CaseSummaries.class)
                                .withValue(CaseSummaries::getId, applicationCase.getProsecutionCaseId())
                                .with(CaseSummaries::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                        .withValue(ProsecutionCaseIdentifier::getCaseURN, applicationCase.getProsecutionCaseIdentifier().getCaseURN())
                                        .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityId, applicationCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId())
                                        .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, applicationCase.getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))))
                        .with(CourtApplicationSummaries::getSubject, isBean(Subject.class)
                                .withValue(Subject::getId, courtApplication.getSubject().getId())
                                .withValue(Subject::getFirstName, courtApplication.getSubject().getPersonDetails().getFirstName())
                                .withValue(Subject::getLastName, courtApplication.getSubject().getPersonDetails().getLastName())
                                .withValue(Subject::getMiddleName, courtApplication.getSubject().getPersonDetails().getMiddleName())
                                .withValue(Subject::getOrganisationName, courtApplication.getSubject().getOrganisation().getName()))
                )));
    }

    @Test
    public void shouldTransformHearingWithCourtApplicationsWithCourtOrder() throws Exception {
        ZonedDateTime localDate = now().withZoneSameLocal(ZoneId.of("UTC"));
        final Hearing hearing = CoreTestTemplates.hearingWithCourtOrder(defaultArguments()
                        .setDefendantType(PERSON)
                        .setHearingLanguage(ENGLISH)
                        .setJurisdictionType(CROWN)
                        .setMinimumAssociatedPerson(true)
                        .setMinimumDefenceOrganisation(true), randomUUID(),
                randomUUID(), "CourtRoom 1", localDate.toLocalDate(), randomUUID(), randomUUID(),
                Optional.of(fromString("9cc41e45-b594-4ba6-906e-1a4626b08fed"))).build();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);
        final CourtApplicationParty applicant = courtApplication.getApplicant();
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final CourtApplicationParty courtApplicationParty = courtApplication.getRespondents().get(0);
        final Person respondantPerson = courtApplicationParty.getPersonDetails();
        final CourtOrder courtOrder = courtApplication.getCourtOrder();
        final HearingSummaries hearingSummary = target.summary(hearing).build();

        assertThat(hearingSummary, isBean(HearingSummaries.class)
                .with(HearingSummaries::getProsecutionCaseSummaries, first(isBean(ProsecutionCaseSummaries.class)
                        .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                        .withValue(ProsecutionCaseSummaries::getProsecutionCaseIdentifier, prosecutionCase.getProsecutionCaseIdentifier())
                        .with(ProsecutionCaseSummaries::getDefendants, first(isBean(Defendants.class)
                                .withValue(Defendants::getId, defendant.getId())
                                .withValue(Defendants::getMasterDefendantId, defendant.getMasterDefendantId())
                                .withValue(Defendants::getCourtProceedingsInitiated, defendant.getCourtProceedingsInitiated())))
                ))
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
                                .withValue(Respondents::getFirstName, respondantPerson.getFirstName())
                                .withValue(Respondents::getMiddleName, respondantPerson.getMiddleName())
                                .withValue(Respondents::getLastName, respondantPerson.getLastName())
                                .withValue(Respondents::getOrganisationName, courtApplicationParty.getOrganisation().getName())
                        ))
                        .with(CourtApplicationSummaries::getCaseSummaries, first(isBean(CaseSummaries.class)
                                .withValue(CaseSummaries::getId, courtOrder.getCourtOrderOffences().get(0).getProsecutionCaseId())
                                .with(CaseSummaries::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                        .withValue(ProsecutionCaseIdentifier::getCaseURN, courtOrder.getCourtOrderOffences().get(0).getProsecutionCaseIdentifier().getCaseURN())
                                        .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityId, courtOrder.getCourtOrderOffences().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())
                                        .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, courtOrder.getCourtOrderOffences().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))))
                        .with(CourtApplicationSummaries::getSubject, isBean(Subject.class)
                                .withValue(Subject::getId, courtApplication.getSubject().getId())
                                .withValue(Subject::getFirstName, courtApplication.getSubject().getPersonDetails().getFirstName())
                                .withValue(Subject::getLastName, courtApplication.getSubject().getPersonDetails().getLastName())
                                .withValue(Subject::getMiddleName, courtApplication.getSubject().getPersonDetails().getMiddleName())
                                .withValue(Subject::getOrganisationName, courtApplication.getSubject().getOrganisation().getName()))
                )));
    }

    @Test
    public void shouldTransformHearingWithCivilBulkCases_WhenSomeCasesRemoved() {
        final UUID groupId = randomUUID();

        final ProsecutionCase prosecutionCase1 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(true)
                .withIsGroupMaster(true)
                .build();

        final ProsecutionCase prosecutionCase2 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(true)
                .withIsGroupMaster(false)
                .build();

        final ProsecutionCase prosecutionCase3 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(false)
                .withIsGroupMaster(false)
                .build();

        final ProsecutionCase prosecutionCase4 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(true)
                .withIsGroupMaster(false)
                .build();

        final Hearing hearing = CoreTestTemplates.hearing(CoreTestTemplates.defaultArguments())
                .withIsGroupProceedings(TRUE)
                .withNumberOfGroupCases(NUMBER_OF_GROUP_CASES)
                .withProsecutionCases(asList(prosecutionCase1, prosecutionCase2, prosecutionCase3, prosecutionCase4))
                .build();

        final List<UUID> filteredCases = asList(prosecutionCase1.getId(), prosecutionCase3.getId());

        final HearingSummaries hearingSummary = target.summary(hearing).build();
        assertThat(hearingSummary.getIsGroupProceedings(), CoreMatchers.equalTo(TRUE));
        assertThat(hearingSummary.getNumberOfGroupCases().intValue(), CoreMatchers.equalTo(NUMBER_OF_GROUP_CASES));
        assertThat(hearingSummary.getProsecutionCaseSummaries().size(), equalTo(2));
        assertThat(hearingSummary.getProsecutionCaseSummaries().get(0).getIsCivil(), equalTo(true));
        assertTrue(filteredCases.contains(hearingSummary.getProsecutionCaseSummaries().get(0).getId()));
        assertTrue(filteredCases.contains(hearingSummary.getProsecutionCaseSummaries().get(1).getId()));

        final HearingSummaries hearingSummaryForToday = target.summaryForHearingsForToday(hearing).build();
        assertThat(hearingSummaryForToday.getIsGroupProceedings(), CoreMatchers.equalTo(TRUE));
        assertThat(hearingSummaryForToday.getNumberOfGroupCases().intValue(), CoreMatchers.equalTo(NUMBER_OF_GROUP_CASES));
        assertThat(hearingSummaryForToday.getProsecutionCaseSummaries().size(), equalTo(2));
        assertThat(hearingSummaryForToday.getProsecutionCaseSummaries().get(0).getIsCivil(), equalTo(true));
        assertTrue(filteredCases.contains(hearingSummaryForToday.getProsecutionCaseSummaries().get(0).getId()));
        assertTrue(filteredCases.contains(hearingSummaryForToday.getProsecutionCaseSummaries().get(1).getId()));
    }

    @Test
    public void shouldTransformHearingWithCivilBulkCases_WhenAllCasesRemoved() {
        final UUID groupId = randomUUID();

        final ProsecutionCase prosecutionCase1 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(false)
                .withIsGroupMaster(false)
                .build();

        final ProsecutionCase prosecutionCase2 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(false)
                .withIsGroupMaster(false)
                .build();

        final ProsecutionCase prosecutionCase3 = ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withIsCivil(true)
                .withGroupId(groupId)
                .withIsGroupMember(false)
                .withIsGroupMaster(false)
                .build();

        final Hearing hearing = CoreTestTemplates.hearing(CoreTestTemplates.defaultArguments())
                .withIsGroupProceedings(TRUE)
                .withNumberOfGroupCases(NUMBER_OF_GROUP_CASES)
                .withProsecutionCases(asList(prosecutionCase1, prosecutionCase2, prosecutionCase3))
                .build();

        final List<UUID> filteredCases = asList(prosecutionCase1.getId(), prosecutionCase2.getId(), prosecutionCase3.getId());

        final HearingSummaries hearingSummary = target.summary(hearing).build();
        assertThat(hearingSummary.getIsGroupProceedings(), CoreMatchers.equalTo(TRUE));
        assertThat(hearingSummary.getNumberOfGroupCases().intValue(), CoreMatchers.equalTo(NUMBER_OF_GROUP_CASES));
        assertThat(hearingSummary.getProsecutionCaseSummaries().size(), equalTo(3));
        assertThat(hearingSummary.getProsecutionCaseSummaries().get(0).getIsCivil(), equalTo(true));
        assertTrue(filteredCases.contains(hearingSummary.getProsecutionCaseSummaries().get(0).getId()));
        assertTrue(filteredCases.contains(hearingSummary.getProsecutionCaseSummaries().get(1).getId()));
        assertTrue(filteredCases.contains(hearingSummary.getProsecutionCaseSummaries().get(2).getId()));

        final HearingSummaries hearingSummaryForToday = target.summaryForHearingsForToday(hearing).build();
        assertThat(hearingSummaryForToday.getNumberOfGroupCases().intValue(), CoreMatchers.equalTo(NUMBER_OF_GROUP_CASES));
        assertThat(hearingSummaryForToday.getProsecutionCaseSummaries().size(), equalTo(3));
        assertThat(hearingSummaryForToday.getProsecutionCaseSummaries().get(0).getIsCivil(), equalTo(true));
        assertTrue(filteredCases.contains(hearingSummaryForToday.getProsecutionCaseSummaries().get(0).getId()));
        assertTrue(filteredCases.contains(hearingSummaryForToday.getProsecutionCaseSummaries().get(1).getId()));
        assertTrue(filteredCases.contains(hearingSummaryForToday.getProsecutionCaseSummaries().get(2).getId()));
    }
}
