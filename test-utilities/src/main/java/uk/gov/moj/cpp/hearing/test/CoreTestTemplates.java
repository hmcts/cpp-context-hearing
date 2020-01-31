package uk.gov.moj.cpp.hearing.test;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.core.courts.BailStatus.bailStatus;
import static uk.gov.justice.core.courts.DefenceCounsel.defenceCounsel;
import static uk.gov.justice.core.courts.HearingLanguage.WELSH;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.NI_NUMBER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.POST_CODE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.ORGANISATION;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.Pair.p;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtIndicatedSentence;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.DocumentationLanguageNeeds;
import uk.gov.justice.core.courts.Ethnicity;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.NotifiedPlea;
import uk.gov.justice.core.courts.NotifiedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ReferralReason;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Title;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.JudicialRoleTypeEnum;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "squid:S1067"})
public class CoreTestTemplates {

    private static final UUID BAIL_STATUS_ID = randomUUID();
    private static final String JSON_STRING = "json string";

    public static CoreTemplateArguments defaultArguments() {
        return new CoreTemplateArguments();
    }

    public static HearingDay.Builder hearingDay() {
        return HearingDay.hearingDay()
                .withSittingDay(RandomGenerator.PAST_UTC_DATE_TIME.next())
                .withListingSequence(INTEGER.next())
                .withListedDurationMinutes(INTEGER.next());
    }

