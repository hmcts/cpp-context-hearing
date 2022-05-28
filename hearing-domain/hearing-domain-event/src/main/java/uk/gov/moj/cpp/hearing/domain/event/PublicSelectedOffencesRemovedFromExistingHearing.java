package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import uk.gov.justice.domain.annotation.Event;

@Event("public.hearing.selected-offences-removed-from-existing-hearing")
public class PublicSelectedOffencesRemovedFromExistingHearing implements Serializable {

    private static final long serialVersionUID = -4529051147739803041L;

    private UUID hearingId;

    private List<UUID> offenceIds;

    @JsonCreator
    public PublicSelectedOffencesRemovedFromExistingHearing(@JsonProperty("hearingId") final UUID hearingId,
                                                            @JsonProperty("offenceIds") final List<UUID> offenceIds) {

        this.hearingId = hearingId;
        this.offenceIds = offenceIds;

    }

    public UUID getHearingId() {
        return hearingId;
    }


    public List<UUID> getOffenceIds() {
        return offenceIds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId, this.offenceIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PublicSelectedOffencesRemovedFromExistingHearing that = (PublicSelectedOffencesRemovedFromExistingHearing) o;
        return Objects.equals(this.hearingId, that.hearingId)
                && Objects.equals(this.offenceIds, that.offenceIds);
    }

}
