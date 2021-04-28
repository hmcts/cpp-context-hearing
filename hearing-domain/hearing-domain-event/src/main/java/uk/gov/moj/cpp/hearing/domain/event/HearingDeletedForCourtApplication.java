package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.UUID;

@Event("hearing.events.hearing-deleted-for-court-application")
public class HearingDeletedForCourtApplication implements Serializable {

    private static final long serialVersionUID = -3814470529901781337L;

    private UUID courtApplicationId;

    private UUID hearingId;

    @JsonCreator
    public HearingDeletedForCourtApplication(@JsonProperty("courtApplicationId") final UUID courtApplicationId,
                                             @JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
        this.courtApplicationId = courtApplicationId;
    }

    public UUID getCourtApplicationId() {
        return courtApplicationId;
    }

    public void setCourtApplicationId(final UUID courtApplicationId) {
        this.courtApplicationId = courtApplicationId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }
}
