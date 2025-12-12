package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
@Event("hearing.events.results-shared-success")
public class ResultsSharedSuccess implements Serializable {

    private static final long serialVersionUID = -7978961436932712730L;

    private UUID hearingId;

    public UUID getHearingId() {
        return hearingId;
    }

    @JsonCreator
    public ResultsSharedSuccess(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public ResultsSharedSuccess() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;

        public ResultsSharedSuccess.Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ResultsSharedSuccess build() {
            return new ResultsSharedSuccess(hearingId);
        }
    }
}