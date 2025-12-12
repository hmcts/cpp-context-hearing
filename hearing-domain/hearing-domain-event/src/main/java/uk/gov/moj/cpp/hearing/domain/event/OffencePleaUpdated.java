package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.offence-plea-updated")
public class OffencePleaUpdated implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID hearingId;
    private PleaModel pleaModel;

    public OffencePleaUpdated() {
    }

    @JsonCreator
    public OffencePleaUpdated(@JsonProperty("hearingId") final UUID originHearingId,
                              @JsonProperty("pleaModel") final PleaModel pleaModel) {
        this.hearingId = originHearingId;
        this.pleaModel = pleaModel;
    }

    @JsonIgnore
    private OffencePleaUpdated(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.pleaModel = builder.pleaModel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PleaModel getPleaModel() {
        return pleaModel;
    }

    public static final class Builder {

        private UUID hearingId;
        private PleaModel pleaModel;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withPleaModel(final PleaModel pleaModel) {
            this.pleaModel = pleaModel;
            return this;
        }

        public OffencePleaUpdated build() {
            return new OffencePleaUpdated(this);
        }
    }
}