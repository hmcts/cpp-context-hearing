package uk.gov.moj.cpp.hearing.nces;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("hearing.event.defendant-update-with-application")
public class DefendantUpdateWithApplicationDetails implements Serializable {

    private final ApplicationDetailsForDefendant applicationDetailsForDefendant;

    public DefendantUpdateWithApplicationDetails(final ApplicationDetailsForDefendant applicationDetailsForDefendant) {
        this.applicationDetailsForDefendant = applicationDetailsForDefendant;
    }

    public ApplicationDetailsForDefendant getApplicationDetailsForDefendant() {
        return applicationDetailsForDefendant;
    }

    public static final class Builder {
        private ApplicationDetailsForDefendant applicationDetailsForDefendant;

        private Builder() {
        }

        public static DefendantUpdateWithApplicationDetails.Builder newBuilder() {
            return new DefendantUpdateWithApplicationDetails.Builder();
        }

        public DefendantUpdateWithApplicationDetails.Builder withApplicationDetailsForDefendant(ApplicationDetailsForDefendant applicationDetailsForDefendant) {
            this.applicationDetailsForDefendant = applicationDetailsForDefendant;
            return this;
        }

        public DefendantUpdateWithApplicationDetails build() {
            return new DefendantUpdateWithApplicationDetails(applicationDetailsForDefendant);
        }
    }
}
