package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Plea implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID originalHearingId;
    private String value;
    private LocalDate pleaDate;

    @JsonCreator
    public Plea(@JsonProperty("id") UUID id,
                @JsonProperty("originalHearingId") UUID originalHearingId,
                @JsonProperty("value") String value,
                @JsonProperty("pleaDate") LocalDate pleaDate) {
        this.id = id;
        this.originalHearingId = originalHearingId;
        this.value = value;
        this.pleaDate = pleaDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOriginalHearingId() {
        return originalHearingId;
    }

    public String getValue() {
        return value;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public static class Builder {

        private UUID id;
        private UUID originalHearingId;
        private String value;
        private LocalDate pleaDate;

        private Builder() {

        }

        public UUID getId() {
            return id;
        }

        public UUID getOriginalHearingId() {
            return originalHearingId;
        }

        public String getValue() {
            return value;
        }

        public LocalDate getPleaDate() {
            return pleaDate;
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withOriginalHearingId(UUID originalHearingId) {
            this.originalHearingId = originalHearingId;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withPleaDate(LocalDate pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }

        public Plea build() {
            return new Plea(this.id, this.originalHearingId, this.value, this.pleaDate);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(Plea plea) {
        return builder()
                .withId(plea.getId())
                .withValue(plea.getValue())
                .withPleaDate(plea.getPleaDate());
    }
}
