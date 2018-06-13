package uk.gov.moj.cpp.hearing.test;


import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_UTC_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

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
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UncompletedResultLine;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

public class TestTemplates {

    private TestTemplates() {

    }

    public static InitiateHearingCommand.Builder initiateHearingCommandTemplateWithOnlyMandatoryFields() {

        final UUID caseId = randomUUID();
        final ZonedDateTime startDateTime = FUTURE_UTC_DATE_TIME.next();
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
                                        Offence.builder()
                                                .withId(randomUUID())
                                                .withCaseId(caseId)
                                                .withOffenceCode(STRING.next())
                                                .withWording(STRING.next())
                                                .withStartDate(PAST_LOCAL_DATE.next())
                                )
                        )
                );
    }


    public static InitiateHearingCommand.Builder initiateHearingCommandTemplate() {
        final UUID caseId = randomUUID();
        final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
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
                        .withHearingDays(Arrays.asList(startDateTime))
                        .addDefendant(initiateHearingDefendantTemplate(caseId))
                        .addWitness(
                                initiateHearingWitnessTemplate(caseId)
                        )
                );
    }

    public static Defendant.Builder initiateHearingDefendantTemplate(UUID caseId) {
        return Defendant.builder()
                .withId(randomUUID())
                .withPersonId(randomUUID())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withNationality(STRING.next())
                .withGender(STRING.next())
                .withAddress(
                        Address.builder()
                                .withAddress1(STRING.next())
                                .withAddress2(STRING.next())
                                .withAddress3(STRING.next())
                                .withAddress4(STRING.next())
                                .withPostCode(STRING.next())
                )
                .withDateOfBirth(PAST_LOCAL_DATE.next())
                .withDefenceOrganisation(STRING.next())
                .withInterpreter(
                        Interpreter.builder()
                                .withNeeded(false)
                                .withLanguage(STRING.next())
                )
                .addDefendantCase(
                        DefendantCase.builder()
                                .withCaseId(caseId)
                                .withBailStatus(STRING.next())
                                .withCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next())
                )
                .addOffence(
                        initiateHearingOffenceTemplate(caseId)
                );
    }

    public static Offence.Builder initiateHearingOffenceTemplate(UUID caseId) {
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

    public static Witness.Builder initiateHearingWitnessTemplate(UUID caseId) {
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

    public static InitiateHearingCommand.Builder initiateHearingCommandTemplateWithOnlyMandatoryFields(
            final UUID caseId, final UUID hearingId, final UUID... defendantIds) {

        final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next();
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

    public static HearingUpdatePleaCommand.Builder updatePleaTemplate() {
        return HearingUpdatePleaCommand.builder()
                .withCaseId(randomUUID())
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(randomUUID())
                        .withPersonId(randomUUID())
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(randomUUID())
                                .withPlea(Plea.builder()
                                        .withId(randomUUID())
                                        .withPleaDate(PAST_LOCAL_DATE.next())
                                        .withValue(STRING.next())
                                )
                        )

                );
    }

    public static SaveDraftResultCommand saveDraftResultCommandTemplate(final InitiateHearingCommand initiateHearingCommand) {
        return SaveDraftResultCommand.builder()
                .withDefendantId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                .withOffenceId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                .withTargetId(UUID.randomUUID())
                .withDraftResult(STRING.next())
                .build();
    }

    public static ShareResultsCommand basicShareResultsCommandTemplate(final InitiateHearingCommand initiateHearingCommand) {

        return ShareResultsCommand.builder()
                .withCourtClerk(CourtClerk.builder()
                        .withId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLines(
                        asList(CompletedResultLine.builder()
                                        .withId(UUID.randomUUID())
                                        .withResultDefinitionId(UUID.randomUUID())
                                        .withDefendantId(initiateHearingCommand.getHearing().getDefendants().get(0).getPersonId())
                                        .withOffenceId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                                        .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
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
                                        .build(),
                                CompletedResultLine.builder()
                                        .withId(UUID.randomUUID())
                                        .withResultDefinitionId(UUID.randomUUID())
                                        .withDefendantId(UUID.randomUUID())
                                        .withOffenceId(UUID.randomUUID())
                                        .withCaseId(UUID.randomUUID())
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
                                        .build()
                        )
                )
                .withUncompletedResultLines(
                        asList(UncompletedResultLine.builder()
                                .withId(UUID.randomUUID())
                                .withResultDefinitionId(UUID.randomUUID())
                                .withDefendantId(UUID.randomUUID())
                                .build())
                )
                .build();
    }

    public static class CaseDefendantDetailsChangedCommand {

        private CaseDefendantDetailsChangedCommand(){}


        public static CaseDefendantOffencesChangedCommand standard() {
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


}
