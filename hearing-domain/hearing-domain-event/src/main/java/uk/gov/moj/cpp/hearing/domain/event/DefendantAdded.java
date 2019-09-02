package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.defendant-added")
public class DefendantAdded implements Serializable {

    private static final long serialVersionUID = 1493964448765706069L;

    private UUID hearingId;

    private Defendant defendant;

    public DefendantAdded() {
    }

    @JsonCreator
    public DefendantAdded(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("defendant") Defendant defendant) {
        super();
        this.hearingId = hearingId;
        this.defendant = defendant;
    }

    public static DefendantAdded caseDefendantAdded() {
        return new DefendantAdded();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public DefendantAdded setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public DefendantAdded setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }
}
