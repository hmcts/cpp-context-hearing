package uk.gov.moj.cpp.hearing.nows.events;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.pending-nows-requested")
public class PendingNowsRequested implements Serializable {

    private static final long serialVersionUID = 2L;

    private CreateNowsRequest createNowsRequest;

    private List<Target> targets;

    @JsonCreator
    public PendingNowsRequested(@JsonProperty("createNowsRequest") final CreateNowsRequest createNowsRequest,
                                @JsonProperty("targets") final List<Target> targets) {
        this.createNowsRequest = createNowsRequest;
        this.targets = targets;
    }

    public CreateNowsRequest getCreateNowsRequest() {
        return this.createNowsRequest;
    }

    public List<Target> getTargets() {
        return targets;
    }
}