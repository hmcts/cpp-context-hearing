package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.ResultLine;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Event("hearing.results-shared")
public class ResultsShared {

    private UUID hearingId;
    private ZonedDateTime sharedTime;
    private List<ResultLine> resultLines;

    public ResultsShared(final UUID hearingId, final ZonedDateTime sharedTime, final List<ResultLine> resultLines) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.resultLines = resultLines;
    }

    public ResultsShared() {
        // default constructor for Jackson serialisation
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }
}
