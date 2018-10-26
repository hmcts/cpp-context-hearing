package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import java.io.Serializable;
import java.util.stream.Stream;

@SuppressWarnings("squid:S1068")
public class AdjournHearingDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public AdjournHearingDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;

    }

    public Stream<Object> adjournHearing(final AdjournHearing adjournHearing) {
        return Stream.of(
                new HearingAdjourned(adjournHearing.getAdjournedHearing(), adjournHearing.getNextHearings()));

    }
}
