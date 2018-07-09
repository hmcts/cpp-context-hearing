package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class VariantDirectoryDelegate {

    private final HearingAggregateMomento momento;

    public VariantDirectoryDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleNowsVariantsSavedEvent(NowsVariantsSavedEvent nowsVariantsSavedEvent){
        nowsVariantsSavedEvent.getVariants().forEach(
                variant -> this.momento.getVariantDirectory().put(new NewModelHearingAggregate.VariantKeyHolder(variant.getKey()), variant)
        );
    }


    public void handleNowsMaterialStatusUpdatedEvent(final NowsMaterialStatusUpdated nowsMaterialStatusUpdated) {
        this.momento.getVariantDirectory().forEach((variantKeyHolder, variant) -> {
            if (variant.getValue().getMaterialId().equals(nowsMaterialStatusUpdated.getMaterialId())) {
                variant.getValue().setStatus(VariantStatus.GENERATED);
            }
        });
    }

}
