package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.found-hearings-for-new-offence-v2")
@SuppressWarnings("squid:S00107")
public class FoundHearingsForNewOffenceV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID defendantId;
    private UUID prosecutionCaseId;
    private List<uk.gov.justice.core.courts.Offence> offences;
    private List<UUID> hearingIds;

    public FoundHearingsForNewOffenceV2() {
    }

    @JsonCreator
    protected FoundHearingsForNewOffenceV2(@JsonProperty("defendantId") final UUID defendantId,
                                           @JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
                                           @JsonProperty("offences") final List<uk.gov.justice.core.courts.Offence> offences,
                                           @JsonProperty("hearingIds") final List<UUID> hearingIds) {
        this.defendantId = defendantId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.offences = offences;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public static FoundHearingsForNewOffenceV2 foundHearingsForNewOffenceV2() {
        return new FoundHearingsForNewOffenceV2();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public List<uk.gov.justice.core.courts.Offence> getOffences() {
        return offences;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public FoundHearingsForNewOffenceV2 withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public FoundHearingsForNewOffenceV2 withProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public FoundHearingsForNewOffenceV2 withOffences(List<uk.gov.justice.core.courts.Offence> offences) {
        this.offences = offences;
        return this;
    }

    public FoundHearingsForNewOffenceV2 withHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }
}