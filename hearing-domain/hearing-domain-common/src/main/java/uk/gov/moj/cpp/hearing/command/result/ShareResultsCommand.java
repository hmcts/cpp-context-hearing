package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@SuppressWarnings("squid:S2384")
public final class ShareResultsCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private final CourtClerk courtClerk;

    private final List<UncompletedResultLine> uncompletedResultLines;

    private final List<CompletedResultLine> completedResultLines;

    @JsonCreator
    private ShareResultsCommand(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("courtClerk") final CourtClerk courtClerk,
            @JsonProperty("uncompletedResultLines") final List<UncompletedResultLine> uncompletedResultLines,
            @JsonProperty("completedResultLines") final List<CompletedResultLine> completedResultLines) {
        this.hearingId = hearingId;
        this.courtClerk = courtClerk;
        this.uncompletedResultLines = ofNullable(uncompletedResultLines).orElseGet(ArrayList::new);
        this.completedResultLines = ofNullable(completedResultLines).orElseGet(ArrayList::new);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public List<UncompletedResultLine> getUncompletedResultLines() {
        return uncompletedResultLines;
    }

    public List<CompletedResultLine> getCompletedResultLines() {
        return completedResultLines;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public static final class Builder {

        private UUID hearingId;

        private CourtClerk courtClerk;

        private List<UncompletedResultLine> uncompletedResultLines;

        private List<CompletedResultLine> completedResultLines;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withCourtClerk(final CourtClerk courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public Builder withUncompletedResultLines(final List<UncompletedResultLine> uncompletedResultLines) {
            this.uncompletedResultLines = uncompletedResultLines;
            return this;
        }

        public Builder withCompletedResultLines(final List<CompletedResultLine> completedResultLines) {
            this.completedResultLines = completedResultLines;
            return this;
        }

        public ShareResultsCommand build() {
            return new ShareResultsCommand(hearingId, courtClerk, uncompletedResultLines, completedResultLines);
        }
    }
}