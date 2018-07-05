package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;

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
}
