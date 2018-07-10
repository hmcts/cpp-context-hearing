package uk.gov.moj.cpp.hearing.test;


import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.moj.cpp.external.domain.listing.StatementOfOffence;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.offence.AddedOffence;
import uk.gov.moj.cpp.hearing.command.offence.BaseDefendantOffence;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.command.offence.DefendantOffence;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffence;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UncompletedResultLine;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.CourtCentre;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.LesserOrAlternativeOffence;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Offences;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Person;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestTemplates {

    public enum PleaValueType {GUILTY, NOT_GUILTY}

    public enum VerdictCategoryType {GUILTY, NOT_GUILTY, NO_VERDICT}

    public static final String IMPRISONMENT_LABEL = "Imprisonment";
    public static final String IMPRISONMENT_DURATION_VALUE = "Imprisonment duration";
    public static final String WORMWOOD_SCRUBS_VALUE = "Wormwood Scrubs";

    private TestTemplates() {
    }

    public static class InitiateHearingCommandTemplates {
        private InitiateHearingCommandTemplates() {
        }

        public static InitiateHearingCommand basicInitiateHearingTemplate() {

            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            final ZonedDateTime secondDateTime = startDateTime.plusDays(2);

            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(Hearing.hearing()
                            .setId(randomUUID())
                            .setType(STRING.next())
                            .setCourtCentreId(randomUUID())
                            .setCourtCentreName(STRING.next())
                            .setCourtRoomId(randomUUID())
                            .setCourtRoomName(STRING.next())
                            .setJudge(
                                    Judge.judge()
                                            .setId(randomUUID())
                                            .setTitle(STRING.next())
                                            .setFirstName(STRING.next())
                                            .setLastName(STRING.next())
                            )
                            .setHearingDays(asList(startDateTime, secondDateTime))
                    );
        }

        public static InitiateHearingCommand minimalInitiateHearingTemplate() {

            final UUID caseId = randomUUID();
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            final ZonedDateTime secondDateTime = startDateTime.plusDays(2);

            return InitiateHearingCommand.initiateHearingCommand()
                    .setCases(asList(Case.legalCase()
                                    .setCaseId(caseId)
                                    .setUrn(STRING.next())
                            )
                    )
                    .setHearing(Hearing.hearing()
                            .setId(randomUUID())
                            .setType(STRING.next())
                            .setCourtCentreId(randomUUID())
                            .setCourtCentreName(STRING.next())
                            .setCourtRoomId(randomUUID())
                            .setCourtRoomName(STRING.next())
                            .setJudge(
                                    Judge.judge()
                                            .setId(randomUUID())
                                            .setTitle(STRING.next())
                                            .setFirstName(STRING.next())
                                            .setLastName(STRING.next())
                            )
                            .setHearingDays(asList(startDateTime, secondDateTime))
                            .setDefendants(asList(
                                    Defendant.defendant()
                                            .setId(randomUUID())
                                            .setPersonId(randomUUID())
                                            .setFirstName(STRING.next())
                                            .setLastName(STRING.next())
                                            .setDefendantCases(asList(
                                                    DefendantCase.defendantCase()
                                                            .setCaseId(caseId))
                                            )
                                            .setOffences(asList(offenceTemplate(caseId)
                                                            .setSection(null)
                                                            .setEndDate(null)
                                                            .setOrderIndex(null)
                                                            .setCount(null)
                                                            .setConvictionDate(null)
                                                            .setLegislation(null)
                                                            .setTitle(null)
                                                    )
                                            )
                                    )
                            )
                    );
        }

        public static InitiateHearingCommand standardInitiateHearingTemplate() {
            final UUID caseId = randomUUID();
            return InitiateHearingCommand.initiateHearingCommand()
                    .setCases(asList(caseTemplate(caseId)))
                    .setHearing(
                            hearingTemplate()
                                    .setDefendants(asList(defendantTemplate(caseId)))
                    );
        }

        public static Case caseTemplate(final UUID caseId) {
            return Case.legalCase()
                    .setCaseId(caseId)
                    .setUrn(STRING.next());
        }

        public static Hearing hearingTemplate() {
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            return Hearing.hearing()
                    .setId(randomUUID())
                    .setType(STRING.next())
                    .setCourtCentreId(randomUUID())
                    .setCourtCentreName(STRING.next())
                    .setCourtRoomId(randomUUID())
                    .setCourtRoomName(STRING.next())
                    .setJudge(
                            Judge.judge()
                                    .setId(randomUUID())
                                    .setTitle(STRING.next())
                                    .setFirstName(STRING.next())
                                    .setLastName(STRING.next())
                    )
                    .setHearingDays(asList(startDateTime));
        }

        public static Defendant defendantTemplate(final UUID caseId) {
            return defendantTemplate(caseId, randomUUID(), randomUUID());
        }

        public static Defendant defendantTemplate(final UUID caseId, final UUID defendantId, final UUID offenceId) {
            return Defendant.defendant()
                    .setId(defendantId)
                    .setPersonId(randomUUID())
                    .setFirstName(STRING.next())
                    .setLastName(STRING.next())
                    .setNationality(STRING.next())
                    .setGender(STRING.next())
                    .setAddress(addressTemplate())
                    .setDateOfBirth(PAST_LOCAL_DATE.next())
                    .setDefenceOrganisation(STRING.next())
                    .setInterpreter(Interpreter.interpreter()
                            .setNeeded(false)
                            .setLanguage(STRING.next())
                    )
                    .setDefendantCases(asList(
                            defendantCaseTemplate(caseId)
                    ))
                    .setOffences(asList(
                            offenceTemplate(caseId, offenceId)
                    ));
        }

        public static Address addressTemplate() {
            return Address.address()
                    .setAddress1(STRING.next())
                    .setAddress2(STRING.next())
                    .setAddress3(STRING.next())
                    .setAddress4(STRING.next())
                    .setPostCode(STRING.next());
        }

        public static DefendantCase defendantCaseTemplate(final UUID caseId) {
            return DefendantCase.defendantCase()
                    .setCaseId(caseId)
                    .setBailStatus(STRING.next())
                    .setCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next());
        }

        public static Offence offenceTemplate(final UUID caseId, final UUID offenceId) {
            return Offence.offence()
                    .setId(offenceId)
                    .setCaseId(caseId)
                    .setOffenceCode(STRING.next())
                    .setWording(STRING.next())
                    .setSection(STRING.next())
                    .setStartDate(PAST_LOCAL_DATE.next())
                    .setEndDate(PAST_LOCAL_DATE.next())
                    .setOrderIndex(INTEGER.next())
                    .setCount(INTEGER.next())
                    .setConvictionDate(PAST_LOCAL_DATE.next())
                    .setLegislation(STRING.next())
                    .setTitle(STRING.next());
        }

        public static Offence offenceTemplate(final UUID caseId) {
            return offenceTemplate(caseId, randomUUID());
        }

        public static InitiateHearingCommand minimalInitiateHearingTemplate(
                final UUID caseId, final UUID hearingId, final UUID... defendantIds) {

            return InitiateHearingCommand.initiateHearingCommand()
                    .setCases(asList(Case.legalCase()
                            .setCaseId(caseId)
                            .setUrn(STRING.next())
                    ))
                    .setHearing(Hearing.hearing()
                            .setId(hearingId)
                            .setType(STRING.next())
                            .setCourtCentreId(randomUUID())
                            .setCourtCentreName(STRING.next())
                            .setCourtRoomId(randomUUID())
                            .setCourtRoomName(STRING.next())
                            .setJudge(Judge.judge()
                                    .setId(randomUUID())
                                    .setTitle(STRING.next())
                                    .setFirstName(STRING.next())
                                    .setLastName(STRING.next()))
                            .setHearingDays(asList(FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"))))
                            .setDefendants(
                                    Arrays.stream(defendantIds)
                                            .map(id -> Defendant.defendant()
                                                    .setId(id)
                                                    .setPersonId(randomUUID())
                                                    .setFirstName(STRING.next())
                                                    .setLastName(STRING.next())
                                                    .setDefendantCases(asList(
                                                            DefendantCase.defendantCase().setCaseId(caseId)
                                                    ))
                                                    .setOffences(asList(
                                                            Offence.offence().setId(randomUUID()).setCaseId(caseId)
                                                                    .setOffenceCode(STRING.next())
                                                                    .setWording(STRING.next())
                                                                    .setStartDate(PAST_LOCAL_DATE.next())
                                                    )))
                                            .collect(toList())
                            )
                    );
        }
    }

    public static class UpdatePleaCommandTemplates {
        private UpdatePleaCommandTemplates() {
        }

        public static HearingUpdatePleaCommand.Builder updatePleaTemplate(final UUID offenceId, final PleaValueType pleaValueType) {
            return HearingUpdatePleaCommand.builder()
                    .withCaseId(randomUUID())//not used
                    .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                            .withId(randomUUID())//not used
                            .withPersonId(randomUUID())//not used
                            .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                    .withId(offenceId)
                                    .withPlea(Plea.builder()
                                            .withId(randomUUID())
                                            .withPleaDate(PAST_LOCAL_DATE.next())
                                            .withValue(pleaValueType.name())
                                    )
                            )

                    );
        }
    }

    public static class UpdateVerdictCommandTemplates {
        private UpdateVerdictCommandTemplates() {
        }

        public static HearingUpdateVerdictCommand.Builder updateVerdictTemplate(final UUID caseId, final UUID defendantId, final UUID offenceId, final VerdictCategoryType verdictCategoryType) {
            return HearingUpdateVerdictCommand.builder()
                    .withCaseId(caseId)
                    .addDefendant(
                            uk.gov.moj.cpp.hearing.command.verdict.Defendant.builder().withId(defendantId)
                                    .withPersonId(randomUUID())
                                    .addOffence(uk.gov.moj.cpp.hearing.command.verdict.Offence.builder()
                                            .withId(offenceId)
                                            .withVerdict(Verdict.builder().withId(randomUUID())
                                                    .withValue(VerdictValue.builder().withId(randomUUID())
                                                            .withCategoryType(verdictCategoryType.name())
                                                            .withCategory(STRING.next())
                                                            .withCode(STRING.next())
                                                            .withDescription(STRING.next())
                                                            .withVerdictTypeId(randomUUID())

                                                    ).withNumberOfJurors(integer(9, 12).next())
                                                    .withNumberOfSplitJurors(integer(0, 3).next())
                                                    .withUnanimous(BOOLEAN.next())
                                                    .withVerdictDate(PAST_LOCAL_DATE.next()))));
        }
    }

    public static SaveDraftResultCommand saveDraftResultCommandTemplate(
            final InitiateHearingCommand initiateHearingCommand) {
        return SaveDraftResultCommand.builder()
                .withDefendantId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                .withOffenceId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                .withTargetId(randomUUID())
                .withDraftResult(STRING.next())
                .build();
    }

    public static class ShareResultsCommandTemplates {
        private ShareResultsCommandTemplates() {
        }

        public static ShareResultsCommand basicShareResultsCommandTemplate() {

            return ShareResultsCommand.builder()
                    .withCourtClerk(CourtClerk.builder()
                            .withId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build())
                    .build();
        }

        public static ShareResultsCommand standardShareResultsCommandTemplate(final UUID defendantId, final UUID offenceId, final UUID caseId, final UUID resultLineId1, final UUID resultLineId2) {
            return with(basicShareResultsCommandTemplate(), command -> {
                command.getCompletedResultLines().add(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId1));
                command.getCompletedResultLines().add(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId2));                
            });
        }

        public static CompletedResultLine completedResultLineTemplate(final UUID defendantId, final UUID offenceId, final UUID caseId, final UUID resultLineId) {
            return completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, randomUUID());
        }

        public static CompletedResultLine completedResultLineTemplate(final UUID defendantId, final UUID offenceId, final UUID caseId, final UUID resultLineId, final UUID resultDefinitionId) {
            return CompletedResultLine.builder()
                    .withId(resultLineId)
                    .withResultDefinitionId(resultDefinitionId)
                    .withDefendantId(defendantId)
                    .withOffenceId(offenceId)
                    .withOrderedDate(LocalDate.now())
                    .withCaseId(caseId)
                    .withLevel(values(Level.values()).next())
                    .withResultLabel(STRING.next())
                    .withResultPrompts(asList(
                            ResultPrompt.builder()
                                    .withId(randomUUID())
                                    .withLabel(STRING.next())
                                    .withValue(STRING.next())
                                    .build(),
                            ResultPrompt.builder()
                                    .withId(randomUUID())
                                    .withLabel(STRING.next())
                                    .withValue(STRING.next())
                                    .build()))
                    .build();
        }

        public static UncompletedResultLine uncompletedResultLineTemplate(final UUID defendantId) {
            return UncompletedResultLine.builder()
                    .withId(randomUUID())
                    .withResultDefinitionId(randomUUID())
                    .withDefendantId(defendantId)
                    .withOrderedDate(LocalDate.now())
                    .build();
        }
    }

    public static class CompletedResultLineStatusTemplates {

        private CompletedResultLineStatusTemplates() {
        }

        public static CompletedResultLineStatus completedResultLineStatus(final UUID resultLineId) {
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            return CompletedResultLineStatus.builder()
                    .withId(resultLineId)
                    .withLastSharedDateTime(startDateTime)
                    .withCourtClerk(CourtClerk.builder()
                            .withId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build())
                    .build();
        }

    }

    public static class CaseDefendantDetailsChangedCommandTemplates {

        private CaseDefendantDetailsChangedCommandTemplates() {
        }

        public static CaseDefendantOffencesChangedCommand minimalCaseDefendantDetailsChangedTemplate() {
            return CaseDefendantOffencesChangedCommand.builder()
                    .withModifiedDate(LocalDate.now())
                    .build();
        }

        public static AddedOffence addedOffence() {
            return AddedOffence.builder()
                    .withDefendantId(randomUUID())
                    .withCaseId(randomUUID())
                    .withAddedOffences(asList(
                            DefendantOffence.builder(randomUUID(), STRING.next(), STRING.next(), PAST_LOCAL_DATE.next(), PAST_LOCAL_DATE.next(), INTEGER.next(), PAST_LOCAL_DATE.next())
                                    .withStatementOfOffence(new StatementOfOffence(STRING.next(), STRING.next()))
                                    .build()))
                    .build();
        }

        public static UpdatedOffence updatedOffence() {

            final BaseDefendantOffence offences = BaseDefendantOffence.builder()
                    .withId(randomUUID())
                    .withOffenceCode(STRING.next())
                    .withWording(STRING.next())
                    .withStartDate(PAST_LOCAL_DATE.next())
                    .withEndDate(PAST_LOCAL_DATE.next())
                    .withCount(INTEGER.next())
                    .withConvictionDate(PAST_LOCAL_DATE.next())
                    .build();


            return UpdatedOffence.builder()
                    .withUpdatedOffences(Arrays.asList(offences))
                    .withCaseId(randomUUID())
                    .withDefendantId(randomUUID())
                    .build();
        }

        public static DeletedOffence deletedOffence() {
            return DeletedOffence.builder().withCaseId(randomUUID()).withDefendantId(randomUUID()).build();
        }

    }

    public static class AddDefenceCounselCommandTemplates {
        private AddDefenceCounselCommandTemplates() {
        }

        public static AddDefenceCounselCommand standardAddDefenceCounselCommandTemplate(final UUID hearingId, final UUID defendantId) {
            return AddDefenceCounselCommand.builder()
                    .withAttendeeId(randomUUID())
                    .withPersonId(randomUUID())
                    .withHearingId(hearingId)
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withTitle(STRING.next())
                    .withStatus(STRING.next())
                    .addDefendantId(DefendantId.builder().withDefendantId(defendantId))
                    .build();
        }
    }

    public static class AddProsecutionCounselCommandTemplates {
        private AddProsecutionCounselCommandTemplates() {
        }

        public static AddProsecutionCounselCommand addProsecutionCounselCommandTemplate(final UUID hearingId) {
            return AddProsecutionCounselCommand.builder()
                    .withAttendeeId(randomUUID())
                    .withPersonId(randomUUID())
                    .withHearingId(hearingId)
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withTitle(STRING.next())
                    .withStatus(STRING.next())
                    .build();
        }
    }

    public static GenerateNowsCommand generateNowsCommandTemplate(final UUID hearingId, final UUID defendantId) {
        final UUID caseId = UUID.randomUUID();
        final UUID offenceId = UUID.randomUUID();
        final UUID nowsTypeId = UUID.randomUUID();
        final UUID materialId = UUID.randomUUID();
        final UUID sharedResultLineId0 = UUID.randomUUID();
        final UUID sharedResultLineId1 = UUID.randomUUID();
        final UUID sharedResultLineId2 = UUID.randomUUID();
        final UUID promptId00 = UUID.randomUUID();
        final UUID promptId01 = UUID.randomUUID();
        final UUID promptId10 = UUID.randomUUID();
        final UUID promptId11 = UUID.randomUUID();
        final UUID promptId20 = UUID.randomUUID();
        final UUID promptId21 = UUID.randomUUID();
        final String promptLabel0 = "Imprisonment Duration";
        final String promptLabel1 = "Prison";
        return GenerateNowsCommand.generateNowsCommand().setHearing(
                uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing.hearing()
                        .setId(hearingId)
                        .setStartDateTime("2016-06-01T10:00:00Z")
                        .setHearingDates(singletonList(ZonedDateTime.parse("2016-06-01T10:00:00Z")))
                        .setCourtCentre(CourtCentre.courtCentre()
                                .setCourtCentreName("Liverpool Crown Court")
                                .setCourtRoomId(UUID.randomUUID())
                                .setCourtRoomName("3"))
                        .setHearingType("Sentencing")
                        .setAttendees(Arrays.asList(
                                Attendees.attendees()
                                        //.setAttendeeId(UUID.randomUUID())
                                        .setFirstName("Cherie")
                                        .setLastName("Blair")
                                        .setType("COURTCLERK")
                                ,
                                Attendees.attendees()
                                        //.setAttendeeId(UUID.randomUUID())
                                        .setFirstName("Nina")
                                        .setLastName("Turner")
                                        //.setTitle("HHJ")
                                        .setType("JUDGE")
                                ,
                                Attendees.attendees()
                                        //.setAttendeeId(UUID.randomUUID())
                                        .setFirstName("Donald")
                                        .setLastName("Smith")
                                        .setType("DEFENCEADVOCATE")
                                //.setStatus("Leading QC")
                                //.setDefendants(Arrays.asList(defendantId))
                                //.setCases(Arrays.asList(caseId))
                                )
                        )
                        .setDefendants(Arrays.asList(
                                Defendants.defendants()
                                        .setId(defendantId)
                                        .setPerson(Person.person()
                                                .setId(UUID.randomUUID())
                                                .setTitle("Mr")
                                                .setFirstName("David")
                                                .setLastName("LLOYD")
                                                .setDateOfBirth("1980-07-15")
                                                .setNationality("England")
                                                .setGender("Male")
                                                .setHomeTelephone("02012345678")
                                                .setWorkTelephone("02012345679")
                                                .setMobile("07777777777")
                                                .setFax("02011111111")
                                                .setEmail("email@email.com")
                                                .setAddress(uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address.address()
                                                        .setAddressId(UUID.randomUUID())
                                                        .setAddress1("14 Tottenham Court Road")
                                                        .setAddress2("London")
                                                        .setAddress3("England")
                                                        .setAddress4("UK")
                                                        .setPostCode("W1T 1JY")
                                                )
                                        )

                                        .setDefenceOrganisation("XYZ Solicitors")
                                        .setInterpreter(uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Interpreter.interpreter()
                                                .setName("Robert Carlyle")
                                                .setLanguage("English"))
                                        .setCases(Arrays.asList(
                                                Cases.cases()
                                                        .setId(caseId)
                                                        .setUrn("URN123452")
                                                        .setBailStatus("in custody")
                                                        .setCustodyTimeLimitDate(LocalDate.parse("2018-01-30"))
                                                        .setOffences(Arrays.asList(
                                                                Offences.offences()
                                                                        .setId(offenceId)
                                                                        .setCode("OF61131")
                                                                        .setConvictionDate(LocalDate.parse("2017-08-02"))
                                                                        .setPlea(
                                                                                uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Plea.plea()
                                                                                        .setId(UUID.randomUUID())
                                                                                        .setValue("NOT GUILTY")
                                                                                        .setDate(LocalDate.parse("2017-02-02"))
                                                                                        .setEnteredHearingId(UUID.randomUUID())
                                                                        )
                                                                        .setVerdict(
                                                                                uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Verdict.verdict()
                                                                                        .setTypeId(UUID.randomUUID())
                                                                                        .setVerdictDescription("Not Guilty, guilty of a lesser or alternative offence")
                                                                                        .setVerdictCategory("GUILTY")
                                                                                        .setLesserOrAlternativeOffence(
                                                                                                LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                                                                                        .setOffenceTypeId(UUID.randomUUID())
                                                                                                        .setCode("OF62222")
                                                                                                        .setConvictionDate("2017-08-01")
                                                                                                        .setWording("On 19/01/2016 At wandsworth bridge rd SW6 Being a passenger on a Public Service Vehicle operated on behalf of London Bus Services Limited being used for the carriage of passengers at separate fares did use in relation to the journey you did not have a ticket")
                                                                                        )
                                                                                        .setNumberOfSplitJurors("9-1")
                                                                                        .setVerdictDate(LocalDate.parse("2017-02-02"))
                                                                                        .setNumberOfJurors(10)
                                                                                        .setUnanimous(false)
                                                                                        .setEnteredHearingId(UUID.randomUUID())
                                                                        )
                                                                        .setWording("On 19/01/2016 At wandsworth bridge rd SW6 Being a passenger on a Public Service Vehicle operated on behalf of London Bus Services Limited being used for the carriage of passengers at separate fares did use in relation to the journey you were taking a ticket which had been issued for use by another person on terms that it is not transferable")
                                                                        .setStartDate(LocalDate.parse("2016-06-21"))
                                                                        .setEndDate(LocalDate.parse("2017-08-01"))
                                                        ))
                                        ))
                        ))
                        .setSharedResultLines(Arrays.asList(
                                SharedResultLines.sharedResultLines()
                                        .setId(sharedResultLineId0)
                                        .setCaseId(caseId)
                                        .setDefendantId(defendantId)
                                        .setOffenceId(offenceId)
                                        .setLevel("CASE")
                                        .setLabel(IMPRISONMENT_LABEL)
                                        .setRank(1)
                                        .setPrompts(
                                                Arrays.asList(
                                                        Prompts.prompts()
                                                                .setId(promptId00)
                                                                .setLabel(promptLabel0)
                                                                .setValue(IMPRISONMENT_DURATION_VALUE),
                                                        Prompts.prompts()
                                                                .setId(promptId01)
                                                                .setLabel(promptLabel1)
                                                                .setValue(WORMWOOD_SCRUBS_VALUE)
                                                )
                                        ),
                                SharedResultLines.sharedResultLines()
                                        .setId(sharedResultLineId1)
                                        .setCaseId(caseId)
                                        .setDefendantId(defendantId)
                                        .setOffenceId(offenceId)
                                        .setLevel("DEFENDANT")
                                        .setLabel(IMPRISONMENT_LABEL)
                                        .setRank(2)
                                        .setPrompts(
                                                Arrays.asList(
                                                        Prompts.prompts()
                                                                .setId(promptId10)
                                                                .setLabel(promptLabel0)
                                                                .setValue(IMPRISONMENT_DURATION_VALUE),
                                                        Prompts.prompts()
                                                                .setId(promptId11)
                                                                .setLabel(promptLabel1)
                                                                .setValue(WORMWOOD_SCRUBS_VALUE)
                                                )
                                        ),
                                SharedResultLines.sharedResultLines()
                                        .setId(sharedResultLineId2)
                                        .setCaseId(caseId)
                                        .setDefendantId(defendantId)
                                        .setOffenceId(offenceId)
                                        .setLevel("OFFENCE")
                                        .setLabel(IMPRISONMENT_LABEL)
                                        .setRank(3)
                                        .setPrompts(
                                                Arrays.asList(
                                                        Prompts.prompts()
                                                                .setId(promptId20)
                                                                .setLabel(promptLabel0)
                                                                .setValue(IMPRISONMENT_DURATION_VALUE),
                                                        Prompts.prompts()
                                                                .setId(promptId21)
                                                                .setLabel(promptLabel1)
                                                                .setValue(WORMWOOD_SCRUBS_VALUE)
                                                )
                                        )

                        ))
                        .setNows(
                                Collections.singletonList(
                                        Nows.nows()
                                                .setId(UUID.randomUUID())
                                                .setNowsTypeId(nowsTypeId)
                                                .setDefendantId(defendantId)
                                                //.setNowsTemplateName("SingleTemplate")
                                                .setMaterials(Arrays.asList(
                                                        Material.material()
                                                                .setId(materialId)
                                                                .setUserGroups(Arrays.asList(
                                                                        UserGroups.userGroups()
                                                                                .setGroup("COURTCLERK"),
                                                                        UserGroups.userGroups()
                                                                                .setGroup("DEFENCECOUNSEL")
                                                                ))
                                                                .setNowResult(Arrays.asList(
                                                                        NowResult.nowResult()
                                                                                .setSharedResultId(sharedResultLineId0)
                                                                                .setSequence(1)
                                                                                .setPrompts(
                                                                                        Arrays.asList(
                                                                                                PromptRef.promptRef().setId(promptId00)
                                                                                                        .setLabel(promptLabel0),
                                                                                                PromptRef.promptRef().setId(promptId01).setLabel(promptLabel1)
                                                                                        )
                                                                                ),
                                                                        NowResult.nowResult()
                                                                                .setSharedResultId(sharedResultLineId1)
                                                                                .setSequence(2)
                                                                                .setPrompts(
                                                                                        Arrays.asList(
                                                                                                PromptRef.promptRef().setId(promptId10)
                                                                                                        .setLabel(promptLabel0),
                                                                                                PromptRef.promptRef().setId(promptId11).setLabel(promptLabel1)
                                                                                        )
                                                                                ),
                                                                        NowResult.nowResult()
                                                                                .setSharedResultId(sharedResultLineId2)
                                                                                .setSequence(3)
                                                                                .setPrompts(
                                                                                        Arrays.asList(
                                                                                                PromptRef.promptRef().setId(promptId20)
                                                                                                        .setLabel(promptLabel0),
                                                                                                PromptRef.promptRef().setId(promptId21).setLabel(promptLabel1)
                                                                                        )
                                                                                )
                                                                ))
                                                ))

                                )
                        )
                        .setNowTypes(
                                Arrays.asList(
                                        NowTypes.nowTypes()
                                                .setId(nowsTypeId)
                                                .setTemplateName("SingleTemplate")
                                                .setDescription("Imprisonment Order")
                                                .setRank(1)
                                                .setStaticText("<h3>Imprisonment</h3><p>You have been sentenced to a term of imprisonment. If you<ul><li>Do not comply with the requirements of this order during the <u>supervision period</u>; or</li><li>Commit any other offence during the <u>operational period</u></li></ul>you may be liable to serve the <u>custodial period</u> in prison.<br/><br/><br/><p>For the duration of the <u>supervision period</u>, you will be supervised by your Probation Officer, and<br/>You must<ul><li>Keep in touch with your Probation Officer as they tell you</li><li>Tell your Probation Officer if you intend to change your address</li><li>Comply with all other requirements</li></ul><p><strong>Requirements</strong> – Please refer only to the requirements that the court has specified in the details of your order, <u>as set out above</u><p><strong>Unpaid Work Requirement</strong><p>You must carry out unpaid work for the hours specified as you are told and by the date specified in the order. Your Probation Officer will tell you who will be responsible for supervising work.<p><strong>Activity Requirement</strong><p>You must present yourself as directed at the time and on the days specified in the order and you must undertake the activity the court has specified for the duration specified in the order in the way you are told by your Probation Officer<p><strong>Programme Requirement</strong><p>You must participate in the programme specified in the order at the location specified and for the number of days specified in the order<p><strong>Prohibited Activity Requirement</strong><p>You must not take part in the activity that the court has prohibited in the order for the number of days the court specified<p><strong>Curfew Requirement</strong><p>You must remain in the place or places the court has specified during the periods specified. The curfew requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Exclusion Requirement</strong><p>You must not enter the place or places the court has specified between the hours specified in the order. The exclusion requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Residence Requirement</strong><p>You must live at the premises the court has specified and obey any rules that apply there for the number of days specified in the order. You may live at ???? with the prior approval of your Probation Officer.<p><strong>Foreign Travel Prohibition Requirement</strong><p>You must not travel to the prohibited location specified in the order during the period the court has specified in the order.<p><strong>Mental Health Treatment Requirement</strong><p>You must have mental health treatment by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Drug Rehabilitation Requirement</strong><p>You must have treatment for drug dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p>To be sure that you do not have any illegal drug in your body, you must provide samples for testing at such times or in such circumstances as your Probation Officer or the person responsible for your treatment will tell you. The results of tests on the samples will be sent to your Probation Officer who will report the results to the court. Your Probation Officer will also tell the court how your order is progressing and the views of your treatment provider.<p>The court will review this order ????. The first review will be on the date and time specified at the court specified.<p>You must / need not attend this review hearing.<p><strong>Alcohol Treatment Requirement</strong><p>You must have treatment for alcohol dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Supervision Requirement</strong><p>You must attend appointments with your Probation Officer or another person at the times and places your Probation Officer says.<p><strong>Attendance Centre Requirement</strong><p>You must attend an attendance centre - see separate sheet for details<p><strong>WARNING</strong><p>If you do not comply with your order, you will be brought back to court. The court may then<ul><li>Change the order by adding extra requirements</li><li>Pass a different sentence for the original offences; or</li><li>Send you to prison</li></ul><p><strong>NOTE</strong><p>Either you or your Probation Officer can ask the court to look again at this order and the court can then change it or cancel it if it feels that is the right thing to do. The court may also pass a different sentence for the original offence(s). If you wish to ask the court to look at your order again you should get in touch with the court at the address above.")
                                                .setStaticTextWelsh("<h3> Prison </h3> <p> Fe'ch dedfrydwyd i dymor o garchar. Os ydych <ul> <li> Peidiwch â chydymffurfio â gofynion y gorchymyn hwn yn ystod y cyfnod goruchwylio </u>; neu </li> <li> Ymrwymo unrhyw drosedd arall yn ystod y cyfnod gweithredol </u> </li> </ul> efallai y byddwch yn atebol i wasanaethu'r cyfnod gwarchodaeth </u> yn y carchar. <br/> <br/> <br/> <p> Yn ystod y cyfnod goruchwylio </u>, byddwch chi'n cael eich goruchwylio gan eich Swyddog Prawf, a <br/> Rhaid ichi <ul> < li> Cadwch mewn cysylltiad â'ch Swyddog Prawf wrth iddyn nhw ddweud wrthych </li> <li> Dywedwch wrth eich Swyddog Prawf os ydych yn bwriadu newid eich cyfeiriad </li> <li> Cydymffurfio â'r holl ofynion eraill </li></ul > <p> <strong> Gofynion </strong> - Cyfeiriwch yn unig at y gofynion a nododd y llys yn manylion eich archeb, fel y nodir uchod </u> <p> <strong> Gwaith Di-dāl Gofyniad </strong><p> Rhaid i chi wneud gwaith di-dāl am yr oriau a bennir fel y dywedir wrthych a chi erbyn y dyddiad a bennir yn y gorchymyn. Bydd eich Swyddog Prawf yn dweud wrthych pwy fydd yn gyfrifol am oruchwylio gwaith.<p> <strong> Gweithgaredd Gofyniad </strong> <p> Rhaid i chi gyflwyno eich hun fel y'i cyfarwyddir ar yr amser ac ar y diwrnodau a bennir yn y gorchymyn a rhaid i chi ymgymryd â chi y gweithgaredd y mae'r llys wedi ei nodi ar gyfer y cyfnod a bennir yn y drefn yn y ffordd y dywedir wrth eich Swyddog Prawf <p> <strong> Gofyniad Rhaglen </strong><p> Rhaid i chi gymryd rhan yn y rhaglen a bennir yn y drefn yn y lleoliad a bennir ac am y nifer o ddyddiau a bennir yn y gorchymyn <p> <strong> Gofyniad Gweithgaredd Gwahardd </strong> <p> Rhaid i chi beidio â chymryd rhan yn y gweithgaredd a waharddodd y llys yn y drefn ar gyfer nifer y dyddiau llys penodol <p> <strong> Curfew Requirement </strong> <p> Rhaid i chi aros yn y lle neu lle mae'r llys wedi nodi yn ystod y cyfnodau a bennir. Mae'r gofyniad cyrffyw yn para am y nifer o ddyddiau a bennir yn y<p> Gweler \"Darpariaeth Monitro Electronig\" yn yr orchymyn hwn <p> <strong> Gofyniad Preswyl </strong> <p> Rhaid i chi fyw yn yr adeilad y llys wedi nodi ac ufuddhau i unrhyw reolau sy'n berthnasol yno am y nifer o ddyddiau a bennir yn y gorchymyn. Efallai y byddwch yn byw yn ???? gyda chymeradwyaeth ymlaen llaw eich Swyddog Prawf. <p> <strong> Gofyniad Gwahardd Teithio Tramor </strong> <p> Rhaid i chi beidio â theithio i'r lleoliad gwaharddedig a bennir yn yr orchymyn yn ystod y cyfnod y mae'r llys wedi'i bennu yn y gorchymyn. < p> <strong> Gofyniad Triniaeth Iechyd Meddwl </strong> <p> Rhaid i chi gael triniaeth iechyd meddwl gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y <p> <strong> Angen Adsefydlu Cyffuriau </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar gyffuriau gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am nifer y dyddiau <p> Er mwyn sicrhau nad oes gennych unrhyw gyffur anghyfreithlon yn eich corff, rhaid i chi ddarparu samplau i'w profi ar yr adegau hynny neu mewn amgylchiadau o'r fath y bydd eich Swyddog Prawf neu'r person sy'n gyfrifol am eich triniaeth yn dweud wrthych chi . Anfonir canlyniadau'r profion ar y samplau i'ch Swyddog Prawf a fydd yn adrodd y canlyniadau i'r llys. Bydd eich Swyddog Prawf hefyd yn dweud wrth y llys sut mae'ch gorchymyn yn mynd rhagddo a barn eich darparwr triniaeth. <P> Bydd y llys yn adolygu'r gorchymyn hwn ????. Bydd yr adolygiad cyntaf ar y dyddiad a'r amser a bennir yn y llys a bennir. <P> Rhaid i chi / nid oes angen i chi fynychu'r gwrandawiad hwn. <P> <strong> Gofyniad Trin Alcohol </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar alcohol gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y gorchymyn. <p> <strong> Gofyniad Goruchwylio </strong> <p> Rhaid i chi fynychu penodiadau gyda'ch Swyddog Prawf neu berson arall ar yr adegau a lle mae eich Swyddog Prawf yn dweud. <p> <strong> Gofyniad y Ganolfan Bresennol </strong> <p> Rhaid i chi fynychu canolfan bresenoldeb - <p> <strong> RHYBUDD </strong> <p> Os na fyddwch chi'n cydymffurfio â'ch archeb, fe'ch cewch eich troi'n ôl i'r llys. Gall y llys wedyn <ul> <li> Newid y gorchymyn trwy ychwanegu gofynion ychwanegol </li> <li> Pasiwch frawddeg wahanol ar gyfer y troseddau gwreiddiol; neu </li> <li> Anfonwch chi at y carchar </li> </ul> <p> <strong> NOTE </strong> <p> Naill ai chi neu'ch Swyddog Prawf all ofyn i'r llys edrych eto ar y gorchymyn hwn ac yna gall y llys ei newid neu ei ganslo os yw'n teimlo mai dyna'r peth iawn i'w wneud. Gall y llys hefyd basio brawddeg wahanol ar gyfer y trosedd (wyr) gwreiddiol. Os hoffech ofyn i'r llys edrych ar eich archeb eto dylech gysylltu â'r llys yn y cyfeiriad uchod. ")
                                                .setPriority("0.5 hours")
                                                .setJurisdiction("B")
                                )
                        )
        );
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
                    .setNowText(STRING.next())
                    .setUrgentTimeLimitInMinutes(INTEGER.next())
                    .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions()
                            .setId(randomUUID())
                            .setMandatory(true)
                            .setPrimary(true)
                            .setNowText(STRING.next())
                            .setSequence(1)
                    ));
        }
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
                            .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                    .setResultLineId(randomUUID())
                                    .setLastSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                            ))
                            ).setReferenceDate(LocalDate.now());
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

        private static UploadSubscriptionCommand buildUploadSubscriptionCommand() {

            final Map<String, String> properties = new HashMap<>();
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent("template", UUID.randomUUID().toString());
            properties.putIfAbsent("fromAddress", "noreply@test.com");

            final List<UUID> courtCentreIds = asList(randomUUID(), randomUUID());

            final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

            final UploadSubscriptionCommand command = new UploadSubscriptionCommand();
            command.setChannel("email");
            command.setChannelProperties(properties);
            command.setDestination(STRING.next());
            command.setUserGroups(asList(STRING.next(), STRING.next()));
            command.setCourtCentreIds(courtCentreIds);
            command.setNowTypeIds(nowTypeIds);

            return command;
        }
    }
}
