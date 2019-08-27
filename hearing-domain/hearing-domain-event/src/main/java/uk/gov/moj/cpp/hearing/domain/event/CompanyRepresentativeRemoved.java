package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.util.UUID;

@Event("hearing.company-representative-removed")
public class CompanyRepresentativeRemoved implements Serializable {

    private static final long serialVersionUID = 5358547414304909583L;

    private final UUID id;
    private final UUID hearingId;

    public CompanyRepresentativeRemoved(final UUID id, final UUID hearingId) {
        this.id = id;
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getId() {
        return id;
    }

}
