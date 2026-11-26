package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Records that sharing a STAT DEC application's result cleared (granted) or restored (not granted)
 * the plea, verdict and conviction date cloned onto its offences from an earlier hearing:
 * {@code adjustment} says what the system did, {@code resultDefinitionId} the application result
 * (G, STDEC, RFSD, DISM or WDRN) that caused it. Private: no listener, processor or view store (no Jira AC).
 */
@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
@Event("hearing.events.application-cloned-values-adjusted")
public class ApplicationClonedValuesAdjusted implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Adjustment {CLEARED, RESTORED}

    private final UUID hearingId;
    private final LocalDate hearingDay;
    private final ZonedDateTime sharedTime;
    private final UUID applicationId;
    private final String applicationTypeCode;
    private final Adjustment adjustment;
    private final UUID resultDefinitionId;
    private final List<AdjustedApplicationOffence> offences;

    @JsonCreator
    private ApplicationClonedValuesAdjusted(@JsonProperty("hearingId") final UUID hearingId,
                                            @JsonProperty("hearingDay") final LocalDate hearingDay,
                                            @JsonProperty("sharedTime") final ZonedDateTime sharedTime,
                                            @JsonProperty("applicationId") final UUID applicationId,
                                            @JsonProperty("applicationTypeCode") final String applicationTypeCode,
                                            @JsonProperty("adjustment") final Adjustment adjustment,
                                            @JsonProperty("resultDefinitionId") final UUID resultDefinitionId,
                                            @JsonProperty("offences") final List<AdjustedApplicationOffence> offences) {
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.sharedTime = sharedTime;
        this.applicationId = applicationId;
        this.applicationTypeCode = applicationTypeCode;
        this.adjustment = adjustment;
        this.resultDefinitionId = resultDefinitionId;
        this.offences = offences;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getApplicationTypeCode() {
        return applicationTypeCode;
    }

    public Adjustment getAdjustment() {
        return adjustment;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public List<AdjustedApplicationOffence> getOffences() {
        return offences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private LocalDate hearingDay;
        private ZonedDateTime sharedTime;
        private UUID applicationId;
        private String applicationTypeCode;
        private Adjustment adjustment;
        private UUID resultDefinitionId;
        private List<AdjustedApplicationOffence> offences;

        private Builder() {
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public Builder withSharedTime(final ZonedDateTime sharedTime) {
            this.sharedTime = sharedTime;
            return this;
        }

        public Builder withApplicationId(final UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder withApplicationTypeCode(final String applicationTypeCode) {
            this.applicationTypeCode = applicationTypeCode;
            return this;
        }

        public Builder withAdjustment(final Adjustment adjustment) {
            this.adjustment = adjustment;
            return this;
        }

        public Builder withResultDefinitionId(final UUID resultDefinitionId) {
            this.resultDefinitionId = resultDefinitionId;
            return this;
        }

        public Builder withOffences(final List<AdjustedApplicationOffence> offences) {
            this.offences = offences;
            return this;
        }

        public ApplicationClonedValuesAdjusted build() {
            return new ApplicationClonedValuesAdjusted(hearingId, hearingDay, sharedTime, applicationId, applicationTypeCode, adjustment, resultDefinitionId, offences);
        }
    }
}
