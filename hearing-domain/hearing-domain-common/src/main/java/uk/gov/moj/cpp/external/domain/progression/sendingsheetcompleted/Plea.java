package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S3437"})
public class Plea implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final UUID originatingHearingId;

    private final LocalDate pleaDate;

    private final String value;

    @JsonCreator
    public Plea(@JsonProperty("id") final UUID id,
                @JsonProperty("originatingHearingId") final UUID originatingHearingId,
                @JsonProperty("pleaDate") final LocalDate pleaDate,
                @JsonProperty("value") final String value) {
        this.id = id;
        this.originatingHearingId=originatingHearingId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    public static Builder plea() {
        return new Plea.Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOriginatingHearingId() {
        return originatingHearingId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }

    public static class Builder {
        private UUID id;

        private UUID originatingHearingId;

        private LocalDate pleaDate;

        private String pleaValue;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withOriginatingHearingId(final UUID originatingHearingId) {
            this.originatingHearingId = originatingHearingId;
            return this;
        }

        public Builder withPleaDate(final LocalDate pleaDate) {
            this.pleaDate = pleaDate;
            return this;
        }


        public Builder withPleaValue(final String pleaValue) {
            this.pleaValue = pleaValue;
            return this;
        }


        public Plea build() {
            return new Plea(id, originatingHearingId, pleaDate, pleaValue);
        }
    }
}
