package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.util.List;

public class FinancialImpositionHelper {

    private static final List<String> PROMPT_REFERENCES = of("AOBD", "AOC", "AOCOM", "AOF", "AOS");

    private final Boolean financialImposition;

    public FinancialImpositionHelper(final Prompt promptDefinition, final uk.gov.justice.core.courts.Prompt prompt) {
        this.financialImposition = promptDefinition.getId().equals(prompt.getId()) && PROMPT_REFERENCES.contains(promptDefinition.getReference());
    }

    public boolean isFinancialImposition() {
        return financialImposition;
    }
}
