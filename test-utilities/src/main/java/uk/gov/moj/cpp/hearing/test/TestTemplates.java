package uk.gov.moj.cpp.hearing.test;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.HearingLanguage.WELSH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand.updatePleaCommand;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.ORGANISATION;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.hearingDay;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.legalEntityDefendant;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.organisation;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.personDefendant;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.AttendanceType;
import uk.gov.justice.core.courts.Attendant;
import uk.gov.justice.core.courts.BreachType;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantAlias;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.Jurisdiction;
import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LaaReference;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.LinkType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceActiveOrder;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.SummonsTemplateType;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.hearing.courts.AddApplicantCounsel;
import uk.gov.justice.hearing.courts.AddCompanyRepresentative;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.AttendantType;
import uk.gov.justice.hearing.courts.Position;
import uk.gov.justice.hearing.courts.Role;
import uk.gov.justice.hearing.courts.UpdateApplicantCounsel;
import uk.gov.justice.hearing.courts.UpdateCompanyRepresentative;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateInterpreterIntermediary;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.offence.DefendantCaseOffences;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffences;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscription;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1188", "squid:S1135", "squid:S1314", "squid:S1192"})
public class TestTemplates {

    private static final String DAVID = "David";
    private static final String BOWIE = "Bowie";
    private static final String HEARING_DAY = "2021-03-01";
    private static final String WELSH_LABEL_SPACE = "welshLabel ";
    private static final String IMPRISONMENT = "imprisonment";
    private static final String DRAFT_RESULTS_CONTENT = "{}";
    public static final String FIXEDLISTCODE_0 = "fixedlistcode0";
    public static final String IMPRISONMENT_TERM = "imprisonment term";
    public static final String SIX_YEARS = "6 years";
    public static final String WELSH_VALUE = "6 blynedd";

    private TestTemplates() {
    }

    public static InterpreterIntermediary addInterpreterIntermediaryCommandTemplate(final Attendant attendant) {
        return new InterpreterIntermediary(
                Arrays.asList(now()),
                attendant,
                STRING.next(),
                randomUUID(),
                STRING.next(),
                Role.INTERMEDIARY);
    }

    public static class UpdateInterpreterIntermediaryCommandTemplates {
        private UpdateInterpreterIntermediaryCommandTemplates() {
        }

        public static UpdateInterpreterIntermediary updateInterpreterIntermediaryCommandTemplate(final UUID hearingId) {

            final Attendant attendant = new Attendant(AttendantType.WITNESS, null, STRING.next());
            final InterpreterIntermediary interpreterIntermediary = new InterpreterIntermediary(
                    Arrays.asList(now()),
                    attendant,
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    Role.INTERMEDIARY
            );

            return new UpdateInterpreterIntermediary(hearingId, interpreterIntermediary);
        }

        public static UpdateInterpreterIntermediary updateInterpreterIntermediaryCommandTemplate(final UUID hearingId, final InterpreterIntermediary interpreterIntermediary) {
            return new UpdateInterpreterIntermediary(hearingId, interpreterIntermediary);
        }
    }

    public static Target targetTemplate(final LocalDate hearingDay) {
        return Target.target()
                .withHearingId(randomUUID())
                .withHearingDay(hearingDay)
                .withDefendantId(randomUUID())
                .withDraftResult("{}")
                .withOffenceId(randomUUID())
                .withTargetId(randomUUID())
                .withResultLines(asList(
                        ResultLine.resultLine()
                                .withDelegatedPowers(
                                        DelegatedPowers.delegatedPowers()
                                                .withUserId(randomUUID())
                                                .withLastName(STRING.next())
                                                .withFirstName(STRING.next())
                                                .build())
                                .withIsComplete(BOOLEAN.next())
                                .withIsModified(BOOLEAN.next())
                                .withLevel(randomEnum(uk.gov.justice.core.courts.Level.class).next())
                                .withOrderedDate(PAST_LOCAL_DATE.next())
                                .withResultLineId(randomUUID())
                                .withResultLabel(STRING.next())
                                .withSharedDate(PAST_LOCAL_DATE.next())
                                .withResultDefinitionId(randomUUID())
                                .withPrompts(asList(
                                        uk.gov.justice.core.courts.Prompt.prompt()
                                                .withFixedListCode(STRING.next())
                                                .withId(randomUUID())
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .withWelshValue(STRING.next())
                                                .build()))
                                .build()))
                .build();
    }

    public static Target targetTemplate() {
        return targetTemplate(LocalDate.parse(HEARING_DAY));
    }

