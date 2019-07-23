package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Target;

import java.io.Serializable;
import java.util.List;


public class PendingNowsRequestedCommand implements Serializable {

    private static final long serialVersionUID = 2L;

    private CreateNowsRequest createNowsRequest;

    private List<Target> targets;

    private PendingNowsRequestedCommand() {
    }

    @JsonCreator
    public PendingNowsRequestedCommand(
            @JsonProperty("createNowsRequest") final CreateNowsRequest createNowsRequest,
            @JsonProperty("targets") final List<Target> targets) {
        this.createNowsRequest = createNowsRequest;
        this.targets = targets;
    }


    public CreateNowsRequest getCreateNowsRequest() {
        return createNowsRequest;
    }

    public static PendingNowsRequestedCommand pendingNowsRequestedCommand() {
        return new PendingNowsRequestedCommand();
    }

    public PendingNowsRequestedCommand setCreateNowsRequest(final CreateNowsRequest createNowsRequest) {
        this.createNowsRequest = createNowsRequest;
        return this;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public PendingNowsRequestedCommand setTargets(List<Target> targets) {
        this.targets = targets;
        return this;
    }
}
