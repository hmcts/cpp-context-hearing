package uk.gov.moj.cpp.hearing.message.shareResults;

public class ShareResultsMessage {

    private Hearing hearing;

    public Hearing getHearing() {
        return hearing;
    }

    public ShareResultsMessage setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public static ShareResultsMessage shareResultsMessage(){
        return new ShareResultsMessage();
    }
}
