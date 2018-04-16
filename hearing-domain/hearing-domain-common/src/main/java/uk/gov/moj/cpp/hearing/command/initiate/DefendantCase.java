package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

public class DefendantCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID caseId;
    private final String bailStatus;
    private final ZonedDateTime custodyTimeLimitDate;

    @JsonCreator
    public DefendantCase(@JsonProperty("caseId") final UUID caseId,
                         @JsonProperty("bailStatus") final String bailStatus,
                         @JsonProperty("custodyTimeLimitDate") final ZonedDateTime custodyTimeLimitDate
    ) {

        this.caseId = caseId;
        this.bailStatus = bailStatus;
        this.custodyTimeLimitDate = custodyTimeLimitDate;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public ZonedDateTime getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public static class Builder {

        private UUID caseId;
        private String bailStatus;
        private ZonedDateTime custodyTimeLimitDate;

        private Builder() {

        }

        public UUID getCaseId() {
            return caseId;
        }

        public String getBailStatus() {
            return bailStatus;
        }

        public ZonedDateTime getCustodyTimeLimitDate() {
            return custodyTimeLimitDate;
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withBailStatus(String bailStatus) {
            this.bailStatus = bailStatus;
            return this;
        }

        public Builder withCustodyTimeLimitDate(ZonedDateTime custodyTimeLimitDate) {
            this.custodyTimeLimitDate = custodyTimeLimitDate;
            return this;
        }

        public DefendantCase build() {
            return new DefendantCase(caseId, bailStatus, custodyTimeLimitDate);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(DefendantCase defendantCase) {
        return builder()
                .withCaseId(defendantCase.getCaseId())
                .withBailStatus(defendantCase.getBailStatus())
                .withCustodyTimeLimitDate(defendantCase.getCustodyTimeLimitDate());
    }
}