    public static Target targetTemplate(final UUID hearingId,
                                        final UUID defendantId,
                                        final UUID offenceId,
                                        final List<UUID> resultLineIds,
                                        final Map<UUID, UUID> resultDefinitionMap) {

        final List<ResultLine> resultLineList = new ArrayList<>();

        for (final UUID resultLineId : resultLineIds) {

            final UUID resultDefinitionId = resultDefinitionMap.getOrDefault(resultLineId, randomUUID());

            resultLineList.add(ResultLine.resultLine()
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(randomUUID())
                                    .withLastName(STRING.next())
                                    .withFirstName(STRING.next())
                                    .build())
                    .withIsComplete(true)
                    .withIsModified(BOOLEAN.next())
                    .withLevel(randomEnum(uk.gov.justice.core.courts.Level.class).next())
                    .withOrderedDate(PAST_LOCAL_DATE.next())
                    .withResultLineId(resultLineId)
                    .withResultLabel(STRING.next())
                    .withSharedDate(PAST_LOCAL_DATE.next())
                    .withResultDefinitionId(resultDefinitionId)
                    .withPrompts(asList(
                            uk.gov.justice.core.courts.Prompt.prompt()
                                    .withFixedListCode(STRING.next())
                                    .withId(randomUUID())
                                    .withLabel(STRING.next())
                                    .withValue(STRING.next())
                                    .withWelshValue(STRING.next())
                                    .build()))
                    .build());
        }
        return Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withDraftResult(STRING.next())
                .withOffenceId(offenceId)
                .withTargetId(randomUUID())
                .withResultLines(resultLineList)
                .build();
    }


    public static CaseDefendantDetailsWithHearingCommand initiateDefendantCommandTemplate(final UUID hearingId) {
        return CaseDefendantDetailsWithHearingCommand.caseDefendantDetailsWithHearingCommand()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate());
    }

    public static uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantTemplate() {

        final Defendant defendant = new Defendant();

        defendant.setId(randomUUID());

        defendant.setMasterDefendantId(randomUUID());

        defendant.setProsecutionCaseId(randomUUID());

        defendant.setNumberOfPreviousConvictionsCited(INTEGER.next());

        defendant.setProsecutionAuthorityReference(STRING.next());

        defendant.setWitnessStatement(STRING.next());

        defendant.setWitnessStatementWelsh(STRING.next());

        defendant.setMitigation(STRING.next());

        defendant.setMitigationWelsh(STRING.next());

        defendant.setAssociatedPersons(asList(associatedPerson(defaultArguments()).build()));

        defendant.setDefenceOrganisation(organisation(defaultArguments()).build());

        defendant.setPersonDefendant(personDefendant(defaultArguments()).build());

        defendant.setLegalEntityDefendant(legalEntityDefendant(defaultArguments()).build());

        defendant.setPncId("pnc1234");

        defendant.setAliases(Arrays.asList(DefendantAlias.defendantAlias()
                .withFirstName("Steve")
                .withLastName("Walsh")
                .build()));
        defendant.setAssociatedDefenceOrganisation(CoreTestTemplates.associatedDefenceOrganisation().build());

        return defendant;
    }

    public static Verdict verdictTemplate(final UUID offenceId, final VerdictCategoryType verdictCategoryType) {

        final boolean unanimous = BOOLEAN.next();

        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        return Verdict.verdict()
                .withVerdictType(VerdictType.verdictType()
                        .withId(randomUUID())
                        .withCategory(STRING.next())
                        .withCategoryType(verdictCategoryType.name())
                        .withDescription(STRING.next())
                        .withSequence(INTEGER.next())
                        .build()
                )
                .withOffenceId(offenceId)
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                        .withOffenceDefinitionId(randomUUID())
                        .withOffenceCode(STRING.next())
                        .withOffenceTitle(STRING.next())
                        .withOffenceTitleWelsh(STRING.next())
                        .withOffenceLegislation(STRING.next())
                        .withOffenceLegislationWelsh(STRING.next())
                        .build()
                )
                .withJurors(Jurors.jurors()
                        .withNumberOfJurors(integer(9, 12).next())
                        .withNumberOfSplitJurors(numberOfSplitJurors)
                        .withUnanimous(unanimous)
                        .build()
                )
                .build();
    }

    public static Verdict applicationVerdictTemplate() {

        final boolean unanimous = BOOLEAN.next();

        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        return Verdict.verdict()
                .withVerdictType(VerdictType.verdictType()
                        .withId(randomUUID())
                        .withCategory(STRING.next())
                        .withCategoryType("GUILTY")
                        .withDescription(STRING.next())
                        .withSequence(INTEGER.next())
                        .build()
                )
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                        .withOffenceDefinitionId(randomUUID())
                        .withOffenceCode(STRING.next())
                        .withOffenceTitle(STRING.next())
                        .withOffenceTitleWelsh(STRING.next())
                        .withOffenceLegislation(STRING.next())
                        .withOffenceLegislationWelsh(STRING.next())
                        .build()
                )
                .withJurors(Jurors.jurors()
                        .withNumberOfJurors(integer(9, 12).next())
                        .withNumberOfSplitJurors(numberOfSplitJurors)
                        .withUnanimous(unanimous)
                        .build()
                )
                .withApplicationId(randomUUID())
                .build();
    }

    public enum PleaValueType {GUILTY, NOT_GUILTY}

    public enum VerdictCategoryType {GUILTY, NOT_GUILTY, NO_VERDICT}

    public static class InitiateHearingCommandTemplates {
        private InitiateHearingCommandTemplates() {
        }

        public static InitiateHearingCommand minimumInitiateHearingTemplate() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setMinimumAssociatedPerson(true)
                            .setMinimumDefenceOrganisation(true)
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplate() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithIsBoxHearing(final boolean isBoxHearing) {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setIsBoxHearing(isBoxHearing)
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithMultiHearingDays() {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                    ).withHearingDays(Arrays.asList(hearingDay().build(), hearingDay().build())).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithOffenceDateCode(final Integer offenceDateCode) {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setOffenceDateCode(offenceDateCode)
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithMultidayHearing() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                    ).withHearingDays(Arrays.asList(hearingDay(PAST_ZONED_DATE_TIME.next()).build(), hearingDay(FUTURE_ZONED_DATE_TIME.next()).build()))
                            .build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithPastMultidayHearing(ZonedDateTime resultSharedDate) {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                    ).withHearingDays(Arrays.asList(hearingDay(resultSharedDate.minusDays(1)).build(), hearingDay(resultSharedDate.minusDays(2)).build()))
                            .build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithDefendantJudicialResultsForMagistrates() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                                    .setDefendantType(PERSON)
                                    .setHearingLanguage(ENGLISH)
                                    .setJurisdictionType(MAGISTRATES),
                            true
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithAllLevelJudicialResults() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearingWithAllLevelJudicialResults(defaultArguments()).build());
        }

        public static InitiateHearingCommand standardInitiateHearingWithApplicationTemplate(final List<CourtApplication> courtApplications) {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN))
                            .withCourtApplications(courtApplications)
                            .build());
        }

        public static InitiateHearingCommand standardInitiateHearingWithDefaultApplicationTemplate(final UUID offenceId) {
            final List<CourtApplication> courtApplications = Collections.singletonList((new HearingFactory()).courtApplication()
                    .withCourtApplicationCases(Collections.singletonList(CourtApplicationCase.courtApplicationCase()
                            .withProsecutionCaseId(randomUUID())
                            .withIsSJP(false)
                            .withCaseStatus("ACTIVE")
                            .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                    .withCaseURN("caseURN")
                                    .withProsecutionAuthorityId(randomUUID())
                                    .withProsecutionAuthorityCode("ABC")
                                    .build())
                            .withOffences(Collections.singletonList(uk.gov.justice.core.courts.Offence.offence()
                                    .withOffenceDefinitionId(randomUUID())
                                    .withOffenceCode("ABC")
                                    .withOffenceTitle("ABC")
                                    .withWording("ABC")
                                    .withStartDate(LocalDate.now())
                                    .withId(offenceId).build()))
                            .build()))
                    .build());
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN))
                            .withCourtApplications(courtApplications)
                            .build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithJurisdictionMagistrate() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(MAGISTRATES)
                    ).build());
        }

        public static InitiateHearingCommand welshInitiateHearingTemplate() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(WELSH)
                            .setJurisdictionType(CROWN)
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplateForIndicatedPlea(
                final IndicatedPleaValue indicatedPleaValue,
                final boolean isAllocationDecision) {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setIndicatedPleaValue(indicatedPleaValue)
                            .setAllocationDecision(isAllocationDecision)
                    ).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateForMagistrates() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(MAGISTRATES)
                    ).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateWithParam(final UUID courtAndRoomId, final String courtRoomName, final LocalDate localDate) throws NoSuchAlgorithmException {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearingWithParam(defaultArguments()
                                    .setDefendantType(PERSON)
                                    .setHearingLanguage(ENGLISH)
                                    .setJurisdictionType(CROWN)
                                    .setMinimumAssociatedPerson(true)
                                    .setMinimumDefenceOrganisation(true)
                            , courtAndRoomId, courtRoomName, localDate).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateWithParam(final UUID courtId, final UUID courtRoomId, final String courtRoomName, final LocalDate localDate, final UUID defenceCounselId, final UUID caseId, final Optional<UUID> hearingTypeId) throws NoSuchAlgorithmException {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearingWithParam(defaultArguments()
                                    .setDefendantType(PERSON)
                                    .setHearingLanguage(ENGLISH)
                                    .setJurisdictionType(CROWN)
                                    .setMinimumAssociatedPerson(true)
                                    .setMinimumDefenceOrganisation(true)
                            , courtId, courtRoomId, courtRoomName, localDate, defenceCounselId, caseId, hearingTypeId).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateForDefendantTypeOrganisation() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(ORGANISATION)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setMinimumAssociatedPerson(true)
                            .setMinimumDefenceOrganisation(true)
                    ).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateForCrownCourtOffenceCountNull() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(ORGANISATION)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setMinimumAssociatedPerson(true)
                            .setMinimumDefenceOrganisation(true)
                            .setOffenceWithNullCount()
                    ).build());
        }

        public static InitiateHearingCommand customStructureInitiateHearingTemplate(final Map<UUID, Map<UUID, List<UUID>>> structure) {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setStructure(structure)
                    ).build());
        }

        public static InitiateHearingCommand withMultipleHearingDays(List<HearingDay> hearingDays) {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setMinimumAssociatedPerson(true)
                            .setMinimumDefenceOrganisation(true)
                    )
                            .withHearingDays(hearingDays)
                            .withCourtCentre(CourtCentre.courtCentre()
                                    .withId(hearingDays.get(0).getCourtCentreId())
                                    .withName(STRING.next())
                                    .withRoomId(hearingDays.get(0).getCourtRoomId()).build())
                            .build());
        }
    }

    public static class UpdatePleaCommandTemplates {
        private UpdatePleaCommandTemplates() {
        }


        public static UpdatePleaCommand updatePleaTemplate(final UUID originatingHearingId,
                                                           final UUID offenceId,
                                                           final UUID defendantId,
                                                           final UUID prosecutionCaseId,
                                                           final IndicatedPleaValue indicatedPleaValue,
                                                           final String pleaValue,
                                                           final boolean isAllocationDecision,
                                                           final UUID courtApplicationId) {


            final PleaModel.Builder pleaModel = pleaModel().withDefendantId(defendantId)
                    .withOffenceId(offenceId)
                    .withApplicationId(courtApplicationId)
                    .withProsecutionCaseId(prosecutionCaseId);


            if (indicatedPleaValue != null) {
                pleaModel.withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, indicatedPleaValue).build());
            }

            if (isAllocationDecision) {
                pleaModel.withAllocationDecision(CoreTestTemplates.allocationDecision(offenceId).build());
            }

            if (pleaValue != null) {
                pleaModel.withPlea(CoreTestTemplates.plea(offenceId, PAST_LOCAL_DATE.next(), pleaValue, courtApplicationId).build());
            }

            return updatePleaCommand().setHearingId(originatingHearingId).setPleas(
                    asList(pleaModel.build()));

        }
    }

    public static class UpdateVerdictCommandTemplates {
        private UpdateVerdictCommandTemplates() {
        }

        public static HearingUpdateVerdictCommand updateVerdictTemplate(final UUID hearingId, final UUID offenceId, final VerdictCategoryType verdictCategoryType) {
            return updateVerdictTemplate(hearingId, offenceId, verdictCategoryType, randomUUID(), STRING.next(), null);
        }

        public static HearingUpdateVerdictCommand updateVerdictTemplate(final UUID hearingId, final UUID offenceId, final VerdictCategoryType verdictCategoryType, final UUID applicationId) {
            return updateVerdictTemplate(hearingId, offenceId, verdictCategoryType, randomUUID(), STRING.next(), applicationId);
        }

        public static HearingUpdateVerdictCommand updateVerdictTemplate(final UUID hearingId, final UUID offenceId, final VerdictCategoryType verdictCategoryType, final UUID verdictTypeId, final String verdictCode) {
            return updateVerdictTemplate(hearingId, offenceId,verdictCategoryType, verdictTypeId, verdictCode, null);
        }

        public static HearingUpdateVerdictCommand updateVerdictTemplate(final UUID hearingId, final UUID offenceId, final VerdictCategoryType verdictCategoryType, final UUID verdictTypeId, final String verdictCode, final UUID applicationId) {

            final boolean unanimous = BOOLEAN.next();
            final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

            return HearingUpdateVerdictCommand.hearingUpdateVerdictCommand()
                    .withHearingId(hearingId)
                    .withVerdicts(singletonList(Verdict.verdict()
                            .withVerdictType(VerdictType.verdictType()
                                    .withId(verdictTypeId)
                                    .withCategory(STRING.next())
                                    .withCategoryType(verdictCategoryType.name())
                                    .withDescription(STRING.next())
                                    .withSequence(INTEGER.next())
                                    .withVerdictCode(verdictCode)
                                    .build()
                            )
                            .withOffenceId(offenceId)
                            .withApplicationId(applicationId)
                            .withVerdictDate(PAST_LOCAL_DATE.next())
                            .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                    .withOffenceDefinitionId(randomUUID())
                                    .withOffenceCode(STRING.next())
                                    .withOffenceTitle(STRING.next())
                                    .withOffenceTitleWelsh(STRING.next())
                                    .withOffenceLegislation(STRING.next())
                                    .withOffenceLegislationWelsh(STRING.next())
                                    .build()
                            )
                            .withJurors(Jurors.jurors()
                                    .withNumberOfJurors(integer(9, 12).next())
                                    .withNumberOfSplitJurors(numberOfSplitJurors)
                                    .withUnanimous(unanimous)
                                    .build()
                            )
                            .withOriginatingHearingId(hearingId)
                            .build()
                    ));
        }
    }

    public static class SaveDraftResultsCommandTemplates {
        private SaveDraftResultsCommandTemplates() {
        }

        public static SaveDraftResultCommand standardSaveDraftTemplate(final UUID hearingId, final UUID defendantId, final UUID offenceId, final UUID resultLineId) {
            return SaveDraftResultCommand.saveDraftResultCommand()
                    .setTarget(CoreTestTemplates.target(hearingId, defendantId, offenceId, resultLineId).build());
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplate(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final LocalDate hearingDay) {
            return saveDraftResultCommandTemplate(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE, hearingDay);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplication(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate) {
            return saveDraftResultCommandTemplateWithApplication(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplication(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final LocalDate hearingDay) {
            return saveDraftResultCommandTemplateWithApplication(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE, hearingDay);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplicationAndOffence(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate) {
            return saveDraftResultCommandTemplateWithApplicationAndOffence(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplicationAndOffence(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final LocalDate hearingDay) {
            return saveDraftResultCommandTemplateWithApplicationAndOffence(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE, hearingDay);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateForDeletedResult(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate) {
            return saveDraftResultCommandTemplateForDeletedResult(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID());
        }

        public static SaveMultipleDaysResultsCommand saveMultipleDraftResultsCommandTemplate(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final LocalDate hearingDay) {
            return saveMultipleDaysDraftResultCommandTemplate(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE, hearingDay);
        }

        public static SaveMultipleDaysResultsCommand saveMultipleDraftResultsCommandTemplateWithInvalidTarget(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final LocalDate hearingDay) {
            return saveMultipleDaysDraftResultCommandTemplateInvalidTarget(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID(), Boolean.FALSE, hearingDay);
        }

        public static List<SaveDraftResultCommand> saveDraftResultCommandForMultipleOffences(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final UUID resultDefId) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final uk.gov.justice.core.courts.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence0 = defendant0.getOffences().get(0);

            final Target target0 = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence0.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(randomUUID(), resultDefId, orderedDate).build()))
                    .build();

            final List<SaveDraftResultCommand> saveDraftResultCommandList = new ArrayList<>();
            saveDraftResultCommandList.add(new SaveDraftResultCommand(target0, hearing.getId()));
            final Offence offence1 = defendant0.getOffences().get(1);
            final Target target1 = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence1.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(randomUUID(), randomUUID(), orderedDate).build()))
                    .build();

            saveDraftResultCommandList.add(new SaveDraftResultCommand(target1, hearing.getId()));

            return saveDraftResultCommandList;

        }

        public static ResultLine.Builder standardResultLineTemplate(final UUID resultLineId, final UUID resultDefinitionId, final LocalDate orderedDate) {
            return ResultLine.resultLine()
                    .withResultLineId(resultLineId)
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(UUID.randomUUID())
                                    .withLastName(BOWIE)
                                    .withFirstName(DAVID)
                                    .build()
                    )
                    .withIsComplete(true)
                    .withIsModified(true)
                    .withIsDeleted(false)
                    .withLevel(uk.gov.justice.core.courts.Level.OFFENCE)
                    .withOrderedDate(orderedDate)
                    .withResultLineId(UUID.randomUUID())
                    .withResultLabel(IMPRISONMENT)
                    .withSharedDate(now())
                    .withResultDefinitionId(resultDefinitionId)
                    .withPrompts(
                            asList(
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withFixedListCode(FIXEDLISTCODE_0)
                                            .withId(UUID.randomUUID())
                                            .withLabel(IMPRISONMENT_TERM)
                                            .withValue(SIX_YEARS)
                                            .withWelshLabel(WELSH_LABEL_SPACE + IMPRISONMENT_TERM)
                                            .withWelshValue(WELSH_VALUE)
                                            .withPromptRef(IMPRISONMENT)
                                            .build()
                            )
                    );

        }
        public static ResultLine.Builder ctlResultLineTemplate(final UUID resultLineId, final UUID resultDefinitionId, final LocalDate orderedDate) {
            return ResultLine.resultLine()
                    .withResultLineId(resultLineId)
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(UUID.randomUUID())
                                    .withLastName(BOWIE)
                                    .withFirstName(DAVID)
                                    .build()
                    )
                    .withIsComplete(true)
                    .withIsModified(true)
                    .withIsDeleted(false)
                    .withLevel(uk.gov.justice.core.courts.Level.OFFENCE)
                    .withOrderedDate(orderedDate)
                    .withResultLineId(UUID.randomUUID())
                    .withResultLabel(IMPRISONMENT)
                    .withSharedDate(now())
                    .withResultDefinitionId(resultDefinitionId)
                    .withPrompts(
                            asList(
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withFixedListCode(FIXEDLISTCODE_0)
                                            .withId(UUID.randomUUID())
                                            .withLabel(IMPRISONMENT_TERM)
                                            .withValue(SIX_YEARS)
                                            .withWelshLabel(WELSH_LABEL_SPACE + IMPRISONMENT_TERM)
                                            .withWelshValue(WELSH_VALUE)
                                            .withPromptRef(IMPRISONMENT)
                                            .build()
                            )
                    );

        }

        public static InitiateHearingCommand standardInitiateHearingTemplateWithDefendantJudicialResults() {
            return initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                                    .setDefendantType(PERSON)
                                    .setHearingLanguage(ENGLISH)
                                    .setJurisdictionType(CROWN),
                            true
                    ).build());
        }

        public static ResultLine.Builder standardResultLineTemplateForDeletedResult(final UUID resultLineId, final UUID resultDefinitionId, final LocalDate orderedDate) {
            return ResultLine.resultLine()
                    .withResultLineId(resultLineId)
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(UUID.randomUUID())
                                    .withLastName(BOWIE)
                                    .withFirstName(DAVID)
                                    .build()
                    )
                    .withIsComplete(true)
                    .withIsModified(true)
                    .withIsDeleted(true)
                    .withLevel(uk.gov.justice.core.courts.Level.OFFENCE)
                    .withOrderedDate(orderedDate)
                    .withResultLineId(UUID.randomUUID())
                    .withResultLabel(IMPRISONMENT)
                    .withSharedDate(now())
                    .withResultDefinitionId(resultDefinitionId)
                    .withPrompts(
                            asList(
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withFixedListCode(FIXEDLISTCODE_0)
                                            .withId(UUID.randomUUID())
                                            .withLabel(IMPRISONMENT_TERM)
                                            .withValue(SIX_YEARS)
                                            .withWelshValue(WELSH_VALUE)
                                            .withWelshLabel(WELSH_LABEL_SPACE + IMPRISONMENT_TERM)
                                            .build()
                            )
                    );

        }

        public static ResultLine.Builder standardAmendedResultLineTemplate(final UUID resultLineId, final UUID resultDefinitionId, final LocalDate orderedDate) {
            return ResultLine.resultLine()
                    .withResultLineId(resultLineId)
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(UUID.randomUUID())
                                    .withLastName(BOWIE)
                                    .withFirstName(DAVID)
                                    .build()
                    )
                    .withIsComplete(true)
                    .withIsModified(true)
                    .withIsDeleted(false)
                    .withLevel(uk.gov.justice.core.courts.Level.OFFENCE)
                    .withOrderedDate(orderedDate)
                    .withResultLineId(UUID.randomUUID())
                    .withResultLabel(IMPRISONMENT)
                    .withSharedDate(now())
                    .withResultDefinitionId(resultDefinitionId)
                    .withAmendmentReason(STRING.next())
                    .withAmendmentReasonId(randomUUID())
                    .withApprovedDate(PAST_LOCAL_DATE.next())
                    .withFourEyesApproval(DelegatedPowers.delegatedPowers()
                            .withUserId(UUID.randomUUID())
                            .withLastName(STRING.next())
                            .withFirstName(STRING.next())
                            .build())
                    .withAmendmentDate(now())
                    .withPrompts(
                            asList(
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withFixedListCode(FIXEDLISTCODE_0)
                                            .withId(UUID.randomUUID())
                                            .withLabel(IMPRISONMENT_TERM)
                                            .withValue(SIX_YEARS)
                                            .withWelshLabel(WELSH_LABEL_SPACE + IMPRISONMENT_TERM)
                                            .withWelshValue(WELSH_VALUE)
                                            .build()
                            )
                    );

        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplate(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed, final LocalDate hearingDay) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final uk.gov.justice.core.courts.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence0 = defendant0.getOffences().get(0);
            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence0.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withHearingDay(hearingDay)
                    .build();
            return new SaveDraftResultCommand(target, null);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplication(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final UUID offenceId ;
            if(hearing.getCourtApplications().get(0).getCourtApplicationCases() != null){
                offenceId = hearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId();
            }else if(hearing.getCourtApplications().get(0).getCourtOrder() != null){
                offenceId = hearing.getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId();
            }else{
                offenceId = null;
            }

            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withTargetId(UUID.randomUUID())
                    .withApplicationId(hearing.getCourtApplications().get(0).getId())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withOffenceId(offenceId)
                    .build();
            return new SaveDraftResultCommand(target, null);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplication(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed, final LocalDate hearingDay) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final UUID offenceId ;
            if(hearing.getCourtApplications().get(0).getCourtApplicationCases() != null){
                offenceId = hearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId();
            }else if(hearing.getCourtApplications().get(0).getCourtOrder() != null){
                offenceId = hearing.getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId();
            }else{
                offenceId = null;
            }

            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withTargetId(UUID.randomUUID())
                    .withApplicationId(hearing.getCourtApplications().get(0).getId())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withOffenceId(offenceId)
                    .withHearingDay(hearingDay)
                    .build();
            return new SaveDraftResultCommand(target, null);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplicationAndOffence(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withTargetId(UUID.randomUUID())
                    .withApplicationId(hearing.getCourtApplications().get(0).getId())
                    .withOffenceId(hearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .build();
            return new SaveDraftResultCommand(target, null);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateWithApplicationAndOffence(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed, final LocalDate hearingDay) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withTargetId(UUID.randomUUID())
                    .withApplicationId(hearing.getCourtApplications().get(0).getId())
                    .withOffenceId(hearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withHearingDay(hearingDay)
                    .build();
            return new SaveDraftResultCommand(target, null);
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplateForDeletedResult(final InitiateHearingCommand initiateHearingCommand,
                                                                                            final LocalDate orderedDate, final UUID resultLineId,
                                                                                            final UUID resultDefinitionId) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final uk.gov.justice.core.courts.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence0 = defendant0.getOffences().get(0);
            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence0.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplateForDeletedResult(resultLineId, resultDefinitionId, orderedDate).build()))
                    .build();
            return new SaveDraftResultCommand(target, null);
        }

        public static SaveMultipleDaysResultsCommand saveMultipleDaysDraftResultCommandTemplateInvalidTarget(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                                                                                                             final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed, final LocalDate hearingDay) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final uk.gov.justice.core.courts.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence1 = defendant0.getOffences().get(0);
            final Target target1 = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence1.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withHearingDay(hearingDay)
                    .build();

            return new SaveMultipleDaysResultsCommand(hearing.getId(), Arrays.asList(target1), hearingDay);

        }

        public static SaveMultipleDaysResultsCommand saveMultipleDaysDraftResultCommandTemplate(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate,
                final UUID resultLineId, final UUID resultDefinitionId, final Boolean shadowListed, final LocalDate hearingDay) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final uk.gov.justice.core.courts.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence1 = defendant0.getOffences().get(0);
            final Target target1 = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence1.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withHearingDay(hearingDay)
                    .build();

            final Offence offence2 = defendant0.getOffences().get(1);
            final Target target2 = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult(DRAFT_RESULTS_CONTENT)
                    .withOffenceId(offence2.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .withShadowListed(shadowListed)
                    .withHearingDay(hearingDay)
                    .build();
            return new SaveMultipleDaysResultsCommand(hearing.getId(), Arrays.asList(target1, target2), hearingDay);
        }
    }


    public static class ShareResultsCommandTemplates {
        private ShareResultsCommandTemplates() {
        }

        public static ShareResultsCommand basicShareResultsCommandTemplate() {

            return ShareResultsCommand.shareResultsCommand()
                    .setCourtClerk(DelegatedPowers.delegatedPowers()
                            .withUserId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build());

        }

        public static ShareDaysResultsCommand basicShareResultsCommandV2Template() {

            return ShareDaysResultsCommand.shareResultsCommand()
                    .setCourtClerk(DelegatedPowers.delegatedPowers()
                            .withUserId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build());

        }

        public static ShareResultsCommand standardShareResultsCommandTemplate(final UUID hearingId) {
            return basicShareResultsCommandTemplate().setHearingId(hearingId);
        }

        public static ShareDaysResultsCommand standardShareResultsPerDaysCommandTemplate(final UUID hearingId) {
            return basicShareResultsCommandV2Template().setHearingId(hearingId);
        }
    }

    public static class CompletedResultLineStatusTemplates {

        private CompletedResultLineStatusTemplates() {
        }

        public static CompletedResultLineStatus completedResultLineStatus(final UUID resultLineId) {
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));
            return CompletedResultLineStatus.builder()
                    .withId(resultLineId)
                    .withLastSharedDateTime(startDateTime)
                    .withCourtClerk(DelegatedPowers.delegatedPowers()
                            .withUserId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build())
                    .build();
        }

    }

    public static class CaseDefendantOffencesChangedCommandTemplates {

        private CaseDefendantOffencesChangedCommandTemplates() {
        }

        public static TemplateArguments updateOffencesForDefendantArguments(final UUID prosecutionCaseId, final UUID defendantId) {
            return new TemplateArguments(prosecutionCaseId, defendantId);
        }

        public static UpdateOffencesForDefendantCommand addOffencesForDefendantTemplate(final TemplateArguments args) {
            return UpdateOffencesForDefendantCommand.updateOffencesForDefendantCommand()
                    .setAddedOffences(asList(defendantCaseOffences(args.getProsecutionCaseId(), args.getDefendantId(), args.getOffencesToAdd(), args.getOffenceDateCode())))
                    .setModifiedDate(PAST_LOCAL_DATE.next());
        }

        public static UpdateOffencesForDefendantCommand updateOffencesForDefendantTemplate(final TemplateArguments args) {
            return UpdateOffencesForDefendantCommand.updateOffencesForDefendantCommand()
                    .setUpdatedOffences(asList(defendantCaseOffences(args.getProsecutionCaseId(), args.getDefendantId(), args.getOffencesToUpdate(), args.getOffenceDateCode())))
                    .setModifiedDate(PAST_LOCAL_DATE.next());
        }

        public static UpdateOffencesForDefendantCommand deleteOffencesForDefendantTemplate(final TemplateArguments args) {
            return UpdateOffencesForDefendantCommand.updateOffencesForDefendantCommand()
                    .setDeletedOffences(asList(deletedOffence(args.getProsecutionCaseId(), args.getDefendantId(), args.getOffenceToDelete())))
                    .setModifiedDate(PAST_LOCAL_DATE.next());
        }

        public static DefendantCaseOffences defendantCaseOffences(final UUID prosecutionCaseId, final UUID defendantId, final List<UUID> offenceIds, final Integer offenceDateCode) {
            return DefendantCaseOffences.defendantCaseOffences()
                    .withProsecutionCaseId(prosecutionCaseId)
                    .withDefendantId(defendantId)
                    .withOffences(offenceIds.stream()
                            .map(offenceId -> Offence.offence()
                                    .withArrestDate(PAST_LOCAL_DATE.next())
                                    .withChargeDate(PAST_LOCAL_DATE.next())
                                    .withCount(INTEGER.next())
                                    .withEndDate(PAST_LOCAL_DATE.next())
                                    .withId(offenceId)
                                    .withIndicatedPlea(uk.gov.justice.core.courts.IndicatedPlea.indicatedPlea()
                                            .withIndicatedPleaDate(PAST_LOCAL_DATE.next())
                                            .withIndicatedPleaValue(RandomGenerator.values(IndicatedPleaValue.values()).next())
                                            .withOffenceId(offenceId)
                                            .withSource(RandomGenerator.values(Source.values()).next())
                                            .build())
                                    .withAllocationDecision(uk.gov.justice.core.courts.AllocationDecision.allocationDecision()
                                            .withOriginatingHearingId(randomUUID())
                                            .withOffenceId(offenceId)
                                            .withMotReasonId(randomUUID())
                                            .withMotReasonDescription(STRING.next())
                                            .withMotReasonCode(STRING.next())
                                            .withAllocationDecisionDate(FUTURE_LOCAL_DATE.next())
                                            .withSequenceNumber(INTEGER.next())
                                            .build())
                                    .withLaaApplnReference(LaaReference.laaReference()
                                            .withStatusDate(now())
                                            .withApplicationReference(STRING.next())
                                            .withStatusId(UUID.randomUUID())
                                            .withStatusCode(STRING.next())
                                            .withStatusDescription(STRING.next())
                                            .build())
                                    .withLaaApplnReference(LaaReference.laaReference()
                                            .withStatusDate(now())
                                            .withApplicationReference(STRING.next())
                                            .withStatusId(UUID.randomUUID())
                                            .withStatusCode(STRING.next())
                                            .withStatusDescription(STRING.next())
                                            .build())
                                    .withModeOfTrial(STRING.next())
                                    .withOffenceCode(STRING.next())
                                    .withOffenceDefinitionId(randomUUID())
                                    .withOffenceFacts(uk.gov.justice.core.courts.OffenceFacts.offenceFacts()
                                            .withAlcoholReadingAmount(INTEGER.next())
                                            .withAlcoholReadingMethodCode(STRING.next())
                                            .withVehicleRegistration(STRING.next())
                                            .withVehicleMake(STRING.next())
                                            .build())
                                    .withOffenceLegislation(STRING.next())
                                    .withOffenceLegislationWelsh(STRING.next())
                                    .withOffenceTitle(STRING.next())
                                    .withOffenceTitleWelsh(STRING.next())
                                    .withOrderIndex(INTEGER.next())
                                    .withStartDate(PAST_LOCAL_DATE.next())
                                    .withWording(STRING.next())
                                    .withWordingWelsh(STRING.next())
                                    .withProceedingsConcluded(true)
                                    .withIsDiscontinued(true)
                                    .withIntroducedAfterInitialProceedings(true)
                                    .withOffenceDateCode(offenceDateCode)
                                    .withReportingRestrictions(asList(ReportingRestriction.reportingRestriction()
                                            .withJudicialResultId(randomUUID())
                                            .withLabel(STRING.next())
                                            .withId(randomUUID())
                                            .withOrderedDate(PAST_LOCAL_DATE.next())
                                            .build()))
                                    .build())
                            .collect(Collectors.toList())
                    );
        }

        public static DeletedOffences deletedOffence(final UUID caseId, final UUID defendantId, final List<UUID> offenceIds) {
            return DeletedOffences.deletedOffences()
                    .setProsecutionCaseId(caseId)
                    .setDefendantId(defendantId)
                    .setOffences(offenceIds);
        }

        public static class TemplateArguments {

            private UUID prosecutionCaseId;
            private UUID defendantId;
            private Integer offenceDateCode;
            private List<UUID> offencesToAdd = new ArrayList<>();
            private List<UUID> offencesToUpdate = new ArrayList<>();
            private List<UUID> offenceToDelete = new ArrayList<>();

            public TemplateArguments(final UUID prosecutionCaseId, final UUID defendantId) {
                this.prosecutionCaseId = prosecutionCaseId;
                this.defendantId = defendantId;
            }

            public UUID getProsecutionCaseId() {
                return prosecutionCaseId;
            }

            public TemplateArguments setProsecutionCaseId(final UUID caseId) {
                this.prosecutionCaseId = caseId;
                return this;
            }

            public UUID getDefendantId() {
                return defendantId;
            }

            public TemplateArguments setDefendantId(final UUID defendantId) {
                this.defendantId = defendantId;
                return this;
            }

            public List<UUID> getOffencesToAdd() {
                return unmodifiableList(offencesToAdd);
            }

            public TemplateArguments setOffencesToAdd(final List<UUID> offencesToAdd) {
                this.offencesToAdd = unmodifiableList(offencesToAdd);
                return this;
            }

            public List<UUID> getOffencesToUpdate() {
                return unmodifiableList(ofNullable(offencesToUpdate).orElseGet(ArrayList::new));
            }

            public TemplateArguments setOffencesToUpdate(final List<UUID> offencesToUpdate) {
                this.offencesToUpdate = unmodifiableList(ofNullable(offencesToUpdate).orElseGet(ArrayList::new));
                return this;
            }

            public List<UUID> getOffenceToDelete() {
                return unmodifiableList(ofNullable(offenceToDelete).orElseGet(ArrayList::new));
            }

            public TemplateArguments setOffenceToDelete(final List<UUID> offenceToDelete) {
                this.offenceToDelete = unmodifiableList(ofNullable(offenceToDelete).orElseGet(ArrayList::new));
                return this;
            }

            public Integer getOffenceDateCode() {
                return offenceDateCode;
            }

            public TemplateArguments setOffenceDateCode(final Integer offenceDateCode) {
                this.offenceDateCode = offenceDateCode;
                return this;
            }
        }
    }

    public static class CaseDefendantDetailsChangedCommandTemplates {

        private CaseDefendantDetailsChangedCommandTemplates() {

        }

        public static CaseDefendantDetails caseDefendantDetailsChangedCommandTemplate() {
            return CaseDefendantDetails.caseDefendantDetails()
                    .setDefendants(asList(defendantTemplate()));
        }
    }

    public static class AddDefenceCounselCommandTemplates {
        private AddDefenceCounselCommandTemplates() {
        }

        public static AddDefenceCounsel addDefenceCounselCommandTemplate(final UUID hearingId) {
            final DefenceCounsel defenceCounsel = new DefenceCounsel(
                    Arrays.asList(now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    randomUUID()
            );
            return new AddDefenceCounsel(defenceCounsel, hearingId);
        }

        public static AddDefenceCounsel addDefenceCounselCommandTemplateWithoutMiddleName(final UUID hearingId) {
            final DefenceCounsel defenceCounsel = new DefenceCounsel(
                    Arrays.asList(now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    STRING.next(),
                    STRING.next(),
                    randomUUID()
            );
            return new AddDefenceCounsel(defenceCounsel, hearingId);
        }

        public static AddDefenceCounsel addDefenceCounselCommandTemplate(final UUID hearingId, final DefenceCounsel defenceCounsel) {
            return new AddDefenceCounsel(defenceCounsel, hearingId);
        }
    }

    public static class UpdateDefenceCounselCommandTemplates {
        private UpdateDefenceCounselCommandTemplates() {
        }

        public static UpdateDefenceCounsel updateDefenceCounselCommandTemplate(final UUID hearingId) {
            final DefenceCounsel defenceCounsel = new DefenceCounsel(
                    Arrays.asList(now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    randomUUID()
            );
            return new UpdateDefenceCounsel(defenceCounsel, hearingId);
        }

        public static UpdateDefenceCounsel updateDefenceCounselCommandTemplate(final UUID hearingId, final DefenceCounsel defenceCounsel) {
            return new UpdateDefenceCounsel(defenceCounsel, hearingId);
        }
    }

    public static class AddProsecutionCounselCommandTemplates {
        private AddProsecutionCounselCommandTemplates() {
        }

        public static AddProsecutionCounsel addProsecutionCounselCommandTemplate(final UUID hearingId) {
            final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next(),
                    randomUUID()
            );
            return new AddProsecutionCounsel(hearingId, prosecutionCounsel);
        }

        public static AddProsecutionCounsel addProsecutionCounselCommandTemplateWithoutMiddleName(final UUID hearingId) {
            final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next(),
                    randomUUID()
            );
            return new AddProsecutionCounsel(hearingId, prosecutionCounsel);
        }

        public static AddProsecutionCounsel addProsecutionCounselCommandTemplate(final UUID hearingId, final ProsecutionCounsel prosecutionCounsel) {
            return new AddProsecutionCounsel(hearingId, prosecutionCounsel);
        }
    }

    public static class AddApplicantCounselCommandTemplates {
        private AddApplicantCounselCommandTemplates() {
        }

        public static AddApplicantCounsel addApplicantCounselCommandTemplate(final UUID hearingId) {
            final ApplicantCounsel applicantCounsel = new ApplicantCounsel(
                    Arrays.asList(UUID.randomUUID()),
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next()
            );
            return new AddApplicantCounsel(applicantCounsel, hearingId);
        }

        public static AddApplicantCounsel addApplicantCounselCommandTemplateWithoutMiddleName(final UUID hearingId) {
            final ApplicantCounsel applicantCounsel = new ApplicantCounsel(
                    Arrays.asList(UUID.randomUUID()),
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    STRING.next(),
                    STRING.next()
            );
            return new AddApplicantCounsel(applicantCounsel, hearingId);
        }

        public static AddApplicantCounsel addApplicantCounselCommandTemplate(final UUID hearingId, final ApplicantCounsel applicantCounsel) {
            return new AddApplicantCounsel(applicantCounsel, hearingId);
        }
    }

    public static class UpdateProsecutionCounselCommandTemplates {
        private UpdateProsecutionCounselCommandTemplates() {
        }

        public static UpdateProsecutionCounsel updateProsecutionCounselCommandTemplate(final UUID hearingId) {
            final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next(),
                    randomUUID()
            );
            return new UpdateProsecutionCounsel(hearingId, prosecutionCounsel);
        }

        public static UpdateProsecutionCounsel updateProsecutionCounselCommandTemplate(final UUID hearingId, final ProsecutionCounsel prosecutionCounsel) {
            return new UpdateProsecutionCounsel(hearingId, prosecutionCounsel);
        }
    }

    public static class UpdateApplicantCounselCommandTemplates {
        private UpdateApplicantCounselCommandTemplates() {
        }

        public static UpdateApplicantCounsel updateApplicantCounselCommandTemplate(final UUID hearingId) {
            final ApplicantCounsel applicantCounsel = new ApplicantCounsel(
                    Arrays.asList(UUID.randomUUID()),
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    STRING.next(),
                    STRING.next()
            );
            return new UpdateApplicantCounsel(applicantCounsel, hearingId);
        }

        public static UpdateApplicantCounsel updateApplicantCounselCommandTemplate(final UUID hearingId, final ApplicantCounsel applicantCounsel) {
            return new UpdateApplicantCounsel(applicantCounsel, hearingId);
        }
    }


    public static class NowDefinitionTemplates {
        private NowDefinitionTemplates() {
        }

        public static NowDefinition standardNowDefinition() {
            return NowDefinition.now()
                    .setId(UUID.randomUUID())
                    .setJurisdiction(STRING.next())
                    .setName(STRING.next())
                    .setRank(INTEGER.next())
                    .setJurisdiction(STRING.next())
                    .setTemplateName(STRING.next())
                    .setText(STRING.next())
                    .setWelshText(STRING.next())
                    .setWelshName(STRING.next())
                    .setBilingualTemplateName(STRING.next())
                    .setRemotePrintingRequired(BOOLEAN.next())
                    .setUrgentTimeLimitInMinutes(INTEGER.next())
                    .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                            .setId(randomUUID())
                            .setMandatory(true)
                            .setPrimary(true)
                            .setText(STRING.next())
                            .setWelshText(STRING.next())
                            .setSequence(1)
                    ));
        }
    }

    public static NowDefinition multiPrimaryNowDefinition() {
        return NowDefinition.now()
                .setId(UUID.randomUUID())
                .setJurisdiction(STRING.next())
                .setName(STRING.next())
                .setRank(INTEGER.next())
                .setJurisdiction(STRING.next())
                .setTemplateName(STRING.next())
                .setText(STRING.next())
                .setWelshText(STRING.next())
                .setWelshName(STRING.next())
                .setBilingualTemplateName(STRING.next())
                .setRemotePrintingRequired(BOOLEAN.next())
                .setUrgentTimeLimitInMinutes(INTEGER.next())
                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(true)
                                .setText(STRING.next())
                                .setWelshText(STRING.next())
                                .setSequence(1),
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(true)
                                .setText(STRING.next())
                                .setWelshText(STRING.next())
                                .setSequence(2),
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(true)
                                .setText(STRING.next())
                                .setWelshText(STRING.next())
                                .setSequence(3)
                ));
    }


    public static class VariantDirectoryTemplates {
        private VariantDirectoryTemplates() {
        }

        public static Variant standardVariantTemplate(final UUID nowTypeId, final UUID hearingId, final UUID defendantId) {
            return Variant.variant()
                    .setKey(VariantKey.variantKey()
                            .setNowsTypeId(nowTypeId)
                            .setUsergroups(asList(STRING.next(), STRING.next()))
                            .setDefendantId(defendantId)
                            .setHearingId(hearingId)
                    )
                    .setValue(VariantValue.variantValue()
                            .setMaterialId(randomUUID())
                            .setStatus(VariantStatus.BUILDING)
                            .setResultLines(asList(ResultLineReference.resultLineReference()
                                    .setResultLineId(randomUUID())
                                    .setLastSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                            ))
                    ).setReferenceDate(now());
        }
    }

    public static class UploadSubscriptionsCommandTemplates {

        private UploadSubscriptionsCommandTemplates() {
        }

        public static UploadSubscriptionsCommand buildUploadSubscriptionsCommand() {

            final UploadSubscriptionsCommand uploadSubscriptionsCommand = new UploadSubscriptionsCommand();

            uploadSubscriptionsCommand.setSubscriptions(
                    asList(
                            buildUploadSubscriptionCommand(),
                            buildUploadSubscriptionCommand()));

            return uploadSubscriptionsCommand;
        }

        private static UploadSubscription buildUploadSubscriptionCommand() {

            final Map<String, String> properties = new HashMap<>();
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent("templateId", UUID.randomUUID().toString());
            properties.putIfAbsent("fromAddress", "noreply@test.com");

            final List<UUID> courtCentreIds = asList(randomUUID(), randomUUID());

            final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

            final UploadSubscription command = new UploadSubscription();
            command.setChannel("email");
            command.setChannelProperties(properties);
            command.setDestination(STRING.next());
            command.setUserGroups(asList(STRING.next(), STRING.next()));
            command.setCourtCentreIds(courtCentreIds);
            command.setNowTypeIds(nowTypeIds);

            return command;
        }
    }

    public static class HearingEventDefinitionsTemplates {

        private HearingEventDefinitionsTemplates() {
        }

        public static CreateHearingEventDefinitionsCommand buildCreateHearingEventDefinitionsCommand() {
            return CreateHearingEventDefinitionsCommand.builder()
                    .withId(randomUUID())
                    .withEventDefinitions(asList(
                            HearingEventDefinition.builder()
                                    .withId(randomUUID())
                                    .withActionLabel(STRING.next())
                                    .withRecordedLabel(STRING.next())
                                    .withActionSequence(INTEGER.next())
                                    .withGroupSequence(INTEGER.next())
                                    .withAlterable(BOOLEAN.next())
                                    .build(),
                            HearingEventDefinition.builder()
                                    .withId(randomUUID())
                                    .withGroupLabel(STRING.next())
                                    .withActionLabel(STRING.next())
                                    .withRecordedLabel(STRING.next())
                                    .withActionSequence(INTEGER.next())
                                    .withGroupSequence(INTEGER.next())
                                    .withCaseAttribute(STRING.next())
                                    .withAlterable(BOOLEAN.next())
                                    .build(),
                            HearingEventDefinition.builder().
                                    withId(randomUUID())
                                    .withActionLabel(STRING.next())
                                    .withRecordedLabel(STRING.next())
                                    .withActionSequence(INTEGER.next())
                                    .withGroupSequence(INTEGER.next())
                                    .withAlterable(BOOLEAN.next())
                                    .build()))
                    .build();
        }
    }

    public static class UpdateDefendantAttendanceCommandTemplates {
        private UpdateDefendantAttendanceCommandTemplates() {
        }

        public static UpdateDefendantAttendanceCommand updateDefendantAttendanceTemplate(final UUID hearingId, final UUID defendantId, final LocalDate attendanceDate, final AttendanceType attendanceType) {
            return UpdateDefendantAttendanceCommand.updateDefendantAttendanceCommand()
                    .setHearingId(hearingId)
                    .setDefendantId(defendantId)
                    .setAttendanceDay(AttendanceDay.attendanceDay()
                            .withDay(attendanceDate)
                            .withAttendanceType(attendanceType)
                            .build());
        }
    }

    public static class AddRespondentCounselCommandTemplates {
        private AddRespondentCounselCommandTemplates() {
        }

        public static AddRespondentCounsel addRespondentCounselCommandTemplate(final UUID hearingId) {
            final RespondentCounsel respondentCounsel = new RespondentCounsel(
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next()
            );
            return new AddRespondentCounsel(hearingId, respondentCounsel);
        }

        public static AddRespondentCounsel addRespondentCounselCommandTemplateWithoutMiddleName(final UUID hearingId) {
            final RespondentCounsel respondentCounsel = new RespondentCounsel(
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next()
            );
            return new AddRespondentCounsel(hearingId, respondentCounsel);
        }

        public static AddRespondentCounsel addRespondentCounselCommandTemplate(final UUID hearingId, final RespondentCounsel respondentCounsel) {
            return new AddRespondentCounsel(hearingId, respondentCounsel);
        }
    }

    public static class UpdateRespondentCounselCommandTemplates {
        private UpdateRespondentCounselCommandTemplates() {
        }

        public static UpdateRespondentCounsel updateRespondentCounselCommandTemplate(final UUID hearingId) {
            final RespondentCounsel respondentCounsel = new RespondentCounsel(
                    Arrays.asList(now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next()
            );
            return new UpdateRespondentCounsel(hearingId, respondentCounsel);
        }

        public static UpdateRespondentCounsel updateRespondentCounselCommandTemplate(final UUID hearingId, final RespondentCounsel respondentCounsel) {
            return new UpdateRespondentCounsel(hearingId, respondentCounsel);
        }
    }

    public static class AddCompanyRepresentativeCommandTemplates {
        private AddCompanyRepresentativeCommandTemplates() {
        }

        public static AddCompanyRepresentative addCompanyRepresentativeCommandTemplate(final UUID hearingId) {
            final CompanyRepresentative companyRepresentative = new CompanyRepresentative(
                    Arrays.asList(now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    Position.DIRECTOR,
                    STRING.next()
            );
            return new AddCompanyRepresentative(companyRepresentative, hearingId);
        }

        public static AddCompanyRepresentative addCompanyRepresentativeCommandTemplate(final UUID hearingId, final CompanyRepresentative companyRepresentative) {
            return new AddCompanyRepresentative(companyRepresentative, hearingId);
        }
    }

    public static class UpdateCompanyRepresentativeCommandTemplates {
        private UpdateCompanyRepresentativeCommandTemplates() {
        }

        public static UpdateCompanyRepresentative updateCompanyRepresentativeCommandTemplate(final UUID hearingId) {
            final CompanyRepresentative companyRepresentative = new CompanyRepresentative(
                    Arrays.asList(now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    Position.DIRECTOR,
                    STRING.next()
            );
            return new UpdateCompanyRepresentative(companyRepresentative, hearingId);
        }

        public static UpdateCompanyRepresentative updateCompanyRepresentativeCommandTemplate(final UUID hearingId, final CompanyRepresentative companyRepresentative) {
            return new UpdateCompanyRepresentative(companyRepresentative, hearingId);
        }
    }

    public static List<CourtApplication> createCourtApplications() {
        final List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(UUID.randomUUID())
                //TODO need to revisit linked case scenario
              /*  .withCourtApplicationCases(Collections.singletonList(CourtApplicationCase.courtApplicationCase()
                        .withProsecutionCaseId(UUID.randomUUID())
                        .withProsecutionCaseReference("Case Reference")
                        .withIsSJP(false)
                        .build()))*/
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(UUID.randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .build())
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withId(UUID.randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .withMasterDefendant(uk.gov.justice.core.courts.MasterDefendant.masterDefendant()
                                .withMasterDefendantId(UUID.randomUUID())
                                .build())
                        .build())
                .withRespondents(Arrays.asList(CourtApplicationParty.courtApplicationParty()
                        .withId(UUID.randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .withProsecutingAuthority(ProsecutingAuthority.prosecutingAuthority()
                                .withProsecutionAuthorityId(UUID.randomUUID())

                                .build())

                        .build()))
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
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
                        .withOffenceActiveOrder(OffenceActiveOrder.NOT_APPLICABLE)
                        .build())
                .build());
        return courtApplications;
    }

    public static List<CourtApplicationCase> createCourtApplicationCases(){
        return Arrays.asList(CourtApplicationCase.courtApplicationCase()
                .withProsecutionCaseId(randomUUID())
                .withIsSJP(false)
                .withCaseStatus("ACTIVE")
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                        .withCaseURN("caseURN1")
                        .withProsecutionAuthorityId(randomUUID())
                        .withProsecutionAuthorityCode("ABC")
                        .build())
                .withOffences(Collections.singletonList(uk.gov.justice.core.courts.Offence.offence()
                        .withOffenceDefinitionId(randomUUID())
                        .withOffenceCode("ABC")
                        .withOffenceTitle("ABC")
                        .withWording("ABC")
                        .withStartDate(LocalDate.now())
                        .withId(randomUUID()).build()))
                .build(), CourtApplicationCase.courtApplicationCase()
                .withProsecutionCaseId(randomUUID())
                .withIsSJP(false)
                .withCaseStatus("ACTIVE")
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                        .withCaseURN("caseURN2")
                        .withProsecutionAuthorityId(randomUUID())
                        .withProsecutionAuthorityCode("ABC")
                        .build())
                .withOffences(Collections.singletonList(uk.gov.justice.core.courts.Offence.offence()
                        .withOffenceDefinitionId(randomUUID())
                        .withOffenceCode("ABC")
                        .withOffenceTitle("ABC")
                        .withWording("ABC")
                        .withStartDate(LocalDate.now())
                        .withId(randomUUID()).build()))
                .build());
    }
}
