package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;
import uk.gov.justice.core.courts.LaaReference;
import uk.gov.justice.domain.annotation.Event;

@Event("hearing.events.application-laareference-updated")
public class ApplicationLaareferenceUpdated implements Serializable {

    private static final long serialVersionUID = 2L;

    private final UUID hearingId;
    private final UUID applicationId;
    private final UUID subjectId;
    private final UUID offenceId;
    private final LaaReference laaReference;

    @JsonCreator
    public ApplicationLaareferenceUpdated(@JsonProperty("hearingId") final UUID hearingId, @JsonProperty("applicationId") final UUID applicationId, @JsonProperty("subjectId") final UUID subjectId, @JsonProperty("offenceId") final UUID offenceId, @JsonProperty("laaReference") final LaaReference laaReference) {
        this.hearingId = hearingId;
        this.laaReference = laaReference;
        this.applicationId = applicationId;
        this.subjectId = subjectId;
        this.offenceId = offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public LaaReference getLaaReference() {
        return laaReference;
    }
}