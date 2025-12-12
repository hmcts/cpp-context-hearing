package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;


public class NowResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("hearingId")
    private UUID hearingId;

    public NowResponse(final UUID id, final UUID hearingId) {
        this.id = id;
        this.hearingId = hearingId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
