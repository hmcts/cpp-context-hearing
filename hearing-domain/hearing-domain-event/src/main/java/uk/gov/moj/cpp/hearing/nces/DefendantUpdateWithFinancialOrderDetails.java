package uk.gov.moj.cpp.hearing.nces;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.event.defendant-update-with-financial-order")
public class DefendantUpdateWithFinancialOrderDetails implements Serializable {

    private FinancialOrderForDefendant financialOrderForDefendant;


    public FinancialOrderForDefendant getFinancialOrderForDefendant() {
        return financialOrderForDefendant;
    }

    public DefendantUpdateWithFinancialOrderDetails(final FinancialOrderForDefendant financialOrderForDefendant) {
        this.financialOrderForDefendant = financialOrderForDefendant;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private FinancialOrderForDefendant financialOrderForDefendant;

        private Builder() {
        }

        public Builder withFinancialOrderForDefendant(FinancialOrderForDefendant val) {
            financialOrderForDefendant = val;
            return this;
        }


        public DefendantUpdateWithFinancialOrderDetails build() {
            return new DefendantUpdateWithFinancialOrderDetails(financialOrderForDefendant);
        }
    }
}
