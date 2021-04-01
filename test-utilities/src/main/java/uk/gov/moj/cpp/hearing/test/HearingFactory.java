package uk.gov.moj.cpp.hearing.test;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.core.courts.CourtApplicationCase.courtApplicationCase;
import static uk.gov.justice.core.courts.CourtOrder.courtOrder;
import static uk.gov.justice.core.courts.CourtOrderOffence.courtOrderOffence;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.BreachType;
import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.Jurisdiction;
import uk.gov.justice.core.courts.LinkType;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceActiveOrder;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.SummonsTemplateType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@SuppressWarnings({"squid:CommentedOutCodeLine", "squid:S1172"})
public class HearingFactory {

    private static final String APPLICATION_REFERENCE = "12AA3456716";

    public ProsecutionCase.Builder prosecutionCase() {
        return ProsecutionCase.prosecutionCase();
    }

    public Person.Builder person() {
        return Person.person()
                .withGender(Gender.FEMALE)
                .withFirstName("Lauren")
                .withMiddleName("Mia")
                .withLastName("Michelle");
    }

    public Person.Builder person2() {
        return Person.person()
                .withGender(Gender.FEMALE)
                .withFirstName("Nina")
                .withLastName("Turner");
    }

    public Person.Builder person3() {
        return Person.person()
                .withGender(Gender.MALE)
                .withFirstName("Gerald")
                .withLastName("Harrison");
    }

    public Organisation organisation() {
        return Organisation.organisation()
                .withName("OrganisationName")
                .build();
    }

    public Defendant defendant() {
        return defendant(randomUUID(), randomUUID());
    }

    public Defendant defendant(UUID defendantId, UUID caseId) {
        return Defendant.defendant()
                .withId(defendantId)
                .withMasterDefendantId(defendantId)
                .withProsecutionCaseId(caseId)
                .withCourtProceedingsInitiated(ZonedDateTime.now())
                .build();
    }

    public MasterDefendant masterDefendant(UUID masterDefendantId, UUID caseId) {
        return MasterDefendant.masterDefendant()
                .withMasterDefendantId(masterDefendantId)
                .withPersonDefendant(PersonDefendant.personDefendant().build())
                .build();
    }

    public CourtApplicationParty.Builder courtApplicationDefendant(UUID defendantId, UUID caseId) {
        return CourtApplicationParty.courtApplicationParty()
                .withMasterDefendant(masterDefendant(defendantId, caseId))
                .withSummonsRequired(false)
                .withNotificationRequired(false)
                .withId(randomUUID())
                .withPersonDetails(person().build());
    }

    public CourtApplicationParty.Builder courtApplicationParty() {
        return courtApplicationParty(person().build());
    }

    public CourtApplicationParty.Builder courtApplicationParty2() {
        return courtApplicationParty(person2().build());
    }

    public CourtApplicationParty.Builder courtApplicationParty3() {
        return courtApplicationParty(person3().build());
    }

    public CourtApplicationParty.Builder courtApplicationParty(Person person) {
        return CourtApplicationParty.courtApplicationParty()
                .withOrganisation(organisation())
                .withId(randomUUID())
                .withSummonsRequired(false)
                .withNotificationRequired(false)
                .withProsecutingAuthority(ProsecutingAuthority.prosecutingAuthority()
                        .withProsecutionAuthorityId(randomUUID())
                        .withProsecutionAuthorityCode(STRING.next())
                        .build())
                .withPersonDetails(person);
    }

    public CourtApplicationType.Builder courtApplicationType(final UUID id) {
        return CourtApplicationType.courtApplicationType()
                .withId(id)
                .withType("applicationType")
                .withCode("appCode")
                .withLegislation("appLegislation")
                .withCategoryCode("appCategory")
                .withLinkType(LinkType.LINKED)
                .withJurisdiction(Jurisdiction.EITHER)
                .withSummonsTemplateType(SummonsTemplateType.BREACH)
                .withBreachType(BreachType.NOT_APPLICABLE)
                .withAppealFlag(false)
                .withApplicantAppellantFlag(false)
                .withPleaApplicableFlag(false)
                .withCommrOfOathFlag(false)
                .withCourtOfAppealFlag(false)
                .withCourtExtractAvlFlag(false)
                .withProsecutorThirdPartyFlag(false)
                .withSpiOutApplicableFlag(false)
                .withOffenceActiveOrder(OffenceActiveOrder.NOT_APPLICABLE);
    }

    public CourtApplicationType.Builder courtApplicationType() {
        return courtApplicationType(randomUUID());
    }

    public CourtApplication.Builder courtApplication() {
        return courtApplication(courtApplicationParty().build());
    }

    public CourtApplication.Builder courtApplicationWithDefendantParty(final UUID defendantId, final UUID caseId, final UUID courtApplicationTypeId) {
        return courtApplication(courtApplicationDefendant(defendantId, caseId).build(), courtApplicationTypeId);
    }

    public CourtApplication.Builder courtApplication(CourtApplicationParty party) {
        return courtApplication(party, randomUUID());
    }

