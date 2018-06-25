package uk.gov.moj.cpp.hearing.message.shareResults;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S2384")
public class ShareResultsMessage {

    private Hearing hearing;

    private ZonedDateTime sharedTime;

    private List<Variant> variantDirectory;

    public List<Variant> getVariantDirectory() {
        return variantDirectory;
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

    public ShareResultsMessage setVariantDirectory(final List<Variant> variantDirectory) {
        this.variantDirectory = new ArrayList<>(variantDirectory);
        return this;
    }

    public static ShareResultsMessage shareResultsMessage(){
        return new ShareResultsMessage();
    }
}
