package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.CourtClerk;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public final class ShareResultsCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private uk.gov.justice.json.schemas.core.CourtClerk courtClerk;

    @JsonCreator
    private ShareResultsCommand(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("courtClerk") final uk.gov.justice.json.schemas.core.CourtClerk courtClerk) {
        this.hearingId = hearingId;
        this.courtClerk = courtClerk;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public uk.gov.justice.json.schemas.core.CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public ShareResultsCommand setCourtClerk(uk.gov.justice.json.schemas.core.CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ShareResultsCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    //TODO GPE-5480 delete this builder
    public static final class Builder {

        private UUID hearingId;

        private uk.gov.justice.json.schemas.core.CourtClerk courtClerk;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withCourtClerk(final CourtClerk courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public ShareResultsCommand build() {
            return new ShareResultsCommand(hearingId, courtClerk);
        }
    }
}