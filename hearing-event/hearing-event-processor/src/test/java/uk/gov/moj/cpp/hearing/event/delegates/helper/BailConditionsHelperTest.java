package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.System.lineSeparator;
import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
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
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditionsWithNoPrompts() {
        final String bailCondition = getBailConditionWithNoPrompt("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithNoPrompt();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditionsWithCourtApplicationCases() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditionsWithCourtApplicationCourtOrder() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testNotMapBailStatusReason() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", true, true, false, false, false, true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);
        final String bailReasonResult = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailReasons();
        assertNull(bailReasonResult);
    }

    @Test
    public void testNotMapBailStatusReasonWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", false, true, false, true, false, true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);
        final String bailReasonResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNull(bailReasonResult);
    }

    @Test
    public void testNotMapBailStatusReasonWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", false, true, false, false, true, true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);
        final String bailReasonResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNull(bailReasonResult);
    }

    @Test
    public void testMapBailConditions_when_resultDefinitionGroup_isNotPresent() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setResultDefinitionGroupAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
    }

    @Test
    public void testMapBailConditions_when_resultDefinitionGroup_isNotPresentWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        setResultDefinitionGroupAsNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult =  resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
    }

    @Test
    public void testMapBailConditions_when_resultDefinitionGroup_isNotPresentWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        setResultDefinitionGroupAsNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult =  resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresent() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", true, false, true, false, false, true);
        setResultDefinitionGroupAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        assertNull(resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant());
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresentWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", false, false, true, true, false, true);
        setResultDefinitionGroupAsNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        assertNull(resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant());
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresentWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", false, false, true, false, true, true);
        setResultDefinitionGroupAsNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        assertNull(resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant());
    }

    @Test
    public void testMapBailConditions_when_rank_isNotPresent() {
        final String bailCondition = getBailCondition("Bail condition: Courthouse name", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Time of hearing", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setRankAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditionsWhenMultipleResultDefinitionGroups() {
        final String bailCondition = getBailCondition("Bail condition: Courthouse name", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Time of hearing", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("P", "Bail conditions,ELMON", true, true, true, false, false, true);
        setRankAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditionsWhenMultipleResultDefinitionGroupsWithSpaceBetweenGroupDefinitionName() {
        final String bailCondition = getBailCondition("Bail condition: Courthouse name", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Time of hearing", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("P", "ELMON, Bail conditions", true, true, true, false, false, true);
        setRankAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_rank_isNotPresentForCourtApplicationCases() {
        final String bailCondition = getBailCondition("Bail condition: Courthouse name", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Time of hearing", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        setRankAsNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_rank_isNotPresentForCourtApplicationCourtOrder() {
        final String bailCondition = getBailCondition("Bail condition: Courthouse name", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courtroom", "Bail condition: Time of hearing", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        setRankAsNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_promptSequence_isNotPresent() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Courtroom : 555", "Courthouse name : 666", "Date of hearing : 777", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Date of hearing : 777", "Time of hearing : 999");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setPromptSequenceAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_promptSequence_isNotPresentWithCourtApplicationCases() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Courtroom : 555", "Courthouse name : 666", "Date of hearing : 777", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Date of hearing : 777", "Time of hearing : 999");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        setPromptSequenceAsNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_promptSequence_isNotPresentWithCourtApplicationCourtOrder() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Courtroom : 555", "Courthouse name : 666", "Date of hearing : 777", "Bail condition: Courtroom", "Bail condition: Courthouse name", "Date of hearing : 777", "Time of hearing : 999");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        setPromptSequenceAsNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }


    @Test
    public void testMapBailConditions_when_judicialResultPrompts_areNotPresent() {
        final String bailCondition = "Bail condition: Time of hearing" + " ;" +
                "Bail condition: Courtroom" + " ;" +
                "Bail condition: Courthouse name";
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setJudicialResultPromptsAreNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_judicialResultPrompts_areNotPresentWithCourtApplicationCases() {
        final String bailCondition = "Bail condition: Time of hearing" + " ;" +
                "Bail condition: Courtroom" + " ;" +
                "Bail condition: Courthouse name";
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        setJudicialResultPromptsAreNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_judicialResultPrompts_areNotPresentWithCourtApplicationCourtOrder() {
        final String bailCondition = "Bail condition: Time of hearing" + " ;" +
                "Bail condition: Courtroom" + " ;" +
                "Bail condition: Courthouse name";
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        setJudicialResultPromptsAreNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when__rank_is_equal_and_null() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courthouse name", "Bail condition: Courtroom", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setRankAsEqualAndNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when__rank_is_equal_and_nullWithCourtApplicationCases() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courthouse name", "Bail condition: Courtroom", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        setRankAsEqualAndNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when__rank_is_equal_and_nullWithCourtApplicationCourtOrder() {
        final String bailCondition = getBailCondition("Bail condition: Time of hearing", "Date of hearing : 777", "Courtroom : 555", "Courthouse name : 666", "Bail condition: Courthouse name", "Bail condition: Courtroom", "Time of hearing : 999", "Courthouse name : 666");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        setRankAsEqualAndNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
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
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void shouldMapBailCondition_blank_when_not_a_bailConditions() {
        final String bailCondition = "";

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("L","Not a Bail Conditions", true, true, true, false, false, true);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(bailCondition));
    }

    private ResultsShared buildResultsSharedTemplate() {
        return buildResultsSharedTemplate("P", "Bail Conditions", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithNoPrompt() {
        return buildResultsSharedTemplate("P", "Bail Conditions", true, true, true, false, false, false);
    }

    private ResultsShared buildResultsSharedTemplateWithCourtApplicationCases() {
        return buildResultsSharedTemplate("P", "Bail Conditions", false, true, true, true, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithCourtApplicationCourtOrder() {
        return buildResultsSharedTemplate("P", "Bail Conditions", false, true, true, false, true, true);
    }

    private ResultsShared buildResultsSharedTemplate(final String bailStatus) {
        return buildResultsSharedTemplate(bailStatus, "Bail Conditions", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplate(final String bailStatus, final String resultDefinitionGroup, final boolean withProsecutionCases, final boolean withPersonDefendant, final boolean withResults, final boolean withCourtApplicationCases, final boolean withCourtApplicationCourtOrder, final boolean withPrompt) {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(withProsecutionCases ? getProsecutionCases(bailStatus, resultDefinitionGroup, withPersonDefendant, withResults, withPrompt) : null)
                        .withCourtApplications(withCourtApplicationCases || withCourtApplicationCourtOrder ? getCourtApplication(bailStatus, resultDefinitionGroup, withPersonDefendant, withResults, withCourtApplicationCases, withCourtApplicationCourtOrder): null)
                        .build())
                .build();
    }

    private List<ProsecutionCase> getProsecutionCases(final String bailStatus, final String resultDefinitionGroup, final boolean withPersonDefendant, final boolean withResults, final boolean withPrompt) {
        return singletonList(prosecutionCase()
                .withDefendants(singletonList(defendant()
                        .withOffences(asList(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                        .withLabel("Bail condition: Courthouse name")
                                        .withResultDefinitionGroup(resultDefinitionGroup), 4, "Bail condition: Courtroom", 3, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, withPrompt) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build()))
                        .withPersonDefendant(withPersonDefendant ? buildPersonDefendant(bailStatus) : null)
                        .build())
                )
                .build());
    }

    private List<CourtApplication> getCourtApplication(final String bailStatus, final String resultDefinitionGroup, final boolean withPersonDefendant, final boolean withResults, final boolean withCourtApplicationCases, final boolean withCourtApplicationCourtOrder) {
        return singletonList(CourtApplication.courtApplication()
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withPersonDefendant(withPersonDefendant ? buildPersonDefendant(bailStatus) : null)
                                .build())
                        .build())
                .withCourtApplicationCases(withCourtApplicationCases ? getCourtApplicationCases(resultDefinitionGroup, withResults) : null)
                .withCourtOrder(withCourtApplicationCourtOrder ? getCourtApplicationCourtOrder(resultDefinitionGroup, withResults): null)
                .build());
    }

    private List<CourtApplicationCase> getCourtApplicationCases(final String resultDefinitionGroup, final boolean withResults) {
        return singletonList(CourtApplicationCase.courtApplicationCase()
                .withOffences(asList(offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                .withLabel("Bail condition: Courthouse name")
                                .withResultDefinitionGroup(resultDefinitionGroup), 4, "Bail condition: Courtroom", 3, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                .withResultDefinitionGroup("Not a Bail Conditions")
                                .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                        .build()))
                .withCaseStatus("ACTIVE")
                .build());
    }

    private CourtOrder getCourtApplicationCourtOrder(final String resultDefinitionGroup, final boolean withResults) {
        return CourtOrder.courtOrder()
                .withCourtOrderOffences(asList(CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                        .withLabel("Bail condition: Courthouse name")
                                        .withResultDefinitionGroup(resultDefinitionGroup), 4, "Bail condition: Courtroom", 3, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup, judicialResult()
                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build()))
                .build();
    }

    private List<JudicialResult> getJudicialResults(final String resultDefinitionGroup, final JudicialResult.Builder builder, final int i, final String s, final int i2, final boolean withPrompt) {
        return asList(builder
                .withRank(new BigDecimal(i))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .build(), judicialResult()
                .withResultDefinitionGroup(resultDefinitionGroup)
                .withLabel(s)
                .withRank(new BigDecimal(i2))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .build());
    }

    private PersonDefendant buildPersonDefendant(final String bailStatus) {
        if (null == bailStatus) {
            return personDefendant().build();
        }
        return personDefendant().withBailStatus(BailStatus.bailStatus().withCode(bailStatus).build()).build();
    }

    private List<JudicialResultPrompt> getJudicialResultPromptList(final boolean withPrompt) {
        if(!withPrompt){
            return emptyList();
        }
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

    private void setPromptSequenceAsNullForCourtApplicationCases(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(0).getJudicialResultPrompts().get(0).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(1).getJudicialResultPrompts().get(1).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(0).getJudicialResultPrompts().get(0).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(1).getJudicialResultPrompts().get(1).setPromptSequence(null);
    }

    private void setPromptSequenceAsNullForCourtApplicationCourtOrder(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(0).getJudicialResultPrompts().get(0).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(1).getJudicialResultPrompts().get(1).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(0).getJudicialResultPrompts().get(0).setPromptSequence(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(1).getJudicialResultPrompts().get(1).setPromptSequence(null);
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

    private void setJudicialResultPromptsAreNullForCourtApplicationCases(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(0).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(1).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(0).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(1).setJudicialResultPrompts(null);
    }

    private void setJudicialResultPromptsAreNullForCourtApplicationCourtOrder(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(0).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(1).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(0).setJudicialResultPrompts(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(1).setJudicialResultPrompts(null);
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

    private void setRankAsEqualAndNullForCourtApplicationCases(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(0).setRank(new BigDecimal(1));
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(1).setRank(new BigDecimal(1));
    }

    private void setRankAsEqualAndNullForCourtApplicationCourtOrder(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(0).setRank(new BigDecimal(1));
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(1).setRank(new BigDecimal(1));
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

    private void setRankAsNullForCourtApplicationCases(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(1).setRank(null);
    }

    private void setRankAsNullForCourtApplicationCourtOrder(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(1).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(0).setRank(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(1).setRank(null);
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

    private void setResultDefinitionGroupAsNullForCourtApplicationCases(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(1).getJudicialResults().get(1).setResultDefinitionGroup(null);
    }

    private void setResultDefinitionGroupAsNullForCourtApplicationCourtOrder(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
    }

    private String getBailCondition(final String s, final String s2, final String s3, final String s4, final String s5, final String s6, final String s7, final String s8) {
        return s + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                s2 + lineSeparator() +
                s3 + lineSeparator() +
                s4 + " ;" +
                s5 + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                s2 + lineSeparator() +
                s3 + lineSeparator() +
                s4 + " ;" +
                s6 + lineSeparator() +
                s7 + lineSeparator() +
                s2 + lineSeparator() +
                s3 + lineSeparator() +
                s8;
    }

    private String getBailConditionWithNoPrompt(final String s, final String s2, final String s3, final String s4, final String s5, final String s6, final String s7, final String s8) {
        return s + " ;" +
                s5 + lineSeparator() +
                "Time of hearing : 999" + lineSeparator() +
                s2 + lineSeparator() +
                s3 + lineSeparator() +
                s4 + " ;" +
                s6 + lineSeparator() +
                s7 + lineSeparator() +
                s2 + lineSeparator() +
                s3 + lineSeparator() +
                s8;
    }
}
