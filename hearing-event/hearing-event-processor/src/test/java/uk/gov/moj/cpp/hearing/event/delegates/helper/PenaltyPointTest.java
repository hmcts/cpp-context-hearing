package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;

import uk.gov.justice.core.courts.Prompt;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class PenaltyPointTest {


    @Test
    public void promptReferenceOfPENPTShouldReturnsTheSetPenaltyPoint(){
        final PenaltyPoint penaltyPoint = new PenaltyPoint();
        final BigDecimal actualPenaltyPoint = penaltyPoint.getPenaltyPointFromResults(promptReferenceData0, prompt0);

        assertThat(actualPenaltyPoint, is(new BigDecimal("10.00")));

    }

    @Test
    public void emptyPromptReferenceDataForPenaltyPointShouldReturnsNull(){
        final PenaltyPoint penaltyPoint = new PenaltyPoint();
        final BigDecimal actualPenaltyPoint = penaltyPoint.getPenaltyPointFromResults(promptReferenceData1, prompt0);

        assertNull(actualPenaltyPoint);

    }


    final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData0 =
            uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                    .setId(UUID.randomUUID())
                    .setLabel("promptReferenceData0")
                    .setReference("PENPT")
                    .setUserGroups(Arrays.asList("usergroup0", "usergroup1"));

    final Prompt prompt0 = Prompt.prompt()
            .withLabel(promptReferenceData0.getLabel())
            .withValue("10.00")
            .withId(promptReferenceData0.getId())
            .withFixedListCode("fixedListCode0")
            .build();


    final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData1 =
            uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                    .setId(UUID.randomUUID())
                    .setLabel("promptReferenceData0")
                    .setReference("")
                    .setUserGroups(Arrays.asList("usergroup0", "usergroup1"));


}
