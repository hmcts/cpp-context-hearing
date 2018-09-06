package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.DelegatedPowers;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-plea-updated")
public class OffencePleaUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private UUID offenceId;
    private LocalDate pleaDate;
    private String value;
    private DelegatedPowers delegatedPowers;

    public OffencePleaUpdated() {
    }

    @JsonCreator
    public OffencePleaUpdated(@JsonProperty("hearingId") final UUID originHearingId,
                              @JsonProperty("offenceId") final UUID offenceId,
                              @JsonProperty("pleaDate") final LocalDate pleaDate,
                              @JsonProperty("value") final String value) {
        this.hearingId = originHearingId;
        this.offenceId = offenceId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    @JsonIgnore
    private OffencePleaUpdated(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.offenceId = builder.offenceId;
        this.pleaDate = builder.pleaDate;
        this.value = builder.value;
        this.delegatedPowers = builder.delegatedPowers;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public static final class Builder {

        private UUID hearingId;
        private UUID offenceId;
        private LocalDate pleaDate;
        private String value;
        private DelegatedPowers delegatedPowers;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withPleaDate(final LocalDate pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

        public Builder withValue(final String value) {
            this.value = value;
            return this;
        }

        public Builder withDelegatedPowers(final DelegatedPowers delegatedPowers) {
            this.delegatedPowers = delegatedPowers;
            return this;
        }

        public OffencePleaUpdated build() {
            return new OffencePleaUpdated(this);
        }
    }
}