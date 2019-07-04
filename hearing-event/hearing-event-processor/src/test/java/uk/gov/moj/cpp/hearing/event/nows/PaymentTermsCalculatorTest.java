package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_DAYS_IN_DEFAULT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_START_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_LUMP_SUM_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAYMENT_FREQUENCY;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAY_BY_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_INSTL;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_LUMSI;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_PDATE;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class PaymentTermsCalculatorTest {

    private PaymentTermsCalculator paymentTermsCalculator;

    @Before
    public void setup() {
        paymentTermsCalculator = new PaymentTermsCalculator();
    }

    @Test
    public void calculatePaymentTermsWithResultAsPayByDate() {

        final UUID payByDatePromptId = UUID.randomUUID();

        final Map<UUID, Prompt> id2PromptRef = new HashMap<>();

        id2PromptRef.put(payByDatePromptId, Prompt.prompt().setId(payByDatePromptId).setReference(P_PAY_BY_DATE));

        final List<ResultLine> resultLines4Now = Collections.singletonList(
                ResultLine.resultLine()
                        .withResultDefinitionId(RD_PDATE)
                        .withPrompts(Collections.singletonList(
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(payByDatePromptId)
                                        .withValue("2019-06-01").build()))
                        .build());

        final String expected = "Date to pay in full by 01-Jun-2019";

        String actual = paymentTermsCalculator.calculatePaymentTerms(id2PromptRef, resultLines4Now);

        assertEquals(expected, actual);
    }

    @Test
    public void calculatePaymentTermsWithResultAsPayByDateAndOptional() {

        final UUID payByDatePromptId = UUID.randomUUID();

        final UUID daysInDefaultPromptId = UUID.randomUUID();

        final Map<UUID, Prompt> id2PromptRef = new HashMap<>();

        id2PromptRef.put(payByDatePromptId, Prompt.prompt().setId(payByDatePromptId).setReference(P_PAY_BY_DATE));
        id2PromptRef.put(daysInDefaultPromptId, Prompt.prompt().setId(daysInDefaultPromptId).setReference(P_DAYS_IN_DEFAULT));

        final List<ResultLine> resultLines4Now = Collections.singletonList(
                ResultLine.resultLine()
                        .withResultDefinitionId(RD_PDATE)
                        .withPrompts(asList(
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(payByDatePromptId)
                                        .withValue("2019-06-01").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(daysInDefaultPromptId)
                                        .withValue("5").build())
                        )
                        .build());

        final String expected = "Date to pay in full by 01-Jun-2019 Number of days in default 5";

        String actual = paymentTermsCalculator.calculatePaymentTerms(id2PromptRef, resultLines4Now);

        assertEquals(expected, actual);
    }


    @Test
    public void calculatePaymentTermsWithResultAsLumpSum() {

        final UUID promptId1 = UUID.randomUUID();
        final UUID promptId2 = UUID.randomUUID();
        final UUID promptId3 = UUID.randomUUID();
        final UUID promptId4 = UUID.randomUUID();
        final UUID promptId6 = UUID.randomUUID();

        final Map<UUID, Prompt> id2PromptRef = new HashMap<>();
        id2PromptRef.put(promptId1, Prompt.prompt().setId(promptId1).setReference(P_PAYMENT_FREQUENCY));
        id2PromptRef.put(promptId2, Prompt.prompt().setId(promptId2).setReference(P_DAYS_IN_DEFAULT));
        id2PromptRef.put(promptId3, Prompt.prompt().setId(promptId3).setReference(P_INSTALMENT_START_DATE));
        id2PromptRef.put(promptId4, Prompt.prompt().setId(promptId4).setReference(P_LUMP_SUM_AMOUNT));
        id2PromptRef.put(promptId6, Prompt.prompt().setId(promptId6).setReference(P_INSTALMENT_AMOUNT));

        final List<ResultLine> resultLines4Now = Collections.singletonList(
                ResultLine.resultLine()
                        .withResultDefinitionId(RD_LUMSI)
                        .withPrompts(asList(
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId4)
                                        .withValue("100").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId6)
                                        .withValue("10").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId1)
                                        .withValue("monthly").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId3)
                                        .withValue("2019-02-02").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId2)
                                        .withValue("2").build())
                        )
                        .build());

        final String expected = "Lump sum amount £100 Instalment amount £10 Payment frequency monthly Instalment start date 02-Feb-2019 Number of days in default 2";

        String actual = paymentTermsCalculator.calculatePaymentTerms(id2PromptRef, resultLines4Now);

        assertEquals(expected, actual);
    }

    @Test
    public void calculatePaymentTermsWithResultAsInstalmentsOnly() {
        final UUID promptId1 = UUID.randomUUID();
        final UUID promptId2 = UUID.randomUUID();
        final UUID promptId3 = UUID.randomUUID();
        final UUID promptId4 = UUID.randomUUID();

        final Map<UUID, Prompt> id2PromptRef = new HashMap<>();
        id2PromptRef.put(promptId1, Prompt.prompt().setId(promptId1).setReference(P_PAYMENT_FREQUENCY));
        id2PromptRef.put(promptId2, Prompt.prompt().setId(promptId2).setReference(P_DAYS_IN_DEFAULT));
        id2PromptRef.put(promptId3, Prompt.prompt().setId(promptId3).setReference(P_INSTALMENT_START_DATE));
        id2PromptRef.put(promptId4, Prompt.prompt().setId(promptId4).setReference(P_INSTALMENT_AMOUNT));

        final List<ResultLine> resultLines4Now = Collections.singletonList(
                ResultLine.resultLine()
                        .withResultDefinitionId(RD_INSTL)
                        .withPrompts(asList(
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId4)
                                        .withValue("100").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId1)
                                        .withValue("monthly").build(),
                                uk.gov.justice.core.courts.Prompt.prompt()
                                        .withId(promptId3)
                                        .withValue("2019-02-02").build())
                        )
                        .build());

        final String expected = "Instalment amount £100 Payment frequency monthly Instalment start date 02-Feb-2019";

        String actual = paymentTermsCalculator.calculatePaymentTerms(id2PromptRef, resultLines4Now);

        assertEquals(expected, actual);
    }
}