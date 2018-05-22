package uk.gov.moj.cpp.hearing.nows.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;

@Event("hearing.events.nows-requested")
public class NowsRequested implements Serializable {
    private final static long serialVersionUID = -6266542081056693028L;

    private Hearing hearing;

    @JsonCreator
    public NowsRequested(@JsonProperty("hearing") Hearing hearing) {
        this.hearing = hearing;
    }

    public Hearing getHearing() {
        return hearing;
    }
}
