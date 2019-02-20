package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import uk.gov.justice.core.courts.CreateNowsRequest;

import java.io.Serializable;


public class GenerateNowsCommand implements Serializable {

    private static final long serialVersionUID = 2L;

    private CreateNowsRequest createNowsRequest;

    public static GenerateNowsCommand generateNowsCommand() {
        return new GenerateNowsCommand();
    }

    public CreateNowsRequest getCreateNowsRequest() {
        return createNowsRequest;
    }

    public void setCreateNowsRequest(CreateNowsRequest nowsRequest) {
        this.createNowsRequest = nowsRequest;
    }
}
