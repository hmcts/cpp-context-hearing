package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.offence-added-v2")
@SuppressWarnings("squid:S00107")
public class OffenceAddedV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID defendantId;
    private UUID prosecutionCaseId;
    private List<Offence> offences;

    private OffenceAddedV2() {
    }

    @JsonCreator
    protected OffenceAddedV2(@JsonProperty("hearingId") final UUID hearingId,
                             @JsonProperty("defendantId") final UUID defendantId,
                             @JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
                             @JsonProperty("offences") final List<Offence> offences) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.offences = offences;
    }

    public static OffenceAddedV2 offenceAddedV2() {
        return new OffenceAddedV2();
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

    public List<Offence> getOffences() {
        return offences;
    }

    public OffenceAddedV2 withHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public OffenceAddedV2 withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public OffenceAddedV2 withProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public OffenceAddedV2 withOffence(List<Offence> offences) {
        this.offences = offences;
        return this;
    }
}