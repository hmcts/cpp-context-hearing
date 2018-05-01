package uk.gov.moj.cpp.hearing.command.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class ShareResultsCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private List<ResultLine> resultLines;

    @JsonCreator
    protected ShareResultsCommand(@JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("resultLines") final List<ResultLine> resultLines) {
        this.hearingId = hearingId;
        this.resultLines = unmodifiableList(ofNullable(resultLines).orElseGet(ArrayList::new));
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public void setResultLines(List<ResultLine> resultLines) {
        this.resultLines = resultLines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ShareResultsCommand that = (ShareResultsCommand) o;
        return Objects.equals(this.hearingId, that.hearingId) && Objects.equals(this.resultLines, that.resultLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId, this.resultLines);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;
        private List<ResultLine> resultLines;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withResultLines(final List<ResultLine> resultLines) {
            this.resultLines = resultLines;
            return this;
        }

        public ShareResultsCommand build() {
            return new ShareResultsCommand(this.hearingId, this.resultLines);
        }
    }
}