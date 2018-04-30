package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.ZonedDateTime;

public class ShareResultsMessage {

    private Hearing hearing;

    private ZonedDateTime sharedTime;

    public Hearing getHearing() {
        return hearing;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public ShareResultsMessage setHearing(Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public ShareResultsMessage setSharedTime(ZonedDateTime sharedTime) {
        this.sharedTime = sharedTime;
        return this;
    }

    public static ShareResultsMessage shareResultsMessage(){
        return new ShareResultsMessage();
    }
}
