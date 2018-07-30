package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;

import java.io.Serializable;
import java.util.List;

@Event("hearing.events.initiated")
public class HearingInitiated  implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Case> cases;
    private Hearing hearing;

    public HearingInitiated(){

    }

    @JsonCreator
    public HearingInitiated(
            @JsonProperty("cases") List<Case> cases,
            @JsonProperty("hearing") Hearing hearing){
        this.cases = cases;
        this.hearing = hearing;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Hearing getHearing() {
        return hearing;
    }
}
