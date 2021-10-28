package uk.gov.moj.cpp.hearing.test;


import static com.google.common.collect.ImmutableList.of;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.core.courts.BailStatus.bailStatus;
import static uk.gov.justice.core.courts.DefenceCounsel.defenceCounsel;
import static uk.gov.justice.core.courts.FundingType.REPRESENTATION_ORDER;
import static uk.gov.justice.core.courts.HearingLanguage.WELSH;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.SecondaryCJSCode.secondaryCJSCode;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.NI_NUMBER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_UTC_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.POST_CODE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.ORGANISATION;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.Pair.p;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtIndicatedSentence;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.CustodialEstablishment;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.DefenceOrganisation;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Ethnicity;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.JudicialResult;
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
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ReferralReason;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.JudicialRoleTypeEnum;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "squid:S1067"})
public class CoreTestTemplates {

    private static final UUID BAIL_STATUS_ID = randomUUID();
    private static final String JSON_STRING = "{\"results\":[{\"isDeleted\":false,\"isModified\":false,\"resultCode\":\"d0a369c9-5a28-40ec-99cb-da7943550b13\",\"orderedDate\":\"2021-05-27\"}]}";
    private static final String REPORTING_RESTRICTION_LABEL_YES = "Yes";
    private static final String REPORTING_RESTRICTION_LABEL_SECOND = "Second";
    public static final String VALUE = "2017-05-20";

    public static CoreTemplateArguments defaultArguments() {
        return new CoreTemplateArguments();
    }

    public static HearingDay.Builder hearingDay() {
        return HearingDay.hearingDay()
                .withSittingDay(PAST_UTC_DATE_TIME.next())
                .withListingSequence(INTEGER.next())
                .withListedDurationMinutes(INTEGER.next());
    }

    public static HearingDay.Builder hearingDay(final ZonedDateTime sittingDate) {
        return HearingDay.hearingDay()
                .withSittingDay(sittingDate)
                .withListingSequence(INTEGER.next())
                .withListedDurationMinutes(INTEGER.next());
    }

    public static HearingDay.Builder hearingDay(CourtCentre courtCentre) {
        return HearingDay.hearingDay()
                .withHasSharedResults(true)
                .withSittingDay(RandomGenerator.PAST_UTC_DATE_TIME.next())
                .withListingSequence(INTEGER.next())
                .withListedDurationMinutes(INTEGER.next())
                .withCourtCentreId(courtCentre.getId())
                .withCourtRoomId(courtCentre.getRoomId());
    }

    public static HearingDay.Builder hearingDay(ZonedDateTime sittingDay, int sequence) {
        return HearingDay.hearingDay()
                .withSittingDay(sittingDay)
                .withListingSequence(sequence)
                .withListedDurationMinutes(INTEGER.next())
                .withCourtCentreId(randomUUID())
                .withCourtRoomId(randomUUID());
    }

