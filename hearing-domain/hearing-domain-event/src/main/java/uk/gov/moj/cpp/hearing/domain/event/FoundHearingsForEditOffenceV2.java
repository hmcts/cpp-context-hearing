package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.found-hearings-for-edit-offence-v2")
@SuppressWarnings("squid:S00107")
public class FoundHearingsForEditOffenceV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UUID> hearingIds;
    private UUID defendantId;
    private List<uk.gov.justice.core.courts.Offence> offences;

    private FoundHearingsForEditOffenceV2() {
    }

    @JsonCreator
    protected FoundHearingsForEditOffenceV2(@JsonProperty("hearingIds") final List<UUID> hearingIds,
                                            @JsonProperty("defendantId") final UUID defendantId,
                                            @JsonProperty("offences") final List<uk.gov.justice.core.courts.Offence> offences) {
        this.hearingIds = new ArrayList<>(hearingIds);
        this.defendantId = defendantId;
        this.offences = new ArrayList<>(offences);
    }

    public static FoundHearingsForEditOffenceV2 foundHearingsForEditOffenceV2() {
        return new FoundHearingsForEditOffenceV2();
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public List<uk.gov.justice.core.courts.Offence> getOffences() {
        return offences;
    }

    public FoundHearingsForEditOffenceV2 withHearingIds(final List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }

    public FoundHearingsForEditOffenceV2 withDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public FoundHearingsForEditOffenceV2 withOffences(final List<uk.gov.justice.core.courts.Offence> offences) {
        this.offences = offences;
        return this;
    }
}