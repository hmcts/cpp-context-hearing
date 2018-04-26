package uk.gov.moj.cpp.hearing.query.view;

import static java.time.ZonedDateTime.parse;
import static java.util.UUID.randomUUID;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.AhearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Witness;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HearingTestUtils {

    public static final LocalDate startDate = LocalDate.now();

    public static final UUID HEARING_ID_1 = UUID.fromString("23ef34ec-63e5-422e-8071-9b3753008c10");
    public static final ZonedDateTime START_DATE_1 = parse("2018-02-22T10:30:00Z");

    public static List<Ahearing> buildHearingList() {
        // 1. create the hearing object and set the attendees
        final Ahearing ahearing = buildHering1(HEARING_ID_1, START_DATE_1);
        final Judge judge = buildJudge(ahearing);
        final ProsecutionAdvocate prosecutionAdvocate = buildProsecutionAdvocate(ahearing);
        final DefenceAdvocate defenseAdvocate1 = buildDefenseAdvocate1(ahearing);
        final DefenceAdvocate defenseAdvocate2 = buildDefenseAdvocate2(ahearing);
        ahearing.setAttendees(Arrays.asList(judge, prosecutionAdvocate, defenseAdvocate1, defenseAdvocate2));

        // 2. create the defendants objects and set to hearing
        final Defendant defendant1 = buildDefendant1(ahearing, defenseAdvocate1, defenseAdvocate2);
        final Defendant defendant2 = buildDefendant2(ahearing, defenseAdvocate1, defenseAdvocate2);
        final List<Defendant> defendants = Arrays.asList(defendant1, defendant2);
        ahearing.setDefendants(defendants);

        // 3. link the defendants to to the defenseAdvocates
        defenseAdvocate1.setDefendants(defendants);
        defenseAdvocate2.setDefendants(defendants);

        // 4. create the legal case, offence and set the offence to the existent defendants
        final LegalCase legalCase1 = buildLegalCase1();
        final Offence offence1 = buildOffence1(ahearing, defendant1, legalCase1);
        defendant1.setOffences(Arrays.asList(offence1));
        defendant2.setOffences(Arrays.asList(offence1));

        ahearing.setWitnesses(Arrays.asList(buildWitness(ahearing, legalCase1)));

        // 5. add the created hearing to the hearing list
        return Arrays.asList(ahearing);
    }

    public static Ahearing buildHering1(final UUID hearingId, final ZonedDateTime startDateTime) {
        return Ahearing.builder()
                .withId(hearingId)
                .withHearingType("TRIAL")
                .withStartDateTime(startDateTime)
                .withHearingDays(Arrays.asList(buildHearingDate(hearingId, startDateTime)))
                .withCourtCentreId(UUID.fromString("e8821a38-546d-4b56-9992-ebdd772a561f"))
                .withCourtCentreName("Liverpool Crown Court")
                .withRoomId(UUID.fromString("e7721a38-546d-4b56-9992-ebdd772a561b"))
                .withRoomName("3-1")
                .build();
    }

    public static ProsecutionAdvocate buildProsecutionAdvocate(final Ahearing ahearing) {
        return ProsecutionAdvocate.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), ahearing.getId()))
                .withPersonId(UUID.fromString("35f4d841-a0eb-4a32-b75c-91d241bf83d3"))
                .withFirstName("Brian J.")
                .withLastName("Fox")
                .withTitle("MR")
                .withStatus("QC")
                .build();
    }

    public static DefenceAdvocate buildDefenseAdvocate1(final Ahearing ahearing) {
        return DefenceAdvocate.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("743d333a-b270-4de6-a598-61abb64a8027"), ahearing.getId()))
                .withPersonId(UUID.fromString("effdc5c9-8c00-4ef8-abcf-9e2a79ed1daa"))
                .withFirstName("Mark")
                .withLastName("Zuckerberg")
                .withTitle("MR")
                .withStatus("QC")
                .build();
    }

    public static DefenceAdvocate buildDefenseAdvocate2(final Ahearing ahearing) {
        return DefenceAdvocate.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("cdc14b89-6b4d-4e98-9641-826c355c51b8"), ahearing.getId()))
                .withPersonId(UUID.fromString("78651aea-02da-4be9-8e78-c1748ea89e0c"))
                .withFirstName("Sean")
                .withLastName("Parker")
                .withTitle("MR")
                .withStatus("QC")
                .build();
    }

    public static Defendant buildDefendant1(final Ahearing ahearing, final DefenceAdvocate defenseAdvocate1,
            final DefenceAdvocate defenseAdvocate2) {
        return Defendant.builder()
                        .withId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), ahearing.getId()))
                        .withHearing(ahearing)
                        .withPersonId(UUID.fromString("5a6e2001-91ed-4af2-99af-f30ddc9ef5af"))
                        .withFirstName("Ken")
                        .withLastName("Thompson")
                        .withDateOfBirth(parse("1943-02-04T00:00:00Z").toLocalDate())
                        .withNationality("United States")
                        .withGender("M")
                        .withAddress(Address.builder()
                                .withAddress1("222 Furze Road Exeter")
                                .withAddress2("Lorem Ipsum")
                                .withAddress3("Solor")
                                .withAddress4("Porro Quisquam")
                                .withPostCode("CR0 1XG")
                                .build())
                        .withWorkTelephone("02070101011")
                        .withHomeTelephone("02070101010")
                        .withMobileTelephone("07422263910")
                        .withFax("021111111")
                        .withEmail("ken.thompson@acme.me")
                        .withDefenceAdvocates(Arrays.asList(defenseAdvocate1, defenseAdvocate2))
                        .build();
    }

    public static Defendant buildDefendant2(final Ahearing ahearing, final DefenceAdvocate defenseAdvocate1,
            final DefenceAdvocate defenseAdvocate2) {
        return Defendant.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("3739b4e3-1f81-4d12-a99d-ad27ae672566"), ahearing.getId()))
                .withHearing(ahearing)
                .withPersonId(UUID.fromString("98583be4-8d4a-4552-9252-ceccd61d32db"))
                .withFirstName("William Nelson")
                .withLastName("Joy")
                .withDateOfBirth(parse("1954-11-08T00:00:00Z").toLocalDate())
                .withNationality("United States")
                .withGender("M")
                .withAddress(Address.builder()
                        .withAddress1("222 Furze Road Exeter")
                        .withAddress2("Lorem Ipsum")
                        .withAddress3("Solor")
                        .withAddress4("Porro Quisquam")
                        .withPostCode("CR0 1XG")
                        .build())
                .withWorkTelephone("02070101011")
                .withHomeTelephone("02070101010")
                .withMobileTelephone("07422263910")
                .withFax("021111111")
                .withEmail("william-nelson.joy@acme.me")
                .withDefenceAdvocates(Arrays.asList(defenseAdvocate1, defenseAdvocate2))
                .build();
    }

    public static Judge buildJudge(final Ahearing ahearing) {
        return Judge.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("a38d0d5f-a26c-436b-9b5e-4dc58f28878d"), ahearing.getId()))
                .withPersonId(UUID.fromString("c8912678-213f-421a-89c8-d0dc87ac3558"))
                .withFirstName("Alan")
                .withLastName("Mathison Turing")
                .withTitle("HHJ")
                .build();
    }

    public static LegalCase buildLegalCase1() {
        return LegalCase.builder()
                .withId(UUID.fromString("9b70743c-69b3-4ac2-a362-8c720b32e45b"))
                .withCaseurn("8C720B32E45B")
                .build();
    }

    public static Offence buildOffence1(final Ahearing ahearing, final Defendant defendant,
            final LegalCase legalCase) {
        return Offence.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("4b1318e4-1517-4e4f-a89d-6af0eafa5058"), ahearing.getId()))
                .withDefendant(defendant)
                .withCase(legalCase)
                .withCode("UNKNOWN")
                .withCount(1)
                .withWording("on 01/08/2009 at the County public house, unlawfully and maliciously wounded, John Smith")
                .withTitle("Wound / inflict grievous bodily harm without intent")
                .withLegislation("Contrary to section 20 of the Offences Against the Person Act 1861.")
                .withStartDate(parse("2018-02-21T00:00:00Z").toLocalDate())
                .withEndDate(parse("2018-02-22T00:00:00Z").toLocalDate())
                .withConvictionDate(parse("2018-02-22T00:00:00Z").toLocalDate())
                .withPleaDate(parse("2016-06-08T00:00:00Z").toLocalDate())
                .withPleaValue("GUILTY")
                .withVerdictCode("A1")
                .withVerdictCategory("GUILTY")
                .withVerdictDescription("Guilty By Jury On Judges Direction")
                .withVerdictDate(parse("2018-02-21T00:00:00Z").toLocalDate())
                .withNumberOfJurors(10)
                .withNumberOfSplitJurors(2)
                .withUnanimous(false)
                .build();
    }

    public static Witness buildWitness(final Ahearing ahearing, final LegalCase legalCase) {
        return Witness.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), ahearing.getId()))
                .withHearing(ahearing)
                .withPersonId(UUID.fromString("5a6e2001-91ed-4af2-99af-f30ddc9ef5af"))
                .withFirstName("Ken")
                .withLastName("Thompson")
                .withDateOfBirth(parse("1943-02-04T00:00:00Z").toLocalDate())
                .withNationality("United States")
                .withGender("M")
                .withType("Prosecution")
                .withClassification("classification")
                .withWorkTelephone("02070101011")
                .withHomeTelephone("02070101010")
                .withMobileTelephone("07422263910")
                .withFax("021111111")
                .withEmail("ken.thompson@acme.me")
                .withLegalCase(legalCase)
                .build();
    }
    public static AhearingDate buildHearingDate(final UUID ahearingId, final ZonedDateTime startDate) {
        return AhearingDate.builder()
                .withDate(startDate)
                .withId(new HearingSnapshotKey(UUID.randomUUID(), ahearingId))
                .build();
    }

    public static Optional<Hearing> getHearing() {
        final Hearing hearingA = new Hearing(randomUUID(), startDate, null, 1,
                null, null, null);
        return Optional.of(hearingA);
    }
}
