package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
@Event("hearing.nows-variants-saved")
public class NowsVariantsSavedEvent {

    private UUID hearingId;

    private List<Variant> variants;

    public UUID getHearingId() {
        return this.hearingId;
    }

    public List<Variant> getVariants() {
        return this.variants;
    }

    public NowsVariantsSavedEvent setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public NowsVariantsSavedEvent setVariants(List<Variant> variants) {
        this.variants = new ArrayList<>(variants);
        return this;
    }

    public static NowsVariantsSavedEvent nowsVariantsSavedEvent() {
        return new NowsVariantsSavedEvent();
    }
}