    public CourtApplication.Builder courtApplication(final CourtApplicationParty party, final UUID courtApplicationTypeId) {

        return CourtApplication.courtApplication()
                .withCourtOrder(getCourtOrder())
                .withId(randomUUID())
                .withCourtApplicationCases(asList(courtApplicationCase()
                        .withProsecutionCaseId(randomUUID())
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                .withProsecutionAuthorityCode("prodAuthCode")
                                .withProsecutionAuthorityId(randomUUID())
                                .withProsecutionAuthorityReference("prosecutionAuthorityReference").build())
                        .withIsSJP(false)
                        .withCaseStatus("INACTIVE")
                        .withOffences(asList(getOffence())).build()))
                .withApplicant(party)
                .withApplicationReceivedDate(now())
                .withType(courtApplicationType(courtApplicationTypeId).build())
                .withRespondents(asList(courtApplicationParty2().build(),
                        courtApplicationParty3().build()
                ))
                .withSubject(party)
                .withThirdParties(asList(courtApplicationParty2().build(),
                        courtApplicationParty3().build()))
                .withApplicationReference(APPLICATION_REFERENCE)
                .withApplicationStatus(ApplicationStatus.DRAFT);

    }

    private Offence getOffence() {
        return offence().withId(randomUUID())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceCode("offenceCode")
                .withOffenceTitle("offenceTitle")
                .withWording("wording")
                .withStartDate(now())
                .build();
    }

    private CourtOrder getCourtOrder() {
        return courtOrder()
                .withId(randomUUID())
                .withJudicialResultTypeId(randomUUID())
                .withLabel("label")
                .withOrderDate(now())
                .withStartDate(now().plusDays(1))
                .withOrderingCourt(CourtCentre.courtCentre()
                        .withId(randomUUID())
                        .withName("courtName").build())
                .withOrderingHearingId(randomUUID())
                .withIsSJPOrder(false)
                .withCanBeSubjectOfBreachProceedings(false)
                .withCanBeSubjectOfVariationProceedings(false)
                .withCourtOrderOffences(asList(getCourtOrderOffence())).build();
    }

    private CourtOrderOffence getCourtOrderOffence() {
        return courtOrderOffence().withProsecutionCaseId(randomUUID()).withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                .withProsecutionAuthorityCode("prodAuthCode")
                .withProsecutionAuthorityId(randomUUID())
                .withProsecutionAuthorityReference("prosecutionAuthorityReference").build()).withOffence(getOffence()).build();
    }

    @SuppressWarnings({"squid:UnusedPrivateMethod"})
    private JudicialResult getJudicialResult() {
        return judicialResult()
                .withOrderedHearingId(randomUUID())
                .withLabel("label")
                .withIsAdjournmentResult(false)
                .withIsFinancialResult(false)
                .withIsConvictedResult(false)
                .withIsAvailableForCourtExtract(false)
                .withOrderedDate(now())
                .withCategory(Category.FINAL)
                .withResultText("resultText")
                .withTerminatesOffenceProceedings(false)
                .withLifeDuration(false)
                .withPublishedForNows(false)
                .withRollUpPrompts(false)
                .withPublishedAsAPrompt(false)
                .withExcludedFromResults(false)
                .withAlwaysPublished(false)
                .withUrgent(false)
                .withD20(false)
                .withJudicialResultId(randomUUID())
                .withJudicialResultTypeId(randomUUID())
                .build();
    }

    public CourtApplication.Builder linkedCourtApplication(final UUID linkedCaseId) {

        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicant(courtApplicationParty().build())
                .withApplicationReceivedDate(now())
                .withType(courtApplicationType()
                        .build())
                .withRespondents(asList(courtApplicationParty2().build(),
                        courtApplicationParty3().build()
                ))
                .withSubject(courtApplicationParty().build())
                .withApplicationReference(APPLICATION_REFERENCE)
                .withApplicationStatus(ApplicationStatus.DRAFT);
    }

    public CourtApplication.Builder standAloneChildCourtApplication(final UUID parentApplicationId) {

        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicant(courtApplicationParty().build())
                .withApplicationReceivedDate(now())
                .withType(courtApplicationType()
                        .build())
                .withRespondents(asList(courtApplicationParty2().build(),
                        courtApplicationParty3().build()
                ))
                .withSubject(courtApplicationParty().build())
                .withApplicationReference(APPLICATION_REFERENCE)
                .withApplicationStatus(ApplicationStatus.DRAFT)
                .withParentApplicationId(parentApplicationId);

    }

    public HearingType.Builder standaloneApplicationHearingType() {
        return HearingType.hearingType()
                .withDescription("Application")
                .withId(randomUUID());

    }

    private ZonedDateTime zonedDateTime(String str, String format) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        final LocalDate date = LocalDate.parse(str, formatter);
        return date.atStartOfDay(ZoneId.systemDefault());
    }


    public HearingDay.Builder hearingDay() {
        return HearingDay.hearingDay()
                .withSittingDay(zonedDateTime("07/06/2017", "dd/MM/yyyy"))
                .withListingSequence(2)
                .withListedDurationMinutes(23);
    }

    public JudicialRole.Builder judicialRole() {
        return JudicialRole.judicialRole()
                .withFirstName("Tina")
                .withLastName("Turner");
    }

    public JudicialRole.Builder judicialRole2() {
        return JudicialRole.judicialRole()
                .withFirstName("Gerald")
                .withLastName("Harrison");
    }

    public JudicialRole.Builder judicialRole3() {
        return JudicialRole.judicialRole()
                .withFirstName("Bob")
                .withLastName("Roberts");
    }

    public Hearing.Builder createStandaloneApplicationHearing() {
        return Hearing.hearing()
                .withId(randomUUID())
                .withType(standaloneApplicationHearingType().build())
                .withHearingDays(asList(hearingDay().build()))
                .withProsecutionCases(asList(prosecutionCase().build()))
                .withCourtApplications(asList(courtApplication().build()))
                .withJudiciary(asList(judicialRole().build(), judicialRole2().build(), judicialRole3().build()))
                ;
    }

}