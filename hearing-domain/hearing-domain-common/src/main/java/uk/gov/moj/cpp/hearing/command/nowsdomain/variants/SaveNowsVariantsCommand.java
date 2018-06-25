package uk.gov.moj.cpp.hearing.command.nowsdomain.variants;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class SaveNowsVariantsCommand implements Serializable {

    private UUID hearingId;

    private List<Variant> variants;

    public static SaveNowsVariantsCommand saveNowsVariantsCommand() {
        return new SaveNowsVariantsCommand();
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public SaveNowsVariantsCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public List<Variant> getVariants() {
        return this.variants;
    }

    public SaveNowsVariantsCommand setVariants(List<Variant> variants) {
        this.variants = variants;
        return this;
    }
}
