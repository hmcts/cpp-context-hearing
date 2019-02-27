package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.Target;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
@Event("hearing.draft-result-saved")
public class DraftResultSaved implements Serializable {

    private static final long serialVersionUID = 1L;

    private Target target;

    @JsonCreator
    public DraftResultSaved(@JsonProperty("target") final Target target) {
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }
}