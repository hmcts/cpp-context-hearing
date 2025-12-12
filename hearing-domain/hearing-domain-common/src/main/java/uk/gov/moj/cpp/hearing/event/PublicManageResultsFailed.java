package uk.gov.moj.cpp.hearing.event;


import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.ResultsError;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class PublicManageResultsFailed {
    private final UUID hearingId;

    private final HearingState hearingState;

    private final ResultsError error;

    private final Map<String, Object> info;

    public PublicManageResultsFailed(final UUID hearingId, final HearingState hearingState,
                                     final ResultsError error, final Map<String, Object> info) {
        this.hearingId = hearingId;
        this.hearingState = hearingState;
        this.error = error;
        this.info = info;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public ResultsError getError() {
        return error;
    }

    public Map<String, Object> getInfo() {
        return info;
    }
}
