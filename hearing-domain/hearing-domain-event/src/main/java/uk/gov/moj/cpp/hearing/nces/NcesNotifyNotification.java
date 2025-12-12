package uk.gov.moj.cpp.hearing.nces;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings({"squid:S1948"})
public class NcesNotifyNotification implements Serializable {

    private final UUID defendantId;
    private final String amendmentType;

    public NcesNotifyNotification(UUID defendantId, String amendmentType) {
        this.defendantId = defendantId;
        this.amendmentType = amendmentType;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public String getAmendmentType() {
        return amendmentType;
    }

    public static class Builder {

        private UUID defendantId;
        private String amendmentType;


        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withAmendmentType(final String amendmentType) {
            this.amendmentType = amendmentType;
            return this;
        }

        public NcesNotifyNotification build() {
            return new NcesNotifyNotification(defendantId, amendmentType);
        }
    }
}