    public static HearingDay.Builder hearingDayWithParam(int year, int month, int day, int seq) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.parse("11:00:11.297"), ZoneId.of("UTC"));
        return HearingDay.hearingDay()
                .withSittingDay(zonedDateTime)
                .withListingSequence(seq)
                .withListedDurationMinutes(INTEGER.next());
    }

    public static CourtCentre.Builder courtCentre() {
        return CourtCentre.courtCentre()
                .withId(randomUUID())
                .withName(STRING.next())
                .withWelshName(STRING.next())
                .withRoomId(randomUUID())
                .withRoomName(STRING.next())
                .withWelshRoomName(STRING.next());
    }

    public static CourtCentre.Builder courtCentreWithArgs(final UUID courtAndRoomId, final String courtRoomName) {
        return CourtCentre.courtCentre()
                .withId(courtAndRoomId)
                .withName(STRING.next())
                .withWelshName(STRING.next())
                .withRoomId(courtAndRoomId)
                .withRoomName(courtRoomName)
                .withWelshRoomName(STRING.next());
    }

    public static CourtCentre.Builder courtCentreWithArgs(final UUID courtId, final UUID courtRoomId, final String courtRoomName) {
        return CourtCentre.courtCentre()
                .withId(courtId)
                .withName(STRING.next())
                .withWelshName(STRING.next())
                .withRoomId(courtRoomId)
                .withRoomName(courtRoomName)
                .withWelshRoomName(STRING.next());
    }

    public static CourtCentre.Builder courtCentreWithArgs(String courtRoomName) {
        return CourtCentre.courtCentre()
                .withId(randomUUID())
                .withName(courtRoomName)
                .withWelshName(STRING.next())
                .withRoomId(randomUUID())
                .withRoomName(STRING.next())
                .withWelshRoomName(STRING.next());
    }


    public static JudicialRole.Builder judiciaryRole(CoreTemplateArguments args) {
        return JudicialRole.judicialRole()
                .withJudicialId(randomUUID())
                .withFirstName(STRING.next())
                .withMiddleName(STRING.next())
                .withLastName(STRING.next())
                .withIsBenchChairman(BOOLEAN.next())
                .withIsDeputy(BOOLEAN.next())
                .withTitle(STRING.next())
                .withJudicialRoleType(args.jurisdictionType == JurisdictionType.CROWN ? circuitJudge() : magistrate());
    }

    public static ProsecutionCaseIdentifier.Builder prosecutionCaseIdentifier(CoreTemplateArguments args) {
        return ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withProsecutionAuthorityId(randomUUID())
                .withProsecutionAuthorityReference(args.jurisdictionType == JurisdictionType.MAGISTRATES ? STRING.next() : null)
                .withProsecutionAuthorityCode(STRING.next())
                .withCaseURN(args.jurisdictionType == JurisdictionType.CROWN ? STRING.next() : null);
    }

    public static NotifiedPlea.Builder notifiedPlea(UUID offenceId) {
        return NotifiedPlea.notifiedPlea()
                .withOffenceId(offenceId)
                .withNotifiedPleaValue(RandomGenerator.values(NotifiedPleaValue.values()).next())
                .withNotifiedPleaDate(PAST_LOCAL_DATE.next());
    }

    public static IndicatedPlea.Builder indicatedPlea(UUID offenceId, IndicatedPleaValue indicatedPleaValue) {
        return IndicatedPlea.indicatedPlea()
                .withOffenceId(offenceId)
                .withIndicatedPleaDate(PAST_LOCAL_DATE.next())
                .withIndicatedPleaValue(indicatedPleaValue)
                .withSource(RandomGenerator.values(Source.values()).next());
    }

    public static Plea.Builder plea(UUID offenceId, LocalDate convictionDate, final PleaValue pleaValue) {
        return Plea.plea()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(randomUUID())
                .withDelegatedPowers(delegatedPowers().build())
                .withPleaDate(convictionDate)
                .withPleaValue(pleaValue);
    }

    public static AllocationDecision.Builder allocationDecision(final UUID offenceId) {
        return AllocationDecision.allocationDecision()
                .withOriginatingHearingId(randomUUID())
                .withOffenceId(offenceId)
                .withMotReasonId(randomUUID())
                .withMotReasonDescription(STRING.next())
                .withMotReasonCode(STRING.next())
                .withSequenceNumber(INTEGER.next())
                .withAllocationDecisionDate(LocalDate.now())
                .withCourtIndicatedSentence(courtIndicatedSentence().build());
    }

    public static CourtIndicatedSentence.Builder courtIndicatedSentence() {
        return CourtIndicatedSentence.courtIndicatedSentence()
                .withCourtIndicatedSentenceDescription(STRING.next())
                .withCourtIndicatedSentenceTypeId(randomUUID());
    }

    public static DelegatedPowers.Builder delegatedPowers() {
        return DelegatedPowers.delegatedPowers()
                .withUserId(randomUUID())
                .withFirstName(STRING.next())
                .withLastName(STRING.next());
    }

    public static OffenceFacts.Builder offenceFacts() {
        return OffenceFacts.offenceFacts()
                .withAlcoholReadingAmount(INTEGER.next())
                .withAlcoholReadingMethodCode(STRING.next())
                .withVehicleRegistration(STRING.next());
    }

    public static Offence.Builder offence(CoreTemplateArguments args, UUID offenceId) {

        if (args.isMinimumOffence()) {
            return Offence.offence()
                    .withId(offenceId)
                    .withStartDate(PAST_LOCAL_DATE.next())
                    .withOffenceDefinitionId(randomUUID())
                    .withOffenceCode(STRING.next())
                    .withCount(INTEGER.next())
                    .withWording(STRING.next())
                    .withOrderIndex(INTEGER.next());
        }


        final Offence.Builder result = Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())

                .withIndicatedPlea(indicatedPlea(offenceId, args.indicatedPlea).build())
                .withNotifiedPlea(notifiedPlea(offenceId).build())

                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceFacts(offenceFacts().build())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withWording(STRING.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next())
                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                        .withDaysSpent(INTEGER.next())
                        .withTimeLimit(PAST_LOCAL_DATE.next())
                        .build());

        if (!args.isOffenceCountNull) {
            result.withCount(INTEGER.next());
        }
        if (args.jurisdictionType == JurisdictionType.MAGISTRATES) {
            final LocalDate convictionDate = PAST_LOCAL_DATE.next();
            result.withConvictionDate(convictionDate);
        }

        if (args.convicted) {
            final LocalDate convictionDate = PAST_LOCAL_DATE.next();
            result.withConvictionDate(convictionDate);
        }

        if (args.isAllocationDecision) {
            result.withAllocationDecision(allocationDecision(offenceId).build());
        }

        return result;
    }

    public static Address.Builder address() {
        return Address.address()
                .withAddress1(STRING.next())
                .withAddress2(STRING.next())
                .withAddress3(STRING.next())
                .withAddress4(STRING.next())
                .withAddress5(STRING.next())
                .withWelshAddress1(STRING.next())
                .withWelshAddress2(STRING.next())
                .withWelshAddress3(STRING.next())
                .withWelshAddress4(STRING.next())
                .withWelshAddress5(STRING.next())
                .withPostcode(POST_CODE.next());
    }

    public static ContactNumber.Builder contactNumber() {
        return ContactNumber.contactNumber()
                .withFax(INTEGER.next().toString())
                .withHome(INTEGER.next().toString())
                .withMobile(INTEGER.next().toString())
                .withPrimaryEmail(generateRandomEmail())
                .withSecondaryEmail(generateRandomEmail())
                .withWork(INTEGER.next().toString());
    }

    public static Person.Builder person(CoreTemplateArguments args) {

        if (args.isMinimumPerson()) {
            return Person.person()
                    .withTitle(RandomGenerator.values(Title.values()).next())
                    .withLastName(STRING.next())
                    .withGender(RandomGenerator.values(Gender.values()).next());
        }

        return Person.person()
                .withTitle(RandomGenerator.values(Title.values()).next())
                .withContact(contactNumber().build())
                .withAdditionalNationalityCode(STRING.next())
                .withAdditionalNationalityId(randomUUID())
                .withDateOfBirth(PAST_LOCAL_DATE.next())
                .withDisabilityStatus(STRING.next())
                .withDocumentationLanguageNeeds(args.hearingLanguage == WELSH ? DocumentationLanguageNeeds.WELSH : DocumentationLanguageNeeds.ENGLISH)
                .withEthnicity(Ethnicity.ethnicity().withObservedEthnicityCode(STRING.next()).build())
                .withAddress(address().build())
                .withFirstName(STRING.next())
                .withMiddleName(STRING.next())
                .withLastName(STRING.next())
                .withGender(RandomGenerator.values(Gender.values()).next())
                .withOccupation(STRING.next())
                .withInterpreterLanguageNeeds(STRING.next())
                .withOccupationCode(STRING.next())
                .withSpecificRequirements(STRING.next())
                .withNationalInsuranceNumber(NI_NUMBER.next())
                .withNationalityId(randomUUID())
                .withNationalityDescription(STRING.next())
                .withNationalityCode(STRING.next());
    }

    public static AssociatedPerson.Builder associatedPerson(CoreTemplateArguments args) {
        return AssociatedPerson.associatedPerson()
                .withPerson(person(args).build())
                .withRole(STRING.next());
    }

    public static Organisation.Builder organisation(CoreTemplateArguments args) {

        if (args.isMinimumOrganisation()) {
            return Organisation.organisation()
                    .withName(STRING.next());
        }

        return Organisation.organisation()
                .withAddress(address().build())
                .withContact(contactNumber().build())
                .withIncorporationNumber(STRING.next())
                .withName(STRING.next())
                .withRegisteredCharityNumber(STRING.next());
    }

    public static PersonDefendant.Builder personDefendant(CoreTemplateArguments args) {
        return PersonDefendant.personDefendant()
                .withPersonDetails(person(args).build())
                .withArrestSummonsNumber(STRING.next())
                .withBailStatus(bailStatus().withId(BAIL_STATUS_ID).withCode("C").withDescription("Remanded into Custody").build())
                .withDriverNumber(STRING.next())
                .withPerceivedBirthYear(INTEGER.next())
                .withEmployerOrganisation(organisation(args).build())
                .withEmployerPayrollReference(STRING.next())
                .withCustodyTimeLimit(PAST_LOCAL_DATE.next());
    }

    public static LegalEntityDefendant.Builder legalEntityDefendant(CoreTemplateArguments args) {
        return LegalEntityDefendant.legalEntityDefendant()
                .withOrganisation(organisation(args).build());
    }

    public static Defendant.Builder defendant(UUID prosecutionCaseId, CoreTemplateArguments args, Pair<UUID, List<UUID>> structure) {

        return Defendant.defendant()
                .withId(structure.getK())
                .withProsecutionCaseId(prosecutionCaseId)
                .withNumberOfPreviousConvictionsCited(INTEGER.next())
                .withProsecutionAuthorityReference(STRING.next())
                .withIsYouth(Boolean.TRUE)
                .withOffences(
                        structure.getV().stream()
                                .map(offenceId -> offence(args, offenceId).build())
                                .collect(toList())
                )
                .withAssociatedPersons(args.isMinimumAssociatedPerson() ? asList(associatedPerson(args).build()) : null)
                .withDefenceOrganisation(args.isMinimumDefenceOrganisation() ? organisation(args).build() : null)
                .withPersonDefendant(args.defendantType == PERSON ? personDefendant(args).build() : null)
                .withLegalEntityDefendant(args.defendantType == ORGANISATION ? legalEntityDefendant(args).build() : null);
    }

    public static Marker.Builder marker(Pair<UUID, List<UUID>> structure) {

        return Marker.marker()
                .withId(structure.getK())
                .withMarkerTypeCode(STRING.next())
                .withMarkerTypeDescription(STRING.next())
                .withMarkerTypeid(UUID.randomUUID());
    }

    public static ProsecutionCase.Builder prosecutionCase(CoreTemplateArguments args, Pair<UUID, Map<UUID, List<UUID>>> structure) {

        return ProsecutionCase.prosecutionCase()
                .withId(structure.getK())
                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier(args).build())
                .withCaseStatus(STRING.next())
                .withOriginatingOrganisation(STRING.next())
                .withInitiationCode(RandomGenerator.values(InitiationCode.values()).next())
                .withStatementOfFacts(STRING.next())
                .withStatementOfFactsWelsh(STRING.next())
                .withCaseMarkers(buildCaseMarkers())
                .withDefendants(
                        structure.getV().entrySet().stream()
                                .map(entry -> defendant(structure.getK(), args, p(entry.getKey(), entry.getValue())).build())
                                .collect(toList())
                );
    }

    private static List<Marker> buildCaseMarkers() {
        return singletonList(Marker.marker()
                .withId(randomUUID())
                .withMarkerTypeCode(STRING.next())
                .withMarkerTypeDescription(STRING.next())
                .withMarkerTypeid(randomUUID()).build());
    }

    public static ReferralReason.Builder referralReason() {
        return ReferralReason.referralReason()
                .withId(randomUUID())
                .withDefendantId(randomUUID())
                .withDescription(STRING.next())
                .withWelshDescription("welshDescription");
    }

    public static HearingType.Builder hearingType() {
        return HearingType.hearingType()
                .withId(randomUUID())
                .withDescription(STRING.next())
                .withWelshDescription(STRING.next());
    }

    public static Hearing.Builder hearing(CoreTemplateArguments args) {
        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withId(randomUUID())
                .withType(hearingType().build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(hearingDay().build()))
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue())).build())
                                .collect(toList())
                )

                .withCourtApplications(asList((new HearingFactory().courtApplication().build())));

        if (args.hearingLanguage == WELSH) {
            hearingBuilder.withHearingLanguage(HearingLanguage.WELSH);
            hearingBuilder.withCourtCentre(courtCentreWithArgs("welshCourtRoom").build());
        } else {
            hearingBuilder.withHearingLanguage(HearingLanguage.ENGLISH);
            hearingBuilder.withCourtCentre(courtCentre().build());
        }
        return hearingBuilder;
    }

    public static Hearing.Builder hearingWithParam(CoreTemplateArguments args, UUID courtAndRoomId,final String courtRoomName, int year, int month, int day) throws NoSuchAlgorithmException {
        final Random random = SecureRandom.getInstanceStrong();
        final int min = 1;
        final int max = 5;
        if (day == 31){
            day = 27;
        }
        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withId(randomUUID())
                .withType(hearingType().build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(    hearingDayWithParam(year, month, day+1,random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(year, month, day,random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(year, month, day-1,random.nextInt((max - min) + 1) + min).build()))
                .withCourtCentre(courtCentreWithArgs(courtAndRoomId, courtRoomName).build())
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue())).build())
                                .collect(toList())
                )

                .withCourtApplications(asList((new HearingFactory().courtApplication().build())));

        if (args.hearingLanguage == WELSH) {
            hearingBuilder.withHearingLanguage(HearingLanguage.WELSH);
        } else {
            hearingBuilder.withHearingLanguage(HearingLanguage.ENGLISH);
        }
        return hearingBuilder;
    }

    public static Hearing.Builder hearingWithParam(final CoreTemplateArguments args,
                                                   final UUID courtId,
                                                   final UUID courtRoomId,
                                                   final String courtRoomName,
                                                   int year, int month, int day,
                                                   final UUID defenceCounselId,
                                                   final UUID caseId) throws NoSuchAlgorithmException {
        final Random random = SecureRandom.getInstanceStrong();
        final int min = 1;
        final int max = 5;
        if (day == 31){
            day = 27;
        }
        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withId(randomUUID())
                .withType(hearingType().build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(    hearingDayWithParam(year, month, day+1,random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(year, month, day,random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(year, month, day-1,random.nextInt((max - min) + 1) + min).build()))
                .withCourtCentre(courtCentreWithArgs(courtId, courtRoomId, courtRoomName).build())
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withDefenceCounsels(
                        singletonList(
                                defenceCounsel()
                                        .withId(defenceCounselId)
                                        .withAttendanceDays(Arrays.asList(LocalDate.now()))
                                        .withDefendants(Arrays.asList(randomUUID()))
                                        .withFirstName("John")
                                        .withLastName("Jones")
                                        .withTitle("Mr")
                                        .withStatus("OPEN")
                                        .build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue())).build())
                                .collect(toList())
                )

                .withCourtApplications(asList((new HearingFactory().courtApplication().withLinkedCaseId(caseId).build())));

        if (args.hearingLanguage == WELSH) {
            hearingBuilder.withHearingLanguage(HearingLanguage.WELSH);
        } else {
            hearingBuilder.withHearingLanguage(HearingLanguage.ENGLISH);
        }
        return hearingBuilder;
    }

    public static String generateRandomEmail() {
        return STRING.next().toLowerCase() + "@" + STRING.next().toLowerCase() + "." + STRING.next().toLowerCase();
    }

    public static Target.Builder target(UUID hearingId, UUID defendantId, UUID offenceId, UUID resultLineId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLine(resultLineId))));
    }

    public static ResultLine resultLine(UUID resultLineId) {
        return ResultLine.resultLine()
                .withResultDefinitionId(randomUUID())
                .withResultLineId(resultLineId)
                .withResultLabel(STRING.next())
                .withLevel(Level.CASE)
                .withOrderedDate(PAST_LOCAL_DATE.next())
                .withSharedDate(PAST_LOCAL_DATE.next())
                .withPrompts(new ArrayList<>(singletonList(Prompt.prompt()
                        .withId(randomUUID())
                        .withValue("2017-05-20")
                        .build()))
                )
                .withDelegatedPowers(null)
                .withIsComplete(true)
                .withIsModified(false)
                .withIsDeleted(false)
                .build();
    }

    public static JudicialRoleType circuitJudge() {
        return JudicialRoleType.judicialRoleType()
                .withJudicialRoleTypeId(UUID.randomUUID())
                .withJudiciaryType(JudicialRoleTypeEnum.CIRCUIT_JUDGE.name()).build();
    }

    public static JudicialRoleType magistrate() {
        return JudicialRoleType.judicialRoleType()
                .withJudicialRoleTypeId(UUID.randomUUID())
                .withJudiciaryType(JudicialRoleTypeEnum.MAGISTRATE.name()).build();
    }


    public enum DefendantType {
        PERSON, ORGANISATION
    }

    public static class CoreTemplateArguments {

        private JurisdictionType jurisdictionType = JurisdictionType.CROWN;

        private HearingLanguage hearingLanguage = HearingLanguage.ENGLISH;

        private DefendantType defendantType = PERSON;

        private IndicatedPleaValue indicatedPlea = INDICATED_GUILTY;

        private boolean minimumAssociatedPerson;
        private boolean minimumDefenceOrganisation;
        private boolean minimumPerson;
        private boolean minimumOrganisation;
        private boolean minimumOffence;
        private boolean convicted = false;
        private boolean isOffenceCountNull = false;
        private boolean isAllocationDecision = true;

        private Map<UUID, Map<UUID, List<UUID>>> structure = toMap(randomUUID(), toMap(randomUUID(), asList(randomUUID())));

        public static <T, U> Map<T, U> toMap(T t, U u) {
            final Map<T, U> map = new HashMap<>();
            map.put(t, u);
            return map;
        }

        public static <T, U> Map<T, U> toMap(List<Pair<T, U>> pairs) {
            return pairs.stream().collect(Collectors.toMap(Pair::getK, Pair::getV));
        }

        public CoreTemplateArguments setJurisdictionType(JurisdictionType jurisdictionType) {
            this.jurisdictionType = jurisdictionType;
            return this;
        }

        public CoreTemplateArguments setHearingLanguage(HearingLanguage hearingLanguage) {
            this.hearingLanguage = hearingLanguage;
            return this;
        }

        public CoreTemplateArguments setDefendantType(DefendantType defendantType) {
            this.defendantType = defendantType;
            return this;
        }

        public CoreTemplateArguments setIndicatedPleaValue(IndicatedPleaValue indicatedPleaValue) {
            this.indicatedPlea = indicatedPleaValue;
            return this;
        }

        public boolean isMinimumAssociatedPerson() {
            return minimumAssociatedPerson;
        }

        public CoreTemplateArguments setMinimumAssociatedPerson(boolean minimumAssociatedPerson) {
            this.minimumAssociatedPerson = minimumAssociatedPerson;
            return this;
        }

        public boolean isMinimumDefenceOrganisation() {
            return minimumDefenceOrganisation;
        }

        public CoreTemplateArguments setMinimumDefenceOrganisation(boolean minimumDefenceOrganisation) {
            this.minimumDefenceOrganisation = minimumDefenceOrganisation;
            return this;
        }

        public boolean isMinimumPerson() {
            return minimumPerson;
        }

        public CoreTemplateArguments setMinimumPerson(boolean minimumPerson) {
            this.minimumPerson = minimumPerson;
            return this;
        }

        public boolean isMinimumOrganisation() {
            return minimumOrganisation;
        }

        public CoreTemplateArguments setMinimumOrganisation(boolean minimumOrganisation) {
            this.minimumOrganisation = minimumOrganisation;
            return this;
        }

        public boolean isMinimumOffence() {
            return minimumOffence;
        }

        public CoreTemplateArguments setMinimumOffence(boolean minimumOffence) {
            this.minimumOffence = minimumOffence;
            return this;
        }

        public CoreTemplateArguments setStructure(Map<UUID, Map<UUID, List<UUID>>> structure) {
            this.structure = structure;
            return this;
        }

        public CoreTemplateArguments setConvicted(boolean convicted) {
            this.convicted = convicted;
            return this;
        }

        public CoreTemplateArguments setOffenceWithNullCount() {
            this.isOffenceCountNull = true;
            return this;
        }

        public CoreTemplateArguments setAllocationDecision(final boolean allocationDecision) {
            this.isAllocationDecision = allocationDecision;
            return this;
        }

    }

    public static Target.Builder targetForDocumentIsDeleted(UUID hearingId, UUID defendantId, UUID offenceId, UUID resultLineId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLineForDocumentIsDeleted(resultLineId))));
    }

    public static ResultLine resultLineForDocumentIsDeleted(UUID resultLineId) {
        return ResultLine.resultLine()
                .withResultDefinitionId(randomUUID())
                .withResultLineId(resultLineId)
                .withResultLabel(STRING.next())
                .withLevel(Level.CASE)
                .withOrderedDate(PAST_LOCAL_DATE.next())
                .withSharedDate(PAST_LOCAL_DATE.next())
                .withPrompts(new ArrayList<>(singletonList(Prompt.prompt()
                        .withId(randomUUID())
                        .build()))
                )
                .withDelegatedPowers(null)
                .withIsComplete(true)
                .withIsModified(false)
                .withIsDeleted(true)
                .build();
    }

    public static Target.Builder targetForOffenceResultShared(UUID hearingId, UUID defendantId, UUID offenceId, UUID resultLineId, UUID resultDefinitionId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLineForOffenceResultShared(resultLineId, resultDefinitionId))));
    }

    public static ResultLine resultLineForOffenceResultShared(UUID resultLineId, UUID resultDefinitionId) {
        return ResultLine.resultLine()
                .withResultDefinitionId(resultDefinitionId)
                .withResultLineId(resultLineId)
                .withResultLabel(STRING.next())
                .withLevel(Level.OFFENCE)
                .withOrderedDate(PAST_LOCAL_DATE.next())
                .withSharedDate(PAST_LOCAL_DATE.next())
                .withPrompts(new ArrayList<>(singletonList(Prompt.prompt()
                        .withId(randomUUID())
                        .build()))
                )
                .withDelegatedPowers(null)
                .withIsComplete(true)
                .withIsModified(false)
                .build();
    }

}