package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class HearingTrialTypeDelegateTest {

    private HearingAggregateMomento momento = new HearingAggregateMomento();
    private HearingDelegate hearingDelegate = new HearingDelegate(momento);
    private HearingTrialTypeDelegate hearingTrialTypeDelegate = new HearingTrialTypeDelegate(momento);

    @Test
    public void shouldHandleHearingTrialVacated() {
        final UUID hearingId = UUID.randomUUID();
        final UUID vacatedTrialReasonId = UUID.randomUUID();
        final String code = "A";
        final String type = "Vacated";
        final String description = "Vacated Trial";
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        final List<Object> eventStream = hearingTrialTypeDelegate.setTrialType(hearingId, vacatedTrialReasonId, code, type, description).collect(toList());

        assertThat(eventStream.size(), is(1));

        final HearingTrialVacated hearingTrialVacated = (HearingTrialVacated) eventStream.get(0);
        assertThat(hearingTrialVacated.getHearingId(), is(hearingId));
        assertThat(hearingTrialVacated.getVacatedTrialReasonId(), is(vacatedTrialReasonId));
        assertThat(hearingTrialVacated.getCode(), is(code));
        assertThat(hearingTrialVacated.getType(), is(type));
        assertThat(hearingTrialVacated.getDescription(), is(description));
        assertThat(hearingTrialVacated.getCourtCentreId(), is(hearing.getCourtCentre().getId()));
    }
}
