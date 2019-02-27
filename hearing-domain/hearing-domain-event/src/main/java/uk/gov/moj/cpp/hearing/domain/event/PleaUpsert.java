package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Plea;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-offence-plea-updated")
public class PleaUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Plea plea;

    public PleaUpsert() {

    }

    @JsonCreator
    public PleaUpsert(@JsonProperty("hearingId") final UUID originHearingId,
                      @JsonProperty("plea") final Plea plea) {
        this.hearingId = originHearingId;
        this.plea = plea;
        if (this.getPlea()!=null && this.getPlea().getOriginatingHearingId()==null) {
            this.getPlea().setOriginatingHearingId(originHearingId);
        }
    }

    public static PleaUpsert pleaUpsert() {
        return new PleaUpsert();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public PleaUpsert setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public Plea getPlea() {
        return plea;
    }

    public PleaUpsert setPlea(Plea plea) {
        this.plea = plea;
        return this;
    }
}