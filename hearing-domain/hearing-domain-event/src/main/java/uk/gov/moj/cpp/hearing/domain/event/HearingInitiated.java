package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Hearing;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.initiated")
public class HearingInitiated implements Serializable {

    private static final long serialVersionUID = 1L;

    private Hearing hearing;

    @JsonCreator
    public HearingInitiated(@JsonProperty("hearing") Hearing hearing) {
        this.hearing = hearing;
    }

    public Hearing getHearing() {
        return hearing;
    }
}