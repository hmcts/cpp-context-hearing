package uk.gov.moj.cpp.hearing.test;


import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

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
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultLine;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;

import java.util.Arrays;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

public class TestTemplates {

    private TestTemplates() {

    }

    public static InitiateHearingCommand.Builder initiateHearingCommandTemplateWithOnlyMandatoryFields() {

        final UUID caseId = randomUUID();

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
                        .withStartDateTime(FUTURE_ZONED_DATE_TIME.next())
                        .withEstimateMinutes(INTEGER.next())
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
                        .withStartDateTime(FUTURE_ZONED_DATE_TIME.next())
                        .withNotBefore(false)
                        .withEstimateMinutes(INTEGER.next())
                        .addDefendant(Defendant.builder()
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
                                                .withCustodyTimeLimitDate(FUTURE_ZONED_DATE_TIME.next())
                                )
                                .addOffence(
                                        Offence.builder()
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
                                                .withTitle(STRING.next())
                                )
                        )
                        .addWintess(Witness.builder()
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
                                .withNationality(STRING.next())

                        )
                );
    }

    public static InitiateHearingCommand.Builder initiateHearingCommandTemplateWithOnlyMandatoryFields(
            final UUID caseId, final UUID hearingId, final UUID... defendantIds) {

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
                        .withStartDateTime(FUTURE_ZONED_DATE_TIME.next())
                        .withEstimateMinutes(INTEGER.next())
                );
        Arrays.stream(defendantIds).forEach(id ->

                initiateHearingBuilder.getHearing().addDefendant(Defendant.builder()
                        .withId(id).withPersonId(randomUUID())
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

    public static ShareResultsCommand shareResultsCommandTemplate(final InitiateHearingCommand initiateHearingCommand) {
        return ShareResultsCommand.builder()
                .withResultLines(asList(ResultLine.builder()
                                .withId(UUID.randomUUID())
                                .withResultDefinitionId(UUID.randomUUID())
                                .withPersonId(initiateHearingCommand.getHearing().getDefendants().get(0).getPersonId())
                                .withOffenceId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                                .withLevel(values(Level.values()).next())
                                .withResultLabel(STRING.next())
                                .withComplete(true)
                                .withClerkOfTheCourtId(randomUUID())
                                .withClerkOfTheCourtFirstName(STRING.next())
                                .withClerkOfTheCourtLastName(STRING.next())
                                .withPrompts(asList(ResultPrompt.builder()
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .build(),
                                        ResultPrompt.builder()
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .build()))
                                .build(),
                        ResultLine.builder()
                                .withId(UUID.randomUUID())
                                .withResultDefinitionId(UUID.randomUUID())
                                .withPersonId(UUID.randomUUID())
                                .withOffenceId(UUID.randomUUID())
                                .withCaseId(UUID.randomUUID())
                                .withLevel(values(Level.values()).next())
                                .withResultLabel(STRING.next())
                                .withComplete(false)
                                .withClerkOfTheCourtId(randomUUID())
                                .withClerkOfTheCourtFirstName(STRING.next())
                                .withClerkOfTheCourtLastName(STRING.next())
                                .withPrompts(asList(ResultPrompt.builder()
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .build(),
                                        ResultPrompt.builder()
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .build()))
                                .build()))
                .build();
    }
}
