package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.defendant-details-updated-after-results-published")
public class DefendantDetailsUpdatedAfterResultPublished implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private Defendant defendant;

    public DefendantDetailsUpdatedAfterResultPublished() {
    }

    @JsonCreator
    public DefendantDetailsUpdatedAfterResultPublished(
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
}
