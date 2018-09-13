package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.defendant-details-updated")
public class DefendantDetailsUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Defendant defendant;

    public DefendantDetailsUpdated() {
    }

    @JsonCreator
    public DefendantDetailsUpdated(
            @JsonProperty("hearingId") UUID hearingId,
            @JsonProperty("defendant") Defendant defendant) {
        super();
        this.hearingId = hearingId;
        this.defendant = defendant;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public DefendantDetailsUpdated setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public DefendantDetailsUpdated setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }

    public static DefendantDetailsUpdated defendantDetailsUpdated() {
        return new DefendantDetailsUpdated();
    }
}
