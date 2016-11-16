package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacated;

import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.STATE.LISTED;
import static uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.STATE.NO_STATE;
import static uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate.STATE.VACATED;

public class HearingAggregate implements Aggregate {

    enum STATE {NO_STATE, LISTED, VACATED}

    private STATE state = NO_STATE;

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingAggregate.class);

    public Stream<Object> listHearing(ListHearing listHearing) {

        if (state != NO_STATE) {
            LOGGER.warn("Hearing in wrong state:" + state);
            return Stream.empty();
        }

        return Stream.of(new HearingListed(
                listHearing.getHearingId(),
                listHearing.getCaseId(),
                listHearing.getHearingType(),
                listHearing.getCourtCentreName(),
                listHearing.getStartDateOfHearing(),
                listHearing.getDuration()));
    }

    public Stream<Object> vacateHearing(VacateHearing vacateHearing) {

        if (state != LISTED) {
            LOGGER.warn("Hearing in wrong state:" + state);
            return Stream.empty();
        }

        return Stream.of(new HearingVacated(vacateHearing.getHearingId()));
    }

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(HearingListed.class)
                        .apply(e -> state = LISTED),
                when(HearingVacated.class)
                        .apply(e -> state = VACATED)
        );
    }

}
