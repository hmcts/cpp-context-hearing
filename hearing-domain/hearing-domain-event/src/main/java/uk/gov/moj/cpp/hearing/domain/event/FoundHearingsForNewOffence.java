package uk.gov.moj.cpp.hearing.domain.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;

@Event("hearing.events.found-hearings-for-new-offence")
@SuppressWarnings("squid:S00107")
public class FoundHearingsForNewOffence implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID defendantId;
    private UUID prosecutionCaseId;
    private uk.gov.justice.json.schemas.core.Offence offence;
    private List<UUID> hearingIds;

    private FoundHearingsForNewOffence() {
    }

    @JsonCreator
    protected FoundHearingsForNewOffence(@JsonProperty("defendantId") final UUID defendantId,
            @JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
            @JsonProperty("offence") final uk.gov.justice.json.schemas.core.Offence offence,
            @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.defendantId = defendantId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.offence = offence;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public uk.gov.justice.json.schemas.core.Offence getOffence() {
        return offence;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public FoundHearingsForNewOffence withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public FoundHearingsForNewOffence withProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public FoundHearingsForNewOffence withOffence(uk.gov.justice.json.schemas.core.Offence offence) {
        this.offence = offence;
        return this;
    }

    public FoundHearingsForNewOffence withHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }

    public static FoundHearingsForNewOffence foundHearingsForNewOffence() {
        return new FoundHearingsForNewOffence();
    }
}