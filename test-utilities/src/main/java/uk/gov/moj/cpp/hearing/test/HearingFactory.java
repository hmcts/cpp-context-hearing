package uk.gov.moj.cpp.hearing.test;

import static java.util.Arrays.asList;

import uk.gov.justice.core.courts.ApplicationJurisdictionType;
import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationRespondent;
import uk.gov.justice.core.courts.CourtApplicationType;
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

    public CourtApplicationParty.Builder courtApplicationParty() {
        return CourtApplicationParty.courtApplicationParty()
                .withId(UUID.randomUUID())
                .withOrganisation(organisation())
                .withPersonDetails(person().build())
                ;
    }

    public CourtApplicationParty.Builder courtApplicationParty2() {
        return CourtApplicationParty.courtApplicationParty()
                .withOrganisation(organisation())
                .withId(UUID.randomUUID())
                .withPersonDetails(person2().build())
                ;
    }

    public CourtApplicationParty.Builder courtApplicationParty3() {
        return CourtApplicationParty.courtApplicationParty()
                .withOrganisation(organisation())
                .withId(UUID.randomUUID())
                .withPersonDetails(person3().build())
                ;
    }

    public CourtApplicationRespondent.Builder courtApplicationRespondant1() {
        return CourtApplicationRespondent.courtApplicationRespondent()
                .withPartyDetails(courtApplicationParty2().build())
                .withPartyDetails(courtApplicationParty3().build());
    }

    public CourtApplicationType.Builder courtApplicationType() {
        return CourtApplicationType.courtApplicationType()
                .withId(UUID.randomUUID())
                .withApplicationType("applicationType")
                .withApplicationCode("appCode")
                .withApplicationLegislation("appLegislation")
                .withApplicationCategory("appCategory")
                .withLinkType(LinkType.EITHER)
                .withApplicationJurisdictionType(ApplicationJurisdictionType.EITHER);
    }

    public CourtApplication.Builder courtApplication() {

        return CourtApplication.courtApplication()
                .withId(UUID.randomUUID())
                .withApplicant(courtApplicationParty().build())
                .withApplicationReceivedDate(LocalDate.now())
                .withType(courtApplicationType()
                        .build())
                .withRespondents(asList(courtApplicationRespondant1().build(),
                        courtApplicationRespondant1().build()
                ))
                .withApplicationReference("12AA3456716")
                .withApplicationStatus(ApplicationStatus.DRAFT);

    }

    public HearingType.Builder standaloneApplicationHearingType() {
        return HearingType.hearingType()
                .withDescription("Application")
                .withId(UUID.randomUUID());

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
                .withId(UUID.randomUUID())
                .withType(standaloneApplicationHearingType().build())
                .withHearingDays(asList(hearingDay().build()))
                .withProsecutionCases(asList(prosecutionCase().build()))
                .withCourtApplications(asList(courtApplication().build()))
                .withJudiciary(asList(judicialRole().build(), judicialRole2().build(), judicialRole3().build()))
                ;
    }

}
