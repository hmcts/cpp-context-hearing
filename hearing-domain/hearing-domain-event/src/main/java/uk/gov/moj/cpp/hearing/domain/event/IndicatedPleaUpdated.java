package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.event.indicated-plea-updated")
public class IndicatedPleaUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private IndicatedPlea indicatedPlea;

    public IndicatedPleaUpdated() {

    }

    @JsonCreator
    public IndicatedPleaUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("indicatedPlea") final IndicatedPlea indicatedPlea) {
        this.hearingId = hearingId;
        this.indicatedPlea = indicatedPlea;
    }

    public static IndicatedPleaUpdated updateHearingWithIndicatedPlea() {
        return new IndicatedPleaUpdated();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public IndicatedPleaUpdated setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public IndicatedPlea getIndicatedPlea() {
        return indicatedPlea;
    }

    public IndicatedPleaUpdated setIndicatedPlea(final IndicatedPlea indicatedPlea) {
        this.indicatedPlea = indicatedPlea;
        return this;
    }
}
