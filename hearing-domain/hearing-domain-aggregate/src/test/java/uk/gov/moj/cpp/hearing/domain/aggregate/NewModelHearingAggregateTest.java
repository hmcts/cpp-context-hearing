package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;

import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

public class NewModelHearingAggregateTest {

    @Test
    public void apply() {
    }

    @Test
    public void initiate() {
        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Initiated result = (Initiated) new NewModelHearingAggregate().initiate(initiateHearingCommand).collect(Collectors.toList()).get(0);

        assertThat(result.getCases(), is(initiateHearingCommand.getCases()));
        assertThat(result.getHearing(), is(initiateHearingCommand.getHearing()));
    }

    @Test
    public void initiateHearingOffencePlea() {

        InitiateHearingOffencePleaCommand initiateHearingOffencePleaCommand = new InitiateHearingOffencePleaCommand(
                randomUUID(),
                randomUUID(),
                randomUUID(),
                randomUUID(),
                randomUUID(),
                PAST_LOCAL_DATE.next(),
                "GUILTY"
        );

        InitiateHearingOffencePlead result = (InitiateHearingOffencePlead) new NewModelHearingAggregate()
                .initiateHearingOffencePlea(initiateHearingOffencePleaCommand).collect(Collectors.toList()).get(0);

        assertThat(result.getCaseId(), is(initiateHearingOffencePleaCommand.getCaseId()));
        assertThat(result.getDefendantId(), is(initiateHearingOffencePleaCommand.getDefendantId()));
        assertThat(result.getHearingId(), is(initiateHearingOffencePleaCommand.getHearingId()));
        assertThat(result.getOffenceId(), is(initiateHearingOffencePleaCommand.getOffenceId()));
        assertThat(result.getPleaDate(), is(initiateHearingOffencePleaCommand.getPleaDate()));
        assertThat(result.getValue(), is(initiateHearingOffencePleaCommand.getValue()));
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
                        .withStartDateTime(FUTURE_LOCAL_DATE.next())
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
                                                .withCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next())
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
                                )
                        )
                );
    }
}