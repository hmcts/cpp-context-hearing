package uk.gov.moj.cpp.hearing.nows.events;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.pending-nows-requested")
public class PendingNowsRequested implements Serializable {

    private static final long serialVersionUID = 2L;

    private CreateNowsRequest createNowsRequest;

    @JsonCreator
    public PendingNowsRequested(@JsonProperty("createNowsRequest") final CreateNowsRequest createNowsRequest) {
        this.createNowsRequest = createNowsRequest;
    }

    public CreateNowsRequest getCreateNowsRequest() { return this.createNowsRequest; }

}