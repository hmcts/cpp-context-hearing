package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdjournHearingDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public AdjournHearingDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;

    }

    public void handleHearingAdjournedEvent(final HearingAdjourned hearingAdjourned) {
        momento.setAdjournedHearingIds(hearingAdjourned.getHearings().stream().map(uk.gov.moj.cpp.external.domain.progression.relist.Hearing::getId).collect(Collectors.toList()));
    }

    public Stream<Object> adjournHearing(final AdjournHearing adjournHearing) {

        return Stream.of(
                new HearingAdjourned(adjournHearing.getCaseId(), adjournHearing.getUrn(), adjournHearing.getHearings().stream().map(hearing -> new uk.gov.moj.cpp.external.domain.progression.relist.Hearing(
                        momento.getAdjournedHearingIds().stream().findFirst().orElse(UUID.randomUUID()),
                        hearing.getCourtCentreId(),
                        hearing.getType(),
                        hearing.getStartDate(),
                        hearing.getStartTime(),
                        hearing.getEstimateMinutes(),
                        hearing.getDefendants()
                )).collect(Collectors.toList())));

    }
}
