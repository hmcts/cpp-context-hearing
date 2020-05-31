package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil;
import uk.gov.moj.cpp.hearing.event.delegates.helper.FinancialImpositionHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.PenaltyPoint;

import java.util.Objects;

import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.NO_INITIAL_STR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.YES_INITIAL_STR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getBigDecimal;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getString;

public class JudicialResultPromptMapper {

    private JudicialResultPromptMapper() {
    }

    public static JudicialResultPrompt findJudicialResultPrompt(final Prompt prompt, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition) {
        final FinancialImpositionHelper financialImpositionHelper = new FinancialImpositionHelper(promptDefinition, prompt);

        return JudicialResultPrompt.judicialResultPrompt()
                .withJudicialResultPromptTypeId(prompt.getId())
                .withCourtExtract(findCalculatedCourtExtract(promptDefinition))
                .withLabel(prompt.getLabel())
                .withPromptReference(promptDefinition.getReference())
                .withPromptSequence(getBigDecimal(promptDefinition.getSequence(), null))
                .withUsergroups(promptDefinition.getUserGroups())
                .withValue(PublishResultUtil.reformatValue(prompt.getValue(), promptDefinition))
                .withType(promptDefinition.getType())
                .withQualifier(promptDefinition.getQual())
                .withTotalPenaltyPoints(new PenaltyPoint().getPenaltyPointFromResults(promptDefinition, prompt))
                .withIsFinancialImposition(financialImpositionHelper.isFinancialImposition())
                .withWelshLabel(prompt.getWelshValue())
                .withDurationSequence(promptDefinition.getDurationSequence())
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
