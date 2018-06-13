package uk.gov.moj.cpp.hearing.test;


import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.Builder;
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

        public static InitiateHearingCommand.Builder basicInitiateHearingTemplate() {

            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            final ZonedDateTime secondDateTime = startDateTime.plusDays(2);

            return InitiateHearingCommand.builder()
                    .withHearing(Hearing.builder()
                            .withId(randomUUID())
                            .withType(STRING.next())
                            .withCourtCentreId(randomUUID())
                            .withCourtCentreName(STRING.next())
                            .withCourtRoomId(randomUUID())
                            .withCourtRoomName(STRING.next())
                            .withJudge(
                                    Judge.builder()
                                            .withId(randomUUID())
                                            .withTitle(STRING.next())
                                            .withFirstName(STRING.next())
                                            .withLastName(STRING.next())
                            )
                            .withHearingDays(Arrays.asList(startDateTime, secondDateTime))
                    );
        }

        public static InitiateHearingCommand.Builder minimalInitiateHearingTemplate() {

            final UUID caseId = randomUUID();
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            final ZonedDateTime secondDateTime = startDateTime.plusDays(2);

            return InitiateHearingCommand.builder()
                    .addCase(Case.builder()
                            .withCaseId(caseId)
                            .withUrn(STRING.next())
                    )
                    .withHearing(Hearing.builder()
                            .withId(randomUUID())
                            .withType(STRING.next())
                            .withCourtCentreId(randomUUID())
                            .withCourtCentreName(STRING.next())
                            .withCourtRoomId(randomUUID())
                            .withCourtRoomName(STRING.next())
                            .withJudge(
                                    Judge.builder()
                                            .withId(randomUUID())
                                            .withTitle(STRING.next())
                                            .withFirstName(STRING.next())
                                            .withLastName(STRING.next())
                            )
                            .withHearingDays(Arrays.asList(startDateTime, secondDateTime))
                            .addDefendant(Defendant.builder()
                                    .withId(randomUUID())
                                    .withPersonId(randomUUID())
                                    .withFirstName(STRING.next())
                                    .withLastName(STRING.next())
                                    .addDefendantCase(
                                            DefendantCase.builder()
                                                    .withCaseId(caseId)
                                    )
                                    .addOffence(
                                            offenceTemplate(caseId)
                                                    .withSection(null)
                                                    .withEndDate(null)
                                                    .withOrderIndex(null)
                                                    .withCount(null)
                                                    .withConvictionDate(null)
                                                    .withLegislation(null)
                                                    .withTitle(null)
                                    )
                            )
                    );
        }

        public static InitiateHearingCommand.Builder standardInitiateHearingTemplate() {
            final UUID caseId = randomUUID();
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            return InitiateHearingCommand.builder()
                    .addCase(caseTemplate(caseId))
                    .withHearing(Hearing.builder()
                            .withId(randomUUID())
                            .withType(STRING.next())
                            .withCourtCentreId(randomUUID())
                            .withCourtCentreName(STRING.next())
                            .withCourtRoomId(randomUUID())
                            .withCourtRoomName(STRING.next())
                            .withJudge(
                                    Judge.builder()
                                            .withId(randomUUID())
                                            .withTitle(STRING.next())
                                            .withFirstName(STRING.next())
                                            .withLastName(STRING.next())
                            )
                            .withHearingDays(Arrays.asList(startDateTime))
                            .addDefendant(defendantTemplate(caseId))
                            .addWitness(witnessTemplate(caseId))
                    );
        }

        public static Case.Builder caseTemplate(UUID caseId) {
            return Case.builder()
                    .withCaseId(caseId)
                    .withUrn(STRING.next());
        }

        public static Defendant.Builder defendantTemplate(UUID caseId) {
            return Defendant.builder()
                    .withId(randomUUID())
                    .withPersonId(randomUUID())
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withNationality(STRING.next())
                    .withGender(STRING.next())
                    .withAddress(addressTemplate())
                    .withDateOfBirth(PAST_LOCAL_DATE.next())
                    .withDefenceOrganisation(STRING.next())
                    .withInterpreter(
                            Interpreter.builder()
                                    .withNeeded(false)
                                    .withLanguage(STRING.next())
                    )
                    .addDefendantCase(defendantCaseTemplate(caseId))
                    .addOffence(offenceTemplate(caseId));
        }

        public static Address.Builder addressTemplate() {
            return Address.builder()
                    .withAddress1(STRING.next())
                    .withAddress2(STRING.next())
                    .withAddress3(STRING.next())
                    .withAddress4(STRING.next())
                    .withPostCode(STRING.next());
        }

        public static DefendantCase.Builder defendantCaseTemplate(UUID caseId) {
            return DefendantCase.builder()
                    .withCaseId(caseId)
                    .withBailStatus(STRING.next())
                    .withCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next());
        }

        public static Offence.Builder offenceTemplate(UUID caseId) {
            return Offence.builder()
                    .withId(randomUUID())
                    .withCaseId(caseId)
                    .withOffenceCode(STRING.next())
                    .withWording(STRING.next())
                    .withSection(STRING.next())
                    .withStartDate(PAST_LOCAL_DATE.next())
                    .withEndDate(PAST_LOCAL_DATE.next())
                    .withOrderIndex(INTEGER.next())
                    .withCount(INTEGER.next())
                    .withConvictionDate(PAST_LOCAL_DATE.next())
                    .withLegislation(STRING.next())
                    .withTitle(STRING.next());
        }

        public static Witness.Builder witnessTemplate(UUID caseId) {
            return Witness.builder()
                    .withId(randomUUID())
                    .withCaseId(caseId)
                    .withType("Prosecution")
                    .withClassification("Expert")
                    .withPersonId(randomUUID())
                    .withTitle(STRING.next())
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withGender(STRING.next())
                    .withDateOfBirth(PAST_LOCAL_DATE.next())
                    .withEmail(STRING.next())
                    .withFax(STRING.next())
                    .withHomeTelephone(STRING.next())
                    .withWorkTelephone(STRING.next())
                    .withMobile(STRING.next())
                    .withNationality(STRING.next());
        }

        public static InitiateHearingCommand.Builder minimalInitiateHearingTemplate(
                final UUID caseId, final UUID hearingId, final UUID... defendantIds) {

            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
            final Builder initiateHearingBuilder = InitiateHearingCommand.builder()
                    .addCase(Case.builder().withCaseId(caseId).withUrn(STRING.next()))
                    .withHearing(Hearing.builder().withId(hearingId).withType(STRING.next())
                            .withCourtCentreId(randomUUID())
                            .withCourtCentreName(STRING.next())
                            .withCourtRoomId(randomUUID())
                            .withCourtRoomName(STRING.next())
                            .withJudge(Judge.builder().withId(randomUUID())
                                    .withTitle(STRING.next())
                                    .withFirstName(STRING.next())
                                    .withLastName(STRING.next()))
                            .withHearingDays(Arrays.asList(startDateTime))
                    );
            Arrays.stream(defendantIds).forEach(id ->

                    initiateHearingBuilder.getHearing().addDefendant(Defendant.builder()
                            .withId(id)
                            .withPersonId(randomUUID())
                            .withFirstName(STRING.next()).withLastName(STRING.next())
                            .addDefendantCase(DefendantCase.builder().withCaseId(caseId))
                            .addOffence(Offence.builder().withId(randomUUID()).withCaseId(caseId)
                                    .withOffenceCode(STRING.next())
                                    .withWording(STRING.next())
                                    .withStartDate(PAST_LOCAL_DATE.next()))));
            return initiateHearingBuilder;
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

        public static ShareResultsCommand standardShareResultsCommandTemplate(UUID defendantId, UUID offenceId, UUID caseId) {
            return with(basicShareResultsCommandTemplate(), command -> {
                command.getCompletedResultLines().add(completedResultLineTemplate(defendantId, offenceId, caseId));
                command.getCompletedResultLines().add(completedResultLineTemplate(defendantId, offenceId, caseId));
                command.getUncompletedResultLines().add(uncompletedResultLineTemplate(defendantId));
            });
        }


        public static CompletedResultLine completedResultLineTemplate(UUID defendantId, UUID offenceId, UUID caseId) {
            return CompletedResultLine.builder()
                    .withId(UUID.randomUUID())
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
