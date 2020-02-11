package uk.gov.moj.cpp.hearing.nces;

import java.io.Serializable;

public class UpdateDefendantWithFinancialOrderDetails implements Serializable {
    private static final long serialVersionUID = -4485481131622795301L;

    private FinancialOrderForDefendant financialOrderForDefendant;

    public FinancialOrderForDefendant getFinancialOrderForDefendant() {
        return financialOrderForDefendant;
    }

    public UpdateDefendantWithFinancialOrderDetails(FinancialOrderForDefendant financialOrderForDefendant) {
        this.financialOrderForDefendant = financialOrderForDefendant;
    }

    private UpdateDefendantWithFinancialOrderDetails(Builder builder) {
        if(builder != null) {
            financialOrderForDefendant = builder.financialOrderForDefendant;
        }
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

        public UpdateDefendantWithFinancialOrderDetails build() {
            return new UpdateDefendantWithFinancialOrderDetails(this);
        }
    }
}
