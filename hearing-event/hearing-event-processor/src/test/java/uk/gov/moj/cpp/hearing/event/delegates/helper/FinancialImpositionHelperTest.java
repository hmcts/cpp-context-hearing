package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.Prompt;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class FinancialImpositionHelperTest {

    final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData0 =
            uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                    .setId(UUID.randomUUID())
                    .setLabel("promptReferenceData0")
                    .setReference("AOBD")
                    .setUserGroups(Arrays.asList("usergroup0", "usergroup1"));

    final Prompt prompt0 = Prompt.prompt()
            .withLabel(promptReferenceData0.getLabel())
            .withValue("10")
            .withId(promptReferenceData0.getId())
            .withFixedListCode("fixedListCode0")
            .build();

    @Test
    public void shouldReturnTrueWhenPropmtReferenceIsAOBDIsPresent() {
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(true));

    }

    @Test
    public void shouldReturnTrueWhenPropmtReferenceIsAOCIsPresent() {
        promptReferenceData0.setReference("AOC");
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnTrueWhenPropmtReferenceIsAOCOMIsPresent() {
        promptReferenceData0.setReference("AOCOM");
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnTrueWhenPropmtReferenceIsAOFIsPresent() {
        promptReferenceData0.setReference("AOF");
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnTrueWhenPropmtReferenceIsAOSIsPresent() {
        promptReferenceData0.setReference("AOS");
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnTrueWhenPromptReferenceIsAOBDIsPresent() {
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(true));

    }

    @Test
    public void shouldReturnFalseWhenPromptReferenceIsPENPTIsPresent() {
        promptReferenceData0.setReference("PENPT");
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptReferenceData0, prompt0);
        final boolean result = financialImpositionHelper.isFinancialImposition();
        assertThat(result, is(false));
    }
}