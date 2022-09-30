package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil.reformatValue;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.NO_INITIAL_STR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.YES_INITIAL_STR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getBigDecimal;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getString;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.moj.cpp.hearing.event.delegates.helper.FinancialImpositionHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.PenaltyPoint;

import java.util.Objects;

public class JudicialResultPromptMapper {

    private JudicialResultPromptMapper() {
    }

    public static JudicialResultPrompt findJudicialResultPrompt(final Prompt prompt, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition) {
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptDefinition, prompt);
        final String value = reformatValue(prompt.getValue(), promptDefinition);
        final String welshValue = isNotBlank(prompt.getWelshValue()) ? prompt.getWelshValue() : value;
        return JudicialResultPrompt.judicialResultPrompt()
                .withJudicialResultPromptTypeId(prompt.getId())
                .withCourtExtract(findCalculatedCourtExtract(promptDefinition))
                .withLabel(prompt.getLabel())
                .withPromptReference(promptDefinition.getReference())
                .withPromptSequence(getBigDecimal(promptDefinition.getSequence(), null))
                .withUsergroups(promptDefinition.getUserGroups())
                .withValue(value)
                .withType(promptDefinition.getType())
                .withQualifier(promptDefinition.getQual())
                .withTotalPenaltyPoints(new PenaltyPoint().getPenaltyPointFromResults(promptDefinition, prompt))
                .withIsFinancialImposition(financialImpositionHelper.isFinancialImposition())
                .withWelshLabel(promptDefinition.getWelshLabel())
                .withWelshValue(welshValue)
                .withDurationSequence(promptDefinition.getDurationSequence())
                .withIsDurationStartDate(promptDefinition.getIsDurationStartDate())
                .withIsDurationEndDate(promptDefinition.getIsDurationEndDate())
                .withPartName(promptDefinition.getPartName())
                .build();
    }

    private static String findCalculatedCourtExtract(final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition) {
        final String courtExtract = getString(promptDefinition.getCourtExtract(), null);

        if (Objects.nonNull(courtExtract)) {
            return courtExtract;
        }
        else {
            return promptDefinition.isAvailableForCourtExtract() ? YES_INITIAL_STR : NO_INITIAL_STR;
        }
    }
}
