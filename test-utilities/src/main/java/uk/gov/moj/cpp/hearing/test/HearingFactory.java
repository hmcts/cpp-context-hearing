package uk.gov.moj.cpp.hearing.test;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

import uk.gov.justice.core.courts.ApplicationJurisdictionType;
import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationRespondent;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.LinkType;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

    public CourtApplicationParty.Builder courtApplicationDefendant(UUID defendantId, UUID caseId) {
        return CourtApplicationParty.courtApplicationParty()
                .withDefendant(defendant(defendantId, caseId))
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
                .withPersonDetails(person);
    }


    public CourtApplicationRespondent.Builder courtApplicationRespondant1() {
        return CourtApplicationRespondent.courtApplicationRespondent()
                .withPartyDetails(courtApplicationParty2().build())
                .withPartyDetails(courtApplicationParty3().build());
    }

    public CourtApplicationType.Builder courtApplicationType(final UUID id) {
        return CourtApplicationType.courtApplicationType()
                .withId(id)
                .withApplicationType("applicationType")
                .withApplicationCode("appCode")
                .withApplicationLegislation("appLegislation")
                .withApplicationCategory("appCategory")
                .withLinkType(LinkType.EITHER)
                .withApplicationJurisdictionType(ApplicationJurisdictionType.EITHER);
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
                .withId(randomUUID())
                .withApplicant(party)
                .withApplicationReceivedDate(LocalDate.now())
                .withType(courtApplicationType(courtApplicationTypeId).build())
                .withRespondents(asList(courtApplicationRespondant1().build(),
                        courtApplicationRespondant1().build()
                ))
                .withApplicationReference(APPLICATION_REFERENCE)
                .withApplicationStatus(ApplicationStatus.DRAFT);

    }

    public CourtApplication.Builder linkedCourtApplication(final UUID linkedCaseId) {

        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicant(courtApplicationParty().build())
                .withApplicationReceivedDate(LocalDate.now())
                .withType(courtApplicationType()
                        .build())
                .withRespondents(asList(courtApplicationRespondant1().build(),
                        courtApplicationRespondant1().build()
                ))
                .withApplicationReference(APPLICATION_REFERENCE)
                .withApplicationStatus(ApplicationStatus.DRAFT)
                .withLinkedCaseId(linkedCaseId);

    }

    public CourtApplication.Builder standAloneChildCourtApplication(final UUID parentApplicationId) {

        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicant(courtApplicationParty().build())
                .withApplicationReceivedDate(LocalDate.now())
                .withType(courtApplicationType()
                        .build())
                .withRespondents(asList(courtApplicationRespondant1().build(),
                        courtApplicationRespondant1().build()
                ))
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
