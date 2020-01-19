package uk.gov.moj.cpp.hearing.nces;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.event.nces-notification-requested")
public class NCESNotificationRequested implements Serializable {

    private FinancialOrderForDefendant financialOrderForDefendant;

    public NCESNotificationRequested() {
    }

    public NCESNotificationRequested(final FinancialOrderForDefendant financialOrderForDefendant) {
        this.financialOrderForDefendant = financialOrderForDefendant;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final NCESNotificationRequested copy) {
        Builder builder = new Builder();
        builder.financialOrderForDefendant = copy.getFinancialOrderForDefendant();
        return builder;
    }

    public FinancialOrderForDefendant getFinancialOrderForDefendant() {
        return financialOrderForDefendant;
    }

    public static final class Builder {
        private FinancialOrderForDefendant financialOrderForDefendant;

        private Builder() {
        }

        public Builder withFinancialOrderForDefendant(final FinancialOrderForDefendant financialOrderForDefendant) {
            this.financialOrderForDefendant = financialOrderForDefendant;
            return this;
        }

        public NCESNotificationRequested build() {
            return new NCESNotificationRequested(financialOrderForDefendant);
        }
    }
}
