package uk.gov.moj.cpp.hearing.query.view;

import static java.time.ZonedDateTime.parse;
import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.at;

import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HearingTestUtils {

    public static final LocalDate startDate = LocalDate.now();
    public static final ZonedDateTime START_DATE_1 = parse("2018-02-22T10:30:00Z");
    public static final ZonedDateTime END_DATE_1 = parse("2018-02-24T15:45:00Z");

    public static Hearing buildHearing() {
        final UUID hearingId = randomUUID();
        final Defendant defendant1 = buildDefendant1(hearingId);
        final Defendant defendant2 = buildDefendant2(hearingId);
        final ProsecutionCase prosecutionCase1 = buildLegalCase1(hearingId, asSet(defendant1, defendant2));
        final Hearing hearing = buildHearing1(hearingId, START_DATE_1, END_DATE_1, asSet(prosecutionCase1));
        final Offence offence1 = buildOffence1(hearing, defendant1);
        defendant1.getOffences().add(offence1);
        final JudicialRole judicialRole = buildJudgeJudicialRole(hearing.getId());
        hearing.setJudicialRoles(asSet(judicialRole));
        judicialRole.setHearing(hearing);
        return hearing;
    }

    public static JudicialRole buildJudgeJudicialRole(UUID hearingId) {
        final JudicialRole judicialRole = new JudicialRole();
        judicialRole.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        judicialRole.setJudicialId(randomUUID());
        judicialRole.setJudicialRoleType(CoreTestTemplates.magistrate().getJudiciaryType());
        judicialRole.setFirstName("Alan");
        judicialRole.setMiddleName("Mathison");
        judicialRole.setLastName("Turing");
        judicialRole.setTitle("HHJ");
        judicialRole.setBenchChairman(true);
        judicialRole.setDeputy(false);
        judicialRole.setUserId(randomUUID());
        return judicialRole;
    }

    public static Hearing buildHearing1(final UUID hearingId, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime,
                                        Set<ProsecutionCase> cases) {
        Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(cases);
        hearing.setHearingLanguage(HearingLanguage.ENGLISH);
        final HearingType hearingType = new HearingType();
        hearingType.setId(randomUUID());
        hearingType.setDescription("TRIAL");
        hearing.setHearingType(hearingType);
        hearing.setJurisdictionType(JurisdictionType.CROWN);
        hearing.setHasSharedResults(true);

        hearing.setHearingDays(asSet(buildHearingDate(hearingId, startDateTime), buildHearingDate(hearingId, endDateTime)));

        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setId(randomUUID());
        courtCentre.setName("Liverpool Crown Court");
        courtCentre.setRoomId(randomUUID());
        courtCentre.setRoomName("3-1");

        hearing.setCourtCentre(courtCentre);

        return hearing;
    }

    public static Defendant buildDefendant1(final UUID hearingId) {
        Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), hearingId));
        PersonDefendant personDefendant = new PersonDefendant();
        Person person = new Person();
        person.setFirstName("Ken");
        person.setMiddleName("Rob");
        person.setLastName("Thompson");
        person.setDateOfBirth(parse("1943-02-04T00:00:00Z").toLocalDate());
        person.setNationalityCode("UK");
        person.setGender(Gender.MALE);
        person.setAddress(buildAddress());
        person.setContact(buildContact());
        personDefendant.setPersonDetails(person);
        defendant.setPersonDefendant(personDefendant);
        return defendant;
    }


    public static Address buildAddress() {
        final Address address = new Address();
        address.setAddress1("222 Furze Road Exeter");
        address.setAddress2("Lorem Ipsum");
        address.setAddress3("Solor");
        address.setAddress4("Porro Quisquam");
        address.setPostCode("CR0 1XG");
        return address;
    }

    public static Contact buildContact() {
        final Contact contact = new Contact();
        contact.setWork("02070101011");
        contact.setHome("02070101010");
        contact.setMobile("07422263910");
        contact.setFax("021111111");
        contact.setPrimaryEmail("ken.thompson@acme.me");
        return contact;
    }

    public static Defendant buildDefendant2(final UUID hearingId) {

        Defendant defendant = new Defendant();
        PersonDefendant personDefendant = new PersonDefendant();
        Person person = new Person();
        defendant.setId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), hearingId));
        person.setFirstName("William");
        person.setMiddleName("Nelson");
        person.setLastName("Joy");
        person.setDateOfBirth(parse("1954-11-08T00:00:00Z").toLocalDate());
        person.setNationalityCode("United States");
        person.setGender(Gender.MALE);
        person.setAddress(buildAddress());
        person.setContact(buildContact());
        personDefendant.setPersonDetails(person);
        defendant.setPersonDefendant(personDefendant);
        return defendant;
    }

    /*public static Judge buildJudge(final Hearing hearing) {
        return Judge.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("a38d0d5f-a26c-436b-9b5e-4dc58f28878d"), hearing.getId()))
                .withPersonId(UUID.fromString("c8912678-213f-421a-89c8-d0dc87ac3558"))
                .withFirstName("Alan")
                .withLastName("Mathison Turing")
                .withTitle("HHJ")
                .build();
    }*/

    public static ProsecutionCase buildLegalCase1(final UUID hearingId, final Set<Defendant> defendants) {
        // TODO add more fields
        ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        prosecutionCase.setDefendants(defendants);
        prosecutionCase.setProsecutionCaseIdentifier(buildProsecutionCaseIdentifier());

        return prosecutionCase;
    }

    public static Offence buildOffence1(final Hearing hearing, final Defendant defendant) {
        Offence offence = new Offence();
//        offence.setId(UUID.fromString("4b1318e4-1517-4e4f-a89d-6af0eafa5058"));
        offence.setDefendant(defendant);
        offence.setOffenceCode("UNKNOWN");
        offence.setCount(1);
        offence.setWording("on 01/08/2009 at the County public house, unlawfully and maliciously wounded, John Smith");
        offence.setOffenceTitle("Wound / inflict grievous bodily harm without intent");
        offence.setOffenceLegislation("Contrary to section 20 of the Offences Against the Person Act 1861.");
        offence.setStartDate(parse("2018-02-21T00:00:00Z").toLocalDate());
        offence.setEndDate(parse("2018-02-22T00:00:00Z").toLocalDate());
        offence.setConvictionDate(parse("2018-02-22T00:00:00Z").toLocalDate());
        //offence.setPleaDate(parse("2016-06-08T00:00:00Z").toLocalDate());
        //offence.setPleaValue(PleaValue.GUILTY.toString());
        /*
        offence.setLesserOffenceCode("A1");
        offence.setLesserOffenceDefinitionId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"));
        offence.setVerdictCategory("GUILTY");
        offence.setLesserOffenceTitle("Guilty By Jury");
        offence.setLesserOffenceLegislation("Guilty By Jury On Judges Direction");
        offence.setVerdictTypeId(UUID.randomUUID());
        offence.setVerdictDate(parse("2018-02-21T00:00:00Z").toLocalDate());
        offence.setNumberOfJurors(10);
        offence.setNumberOfSplitJurors(2);
        offence.setUnanimous(false);
        */
        return offence;
    }

    public static HearingDay buildHearingDate(final UUID ahearingId, final ZonedDateTime startDate) {
        final HearingDay hearingDay = new HearingDay();
        hearingDay.setSittingDay(startDate);
        hearingDay.setListedDurationMinutes(2);
        hearingDay.setListingSequence(5);
        hearingDay.setDateTime(startDate);
        hearingDay.setDate(startDate.toLocalDate());
        hearingDay.setId(new HearingSnapshotKey(randomUUID(), ahearingId));
        return hearingDay;
    }

    private static ProsecutionCaseIdentifier buildProsecutionCaseIdentifier() {
        ProsecutionCaseIdentifier entity = new ProsecutionCaseIdentifier();
        entity.setCaseURN("8C720B32E45B");
        entity.setProsecutionAuthorityCode("AUTH CODE");
        entity.setProsecutionAuthorityId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"));
        entity.setProsecutionAuthorityReference("AUTH REF");
        return entity;
    }

    public static HearingHelper helper(Hearing hearing) {
        return new HearingHelper(hearing);
    }

    public static class HearingHelper {
        Hearing hearing;

        public HearingHelper(Hearing hearing) {
            this.hearing = hearing;
        }

        public Hearing it() {
            return hearing;
        }

        public ProsecutionCase getFirstProsecutionCase() {
            return at(hearing.getProsecutionCases(), 0);
        }

        public Defendant getFirstDefendant() {
            return at(at(hearing.getProsecutionCases(), 0).getDefendants(), 0);
        }

        public Person getFirstDefendantPersonDetails() {
            return at(at(hearing.getProsecutionCases(), 0).getDefendants(), 0).getPersonDefendant().getPersonDetails();
        }
    }

    public static List<Hearing> buildHearingAndHearingDays() {

        List<Hearing> hearings = new ArrayList<>();

        // Hearing 1
        final Hearing hearing1 = new Hearing();
        hearing1.setId(randomUUID());
        Set<HearingDay> hearingDays1 = generateHearingDays(hearing1.getId(), 2019, 7, 1, 3);//3
        hearing1.setHearingDays(hearingDays1);

        //Hearing 2
        final Hearing hearing2 = new Hearing();
        hearing2.setId(randomUUID());
        Set<HearingDay> hearingDays2 = generateHearingDays(hearing1.getId(),2019, 7, 4, 1); //5
        hearing2.setHearingDays(hearingDays2);

        //Hearing 3
        final Hearing hearing3 = new Hearing();
        hearing3.setId(randomUUID());
        Set<HearingDay> hearingDays3 = generateHearingDays(hearing1.getId(),2019, 7, 2, 4); //1
        hearing3.setHearingDays(hearingDays3);

        hearings.add(hearing1);
        hearings.add(hearing2);
        hearings.add(hearing3);

        return hearings;
    }

    private static Set<HearingDay> generateHearingDays(UUID hearingId, int year, int month, int day, int sequence) {

        Set<HearingDay> hearingDays = new HashSet<>(); //add 5 days

        HearingDay hearingDay1 = getHearingDay(hearingId, year, month, day++, sequence);
        HearingDay hearingDay2 = getHearingDay(hearingId, year, month, day++, sequence);
        HearingDay hearingDay3 = getHearingDay(hearingId, year, month, day++, sequence);
        HearingDay hearingDay4 = getHearingDay(hearingId, year, month, day++, sequence);
        HearingDay hearingDay5 = getHearingDay(hearingId, year, month, day++, sequence);

        hearingDays.add(hearingDay1);
        hearingDays.add(hearingDay2);
        hearingDays.add(hearingDay3);
        hearingDays.add(hearingDay4);
        hearingDays.add(hearingDay5);

        return hearingDays;
    }

    private static HearingDay getHearingDay(UUID hearingId, int year, int month, int day, int sequence) {
        HearingDay hearingDay = new HearingDay();
        hearingDay.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        hearingDay.setListingSequence(sequence);

        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.parse("11:00:11.297"), ZoneId.of("UTC"));
        hearingDay.setDate(zonedDateTime.toLocalDate());
        hearingDay.setSittingDay(zonedDateTime);
        hearingDay.setDateTime(zonedDateTime);

        return hearingDay;
    }

}
