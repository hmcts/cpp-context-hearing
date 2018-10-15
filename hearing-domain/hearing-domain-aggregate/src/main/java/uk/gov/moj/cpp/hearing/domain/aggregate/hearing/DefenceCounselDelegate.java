package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;

import java.io.Serializable;
import java.util.stream.Stream;

public class DefenceCounselDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public DefenceCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefenceCounselUpsert(DefenceCounselUpsert defenceCounselUpsert) {
        this.momento.getDefenceCounsels().put(defenceCounselUpsert.getAttendeeId(), defenceCounselUpsert);
    }

    public Stream<Object> addDefenceCounsel(final DefenceCounselUpsert defenceCounselUpsert) {
        return Stream.of(defenceCounselUpsert);
    }

}
