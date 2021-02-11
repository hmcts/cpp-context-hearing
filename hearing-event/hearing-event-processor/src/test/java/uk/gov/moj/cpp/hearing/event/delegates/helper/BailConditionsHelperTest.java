package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.System.lineSeparator;
import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.justice.core.courts.PersonDefendant.personDefendant;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared.builder;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BailConditionsHelperTest {

    final BailConditionsHelper bailConditionsHelper = new BailConditionsHelper();

    @Test
    public void testMapBailConditions() {
        final String bailCondition = "Bail condition: Time of hearing" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Bail condition: Courtroom" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Bail condition: Courthouse name" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator();

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testNotMapBailStatusReason() {
        final ResultsShared resultsSharedTemplate = buildOffenceWithoutResultsSharedTemplate();
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);
        final String bailReasonResult = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailReasons();
        assertNull(bailReasonResult);
    }

    @Test
    public void testMapBailConditions_when_resultDefinitionGroup_isNotPresent() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setResultDefinitionGroupAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresent() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithoutPersonDefendant();
        setResultDefinitionGroupAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        assertNull(resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant());
    }

    @Test
    public void testMapBailConditions_when_rank_isNotPresent() {
        final String bailCondition = "Bail condition: Courthouse name" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Bail condition: Courtroom" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Bail condition: Time of hearing" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator();

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setRankAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_promptSequence_isNotPresent() {
        final String bailCondition = "Bail condition: Time of hearing" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Bail condition: Courtroom" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Bail condition: Courthouse name" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator();

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setPromptSequenceAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_judicialResultPrompts_areNotPresent() {
        final String bailCondition = "Bail condition: Time of hearing" + lineSeparator() +
                "Bail condition: Courtroom" + lineSeparator() +
                "Bail condition: Courthouse name" + lineSeparator();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setJudicialResultPromptsAreNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when__rank_is_equal_and_null() {
        final String bailCondition = "Bail condition: Time of hearing" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                //System.lineSeparator() +
                "Bail condition: Courthouse name" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator() +
                //System.lineSeparator() +
                "Bail condition: Courtroom" + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                "Date of hearing : 777" + lineSeparator() +
                "Courtroom : 555" + lineSeparator() +
                "Courthouse name : 666" + lineSeparator();

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setRankAsEqualAndNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void shouldMapBailCondition_blank_when_bailStatus_custody() {
        assertBailConditionIsBlank("C");
        assertBailConditionIsBlank("U");
        assertBailConditionIsBlank("A");
        assertBailConditionIsBlank("S");
    }

    private void assertBailConditionIsBlank(final String bailStatus) {
        final String bailCondition = "";

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(bailStatus);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void shouldMapBailCondition_blank_when_not_a_bailConditions() {
        final String bailCondition = "";

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("L","Not a Bail Conditions");
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(bailCondition));
    }

    private ResultsShared buildOffenceWithoutResultsSharedTemplate() {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(singletonList(prosecutionCase()
                                .withDefendants(singletonList(defendant()
                                        .withOffences(asList(offence().build(), offence().build()))
                                        .withPersonDefendant(personDefendant().build())
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplate() {
        return buildResultsSharedTemplate("P");
    }

    private ResultsShared buildResultsSharedTemplate(final String bailStatus) {
        return buildResultsSharedTemplate(bailStatus, "Bail Conditions");
    }

    private ResultsShared buildResultsSharedTemplate(final String bailStatus, final String resultDefinitionGroup) {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(singletonList(prosecutionCase()
                                .withDefendants(singletonList(defendant()
                                        .withOffences(asList(offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withLabel("Bail condition: Courthouse name")
                                                        .withResultDefinitionGroup(resultDefinitionGroup)
                                                        .withRank(new BigDecimal(4))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build(), judicialResult()
                                                        .withResultDefinitionGroup(resultDefinitionGroup)
                                                        .withLabel("Bail condition: Courtroom")
                                                        .withRank(new BigDecimal(3))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build()))
                                                .build(), offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                                        .withLabel("Bail condition: Date of hearing")
                                                        .withRank(new BigDecimal(2))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build(), judicialResult()
                                                        .withResultDefinitionGroup(resultDefinitionGroup)
                                                        .withLabel("Bail condition: Time of hearing")
                                                        .withRank(new BigDecimal(1))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build()))
                                                .build(), offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                                        .withLabel("Bail condition: Date of hearing")
                                                        .withRank(new BigDecimal(2))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build(), judicialResult()
                                                        .withResultDefinitionGroup(resultDefinitionGroup)
                                                        .withLabel("Bail condition: Time of hearing")
                                                        .withRank(new BigDecimal(1))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build()))
                                                .build()))
                                        .withPersonDefendant(buildPersonDefendant(bailStatus))
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }

    private PersonDefendant buildPersonDefendant(final String bailStatus) {
        if (null == bailStatus) {
            return personDefendant().build();
        }
        return personDefendant().withBailStatus(BailStatus.bailStatus().withCode(bailStatus).build()).build();
    }

    private ResultsShared buildResultsSharedTemplateWithoutPersonDefendant() {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(singletonList(prosecutionCase()
                                .withDefendants(singletonList(defendant()
                                        .withOffences(asList(offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withLabel("Bail condition: Courthouse name")
                                                        .withResultDefinitionGroup("Bail Conditions")
                                                        .withRank(new BigDecimal(4))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build(), judicialResult()
                                                        .withResultDefinitionGroup("Bail Conditions")
                                                        .withLabel("Bail condition: Courtroom")
                                                        .withRank(new BigDecimal(3))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build()))
                                                .build(), offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                                        .withLabel("Bail condition: Date of hearing")
                                                        .withRank(new BigDecimal(2))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build(), judicialResult()
                                                        .withResultDefinitionGroup("Bail Conditions")
                                                        .withLabel("Bail condition: Time of hearing")
                                                        .withRank(new BigDecimal(1))
                                                        .withJudicialResultPrompts(getJudicialResultPromptList())
                                                        .build()))
                                                .build(), offence()
                                                        .withJudicialResults(asList(judicialResult()
                                                                .withResultDefinitionGroup("Not a Bail Conditions")
                                                                .withLabel("Bail condition: Date of hearing")
                                                                .withRank(new BigDecimal(2))
                                                                .withJudicialResultPrompts(getJudicialResultPromptList())
                                                                .build(), judicialResult()
                                                                .withResultDefinitionGroup("Bail Conditions")
                                                                .withLabel("Bail condition: Time of hearing")
                                                                .withRank(new BigDecimal(1))
                                                                .withJudicialResultPrompts(getJudicialResultPromptList())
                                                                .build()))
                                                        .build()))
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }


    private List<JudicialResultPrompt> getJudicialResultPromptList() {
        List<JudicialResultPrompt> list = new ArrayList<>();
        list.add(judicialResultPrompt().withValue("999").withLabel("Time of hearing").withPromptSequence(new BigDecimal(1)).build());
        list.add(judicialResultPrompt().withValue("777").withLabel("Date of hearing").withPromptSequence(new BigDecimal(2)).build());
        list.add(judicialResultPrompt().withValue("555").withLabel("Courtroom").withPromptSequence(new BigDecimal(3)).build());
        list.add(judicialResultPrompt().withValue("666").withLabel("Courthouse name").withPromptSequence(new BigDecimal(4)).build());
        return list;
    }

    private void setPromptSequenceAsNull(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getJudicialResultPrompts().get(0).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).getJudicialResultPrompts().get(1).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).getJudicialResultPrompts().get(0).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).getJudicialResultPrompts().get(1).setPromptSequence(null);
    }

    private void setJudicialResultPromptsAreNull(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).setJudicialResultPrompts(null);
    }

    private void setRankAsEqualAndNull(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).setRank(new BigDecimal(1));
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).setRank(new BigDecimal(1));
    }

    private void setRankAsNull(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(1).setRank(null);
    }

    private void setResultDefinitionGroupAsNull(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(1).setResultDefinitionGroup(null);
    }
}
