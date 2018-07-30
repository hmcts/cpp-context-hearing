package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class VariantDirectoryDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public VariantDirectoryDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleNowsVariantsSavedEvent(NowsVariantsSavedEvent nowsVariantsSavedEvent) {
        Set<Variant> variants = new HashSet<>(this.momento.getVariantDirectory());
        variants.addAll(nowsVariantsSavedEvent.getVariants());
        this.momento.setVariantDirectory(variants);
    }


    public void handleNowsMaterialStatusUpdatedEvent(final NowsMaterialStatusUpdated nowsMaterialStatusUpdated) {
        this.momento.getVariantDirectory().stream()
                .filter(variant -> variant.getValue().getMaterialId().equals(nowsMaterialStatusUpdated.getMaterialId()))
                .findFirst()
                .ifPresent(variant -> variant.getValue().setStatus(VariantStatus.GENERATED));
    }

}
