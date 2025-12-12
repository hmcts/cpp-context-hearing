package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.nces.ApplicationDetailsForDefendant;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;

import java.io.Serializable;

public class DefendantAggregateMomento implements Serializable {

    private static final long serialVersionUID = 1L;

    private FinancialOrderForDefendant financialOrderForDefendant;
    private ApplicationDetailsForDefendant applicationDetailsForDefendant;


    public FinancialOrderForDefendant getFinancialOrderForDefendant() {
        return financialOrderForDefendant;
    }

    public void setFinancialOrderForDefendant(final FinancialOrderForDefendant financialOrderForDefendant) {
        this.financialOrderForDefendant = financialOrderForDefendant;
    }


    public void setApplicationDetailsForDefendant(final ApplicationDetailsForDefendant applicationDetailsForDefendant) {
        this.applicationDetailsForDefendant = applicationDetailsForDefendant;
    }

    public ApplicationDetailsForDefendant getApplicationDetailsForDefendant() {
        return applicationDetailsForDefendant;
    }
}