package uk.gov.moj.cpp.hearing.query.view;

import static java.time.ZonedDateTime.parse;

import uk.gov.justice.json.schemas.core.Gender;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.JudicialRoleType;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.justice.json.schemas.core.PleaValue;
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HearingTestUtils {

    public static final LocalDate startDate = LocalDate.now();
    public static final ZonedDateTime START_DATE_1 = parse("2018-02-22T10:30:00Z");
    public static final ZonedDateTime END_DATE_1 = parse("2018-02-24T15:45:00Z");

    public static List<Hearing> buildHearingList() {
        final UUID hearingId = UUID.randomUUID();
        final Defendant defendant1 = buildDefendant1(hearingId);
        final Defendant defendant2 = buildDefendant2(hearingId);
        final ProsecutionCase prosecutionCase1 = buildLegalCase1(hearingId, Arrays.asList(defendant1, defendant2));
        final Hearing hearing = buildHearing1(hearingId, START_DATE_1, END_DATE_1, Arrays.asList(prosecutionCase1));
        final Offence offence1 = buildOffence1(hearing, defendant1);
        defendant1.getOffences().add(offence1);
        final JudicialRole judicialRole = buildJudgeJudicialRole(hearing.getId());
        hearing.setJudicialRoles(Arrays.asList(judicialRole));
        judicialRole.setHearing(hearing);
        return Arrays.asList(hearing);
    }

    public static JudicialRole buildJudgeJudicialRole(UUID hearingId) {
        final JudicialRole judicialRole = new JudicialRole();
        judicialRole.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingId));
        judicialRole.setJudicialId(UUID.randomUUID());
        judicialRole.setJudicialRoleType(JudicialRoleType.MAGISTRATE);
        judicialRole.setFirstName("Alan");
        judicialRole.setMiddleName("Mathison");
        judicialRole.setLastName("Turing");
        judicialRole.setTitle("HHJ");
        judicialRole.setBenchChairman(true);
        judicialRole.setDeputy(false);
        return judicialRole;
    }

    //TODO remove hand coded UUIDs
    public static Hearing buildHearing1(final UUID hearingId, final ZonedDateTime startDateTime, final ZonedDateTime endDateTime,
                                        List<ProsecutionCase> cases) {
        Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(cases);
        hearing.setHearingLanguage(HearingLanguage.ENGLISH);
        final HearingType hearingType = new HearingType();
        hearingType.setId(UUID.fromString("019556b2-a25e-4ea7-b3f1-8c89d14b02e0"));
        hearingType.setDescription("TRIAL");
        hearing.setHearingType(hearingType);
        hearing.setJurisdictionType(JurisdictionType.CROWN);
        hearing.setHasSharedResults(true);

        hearing.setHearingDays(Arrays.asList(buildHearingDate(hearingId, startDateTime),
                        buildHearingDate(hearingId, endDateTime)));

        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setId(UUID.fromString("e8821a38-546d-4b56-9992-ebdd772a561f"));
        courtCentre.setName("Liverpool Crown Court");
        courtCentre.setRoomId(UUID.fromString("e7721a38-546d-4b56-9992-ebdd772a561b"));
        courtCentre.setRoomName("3-1");
        
        hearing.setCourtCentre(courtCentre);

        return hearing;
    }

    /*public static ProsecutionAdvocate buildProsecutionAdvocate(final Hearing hearing) {
        return ProsecutionAdvocate.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), hearing.getId()))
                .withPersonId(UUID.fromString("35f4d841-a0eb-4a32-b75c-91d241bf83d3"))
                .withFirstName("Brian J.")
                .withLastName("Fox")
                .withTitle("MR")
                .withStatus("QC")
                .build();
    }*/

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

    public static ProsecutionCase buildLegalCase1(final UUID hearingId, final List<Defendant> defendants) {
        // TODO add more fields
        ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingId));
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
        hearingDay.setDateTime(startDate.toLocalDateTime());
        hearingDay.setId(new HearingSnapshotKey(UUID.randomUUID(), ahearingId));
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
}
