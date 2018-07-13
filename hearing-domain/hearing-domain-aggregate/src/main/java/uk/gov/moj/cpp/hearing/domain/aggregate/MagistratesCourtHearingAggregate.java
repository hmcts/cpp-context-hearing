package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

public class MagistratesCourtHearingAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private MagsCourtHearingRecorded magsCourtHearingRecorded;

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(MagsCourtHearingRecorded.class).apply(magsCourtHearingRecorded -> {
                    this.magsCourtHearingRecorded = magsCourtHearingRecorded;
                }),

                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiate(MagsCourtHearingRecorded magsCourtHearingRecorded) {
        return apply(Stream.of(magsCourtHearingRecorded));
    }
}
