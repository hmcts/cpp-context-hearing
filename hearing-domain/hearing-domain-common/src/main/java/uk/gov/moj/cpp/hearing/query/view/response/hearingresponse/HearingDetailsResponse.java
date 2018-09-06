package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.Hearing;

public class HearingDetailsResponse {

    private Hearing hearing;

    public HearingDetailsResponse() {
        this.hearing = null;
    }

    @JsonCreator
    public HearingDetailsResponse(@JsonProperty("hearing") final Hearing hearing) {
        this.hearing = hearing;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }
}