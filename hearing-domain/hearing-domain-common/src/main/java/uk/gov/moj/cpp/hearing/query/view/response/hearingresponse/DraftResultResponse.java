package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import javax.json.JsonObject;

@SuppressWarnings("squid:S2384")
public class DraftResultResponse {

    private JsonObject payload;
    private boolean target;

    public DraftResultResponse() {
    }

    public DraftResultResponse(final JsonObject payload, final boolean target) {
        this.payload = payload;
        this.target = target;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public void setPayload(final JsonObject payload) {
        this.payload = payload;
    }

    public boolean isTarget() {
        return target;
    }

    public void setTarget(final boolean target) {
        this.target = target;
    }
}
