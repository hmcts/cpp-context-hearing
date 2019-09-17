package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-offence-plea-updated")
public class PleaUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private PleaModel pleaModel;

    public PleaUpsert() {
    }

    @JsonCreator
    public PleaUpsert(@JsonProperty("hearingId") final UUID originHearingId,
                      @JsonProperty("pleaModel") final PleaModel pleaModel) {
        this.hearingId = originHearingId;
        this.pleaModel = pleaModel;
        if (pleaModel.getPlea() != null) {
            pleaModel.getPlea().setOriginatingHearingId(hearingId);
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

    public PleaModel getPleaModel() {
        return pleaModel;
    }

    public PleaUpsert setPleaModel(final PleaModel pleaModel) {
        this.pleaModel = pleaModel;
        return this;
    }
}