package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Offence;

@Event("hearing.events.offence-added")
@SuppressWarnings("squid:S00107")
public class OffenceAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID defendantId;
    private UUID prosecutionCaseId;
    private Offence offence;

    private OffenceAdded() {
    }

    @JsonCreator
    protected OffenceAdded(@JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
            @JsonProperty("offence") final Offence offence) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.offence = offence;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public Offence getOffence() {
        return offence;
    }

    public OffenceAdded withHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public OffenceAdded withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public OffenceAdded withProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public OffenceAdded withOffence(Offence offence) {
        this.offence = offence;
        return this;
    }

    public static OffenceAdded offenceAdded() {
        return new OffenceAdded();
    }
}