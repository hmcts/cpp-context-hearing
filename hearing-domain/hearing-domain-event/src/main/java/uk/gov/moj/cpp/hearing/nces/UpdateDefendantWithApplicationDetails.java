package uk.gov.moj.cpp.hearing.nces;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings({"squid:S1948"})
public class UpdateDefendantWithApplicationDetails implements Serializable {

    private final UUID defendantId;
    private final UUID applicationTypeId;
    private final UUID applicationOutcomeTypeId;

    public UpdateDefendantWithApplicationDetails(final UUID defendantId, final UUID applicationTypeId, final UUID applicationOutcomeTypeId) {
        this.defendantId = defendantId;
        this.applicationTypeId = applicationTypeId;
        this.applicationOutcomeTypeId = applicationOutcomeTypeId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getApplicationTypeId() {
        return applicationTypeId;
    }

    public UUID getApplicationOutcomeTypeId() {
        return applicationOutcomeTypeId;
    }

    public static UpdateDefendantWithApplicationDetails.Builder newBuilder() {
        return new UpdateDefendantWithApplicationDetails.Builder();
    }

    public static final class Builder {
        private UUID defendantId;
        private UUID applicationTypeId;
        private UUID applicationOutcomeTypeId;

        private Builder() {
        }

        public UpdateDefendantWithApplicationDetails.Builder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public UpdateDefendantWithApplicationDetails.Builder withApplicationTypeId(UUID applicationTypeId) {
            this.applicationTypeId = applicationTypeId;
            return this;
        }

        public UpdateDefendantWithApplicationDetails.Builder withApplicationOutcomeTypeId(UUID applicationOutcomeTypeId) {
            this.applicationOutcomeTypeId = applicationOutcomeTypeId;
            return this;
        }

        public UpdateDefendantWithApplicationDetails build() {
            return new UpdateDefendantWithApplicationDetails(defendantId, applicationTypeId, applicationOutcomeTypeId);
        }
    }
}
