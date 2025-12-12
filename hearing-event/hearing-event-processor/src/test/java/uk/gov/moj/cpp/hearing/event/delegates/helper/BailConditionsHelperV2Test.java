package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import org.junit.jupiter.api.Test;

public class BailConditionsHelperV2Test {

    final BailConditionsHelperV2 bailConditionsHelper = new BailConditionsHelperV2();

    @Test
    public void testMapBailConditionsWithSuffix() {
        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00 Surety - find a surety In the sum of 2000.00 Surety - find a surety In the sum of 3000.00 Surety - find a surety In the sum of 4000.00 Surety - find a surety In the sum of 5000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", "");
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithOnlyBailConditionsSuffix();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);

        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testDistinctWithAndWithoutSuffixForBailConditionsResultDefintionGroup() {
        final String bailCondition = getBailCondition("Bail Condition : Some condition 1",
                "Some condition 2",
                "Some condition 3",
                "Bail Condition : Some condition 4",
                "Some condition 5", "", "");
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithOnlyBailConditions();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);

        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testDistinctWithAndWithoutSuffixForAllResultDefintionGroups() {
        final String bailCondition = getBailCondition("Pre Some condition 2",
                "Some condition 1",
                "Some condition 3",
                "Some condition 4, ELMON",
                "Some condition 5 ,ELMON", "Some condition 2", "Some ConditionImposedOndefendent condition 3");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithAllBailConditions();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);

        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapAllBailConditionsWithSuffixes() {
        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00 Surety - find a surety In the sum of 2000.00 Surety - find a surety In the sum of 3000.00 Surety - find a surety In the sum of 4000.00 Surety - find a surety In the sum of 5000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", "");
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithAllSuffixes();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);

        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditionsConsideringDuplicates() {
        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00 Surety - find a surety In the sum of 2000.00 Surety - find a surety In the sum of 3000.00 Surety - find a surety In the sum of 4000.00 Surety - find a surety In the sum of 5000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", "");
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);

        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditionsWithCourtApplicationCases() {
        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", ""
        );

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);

        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditionsWithCourtApplicationCourtOrder() {
        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", "");

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testNotMapBailStatusReason() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, true, false, false, false, true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);
        final String bailReasonResult = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailReasons();
        assertNull(bailReasonResult);
    }

    @Test
    public void testNotMapBailStatusReasonWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", false, true, false, true, false, true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);
        final String bailReasonResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNull(bailReasonResult);
    }

    @Test
    public void testNotMapBailStatusReasonWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", false, true, false, false, true, true);
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

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
    }

    @Test
    public void testMapBailConditions_when_resultDefinitionGroup_isNotPresentWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder();
        setResultDefinitionGroupAsNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresent() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, false, true, false, false, true);
        setResultDefinitionGroupAsNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        assertNull(resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant());
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresentWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", false, false, true, true, false, true);
        setResultDefinitionGroupAsNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        assertNull(resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant());
    }

    @Test
    public void shouldMapBailConditionsWhenPersonDefendantIsNotPresentWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate(null, "Bail Conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", false, false, true, false, true, true);
        setResultDefinitionGroupAsNullForCourtApplicationCourtOrder(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        assertNull(resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant());
    }

    @Test
    public void testMapBailConditionsWhenMultipleResultDefinitionGroupsWithResultTexts() {

        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00 Surety - find a surety In the sum of 2000.00 Surety - find a surety In the sum of 3000.00 Surety - find a surety In the sum of 4000.00 Surety - find a surety In the sum of 5000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", ""
        );

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("P", "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, true, true, false, false, true);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));
    }

    @Test
    public void testMapBailConditions_when_promptSequence_isNotPresentWithCourtApplicationCases() {
        final String bailCondition = getBailCondition("Pre-release bail condition: Surety - find a surety In the sum of 1000.00",
                "Pre-release bail condition: Security - lodge with Court Item Security - logdge test",
                "Exclusion - not to sit in the front seat of any motor vehicle",
                "Residence - not to live in the same household As Tes",
                "Must not sit in the front seat of any motor vehicle",
                "Must not go within A radius of 10 miles In respect of this place oxford", ""
        );

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCases();
        setPromptSequenceAsNullForCourtApplicationCases(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailConditions();
        assertNotNull(bailConditionsResult);
        assertThat(bailConditionsResult, is(bailCondition));

    }

    @Test
    public void testMapBailConditions_when_judicialResultTexts_areNotPresent() {

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        setResultTextsAreNull(resultsSharedTemplate);

        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();

        assertThat(bailConditionsResult, is(isEmptyOrNullString()));
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

        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("L", "Not a Bail Conditions", "", "", true, true, true, false, false, true);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate);
        bailConditionsHelper.setBailConditions(resultsSharedTemplate.getHearing());

        final String bailConditionsResult = resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailConditions();
        assertThat(bailConditionsResult, is(bailCondition));
    }

    private ResultsShared buildResultsSharedTemplate() {
        return buildResultsSharedTemplate("P", "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithOnlyBailConditionsSuffix() {
        return buildResultsSharedTemplate("P", "Bail conditions, ELEMON", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithOnlyBailConditions() {
        return buildResultsSharedTemplateV1("P", "Bail conditions", "Bail conditions, ELEMON", null, true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithAllBailConditions() {
        return buildResultsSharedTemplateV2("P", "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", "Bail conditions, ELEMON", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithAllSuffixes() {
        return buildResultsSharedTemplate("P", "Bail conditions, ELEMON", "Pre-release bail conditions, ELEMON", "ConditionsImposedOnDefendant, ELEMON", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithNoPrompt() {
        return buildResultsSharedTemplate("P", "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, true, true, false, false, false);
    }

    private ResultsShared buildResultsSharedTemplateWithCourtApplicationCases() {
        return buildResultsSharedTemplate("P", "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", false, true, true, true, false, true);
    }

    private ResultsShared buildResultsSharedTemplateWithCourtApplicationCourtOrder() {
        return buildResultsSharedTemplate("P", "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", false, true, true, false, true, true);
    }

    private ResultsShared buildResultsSharedTemplate(final String bailStatus) {
        return buildResultsSharedTemplate(bailStatus, "Bail conditions", "Pre-release bail conditions", "ConditionsImposedOnDefendant", true, true, true, false, false, true);
    }

    private ResultsShared buildResultsSharedTemplate(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withProsecutionCases, final boolean withPersonDefendant, final boolean withResults, final boolean withCourtApplicationCases, final boolean withCourtApplicationCourtOrder, final boolean withPrompt) {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(withProsecutionCases ? getProsecutionCases(bailStatus, resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withPersonDefendant, withResults, withPrompt) : null)
                        .withCourtApplications(withCourtApplicationCases || withCourtApplicationCourtOrder ? getCourtApplication(bailStatus, resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withPersonDefendant, withResults, withCourtApplicationCases, withCourtApplicationCourtOrder) : null)
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateV1(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withProsecutionCases, final boolean withPersonDefendant, final boolean withResults, final boolean withCourtApplicationCases, final boolean withCourtApplicationCourtOrder, final boolean withPrompt) {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(withProsecutionCases ? getProsecutionCasesV1(bailStatus, resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withPersonDefendant, withResults, withPrompt) : null)
                        .withCourtApplications(withCourtApplicationCases || withCourtApplicationCourtOrder ? getCourtApplication(bailStatus, resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withPersonDefendant, withResults, withCourtApplicationCases, withCourtApplicationCourtOrder) : null)
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateV2(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final String resultDefinitionGroup4, final boolean withProsecutionCases, final boolean withPersonDefendant, final boolean withResults, final boolean withCourtApplicationCases, final boolean withCourtApplicationCourtOrder, final boolean withPrompt) {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), systemDefault()))
                                .build(), hearingDay()
                                .withSittingDay(of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), systemDefault()))
                                .build()))
                        .withProsecutionCases(withProsecutionCases ? getProsecutionCasesV2(bailStatus, resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, resultDefinitionGroup4, withPersonDefendant, withResults, withPrompt) : null)
                        .withCourtApplications(withCourtApplicationCases || withCourtApplicationCourtOrder ? getCourtApplication(bailStatus, resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withPersonDefendant, withResults, withCourtApplicationCases, withCourtApplicationCourtOrder) : null)
                        .build())
                .build();
    }

    private List<ProsecutionCase> getProsecutionCases(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withPersonDefendant, final boolean withResults, final boolean withPrompt) {
        return singletonList(prosecutionCase()
                .withDefendants(singletonList(defendant()
                        .withOffences(asList(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withLabel("Bail condition: Courthouse name")
                                        .withResultText("Exclusion - not to sit in the front seat of any motor vehicle")
                                        .withResultDefinitionGroup(resultDefinitionGroup1), 4, "Bail condition: Courtroom", 3, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup1)
                                        .withResultText("Residence - not to live in the same household As Tes")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, withPrompt) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup1)
                                        .withResultText("Residence - not to live in the same household As Tes")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, withPrompt) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup1)
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup2, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup2)
                                        .withResultText("Pre-release bail condition: Surety - find a surety In the sum of 1000.00 Surety - find a surety In the sum of 2000.00 Surety - find a surety In the sum of 3000.00 Surety - find a surety In the sum of 4000.00 Surety - find a surety In the sum of 5000.00")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup2, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup2)
                                        .withResultText("Pre-release bail condition: Security - lodge with Court Item Security - logdge test")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup3, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup3)
                                        .withResultText("Must not sit in the front seat of any motor vehicle")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup3, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup3)
                                        .withResultText("Must not go within A radius of 10 miles In respect of this place oxford")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? getJudicialResults("Next Hearing", judicialResult()
                                        .withResultDefinitionGroup("Next Hearing")
                                        .withLabel("Bail condition: Next Hearing"), 2, "Bail condition: Next Hearing", 1, true) : null)
                                .build()))
                        .withPersonDefendant(withPersonDefendant ? buildPersonDefendant(bailStatus) : null)
                        .build())
                )
                .build());
    }


    private List<ProsecutionCase> getProsecutionCasesV1(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withPersonDefendant, final boolean withResults, final boolean withPrompt) {
        return singletonList(prosecutionCase()
                .withDefendants(singletonList(defendant()
                        .withOffences(asList(offence()
                                .withJudicialResults(getJudicialResultsV1(resultDefinitionGroup1, resultDefinitionGroup1, judicialResult(), 4, "Bail condition", 3, true, "Bail Condition : Some condition 1", "Some condition 2", "Some condition 3"))
                                .build(), offence()
                                        .withJudicialResults(getJudicialResultsV1(resultDefinitionGroup2, resultDefinitionGroup1, judicialResult(), 4, "Bail condition", 3, true, "Bail Condition : Some condition 4", "Some condition 3", "Some condition 5"))
                                        .build(), offence()
                                .withJudicialResults(getJudicialResultsV1(resultDefinitionGroup2, resultDefinitionGroup1, judicialResult(), 4, "Bail condition", 3, true, "Some condition 5", "Some condition 2", "Some condition 5"))
                                .build()))
                        .withPersonDefendant(withPersonDefendant ? buildPersonDefendant(bailStatus) : null)
                        .build())
                )
                .build());
    }

    private List<ProsecutionCase> getProsecutionCasesV2(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final String resultDefinitionGroup4, final boolean withPersonDefendant, final boolean withResults, final boolean withPrompt) {
        return singletonList(prosecutionCase()
                .withDefendants(singletonList(defendant()
                        .withOffences(asList(offence()
                                .withJudicialResults(getJudicialResultsV1(resultDefinitionGroup1, resultDefinitionGroup2, judicialResult(), 4, "Bail condition", 3, true, "Some condition 1", "Some condition 3", "Pre Some condition 2"))
                                .build(), offence()
                                .withJudicialResults(getJudicialResultsV1(resultDefinitionGroup4, resultDefinitionGroup3, judicialResult(), 4, "Bail condition", 3, true, "Some condition 4, ELMON", "", "Some ConditionImposedOndefendent condition 3"))
                                .build(), offence()
                                .withJudicialResults(getJudicialResultsV1(resultDefinitionGroup4, resultDefinitionGroup1, judicialResult(), 4, "Bail condition", 3, true, "Some condition 5 ,ELMON", "", "Some condition 2"))
                                .build()))
                        .withPersonDefendant(withPersonDefendant ? buildPersonDefendant(bailStatus) : null)
                        .build())
                )
                .build());
    }

    private List<CourtApplication> getCourtApplication(final String bailStatus, final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withPersonDefendant, final boolean withResults, final boolean withCourtApplicationCases, final boolean withCourtApplicationCourtOrder) {
        return singletonList(CourtApplication.courtApplication()
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withPersonDefendant(withPersonDefendant ? buildPersonDefendant(bailStatus) : null)
                                .build())
                        .build())
                .withCourtApplicationCases(withCourtApplicationCases ? getCourtApplicationCases(resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withResults) : null)
                .withCourtOrder(withCourtApplicationCourtOrder ? getCourtApplicationCourtOrder(resultDefinitionGroup1, resultDefinitionGroup2, resultDefinitionGroup3, withResults) : null)
                .build());
    }

    private List<CourtApplicationCase> getCourtApplicationCases(final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withResults) {
        return singletonList(CourtApplicationCase.courtApplicationCase()
                .withOffences(asList(offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                .withLabel("Bail condition: Courthouse name")
                                .withResultText("Exclusion - not to sit in the front seat of any motor vehicle")
                                .withResultDefinitionGroup(resultDefinitionGroup1), 4, "Bail condition: Courtroom", 3, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                .withResultDefinitionGroup(resultDefinitionGroup1)
                                .withResultText("Residence - not to live in the same household As Tes")
                                .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                .withLabel("Bail condition: Courthouse name")
                                .withResultDefinitionGroup(resultDefinitionGroup1), 4, "Bail condition: Courtroom", 3, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                .withLabel("Bail condition: Courthouse name")
                                .withResultDefinitionGroup(resultDefinitionGroup2)
                                .withResultText("Pre-release bail condition: Surety - find a surety In the sum of 1000.00"), 4, "Bail condition: Courtroom", 3, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                .withLabel("Bail condition: Courthouse name")
                                .withResultDefinitionGroup(resultDefinitionGroup2)
                                .withResultText("Pre-release bail condition: Security - lodge with Court Item Security - logdge test"), 4, "Bail condition: Courtroom", 3, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withLabel("Bail condition: Courthouse name")
                                        .withResultDefinitionGroup(resultDefinitionGroup3)
                                        .withResultText("Must not sit in the front seat of any motor vehicle")
                                , 4, "Bail condition: Courtroom", 3, true) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                .withLabel("Bail condition: Courthouse name")
                                .withResultDefinitionGroup(resultDefinitionGroup3)
                                .withResultText("Must not go within A radius of 10 miles In respect of this place oxford"), 4, "Bail condition: Courtroom", 3, true) : null)
                        .build()))
                .withCaseStatus("ACTIVE")
                .build());
    }

    private CourtOrder getCourtApplicationCourtOrder(final String resultDefinitionGroup1, final String resultDefinitionGroup2, final String resultDefinitionGroup3, final boolean withResults) {
        return CourtOrder.courtOrder()
                .withCourtOrderOffences(asList(CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withLabel("Bail condition: Courthouse name")
                                        .withResultText("Exclusion - not to sit in the front seat of any motor vehicle")
                                        .withResultDefinitionGroup(resultDefinitionGroup1), 4, "Bail condition: Courtroom", 3, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup1)
                                        .withResultText("Residence - not to live in the same household As Tes")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup1, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup1)
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup2, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup2)
                                        .withResultText("Pre-release bail condition: Surety - find a surety In the sum of 1000.00")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup2, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup2)
                                        .withResultText("Pre-release bail condition: Security - lodge with Court Item Security - logdge test")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup3, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup3)
                                        .withResultText("Must not sit in the front seat of any motor vehicle")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults(resultDefinitionGroup3, judicialResult()
                                        .withResultDefinitionGroup(resultDefinitionGroup3)
                                        .withResultText("Must not go within A radius of 10 miles In respect of this place oxford")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults("Not a Bail Conditions", judicialResult()
                                        .withResultDefinitionGroup("Not a Bail Conditions")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults("Next Hearing", judicialResult()
                                        .withResultDefinitionGroup("Next Hearing")
                                        .withLabel("Bail condition: Next Hearing"), 2, "Bail condition: Next Hearing", 1, true) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? getJudicialResults("Not a Bail Conditions1", judicialResult()
                                        .withResultDefinitionGroup("Not a Bail Conditions1")
                                        .withLabel("Bail condition: Date of hearing"), 2, "Bail condition: Time of hearing", 1, true) : null)
                                .build())
                        .build()))
                .build();


        //add data
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

    private List<JudicialResult> getJudicialResultsV1(final String resultDefinitionGroup1, final String resultDefinitionGroup2, final JudicialResult.Builder builder, final int i, final String s, final int i2, final boolean withPrompt, final String resultText1, final String resultText2, final String resultText3) {
        List<JudicialResult> alist =  asList(builder
                .withRank(new BigDecimal(i))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .build(), judicialResult()
                .withResultDefinitionGroup(resultDefinitionGroup1)
                .withLabel("Bail conditions" + resultText1)
                .withRank(new BigDecimal(i2))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .withResultText(resultText1)
                .build(), builder
                .withRank(new BigDecimal(i))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .build(), judicialResult()
                .withResultDefinitionGroup(resultDefinitionGroup1)
                .withLabel("Bail conditions" + resultText2)
                .withRank(new BigDecimal(i2))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .withResultText(!resultText2.isEmpty() ? resultText2 : null)
                .build(), builder
                .withRank(new BigDecimal(i))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .build(), judicialResult()
                .withResultDefinitionGroup(resultDefinitionGroup2)
                .withLabel("Bail conditions" + resultText3)
                .withRank(new BigDecimal(i2))
                .withJudicialResultPrompts(getJudicialResultPromptList(withPrompt))
                .withResultText(resultText3)
                .build());
        return alist;
    }


    private PersonDefendant buildPersonDefendant(final String bailStatus) {
        if (null == bailStatus) {
            return personDefendant().build();
        }
        return personDefendant().withBailStatus(BailStatus.bailStatus().withCode(bailStatus).build()).build();
    }

    private List<JudicialResultPrompt> getJudicialResultPromptList(final boolean withPrompt) {
        if (!withPrompt) {
            return emptyList();
        }
        List<JudicialResultPrompt> list = new ArrayList<>();
        list.add(judicialResultPrompt().withValue("999").withLabel("Time of hearing").withPromptSequence(new BigDecimal(1)).build());
        list.add(judicialResultPrompt().withValue("777").withLabel("Date of hearing").withPromptSequence(new BigDecimal(2)).build());
        list.add(judicialResultPrompt().withValue("555").withLabel("Courtroom").withPromptSequence(new BigDecimal(3)).build());
        list.add(judicialResultPrompt().withValue("666").withLabel("Courthouse name").withPromptSequence(new BigDecimal(4)).build());
        return list;
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

    private void setResultTextsAreNull(final ResultsShared resultsSharedTemplate) {
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(3).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(3).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(5).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(5).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(6).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(6).getJudicialResults().get(1).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(7).getJudicialResults().get(0).setResultText(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(7).getJudicialResults().get(1).setResultText(null);
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
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(3).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(3).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(5).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(5).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(6).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(6).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(7).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(7).getJudicialResults().get(1).setResultDefinitionGroup(null);

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
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(2).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(2).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(3).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(3).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(4).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(4).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(5).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(5).getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(6).getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(6).getJudicialResults().get(1).setResultDefinitionGroup(null);
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
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(2).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(2).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(3).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(3).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(4).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(4).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(5).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(5).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(6).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(6).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(7).getOffence().getJudicialResults().get(0).setResultDefinitionGroup(null);
        resultsSharedTemplate.getHearing()
                .getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(7).getOffence().getJudicialResults().get(1).setResultDefinitionGroup(null);
    }

    private String getBailCondition(final String s, final String s2, final String s3, final String s4, final String s5, final String s6, final String s7) {

        List<String> results = getResultTexts(s, s2, s3, s4, s5, s6, s7);
        return constructBailConditions(results).toString();
    }


    private StringBuilder constructBailConditions(final List<String> resultTexts) {
        StringBuilder bailConditionsBuilder = new StringBuilder();

        for (String resultText : resultTexts) {
            bailConditionsBuilder = formatLine(bailConditionsBuilder, resultText);
        }
        return bailConditionsBuilder;
    }

    private StringBuilder formatLine(StringBuilder bailConditionsBuilder, String resultText) {

        if (resultText == null) return null;

        bailConditionsBuilder.append(resultText + ";");
        return bailConditionsBuilder;
    }

    private List<String> getResultTexts(final String s1, final String s2, final String s3, final String s4, final String s5, final String s6, final String s7) {

        List<String> results = new ArrayList<>();
        results.add(s1);
        results.add(s2);
        results.add(s3);
        results.add(s4);
        results.add(s5);
        if(!s6.isEmpty()) {
            results.add(s6);
        }
        if(!s7.isEmpty()) {
            results.add(s7);
        }
        return results;
    }

}

