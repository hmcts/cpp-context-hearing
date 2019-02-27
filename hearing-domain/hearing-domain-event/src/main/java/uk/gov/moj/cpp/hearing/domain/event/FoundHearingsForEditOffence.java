package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.found-hearings-for-edit-offence")
@SuppressWarnings("squid:S00107")
public class FoundHearingsForEditOffence implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UUID> hearingIds;
    private UUID defendantId;
    private uk.gov.justice.core.courts.Offence offence;

    private FoundHearingsForEditOffence() {
    }

    @JsonCreator
    protected FoundHearingsForEditOffence(@JsonProperty("hearingIds") final List<UUID> hearingIds,
                                          @JsonProperty("defendantId") final UUID defendantId,
                                          @JsonProperty("offence") final uk.gov.justice.core.courts.Offence offence) {
        this.hearingIds = new ArrayList<>(hearingIds);
        this.defendantId = defendantId;
        this.offence = offence;
    }

    public static FoundHearingsForEditOffence foundHearingsForEditOffence() {
        return new FoundHearingsForEditOffence();
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public uk.gov.justice.core.courts.Offence getOffence() {
        return offence;
    }

    public FoundHearingsForEditOffence withHearingIds(final List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }

    public FoundHearingsForEditOffence withDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public FoundHearingsForEditOffence withOffence(final uk.gov.justice.core.courts.Offence offence) {
        this.offence = offence;
        return this;
    }
}