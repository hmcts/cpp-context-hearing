package uk.gov.moj.cpp.hearing.event.delegates;

import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class CustodyTimeLimitCalculatorTest {

    private CustodyTimeLimitCalculator target = new CustodyTimeLimitCalculator();

    @Test
    public void test1Offence1CTL() {
        final UUID judicialResultId = UUID.randomUUID();

        int daysSpentIn =123;
        LocalDate timeLimitIn = LocalDate.of(2019, 11, 30);

        final Offence offence = Offence.offence()
                .withJudicialResults(asList(JudicialResult.
                        judicialResult()
                        .withJudicialResultId(judicialResultId)
                        .withJudicialResultPrompts(
                                asList(JudicialResultPrompt.judicialResultPrompt()
                                        .withPromptReference(CustodyTimeLimitCalculator.CTL_DAYS_SPENT_PROMPT_REF
                                        )
                                        .withValue(""+daysSpentIn)
                                        .build(),
                                        JudicialResultPrompt.judicialResultPrompt()
                                                .withPromptReference(CustodyTimeLimitCalculator.CTL_TIME_LIMIT_PROMPT_REF
                                                )
                                                .withValue( timeLimitIn.format(DateTimeFormatter.ofPattern(CustodyTimeLimitCalculator.DATE_FORMAT0))      )
                                                .build())
                        )
                        .build()))
                .build();

        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(BailStatus.bailStatus()
                        .withCode(CustodyTimeLimitCalculator.CUSTODY_OR_REMANDED_INTO_CUSTODY)
                        .build())
                .build();

        final Defendant defendant = Defendant.defendant()
                .withPersonDefendant(personDefendant)
                .withOffences(asList(offence))
                .build();

        final Hearing hearing = Hearing.hearing().
                withProsecutionCases(
                        asList(ProsecutionCase.prosecutionCase()
                                .withDefendants(asList(defendant))
                                .build()
                        ))
                .build();
        target.calculate(hearing);
        Assert.assertEquals(timeLimitIn, personDefendant.getCustodyTimeLimit());
        Assert.assertEquals(timeLimitIn, personDefendant.getBailStatus().getCustodyTimeLimit().getTimeLimit());
        Assert.assertEquals(timeLimitIn, offence.getCustodyTimeLimit().getTimeLimit());
        Assert.assertEquals(daysSpentIn, offence.getCustodyTimeLimit().getDaysSpent().intValue());

    }
}
