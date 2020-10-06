package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.master-defendant-id-added")
public class MasterDefendantIdAdded implements Serializable {

    private static final long serialVersionUID = -2495974395118808770L;

    private UUID hearingId;

    private UUID prosecutionCaseId;

    private UUID defendantId;

    private UUID masterDefendantId;

    public MasterDefendantIdAdded() {
    }

    @JsonCreator
    public MasterDefendantIdAdded(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("prosecutionCaseId") UUID prosecutionCaseId,
            @JsonProperty("defendantId") UUID defendantId,
            @JsonProperty("masterDefendantId") UUID masterDefendantId) {
        super();
        this.hearingId = hearingId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.defendantId = defendantId;
        this.masterDefendantId = masterDefendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public void setProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public void setMasterDefendantId(UUID masterDefendantId) {
        this.masterDefendantId = masterDefendantId;
    }
}
