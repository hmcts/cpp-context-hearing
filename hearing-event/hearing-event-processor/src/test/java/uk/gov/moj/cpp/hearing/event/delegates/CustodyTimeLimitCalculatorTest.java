package uk.gov.moj.cpp.hearing.event.delegates;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.test.FileUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustodyTimeLimitCalculatorTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private CustodyTimeLimitCalculator target = null;

    @Before
    public void setUp() {
        target = new CustodyTimeLimitCalculator();
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

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
                                                .withValue(timeLimitIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
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
        assertThat(timeLimitIn, is(personDefendant.getCustodyTimeLimit()));
        assertThat(timeLimitIn, is(personDefendant.getBailStatus().getCustodyTimeLimit().getTimeLimit()));
        assertThat(timeLimitIn, is(offence.getCustodyTimeLimit().getTimeLimit()));
        assertThat(daysSpentIn, is(offence.getCustodyTimeLimit().getDaysSpent()));

    }

    @Test
    public void shouldOffenceAndDefendantCustodyTimeLimit() {

        final int daysSpentIn = 34;
        final LocalDate timeLimitIn = LocalDate.of(2020, 7, 24);
        final JsonObject hearingResultedPayload = new StringToJsonObjectConverter().convert(FileUtil.getPayload("hearing.with.custodytimelimit.json"));
        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(hearingResultedPayload, PublicHearingResulted.class);

        target.calculate(publicHearingResulted.getHearing());

        final Defendant defendant1 = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        final Defendant defendant2 = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(1);

        assertThat(timeLimitIn, is(defendant1.getPersonDefendant().getCustodyTimeLimit()));
        assertThat(timeLimitIn, is(defendant1.getPersonDefendant().getBailStatus().getCustodyTimeLimit().getTimeLimit()));
        assertThat(timeLimitIn, is(defendant1.getOffences().get(0).getCustodyTimeLimit().getTimeLimit()));
        assertThat(daysSpentIn, is(defendant1.getOffences().get(0).getCustodyTimeLimit().getDaysSpent()));
        assertThat(timeLimitIn, is(defendant1.getOffences().get(1).getCustodyTimeLimit().getTimeLimit()));
        assertThat(daysSpentIn, is(defendant1.getOffences().get(1).getCustodyTimeLimit().getDaysSpent()));

        assertThat(timeLimitIn, is(defendant2.getPersonDefendant().getCustodyTimeLimit()));
        assertThat(timeLimitIn, is(defendant2.getPersonDefendant().getBailStatus().getCustodyTimeLimit().getTimeLimit()));
        assertThat(timeLimitIn, is(defendant2.getOffences().get(0).getCustodyTimeLimit().getTimeLimit()));
        assertThat(daysSpentIn, is(defendant2.getOffences().get(0).getCustodyTimeLimit().getDaysSpent()));
        assertThat(timeLimitIn, is(defendant2.getOffences().get(1).getCustodyTimeLimit().getTimeLimit()));
        assertThat(daysSpentIn, is(defendant2.getOffences().get(1).getCustodyTimeLimit().getDaysSpent()));
    }
}
