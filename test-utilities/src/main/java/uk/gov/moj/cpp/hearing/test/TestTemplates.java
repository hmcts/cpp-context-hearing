package uk.gov.moj.cpp.hearing.test;


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
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static java.util.UUID.randomUUID;

public class TestTemplates {


    public static InitiateHearingCommand.Builder initiateHearingCommandTemplateWithOnlyMandatoryFields() {

        UUID caseId = randomUUID();

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
        UUID caseId = randomUUID();
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

}
