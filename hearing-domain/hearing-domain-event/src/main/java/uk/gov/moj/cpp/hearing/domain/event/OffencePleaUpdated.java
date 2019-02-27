package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Plea;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.offence-plea-updated")
public class OffencePleaUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private Plea plea;

    public OffencePleaUpdated() {
    }

    @JsonCreator
    public OffencePleaUpdated(@JsonProperty("hearingId") final UUID originHearingId,
                              @JsonProperty("plea") final Plea plea) {
        this.hearingId = originHearingId;
        this.plea = plea;
    }

    @JsonIgnore
    private OffencePleaUpdated(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.plea = builder.plea;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Plea getPlea() {
        return plea;
    }

    public static final class Builder {

        private UUID hearingId;
        private Plea plea;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withPlea(final Plea plea) {
            this.plea = plea;
            return this;
        }

        public OffencePleaUpdated build() {
            return new OffencePleaUpdated(this);
        }
    }
}