package uk.gov.moj.cpp.hearing.test;


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
import uk.gov.moj.cpp.hearing.command.initiate.Witness;
import uk.gov.moj.cpp.hearing.command.offence.AddedOffence;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffence;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UncompletedResultLine;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

public class TestTemplates {

    public enum PleaValueType {GUILTY, NOT_GUILTY}

    public enum VerdictCategoryType {GUILTY, NOT_GUILTY, NO_VERDICT}

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
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            return InitiateHearingCommand.initiateHearingCommand()
                    .setCases(asList(caseTemplate(caseId)))
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
                            .setHearingDays(asList(startDateTime))
                            .setDefendants(asList(defendantTemplate(caseId)))
                            .setWitnesses(asList(witnessTemplate(caseId)))
                    );
        }

        public static Case caseTemplate(UUID caseId) {
            return Case.legalCase()
                    .setCaseId(caseId)
                    .setUrn(STRING.next());
        }

        public static Defendant defendantTemplate(UUID caseId) {
            return Defendant.defendant()
                    .setId(randomUUID())
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
                            offenceTemplate(caseId)
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

        public static DefendantCase defendantCaseTemplate(UUID caseId) {
            return DefendantCase.defendantCase()
                    .setCaseId(caseId)
                    .setBailStatus(STRING.next())
                    .setCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next());
        }

        public static Offence offenceTemplate(UUID caseId) {
            return Offence.offence()
                    .setId(randomUUID())
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

        public static Witness witnessTemplate(UUID caseId) {
            return Witness.witness()
                    .setId(randomUUID())
                    .setCaseId(caseId)
                    .setType("Prosecution")
                    .setClassification("Expert")
                    .setPersonId(randomUUID())
                    .setTitle(STRING.next())
                    .setFirstName(STRING.next())
                    .setLastName(STRING.next())
                    .setGender(STRING.next())
                    .setDateOfBirth(PAST_LOCAL_DATE.next())
                    .setEmail(STRING.next())
                    .setFax(STRING.next())
                    .setHomeTelephone(STRING.next())
                    .setWorkTelephone(STRING.next())
                    .setMobile(STRING.next())
                    .setNationality(STRING.next());
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

        public static HearingUpdatePleaCommand.Builder updatePleaTemplate(UUID offenceId, PleaValueType pleaValueType) {
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

        public static HearingUpdateVerdictCommand.Builder updateVerdictTemplate(UUID caseId, UUID defendantId, UUID offenceId, VerdictCategoryType verdictCategoryType) {
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
                .withTargetId(UUID.randomUUID())
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

        public static ShareResultsCommand standardShareResultsCommandTemplate(UUID defendantId, UUID offenceId, UUID caseId, UUID resultLineId1, UUID resultLineId2) {
            return with(basicShareResultsCommandTemplate(), command -> {
                command.getCompletedResultLines().add(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId1));
                command.getCompletedResultLines().add(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId2));
            });
        }


        public static CompletedResultLine completedResultLineTemplate(UUID defendantId, UUID offenceId, UUID caseId, UUID resultLineId) {
            return CompletedResultLine.builder()
                    .withId(resultLineId)
                    .withResultDefinitionId(UUID.randomUUID())
                    .withDefendantId(defendantId)
                    .withOffenceId(offenceId)
                    .withCaseId(caseId)
                    .withLevel(values(Level.values()).next())
                    .withResultLabel(STRING.next())
                    .withResultPrompts(asList(
                            ResultPrompt.builder()
                                    .withId(UUID.randomUUID())
                                    .withLabel(STRING.next())
                                    .withValue(STRING.next())
                                    .build(),
                            ResultPrompt.builder()
                                    .withId(UUID.randomUUID())
                                    .withLabel(STRING.next())
                                    .withValue(STRING.next())
                                    .build()))
                    .build();
        }

        public static UncompletedResultLine uncompletedResultLineTemplate(UUID defendantId) {
            return UncompletedResultLine.builder()
                    .withId(UUID.randomUUID())
                    .withResultDefinitionId(UUID.randomUUID())
                    .withDefendantId(defendantId)
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
                            UpdatedOffence.builder()
                                    .withId(randomUUID())
                                    .withOffenceCode(STRING.next())
                                    .withWording(STRING.next())
                                    .withStartDate(PAST_LOCAL_DATE.next())
                                    .withEndDate(PAST_LOCAL_DATE.next())
                                    .withCount(INTEGER.next())
                                    .withConvictionDate(PAST_LOCAL_DATE.next())
                                    .build()
                            )
                    )
                    .build();
        }

        public static UpdatedOffence updatedOffence() {
            return UpdatedOffence.builder()
                    .withId(randomUUID())
                    .withOffenceCode(STRING.next())
                    .withWording(STRING.next())
                    .withStartDate(PAST_LOCAL_DATE.next())
                    .withEndDate(PAST_LOCAL_DATE.next())
                    .withCount(INTEGER.next())
                    .withConvictionDate(PAST_LOCAL_DATE.next())
                    .build();
        }

        public static DeletedOffence deletedOffence() {
            return DeletedOffence.builder().withId(randomUUID()).build();
        }

    }

    public static class AddDefenceCounselCommandTemplates {
        private AddDefenceCounselCommandTemplates() {
        }

        public static AddDefenceCounselCommand standardAddDefenceCounselCommandTemplate(UUID hearingId, UUID defendantId) {
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

        public static AddProsecutionCounselCommand addProsecutionCounselCommandTemplate(UUID hearingId) {
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
}
