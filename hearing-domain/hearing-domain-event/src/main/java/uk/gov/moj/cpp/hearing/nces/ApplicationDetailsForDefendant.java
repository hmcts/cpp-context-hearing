package uk.gov.moj.cpp.hearing.nces;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings({"squid:S1948"})
public class ApplicationDetailsForDefendant implements Serializable {

    private final UUID applicationTypeId;
    private final UUID applicationOutcomeTypeId;

    public ApplicationDetailsForDefendant(final UUID applicationTypeId, final UUID applicationOutcomeTypeId) {
        this.applicationTypeId = applicationTypeId;
        this.applicationOutcomeTypeId = applicationOutcomeTypeId;
    }

    public UUID getApplicationTypeId() {
        return applicationTypeId;
    }

    public UUID getApplicationOutcomeTypeId() {
        return applicationOutcomeTypeId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID applicationTypeId;
        private UUID applicationOutcomeTypeId;

        private Builder() {
        }

        public Builder withApplicationTypeId(UUID applicationTypeId) {
            this.applicationTypeId = applicationTypeId;
            return this;
        }

        public Builder withApplicationOutcomeTypeId(UUID applicationOutcomeTypeId) {
            this.applicationOutcomeTypeId = applicationOutcomeTypeId;
            return this;
        }

        public ApplicationDetailsForDefendant build() {
            return new ApplicationDetailsForDefendant(applicationTypeId, applicationOutcomeTypeId);
        }
    }
}
