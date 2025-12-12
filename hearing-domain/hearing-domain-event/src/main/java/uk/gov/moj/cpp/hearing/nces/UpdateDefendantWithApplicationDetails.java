package uk.gov.moj.cpp.hearing.nces;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings({"squid:S1948"})
public class UpdateDefendantWithApplicationDetails implements Serializable {

    private final UUID defendantId;
    private final UUID applicationTypeId;

    public UpdateDefendantWithApplicationDetails(final UUID defendantId, final UUID applicationTypeId) {
        this.defendantId = defendantId;
        this.applicationTypeId = applicationTypeId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getApplicationTypeId() {
        return applicationTypeId;
    }


    public static UpdateDefendantWithApplicationDetails.Builder newBuilder() {
        return new UpdateDefendantWithApplicationDetails.Builder();
    }

    public static final class Builder {
        private UUID defendantId;
        private UUID applicationTypeId;

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

        public UpdateDefendantWithApplicationDetails build() {
            return new UpdateDefendantWithApplicationDetails(defendantId, applicationTypeId);
        }
    }
}
