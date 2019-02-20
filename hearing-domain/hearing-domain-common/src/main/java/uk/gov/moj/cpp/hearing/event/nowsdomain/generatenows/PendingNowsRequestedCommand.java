package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.CreateNowsRequest;

import java.io.Serializable;


public class PendingNowsRequestedCommand implements Serializable {

    private static final long serialVersionUID = 2L;

    private CreateNowsRequest createNowsRequest;

    private PendingNowsRequestedCommand() {
    }

    @JsonCreator
    public PendingNowsRequestedCommand(@JsonProperty("createNowsRequest") final CreateNowsRequest createNowsRequest) {
        this.createNowsRequest = createNowsRequest;
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

}
