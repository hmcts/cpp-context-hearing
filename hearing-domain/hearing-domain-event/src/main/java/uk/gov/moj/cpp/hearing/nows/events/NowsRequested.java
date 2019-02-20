package uk.gov.moj.cpp.hearing.nows.events;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.CreateNowsRequest;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.nows-requested")
public class NowsRequested implements Serializable {

    private static final long serialVersionUID = 2L;

    private final CreateNowsRequest createNowsRequest;

    private final UUID requestId;

    private String accountNumber;

    @JsonCreator
    public NowsRequested(@JsonProperty("requestId") final UUID requestId,
                         @JsonProperty("createNowsRequest") final CreateNowsRequest createNowsRequest,
                         @JsonProperty("accountNumber") final String accountNumber) {
        this.createNowsRequest = createNowsRequest;
        this.requestId = requestId;
        this.accountNumber = accountNumber;
    }

    public CreateNowsRequest getCreateNowsRequest() { return createNowsRequest; }

    public UUID getRequestId() {
        return requestId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
