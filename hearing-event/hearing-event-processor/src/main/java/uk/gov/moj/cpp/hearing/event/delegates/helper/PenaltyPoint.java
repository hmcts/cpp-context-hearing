package uk.gov.moj.cpp.hearing.event.delegates.helper;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.math.BigDecimal;

public class PenaltyPoint {

    private static final String PENALTY_POINT = "PENPT";

    public BigDecimal getPenaltyPointFromResults(final Prompt promptDefinition, final uk.gov.justice.core.courts.Prompt prompt){

        return  promptDefinition.getId().equals(prompt.getId()) && PENALTY_POINT.equalsIgnoreCase(promptDefinition.getReference())  ? new BigDecimal(prompt.getValue()) : null ;
    }
}