    public static HearingDay.Builder hearingDayWithParam(final int year, final int month, final int day, final int seq) {
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

    public static CourtCentre.Builder courtCentreWithArgs(final String courtRoomName) {
        return CourtCentre.courtCentre()
                .withId(randomUUID())
                .withName(courtRoomName)
                .withWelshName(STRING.next())
                .withRoomId(randomUUID())
                .withRoomName(STRING.next())
                .withWelshRoomName(STRING.next());
    }


    public static JudicialRole.Builder judiciaryRole(final CoreTemplateArguments args) {
        return JudicialRole.judicialRole()
                .withJudicialId(randomUUID())
                .withFirstName(STRING.next())
                .withMiddleName(STRING.next())
                .withLastName(STRING.next())
                .withIsBenchChairman(BOOLEAN.next())
                .withIsDeputy(BOOLEAN.next())
                .withTitle(STRING.next())
                .withUserId(randomUUID())
                .withJudicialRoleType(args.jurisdictionType == JurisdictionType.CROWN ? circuitJudge() : magistrate());
    }

    public static ProsecutionCaseIdentifier.Builder prosecutionCaseIdentifier(final CoreTemplateArguments args) {
        return ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withProsecutionAuthorityId(randomUUID())
                .withProsecutionAuthorityReference(args.jurisdictionType == JurisdictionType.MAGISTRATES ? STRING.next() : null)
                .withProsecutionAuthorityCode(STRING.next())
                .withCaseURN(args.jurisdictionType == JurisdictionType.CROWN ? STRING.next() : null);
    }

    public static NotifiedPlea.Builder notifiedPlea(final UUID offenceId) {
        return NotifiedPlea.notifiedPlea()
                .withOffenceId(offenceId)
                .withNotifiedPleaValue(RandomGenerator.values(NotifiedPleaValue.values()).next())
                .withNotifiedPleaDate(PAST_LOCAL_DATE.next());
    }

    public static IndicatedPlea.Builder indicatedPlea(final UUID offenceId, final IndicatedPleaValue indicatedPleaValue) {
        return IndicatedPlea.indicatedPlea()
                .withOffenceId(offenceId)
                .withIndicatedPleaDate(PAST_LOCAL_DATE.next())
                .withIndicatedPleaValue(indicatedPleaValue)
                .withSource(RandomGenerator.values(Source.values()).next());
    }

    public static Plea.Builder plea(final UUID offenceId, final LocalDate convictionDate, final String pleaValue, final UUID courtapplicationId) {
        return Plea.plea()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(randomUUID())
                .withDelegatedPowers(delegatedPowers().build())
                .withPleaDate(convictionDate)
                .withPleaValue(pleaValue)
                .withApplicationId(courtapplicationId);
    }

    public static AllocationDecision.Builder allocationDecision(final UUID offenceId) {
        return AllocationDecision.allocationDecision()
                .withOriginatingHearingId(randomUUID())
                .withOffenceId(offenceId)
                .withMotReasonId(randomUUID())
                .withMotReasonDescription(STRING.next())
                .withMotReasonCode(STRING.next())
                .withSequenceNumber(INTEGER.next())
                .withAllocationDecisionDate(now())
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
                .withAlcoholReadingMethodCode("B")
                .withVehicleMake(STRING.next())
                .withVehicleRegistration(STRING.next());

    }

    public static Offence.Builder offence(final CoreTemplateArguments args, final UUID offenceId) {

        if (args.isMinimumOffence()) {
            return Offence.offence()
                    .withId(offenceId)
                    .withStartDate(PAST_LOCAL_DATE.next())
                    .withOffenceDefinitionId(randomUUID())
                    .withOffenceCode(STRING.next())
                    .withCount(INTEGER.next())
                    .withWording(STRING.next())
                    .withOrderIndex(INTEGER.next())
                    .withIntroducedAfterInitialProceedings(true)
                    .withIsDiscontinued(true)
                    .withProceedingsConcluded(true)
                    .withEndorsableFlag(true)
                    .withReportingRestrictions(of(ReportingRestriction.reportingRestriction()
                                    .withId(randomUUID())
                                    .withLabel(REPORTING_RESTRICTION_LABEL_YES)
                                    .withJudicialResultId(randomUUID()).build(),
                            ReportingRestriction.reportingRestriction()
                                    .withId(randomUUID())
                                    .withLabel(REPORTING_RESTRICTION_LABEL_SECOND)
                                    .withJudicialResultId(randomUUID()).build()))
                    .withOffenceDateCode(args.getOffenceDateCode());
        }

        final Offence.Builder result = Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withIndicatedPlea(args.indicatedPlea == null ? null : indicatedPlea(offenceId, args.indicatedPlea).build())
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
                .withModeOfTrial("Either Way")
                .withOrderIndex(INTEGER.next())
                .withProceedingsConcluded(true)
                .withIsDiscontinued(true)
                .withIntroducedAfterInitialProceedings(true)
                .withLaidDate(PAST_LOCAL_DATE.next())
                .withListingNumber(INTEGER.next())
                .withEndorsableFlag(true)
                .withOffenceDateCode(args.getOffenceDateCode())
                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                        .withDaysSpent(INTEGER.next())
                        .withTimeLimit(PAST_LOCAL_DATE.next())
                        .withIsCtlExtended(false)
                        .build())
                .withReportingRestrictions(of(ReportingRestriction.reportingRestriction()
                        .withId(randomUUID())
                        .withJudicialResultId(randomUUID())
                        .withLabel(REPORTING_RESTRICTION_LABEL_YES)
                        .withOrderedDate(now())
                        .build()));

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

    public static ReportingRestriction.Builder reportingRestriction() {
        return ReportingRestriction.reportingRestriction()
                .withId(randomUUID())
                .withJudicialResultId(randomUUID())
                .withLabel(REPORTING_RESTRICTION_LABEL_YES)
                .withOrderedDate(now());
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

    public static Person.Builder person(final CoreTemplateArguments args) {

        if (args.isMinimumPerson()) {
            return Person.person()
                    .withTitle("Mr")
                    .withLastName(STRING.next())
                    .withGender(RandomGenerator.values(Gender.values()).next());
        }

        return Person.person()
                .withTitle("Lieutanant")
                .withContact(contactNumber().build())
                .withAdditionalNationalityCode(STRING.next())
                .withAdditionalNationalityId(randomUUID())
                .withDateOfBirth(PAST_LOCAL_DATE.next())
                .withDisabilityStatus(STRING.next())
                .withDocumentationLanguageNeeds(args.hearingLanguage == WELSH ? HearingLanguage.WELSH : HearingLanguage.ENGLISH)
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

    public static AssociatedPerson.Builder associatedPerson(final CoreTemplateArguments args) {
        return AssociatedPerson.associatedPerson()
                .withPerson(person(args).build())
                .withRole(STRING.next());
    }

    public static Organisation.Builder organisation(final CoreTemplateArguments args) {

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

    public static PersonDefendant.Builder personDefendant(final CoreTemplateArguments args) {
        return PersonDefendant.personDefendant()
                .withPersonDetails(person(args).build())
                .withArrestSummonsNumber(STRING.next())
                .withBailStatus(bailStatus().withId(BAIL_STATUS_ID).withCode("C").withDescription("Remanded into Custody").build())
                .withDriverNumber(STRING.next())
                .withPerceivedBirthYear(INTEGER.next())
                .withEmployerOrganisation(organisation(args).build())
                .withCustodialEstablishment(custodialEstablishment(args))
                .withEmployerPayrollReference(STRING.next())
                .withCustodyTimeLimit(PAST_LOCAL_DATE.next());
    }

    public static CustodialEstablishment custodialEstablishment(final CoreTemplateArguments args) {
        if (args.isPutCustodialEstablishment()) {
            return CustodialEstablishment.custodialEstablishment()
                    .withName(STRING.next())
                    .withId(randomUUID())
                    .withCustody(STRING.next())
                    .build();
        } else {
            return null;
        }
    }

    public static LegalEntityDefendant.Builder legalEntityDefendant(final CoreTemplateArguments args) {
        return LegalEntityDefendant.legalEntityDefendant()
                .withOrganisation(organisation(args).build());
    }

    public static AssociatedDefenceOrganisation.Builder associatedDefenceOrganisation() {
        return AssociatedDefenceOrganisation.associatedDefenceOrganisation()
                .withApplicationReference("application-reference")
                .withAssociationStartDate(LocalDate.parse("2019-09-12"))
                .withAssociationEndDate(LocalDate.parse("2019-12-12"))
                .withDefenceOrganisation(DefenceOrganisation.defenceOrganisation()
                        .withLaaContractNumber("LAA44569")
                        .withOrganisation(Organisation.organisation()
                                .withIncorporationNumber("cegH7rIgdX")
                                .withName("Test")
                                .withRegisteredCharityNumber("TestCharity")
                                .withContact(ContactNumber.contactNumber()
                                        .withPrimaryEmail("zdivwdsblf@gxvm7kqbh4.duzrohmbtt")
                                        .withSecondaryEmail("dusl1j0oxw@0rzelb2mln.rvjnuth3ar")
                                        .withWork("584591171")
                                        .withMobile("1444010616")
                                        .withFax("765997700")
                                        .withHome("759019681")
                                        .build())
                                .withAddress(Address.address()
                                        .withAddress1("defenceOrganisation")
                                        .withAddress2("225")
                                        .withAddress3("FuseRoad")
                                        .withAddress4("East Croydon")
                                        .withPostcode("LN72 9NG")
                                        .build())
                                .build())
                        .build())
                .withFundingType(REPRESENTATION_ORDER)
                .withIsAssociatedByLAA(true);
    }

    public static Defendant.Builder defendant(final UUID prosecutionCaseId, final CoreTemplateArguments args, final Pair<UUID, List<UUID>> structure) {

        return Defendant.defendant()
                .withId(structure.getK())
                .withMasterDefendantId(args.getDifferentMasterDefendantId() != null ? args.getDifferentMasterDefendantId() : structure.getK())
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
                .withLegalEntityDefendant(args.defendantType == ORGANISATION ? legalEntityDefendant(args).build() : null)
                .withCourtProceedingsInitiated(args.getCourtProceedingsInitiated() != null ? args.getCourtProceedingsInitiated() : ZonedDateTime.now(ZoneOffset.UTC))
                .withProceedingsConcluded(Boolean.FALSE);

    }


    public static Defendant.Builder defendantJudicialResults(final UUID prosecutionCaseId, final CoreTemplateArguments args, final Pair<UUID, List<UUID>> structure) {

        final List<AssociatedPerson> nonAssociatePerson = null;
        final Organisation noneDefenceOrganisation = null;

        return Defendant.defendant()
                .withId(structure.getK())
                .withMasterDefendantId(args.getDifferentMasterDefendantId() != null ? args.getDifferentMasterDefendantId() : structure.getK())
                .withProsecutionCaseId(prosecutionCaseId)
                .withNumberOfPreviousConvictionsCited(INTEGER.next())
                .withProsecutionAuthorityReference(STRING.next())
                .withIsYouth(Boolean.TRUE)
                .withDefendantCaseJudicialResults(asList(JudicialResult.judicialResult()
                        .withJudicialResultTypeId(fromString("8c67b30a-418c-11e8-842f-0ed5f89f718b"))
                        .withLabel("Defendant's details changed")
                        .withCjsCode("4592")
                        .withDrivingTestStipulation(1)
                        .withPointsDisqualificationCode("TT99")
                        .withDvlaCode("C")
                        .withSecondaryCJSCodes(asList(secondaryCJSCode()
                                .withCjsCode("1234")
                                .withText("SecondaryCJSCode text1")
                                .build(), secondaryCJSCode()
                                .withCjsCode("5678")
                                .withText("SecondaryCJSCode text2")
                                .build()))
                        .build()))
                .withOffences(
                        structure.getV().stream()
                                .map(offenceId -> offence(args, offenceId).build())
                                .collect(toList())
                )
                .withAssociatedPersons(args.isMinimumAssociatedPerson() ? asList(associatedPerson(args).build()) : nonAssociatePerson)
                .withDefenceOrganisation(args.isMinimumDefenceOrganisation() ? organisation(args).build() : noneDefenceOrganisation)
                .withPersonDefendant(args.defendantType == PERSON ? personDefendant(args).build() : null)
                .withLegalEntityDefendant(args.defendantType == ORGANISATION ? legalEntityDefendant(args).build() : null)
                .withCourtProceedingsInitiated(args.getCourtProceedingsInitiated() != null ? args.getCourtProceedingsInitiated() : ZonedDateTime.now(ZoneOffset.UTC))
                .withProceedingsConcluded(Boolean.FALSE);

    }

    public static Marker.Builder marker(final Pair<UUID, List<UUID>> structure) {

        return Marker.marker()
                .withId(structure.getK())
                .withMarkerTypeCode(STRING.next())
                .withMarkerTypeDescription(STRING.next())
                .withMarkerTypeid(UUID.randomUUID());
    }

    public static ProsecutionCase.Builder prosecutionCase(final CoreTemplateArguments args, final Pair<UUID, Map<UUID, List<UUID>>> structure, final boolean withJudicialResults) {

        return ProsecutionCase.prosecutionCase()
                .withId(structure.getK())
                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier(args).build())
                .withCaseStatus(STRING.next())
                .withOriginatingOrganisation(STRING.next())
                .withInitiationCode(RandomGenerator.values(InitiationCode.values()).next())
                .withStatementOfFacts(STRING.next())
                .withStatementOfFactsWelsh(STRING.next())
                .withClassOfCase("Class 1")
                .withCaseMarkers(buildCaseMarkers())
                .withDefendants(
                        structure.getV().entrySet().stream()
                                .map(entry -> withJudicialResults ? defendantJudicialResults(structure.getK(), args, p(entry.getKey(), entry.getValue())).build() :
                                        defendant(structure.getK(), args, p(entry.getKey(), entry.getValue())).build()
                                )
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

    public static HearingType.Builder hearingType(final Optional<UUID> hearingTypeId) {
        return HearingType.hearingType()
                .withId(hearingTypeId.orElseGet(UUID::randomUUID))
                .withDescription(STRING.next())
                .withWelshDescription(STRING.next());
    }

    public static Hearing.Builder hearing(final CoreTemplateArguments args, final boolean withJudicialResults) {
        final Hearing.Builder hearingBuilder = Hearing.hearing();
        CourtCentre courtCentre;

        if (args.hearingLanguage == WELSH) {
            hearingBuilder.withHearingLanguage(HearingLanguage.WELSH);
            courtCentre = courtCentreWithArgs("welshCourtRoom").build();
        } else {
            hearingBuilder.withHearingLanguage(HearingLanguage.ENGLISH);
            courtCentre = courtCentre().build();
        }

        hearingBuilder.withId(randomUUID())
                .withType(hearingType(Optional.empty()).build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(hearingDay(courtCentre).build()))
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue()), withJudicialResults).build())
                                .collect(toList())
                )

                .withCourtApplications(asList((new HearingFactory().courtApplication().build())))
                .withCourtCentre(courtCentre)
                .withIsBoxHearing(args.getIsBoxHearing());

        return hearingBuilder;
    }

    public static Hearing.Builder hearingWithAllLevelJudicialResults(final CoreTemplateArguments args) {
        final Hearing.Builder hearingBuilder = Hearing.hearing();
        CourtCentre courtCentre;

        if (args.hearingLanguage == WELSH) {
            hearingBuilder.withHearingLanguage(HearingLanguage.WELSH);
            courtCentre = courtCentreWithArgs("welshCourtRoom").build();
        } else {
            hearingBuilder.withHearingLanguage(HearingLanguage.ENGLISH);
            courtCentre = courtCentre().build();
        }

        hearingBuilder.withId(randomUUID())
                .withType(hearingType(Optional.empty()).build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(hearingDay(courtCentre).build()))
                .withDefendantJudicialResults(asList(DefendantJudicialResult.defendantJudicialResult().withMasterDefendantId(randomUUID()).build()))
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue()), true).build())
                                .collect(toList())
                )

                .withCourtApplications(asList((new HearingFactory().courtApplication().build())))
                .withCourtCentre(courtCentre);

        return hearingBuilder;
    }


    public static Hearing.Builder hearing(final CoreTemplateArguments args) {
        return hearing(args, false);
    }

    public static Hearing.Builder hearingWithParam(CoreTemplateArguments args, UUID courtAndRoomId, final String courtRoomName, final LocalDate localDate) throws NoSuchAlgorithmException {
        final Random random = SecureRandom.getInstanceStrong();
        final int min = 1;
        final int max = 5;
        final LocalDate dayAfter = localDate.plusDays(1);
        final LocalDate daybefore = localDate.minusDays(1);
        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withId(randomUUID())
                .withType(hearingType(Optional.empty()).build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(hearingDayWithParam(dayAfter.getYear(), dayAfter.getMonthValue(), dayAfter.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(daybefore.getYear(), daybefore.getMonthValue(), daybefore.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build()))
                .withCourtCentre(courtCentreWithArgs(courtAndRoomId, courtRoomName).build())
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue()), false).build())
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

    public static BailStatus getBailStatus(final String code, final String description) {
        return bailStatus()
                .withId(UUID.randomUUID())
                .withCustodyTimeLimit(null)
                .withCode(code)
                .withDescription(description)
                .build();
    }

    public static AllocationDecision.Builder allocationDecision(final UUID offenceId, final String reason) {
        return AllocationDecision.allocationDecision()
                .withOriginatingHearingId(randomUUID())
                .withOffenceId(offenceId)
                .withMotReasonId(randomUUID())
                .withMotReasonDescription(reason)
                .withMotReasonCode(STRING.next())
                .withSequenceNumber(INTEGER.next())
                .withAllocationDecisionDate(now())
                .withCourtIndicatedSentence(courtIndicatedSentence().build());
    }
    @SuppressWarnings("squid:S107")
    public static Hearing.Builder hearingWithParam(final CoreTemplateArguments args,
                                                   final UUID courtId,
                                                   final UUID courtRoomId,
                                                   final String courtRoomName,
                                                   final LocalDate localDate,
                                                   final UUID defenceCounselId,
                                                   final UUID caseId,
                                                   final Optional<UUID> hearingTypeId) throws NoSuchAlgorithmException {
        final Random random = SecureRandom.getInstanceStrong();
        final int min = 1;
        final int max = 5;
        final LocalDate dayAfter = localDate.plusDays(1);
        final LocalDate daybefore = localDate.minusDays(1);

        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withId(randomUUID())
                .withType(hearingType(hearingTypeId).build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(hearingDayWithParam(dayAfter.getYear(), dayAfter.getMonthValue(), dayAfter.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(daybefore.getYear(), daybefore.getMonthValue(), daybefore.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build()))
                .withCourtCentre(courtCentreWithArgs(courtId, courtRoomId, courtRoomName).build())
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withDefenceCounsels(
                        singletonList(
                                defenceCounsel()
                                        .withId(defenceCounselId)
                                        .withAttendanceDays(asList(now()))
                                        .withDefendants(asList(randomUUID()))
                                        .withFirstName("John")
                                        .withLastName("Jones")
                                        .withTitle("Mr")
                                        .withStatus("OPEN")
                                        .build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue()), false).build())
                                .collect(toList())
                )

                .withCourtApplications(asList((new HearingFactory().courtApplication()
                        .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                                .withProsecutionCaseId(caseId)
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                        .withCaseURN("Case Reference")
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode(STRING.next())
                                        .build())
                                .withIsSJP(false)
                                .withCaseStatus("ACTIVE")
                                .withOffences(singletonList(getOffence()))
                                .build()))
                        .withCourtOrder(CourtOrder.courtOrder()
                                .withId(randomUUID())
                                .withJudicialResultTypeId(randomUUID())
                                .withLabel("label")
                                .withOrderDate(localDate)
                                .withStartDate(localDate)
                                .withOrderingCourt(courtCentreWithArgs(courtId, courtRoomId, courtRoomName).build())
                                .withOrderingHearingId(randomUUID())
                                .withIsSJPOrder(false)
                                .withCanBeSubjectOfBreachProceedings(false)
                                .withCanBeSubjectOfVariationProceedings(false)
                                .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                        .withOffence(getOffence())
                                        .withProsecutionCaseId(randomUUID())
                                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier(args).build())
                                        .build()))
                                .build())
                        .build())));

        if (args.hearingLanguage == WELSH) {
            hearingBuilder.withHearingLanguage(HearingLanguage.WELSH);
        } else {
            hearingBuilder.withHearingLanguage(HearingLanguage.ENGLISH);
        }
        return hearingBuilder;
    }

    public static Hearing.Builder hearingWithCourtOrder(final CoreTemplateArguments args,
                                                        final UUID courtId,
                                                        final UUID courtRoomId,
                                                        final String courtRoomName,
                                                        final LocalDate localDate,
                                                        final UUID defenceCounselId,
                                                        final UUID caseId,
                                                        final Optional<UUID> hearingTypeId) throws NoSuchAlgorithmException {
        final Random random = SecureRandom.getInstanceStrong();
        final int min = 1;
        final int max = 5;
        final LocalDate dayAfter = localDate.plusDays(1);
        final LocalDate daybefore = localDate.minusDays(1);

        final Hearing.Builder hearingBuilder = Hearing.hearing()
                .withId(randomUUID())
                .withType(hearingType(hearingTypeId).build())
                .withJurisdictionType(args.jurisdictionType)
                .withReportingRestrictionReason(STRING.next())
                .withHearingDays(asList(hearingDayWithParam(dayAfter.getYear(), dayAfter.getMonthValue(), dayAfter.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build(),
                        hearingDayWithParam(daybefore.getYear(), daybefore.getMonthValue(), daybefore.getDayOfMonth(), random.nextInt((max - min) + 1) + min).build()))
                .withCourtCentre(courtCentreWithArgs(courtId, courtRoomId, courtRoomName).build())
                .withJudiciary(singletonList(judiciaryRole(args).build()))
                .withDefendantReferralReasons(singletonList(referralReason().build()))
                .withDefenceCounsels(
                        singletonList(
                                defenceCounsel()
                                        .withId(defenceCounselId)
                                        .withAttendanceDays(asList(now()))
                                        .withDefendants(asList(randomUUID()))
                                        .withFirstName("John")
                                        .withLastName("Jones")
                                        .withTitle("Mr")
                                        .withStatus("OPEN")
                                        .build()))
                .withProsecutionCases(
                        args.structure.entrySet().stream()
                                .map(entry -> prosecutionCase(args, p(entry.getKey(), entry.getValue()), false).build())
                                .collect(toList())
                )
                .withCourtApplications(asList((new HearingFactory().courtApplication()
                        .withCourtApplicationCases(null)
                        .withCourtOrder(CourtOrder.courtOrder()
                                .withId(randomUUID())
                                .withJudicialResultTypeId(randomUUID())
                                .withLabel("label")
                                .withOrderDate(localDate)
                                .withStartDate(localDate)
                                .withOrderingCourt(courtCentreWithArgs(courtId, courtRoomId, courtRoomName).build())
                                .withOrderingHearingId(randomUUID())
                                .withIsSJPOrder(false)
                                .withCanBeSubjectOfBreachProceedings(false)
                                .withCanBeSubjectOfVariationProceedings(false)
                                .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                        .withOffence(getOffence())
                                        .withProsecutionCaseId(caseId)
                                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier(args).build())
                                        .build()))
                                .build())
                        .build())));

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

    public static Target.Builder target(final UUID hearingId, final UUID defendantId, final UUID offenceId, final UUID resultLineId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLine(resultLineId))));
    }

    public static Target2.Builder target2(final UUID hearingId, final UUID defendantId, final UUID offenceId, final UUID resultLineId) {
        return Target2.target2()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLine2(resultLineId))));
    }

    public static Target.Builder target(final UUID hearingId, final LocalDate hearingDay, final UUID defendantId, final UUID offenceId, final UUID resultLineId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLine(resultLineId))));
    }

    public static ResultLine resultLine(final UUID resultLineId) {
        return ResultLine.resultLine()
                .withResultDefinitionId(randomUUID())
                .withResultLineId(resultLineId)
                .withResultLabel(STRING.next())
                .withLevel(Level.CASE)
                .withOrderedDate(PAST_LOCAL_DATE.next())
                .withSharedDate(PAST_LOCAL_DATE.next())
                .withPrompts(new ArrayList<>(singletonList(Prompt.prompt()
                        .withId(randomUUID())
                        .withValue(VALUE)
                        .build()))
                )
                .withDelegatedPowers(null)
                .withIsComplete(true)
                .withIsModified(false)
                .withIsDeleted(false)
                .build();
    }

    public static ResultLine2 resultLine2(final UUID resultLineId) {
        return ResultLine2.resultLine2()
                .withResultDefinitionId(randomUUID())
                .withResultLineId(resultLineId)
                .withResultLabel(STRING.next())
                .withLevel(Level.CASE)
                .withOrderedDate(PAST_LOCAL_DATE.next())
                .withSharedDate(PAST_LOCAL_DATE.next())
                .withPrompts(new ArrayList<>(singletonList(Prompt.prompt()
                        .withId(randomUUID())
                        .withValue(VALUE)
                        .build()))
                )
                .withDelegatedPowers(null)
                .withIsComplete(true)
                .withIsModified(false)
                .withIsDeleted(false)
                .build();
    }

    public static ResultLine resultLine(final UUID resultDefinitionId, final UUID resultLineId, final boolean isDeleted) {
        return ResultLine.resultLine()
                .withResultDefinitionId(resultDefinitionId)
                .withResultLineId(resultLineId)
                .withResultLabel(STRING.next())
                .withLevel(Level.CASE)
                .withOrderedDate(PAST_LOCAL_DATE.next())
                .withSharedDate(PAST_LOCAL_DATE.next())
                .withPrompts(new ArrayList<>(singletonList(Prompt.prompt()
                        .withId(randomUUID())
                        .withValue(VALUE)
                        .build()))
                )
                .withDelegatedPowers(null)
                .withIsComplete(true)
                .withIsModified(false)
                .withIsDeleted(isDeleted)
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

    public static Target.Builder targetForDocumentIsDeleted(final UUID hearingId, final UUID defendantId, final UUID offenceId, final UUID resultLineId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLineForDocumentIsDeleted(resultLineId))));
    }

    public static ResultLine resultLineForDocumentIsDeleted(final UUID resultLineId) {
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

    public static Target.Builder targetForOffenceResultShared(final UUID hearingId, final UUID defendantId, final UUID offenceId, final UUID resultLineId, final UUID resultDefinitionId) {
        return Target.target()
                .withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withDraftResult(JSON_STRING)
                .withResultLines(new ArrayList<>(asList(resultLineForOffenceResultShared(resultLineId, resultDefinitionId))));
    }

    public static ResultLine resultLineForOffenceResultShared(final UUID resultLineId, final UUID resultDefinitionId) {
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

    private static Offence getOffence() {
        return Offence.offence().withId(randomUUID())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceCode("OFC")
                .withOffenceTitle("OFC TITLE")
                .withWording("WORDING")
                .withStartDate(LocalDate.now())
                .withOffenceLegislation("OffenceLegislation")
                .build();
    }

    public enum DefendantType {
        PERSON, ORGANISATION
    }

    public static class CoreTemplateArguments {

        private JurisdictionType jurisdictionType = JurisdictionType.CROWN;

        private HearingLanguage hearingLanguage = HearingLanguage.ENGLISH;

        private DefendantType defendantType = PERSON;

        private IndicatedPleaValue indicatedPlea = INDICATED_GUILTY;

        private UUID differentMasterDefendantId;

        private ZonedDateTime courtProceedingsInitiated;

        private boolean minimumAssociatedPerson;
        private boolean minimumDefenceOrganisation;
        private boolean minimumPerson;
        private boolean minimumOrganisation;
        private boolean minimumOffence;
        private boolean convicted = false;
        private boolean isOffenceCountNull = false;
        private boolean isAllocationDecision = true;
        private boolean putCustodialEstablishment = true;
        private Boolean isBoxHearing;

        private Map<UUID, Map<UUID, List<UUID>>> structure = toMap(randomUUID(), toMap(randomUUID(), asList(randomUUID())));

        private Integer offenceDateCode;

        public static <T, U> Map<T, U> toMap(final T t, final U u) {
            final Map<T, U> map = new HashMap<>();
            map.put(t, u);
            return map;
        }

        public static <T, U> Map<T, U> toMap(final List<Pair<T, U>> pairs) {
            return pairs.stream().collect(Collectors.toMap(Pair::getK, Pair::getV));
        }

        public CoreTemplateArguments setJurisdictionType(final JurisdictionType jurisdictionType) {
            this.jurisdictionType = jurisdictionType;
            return this;
        }

        public CoreTemplateArguments setHearingLanguage(final HearingLanguage hearingLanguage) {
            this.hearingLanguage = hearingLanguage;
            return this;
        }

        public CoreTemplateArguments setDefendantType(final DefendantType defendantType) {
            this.defendantType = defendantType;
            return this;
        }

        public CoreTemplateArguments setIndicatedPleaValue(final IndicatedPleaValue indicatedPleaValue) {
            this.indicatedPlea = indicatedPleaValue;
            return this;
        }

        public boolean isMinimumAssociatedPerson() {
            return minimumAssociatedPerson;
        }

        public CoreTemplateArguments setMinimumAssociatedPerson(final boolean minimumAssociatedPerson) {
            this.minimumAssociatedPerson = minimumAssociatedPerson;
            return this;
        }

        public boolean isMinimumDefenceOrganisation() {
            return minimumDefenceOrganisation;
        }

        public CoreTemplateArguments setMinimumDefenceOrganisation(final boolean minimumDefenceOrganisation) {
            this.minimumDefenceOrganisation = minimumDefenceOrganisation;
            return this;
        }

        public boolean isMinimumPerson() {
            return minimumPerson;
        }

        public CoreTemplateArguments setMinimumPerson(final boolean minimumPerson) {
            this.minimumPerson = minimumPerson;
            return this;
        }

        public boolean isMinimumOrganisation() {
            return minimumOrganisation;
        }

        public CoreTemplateArguments setMinimumOrganisation(final boolean minimumOrganisation) {
            this.minimumOrganisation = minimumOrganisation;
            return this;
        }

        public boolean isMinimumOffence() {
            return minimumOffence;
        }

        public CoreTemplateArguments setMinimumOffence(final boolean minimumOffence) {
            this.minimumOffence = minimumOffence;
            return this;
        }

        public CoreTemplateArguments setStructure(final Map<UUID, Map<UUID, List<UUID>>> structure) {
            this.structure = structure;
            return this;
        }

        public CoreTemplateArguments setConvicted(final boolean convicted) {
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

        public UUID getDifferentMasterDefendantId() {
            return differentMasterDefendantId;
        }

        public CoreTemplateArguments setDifferentMasterDefendantId(final UUID differentMasterDefendantId) {
            this.differentMasterDefendantId = differentMasterDefendantId;
            return this;
        }

        public boolean isPutCustodialEstablishment() {
            return putCustodialEstablishment;
        }

        public CoreTemplateArguments setPutCustodialEstablishment(final boolean putCustodialEstablishment) {
            this.putCustodialEstablishment = putCustodialEstablishment;
            return this;
        }

        public ZonedDateTime getCourtProceedingsInitiated() {
            return courtProceedingsInitiated;
        }

        public CoreTemplateArguments setCourtProceedingsInitiated(final ZonedDateTime courtProceedingsInitiated) {
            this.courtProceedingsInitiated = courtProceedingsInitiated;
            return this;
        }

        public Integer getOffenceDateCode() {
            return offenceDateCode;
        }

        public CoreTemplateArguments setOffenceDateCode(final Integer offenceDateCode) {
            this.offenceDateCode = offenceDateCode;
            return this;
        }

        public Boolean getIsBoxHearing() { return isBoxHearing;}

        public CoreTemplateArguments setIsBoxHearing(final boolean isBoxHearing){
            this.isBoxHearing = isBoxHearing;
            return this;
        }
    }


}
