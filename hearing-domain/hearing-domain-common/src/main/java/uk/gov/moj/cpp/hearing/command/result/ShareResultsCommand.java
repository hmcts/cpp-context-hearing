package uk.gov.moj.cpp.hearing.command.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public final class ShareResultsCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private List<SharedResultsCommandResultLine> resultLines;

    private uk.gov.justice.json.schemas.core.CourtClerk courtClerk;

    public ShareResultsCommand() {
    }

    @JsonCreator
    private ShareResultsCommand(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("courtClerk") final uk.gov.justice.json.schemas.core.CourtClerk courtClerk,
            @JsonProperty("resultLines") final List<SharedResultsCommandResultLine> resultLines) {
        this.hearingId = hearingId;
        this.courtClerk = courtClerk;
        this.resultLines = resultLines;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<SharedResultsCommandResultLine> getResultLines() {
        return resultLines;
    }

    public uk.gov.justice.json.schemas.core.CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public ShareResultsCommand setCourtClerk(final uk.gov.justice.json.schemas.core.CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public ShareResultsCommand setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public ShareResultsCommand setResultLines(final List<SharedResultsCommandResultLine> resultLines) {
        this.resultLines = resultLines;
        return this;
    }

    public static ShareResultsCommand shareResultsCommand() {
        return new ShareResultsCommand();
    }
}