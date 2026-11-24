package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots.Reason.DROPPED_FROM_DRAFT;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots;

import java.util.List;

import org.junit.jupiter.api.Test;

class HearingAggregateReleaseProvisionalHearingSlotsTest {

    @Test
    void shouldIgnoreAReleaseEventLeftInTheStreamByReserveASlot() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = standardInitiateHearingTemplate().getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));
        final Hearing initiated = hearingAggregate.getHearing();

        hearingAggregate.apply(new ReleaseProvisionalHearingSlots(hearing.getId(), List.of(randomUUID().toString()), DROPPED_FROM_DRAFT));

        assertThat(hearingAggregate.getHearing(), is(sameInstance(initiated)));
    }
}
