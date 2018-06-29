package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S2384")
public class ShareResultsMessage {

    private Hearing hearing;

    private ZonedDateTime sharedTime;

    private List<Variant> variants;

    public List<Variant> getVariants() {
        return variants;
    }

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

    public ShareResultsMessage setVariants(final List<Variant> variants) {
        this.variants = new ArrayList<>(variants);
        return this;
    }

    public static ShareResultsMessage shareResultsMessage(){
        return new ShareResultsMessage();
    }
}
