package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.test.FileUtil;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustodyTimeLimitCalculatorTest {
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    private CustodyTimeLimitCalculator target = new CustodyTimeLimitCalculator();

    @Test
    public void test1Offence1CTL() {
        final UUID judicialResultId = randomUUID();

        int daysSpentIn = 123;
        LocalDate timeLimitIn = LocalDate.of(2019, 11, 30);

        final Offence offence = Offence.offence()
                .withJudicialResults(asList(JudicialResult.
                        judicialResult()
                        .withJudicialResultId(judicialResultId)
                        .withJudicialResultPrompts(
                                asList(JudicialResultPrompt.judicialResultPrompt()
                                        .withPromptReference("CTLDATE")
                                        .withValue(timeLimitIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                        .build())
                        )
                        .build()))
                .build();

        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(BailStatus.bailStatus()
                        .withCode("C")
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
        assertThat(timeLimitIn, is(offence.getCustodyTimeLimit().getTimeLimit()));

    }

    @Test
    public void test1Offence1CTLWithoutCtlTime() {
        final UUID judicialResultId = randomUUID();

        LocalDate timeLimitIn = LocalDate.of(2019, 11, 30);

        final Offence offence = Offence.offence()
                .withJudicialResults(asList(JudicialResult.
                        judicialResult()
                        .withJudicialResultId(judicialResultId)
                        .withJudicialResultPrompts(
                                asList(JudicialResultPrompt.judicialResultPrompt()
                                        .withPromptReference("CTLDATE")
                                        .withValue(timeLimitIn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                        .build())
                        )
                        .build()))
                .build();

        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(BailStatus.bailStatus()
                        .withCode("C")
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
        assertThat(timeLimitIn, is(offence.getCustodyTimeLimit().getTimeLimit()));

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

        assertThat(timeLimitIn, is(defendant1.getOffences().get(0).getCustodyTimeLimit().getTimeLimit()));
        assertThat(timeLimitIn, is(defendant1.getOffences().get(1).getCustodyTimeLimit().getTimeLimit()));

        assertThat(timeLimitIn, is(defendant2.getOffences().get(0).getCustodyTimeLimit().getTimeLimit()));
        assertThat(timeLimitIn, is(defendant2.getOffences().get(1).getCustodyTimeLimit().getTimeLimit()));
    }

    @Test
    public void shouldCalculateHeldInCustodyWhenDefendantIsInCustodyFirstly() {
        final LocalDate hearingDate = LocalDate.now();
        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("C")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing, hearingDate);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), nullValue());
        assertThat(offence.getDateHeldInCustodySince(), is(hearingDate));

    }

    @Test
    public void shouldMaintainPreviousHeldInCustodyWhenDefendantWasInCustodyAndHasPreviousDaysAndStillCustody() {
        final UtcClock utcClock = new UtcClock();
        final ZonedDateTime hearingDay = utcClock.now();
        final LocalDate dateHeldInCustodySince = LocalDate.now().minusDays(10);
        final Hearing hearing = Hearing.hearing()
                .withHearingDays(asList(HearingDay.hearingDay().withSittingDay(hearingDay).build()))
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("C")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withPreviousDaysHeldInCustody(2)
                                        .withDateHeldInCustodySince(dateHeldInCustodySince)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), is(2));
        assertThat(offence.getDateHeldInCustodySince(), is(dateHeldInCustodySince));

    }

    @Test
    public void shouldMaintainPreviousHeldInCustodyWhenDefendantWasInCustodyAndStillCustody() {
        final UtcClock utcClock = new UtcClock();
        final ZonedDateTime hearingDay = utcClock.now();
        final LocalDate dateHeldInCustodySince = LocalDate.now().minusDays(10);
        final Hearing hearing = Hearing.hearing()
                .withHearingDays(asList(HearingDay.hearingDay().withSittingDay(hearingDay).build()))
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("C")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withDateHeldInCustodySince(dateHeldInCustodySince)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculate(hearing);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), nullValue());
        assertThat(offence.getDateHeldInCustodySince(), is(dateHeldInCustodySince));

    }

    @Test
    public void shouldCalculateHeldInCustodyWhenDefendantWasInCustodyButNowOnBail() {
        final UtcClock utcClock = new UtcClock();
        final ZonedDateTime hearingDay = utcClock.now();
        final LocalDate dateHeldInCustodySince = LocalDate.now().minusDays(10);
        final Hearing hearing = Hearing.hearing()
                .withHearingDays(asList(HearingDay.hearingDay().withSittingDay(hearingDay).build()))
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .build())
                                        .withDateHeldInCustodySince(dateHeldInCustodySince)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), is(10));
        assertThat(offence.getDateHeldInCustodySince(), nullValue());

    }

    @Test
    public void shouldCalculateHeldInCustodyWhenDefendantWasInCustodyAndHadPreviousDaysInCustodyButNowOnBail() {
        final LocalDate hearingDate = LocalDate.now();
        final LocalDate dateHeldInCustodySince = LocalDate.now().minusDays(10);
        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .build())
                                        .withAquittalDate(hearingDate)
                                        .withPreviousDaysHeldInCustody(2)
                                        .withDateHeldInCustodySince(dateHeldInCustodySince)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing, hearingDate);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), is(12));
        assertThat(offence.getDateHeldInCustodySince(), nullValue());

    }

    @Test
    public void shouldCalculateHeldInCustodyWhenDefendantWasOnBailAndNowOnBail() {
        final LocalDate hearingDay = LocalDate.now();
        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withPreviousDaysHeldInCustody(2)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing, hearingDay);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), is(2));
        assertThat(offence.getDateHeldInCustodySince(), nullValue());

    }

    @Test
    public void shouldCalculateHeldInCustodyWhenDefendantWasOnBailButNowInCustody() {
        final LocalDate hearingDay = LocalDate.now();
        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("C")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withPreviousDaysHeldInCustody(2)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing, hearingDay);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), is(2));
        assertThat(offence.getDateHeldInCustodySince(), is(hearingDay));

    }

    @Test
    public void shouldNotCalculateHeldInCustodyWhenBailStatusIsNull() {
        final LocalDate hearingDay = LocalDate.now();
        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .build()))
                                .build()))
                        .build()))
                .build();

        target.calculateDateHeldInCustody(hearing, hearingDay);

        final Offence offence = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        assertThat(offence.getPreviousDaysHeldInCustody(), nullValue());
        assertThat(offence.getDateHeldInCustodySince(), nullValue());

    }

    @Test
    public void shouldNotCalculateForUnknownRemandStatuses() {

        final int daysSpentIn = 34;
        final LocalDate timeLimitIn = LocalDate.of(2020, 7, 24);
        final JsonObject hearingResultedPayload = new StringToJsonObjectConverter().convert(FileUtil.getPayload("hearing.with.custodytimelimit-invalid-remand-status.json"));
        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(hearingResultedPayload, PublicHearingResulted.class);

        target.calculate(publicHearingResulted.getHearing());

        final Defendant defendant1 = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        final Defendant defendant2 = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(1);

        assertThat(null, is(defendant1.getOffences().get(0).getCustodyTimeLimit()));
        assertThat(null, is(defendant1.getOffences().get(1).getCustodyTimeLimit()));

        assertThat(null, is(defendant2.getOffences().get(0).getCustodyTimeLimit()));
        assertThat(null, is(defendant2.getOffences().get(1).getCustodyTimeLimit()));
    }

    @Test
    public void shouldNotUpdateOffenceCustodyTimeLimitedWhenThereAreNoCustodyTimeLimitedExtensionResultDefinitionPresent() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Target target1 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(Arrays.asList(Offence.offence()
                                        .withId(randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target1, target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit(), nullValue());

    }

    @Test
    public void shouldUpdateOffenceCustodyTimeLimitedWhenCustodyTimeLimitedExtensionResultDefinitionPresent() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID ctleResultDefinitionId = UUID.fromString("68737dc2-8d10-45e0-8bc1-21a523100fa2");
        final UUID ctlePromptId = UUID.fromString("3c915c3b-57d8-45f4-972e-a5d2b5f91bfa");
        final String extendedCtlDate = "2021-05-31";

        final Target target1 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(ctleResultDefinitionId)
                        .withPrompts(Arrays.asList(Prompt.prompt()
                                .withId(ctlePromptId)
                                .withValue(extendedCtlDate)
                                .build()))
                        .build()))
                .build();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(Arrays.asList(Offence.offence()
                                        .withId(offenceId)
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target1, target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getTimeLimit().toString(), is(extendedCtlDate));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getIsCtlExtended(), is(true));

    }

    @Test
    public void shouldNotUpdateOffenceCustodyTimeLimitedWhenCustodyTimeLimitedExtensionResultDefinitionPresentForDifferentOffence() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID ctleResultDefinitionId = UUID.fromString("68737dc2-8d10-45e0-8bc1-21a523100fa2");
        final UUID ctlePromptId = UUID.fromString("3c915c3b-57d8-45f4-972e-a5d2b5f91bfa");
        final String extendedCtlDate = "2021-05-31";

        final Target target1 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(ctleResultDefinitionId)
                        .withPrompts(Arrays.asList(Prompt.prompt()
                                .withId(ctlePromptId)
                                .withValue(extendedCtlDate)
                                .build()))
                        .build()))
                .build();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(Arrays.asList(Offence.offence()
                                        .withId(randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target1, target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit(), nullValue());

    }

    @Test
    public void shouldUpdateCorrectOffenceCustodyTimeLimitedWhenCustodyTimeLimitedExtensionResultDefinitionPresentForTheOffence() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID ctleResultDefinitionId = UUID.fromString("68737dc2-8d10-45e0-8bc1-21a523100fa2");
        final UUID ctlePromptId = UUID.fromString("3c915c3b-57d8-45f4-972e-a5d2b5f91bfa");
        final String extendedCtlDate = "2021-05-31";

        final Target target1 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId1)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(ctleResultDefinitionId)
                        .withPrompts(Arrays.asList(Prompt.prompt()
                                .withId(ctlePromptId)
                                .withValue(extendedCtlDate)
                                .build()))
                        .build()))
                .build();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId1)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(
                                        Arrays.asList(
                                                Offence.offence()
                                                        .withId(offenceId1)
                                                        .build(),
                                                Offence.offence()
                                                        .withId(offenceId2)
                                                        .build()
                                        ))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target1, target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit(), notNullValue());
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getIsCtlExtended(), is(true));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getTimeLimit(), is(LocalDate.parse(extendedCtlDate)));

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getCustodyTimeLimit(), nullValue());

    }

    @Test
    public void shouldUpdateAllOffencesCustodyTimeLimitWhenCustodyTimeLimitExtensionResultDefinitionPresentForAllOffences() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID ctleResultDefinitionId = UUID.fromString("68737dc2-8d10-45e0-8bc1-21a523100fa2");
        final UUID ctlePromptId = UUID.fromString("3c915c3b-57d8-45f4-972e-a5d2b5f91bfa");
        final String extendedCtlDate1 = "2021-05-31";
        final String extendedCtlDate2 = "2021-07-31";

        final Target target1 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId1)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(ctleResultDefinitionId)
                        .withPrompts(Arrays.asList(Prompt.prompt()
                                .withId(ctlePromptId)
                                .withValue(extendedCtlDate1)
                                .build()))
                        .build()))
                .build();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId2)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(ctleResultDefinitionId)
                        .withPrompts(Arrays.asList(Prompt.prompt()
                                .withId(ctlePromptId)
                                .withValue(extendedCtlDate2)
                                .build()))
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(
                                        Arrays.asList(
                                                Offence.offence()
                                                        .withId(offenceId1)
                                                        .build(),
                                                Offence.offence()
                                                        .withId(offenceId2)
                                                        .build()
                                        ))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target1, target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit(), notNullValue());
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getIsCtlExtended(), is(true));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getTimeLimit(), is(LocalDate.parse(extendedCtlDate1)));

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getCustodyTimeLimit(), notNullValue());
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getCustodyTimeLimit().getIsCtlExtended(), is(true));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getCustodyTimeLimit().getTimeLimit(), is(LocalDate.parse(extendedCtlDate2)));

    }

    @Test
    public void shouldOverrideOffenceCustodyTimeLimitedWhenCustodyTimeLimitedExtensionResultDefinitionPresent() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID ctleResultDefinitionId = UUID.fromString("68737dc2-8d10-45e0-8bc1-21a523100fa2");
        final UUID ctlePromptId = UUID.fromString("3c915c3b-57d8-45f4-972e-a5d2b5f91bfa");
        final String extendedCtlDate = "2021-05-31";

        final Target target1 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(ctleResultDefinitionId)
                        .withPrompts(Arrays.asList(Prompt.prompt()
                                .withId(ctlePromptId)
                                .withValue(extendedCtlDate)
                                .build()))
                        .build()))
                .build();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(Arrays.asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(
                                                CustodyTimeLimit.custodyTimeLimit()
                                                        .withIsCtlExtended(false)
                                                        .withTimeLimit(LocalDate.now())
                                                        .build()
                                        )
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target1, target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getTimeLimit().toString(), is(extendedCtlDate));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getIsCtlExtended(), is(true));

    }

    @Test
    public void shouldNotOverrideOffenceCustodyTimeLimitedWhenNoCustodyTimeLimitedExtensionResultDefinitionPresent() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Target target2 = Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withResultLines(Arrays.asList(ResultLine.resultLine()
                        .withResultDefinitionId(randomUUID())
                        .build()))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withDefendants(Arrays.asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(Arrays.asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(
                                                CustodyTimeLimit.custodyTimeLimit()
                                                        .withIsCtlExtended(false)
                                                        .withTimeLimit(LocalDate.now())
                                                        .build()
                                        )
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withHearing(hearing)
                .withTargets(Arrays.asList(target2))
                .build();

        target.updateExtendedCustodyTimeLimit(resultsSharedV2);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getTimeLimit(), is(LocalDate.now()));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit().getIsCtlExtended(), is(false));

    }

}
