package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FinancialResultCalculatorTest {

    @InjectMocks
    private FinancialResultCalculator target;

    private String formatPenceAsPounds(final long pence) {
        return "" + (pence / 100) + "." + (new DecimalFormat("00").format(pence % 100));
    }

    private long add(final Map<UUID, Prompt> id2PromptRef, final List<ResultLine> resultLines4Now,
                     final String promptRef, final long valueInPence) {
        final Prompt prompt = Prompt.prompt()
                .setReference(promptRef)
                .setId(UUID.randomUUID());
        id2PromptRef.put(prompt.getId(), prompt);
        final ResultLine resultLine = ResultLine.resultLine()
                .withPrompts(Arrays.asList(
                        uk.gov.justice.core.courts.Prompt.prompt()
                                .withId(prompt.getId())
                                .withValue(formatPenceAsPounds(valueInPence))
                                .build()
                ))
                .build();
        resultLines4Now.add(resultLine);

        return valueInPence;
    }

    @Test
    public void testCalculate() {

        long totalInPence = 0;
        long totalAlreadyImposed = 0;

        final Map<UUID, Prompt> id2PromptRef = new HashMap<>();
        final List<ResultLine> resultLines4Now = new ArrayList<>();

        totalInPence += add(id2PromptRef, resultLines4Now, PromptTypesConstant.COMPENSATION_AMOUNT_PROMPT_REFERENCE, 123l);
        totalInPence += add(id2PromptRef, resultLines4Now, PromptTypesConstant.FINE_AMOUNT_PROMPT_REFERENCE, 234l);
        totalInPence += add(id2PromptRef, resultLines4Now, PromptTypesConstant.VICTIM_SURCHARGE_AMOUNT_PROMPT_REFERENCE, 523l);
        totalInPence += add(id2PromptRef, resultLines4Now, PromptTypesConstant.VEHICLE_EXCISE_BACK_DUTY_AMOUNT_PROMPT_REFERENCE, 723l);
        totalInPence += add(id2PromptRef, resultLines4Now, PromptTypesConstant.COSTS_TO_CROWN_PROSECUTION_SERVICE_AMOUNT_PROMPT_REFERENCE, 823l);
        totalInPence += add(id2PromptRef, resultLines4Now, PromptTypesConstant.AMOUNT_OF_COSTS_PROMPT_REFERENCE, 789l);
        totalAlreadyImposed = add(id2PromptRef, resultLines4Now, PromptTypesConstant.TOTAL_AMOUNT_ENFORCED_PROMPT_REFERENCE, 45l);

        final FinancialResultCalculator.FinancialResult result = target.calculate(id2PromptRef, resultLines4Now);

        Assert.assertEquals(formatPenceAsPounds(totalInPence), result.getTotalAmountImposed());
        Assert.assertEquals(formatPenceAsPounds(totalInPence-totalAlreadyImposed), result.getTotalBalance());

    }


}
