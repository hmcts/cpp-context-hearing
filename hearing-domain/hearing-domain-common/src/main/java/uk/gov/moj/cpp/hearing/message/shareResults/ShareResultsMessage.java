package uk.gov.moj.cpp.hearing.message.shareResults;

import java.util.List;

public class ShareResultsMessage {

    private List<Defendant> defendants;


    public List<Defendant> getDefendants() {
        return defendants;
    }

    public ShareResultsMessage setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }

    public static ShareResultsMessage shareResultsMessage(){
        return new ShareResultsMessage();
    }
}
