package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

public class OffenceAggregateTest {

    @Test
    public void initiateHearingOffence_withPreviousPlea() {

        InitiateHearingOffenceCommand initiateHearingOffenceCommand = new InitiateHearingOffenceCommand(randomUUID(), randomUUID(), randomUUID(), randomUUID());
        UUID originHearingId = randomUUID();
        LocalDate pleaDate = PAST_LOCAL_DATE.next();
        String value = STRING.next();

        OffenceAggregate offenceAggregate = new OffenceAggregate();
        offenceAggregate.apply(new OffencePleaUpdated(originHearingId, initiateHearingOffenceCommand.getOffenceId(), pleaDate, value));

        InitiateHearingOffenceEnriched initiateHearingOffenceEnriched = (InitiateHearingOffenceEnriched) offenceAggregate.initiateHearingOffence(initiateHearingOffenceCommand).collect(Collectors.toList()).get(1);
        assertThat(initiateHearingOffenceEnriched.getOriginHearingId(), is(originHearingId));
        assertThat(initiateHearingOffenceEnriched.getOffenceId(), is(initiateHearingOffenceCommand.getOffenceId()));
        assertThat(initiateHearingOffenceEnriched.getDefendantId(), is(initiateHearingOffenceCommand.getDefendantId()));
        assertThat(initiateHearingOffenceEnriched.getCaseId(), is(initiateHearingOffenceCommand.getCaseId()));
        assertThat(initiateHearingOffenceEnriched.getHearingId(), is(initiateHearingOffenceCommand.getHearingId()));
        assertThat(initiateHearingOffenceEnriched.getPleaDate(), is(pleaDate));
        assertThat(initiateHearingOffenceEnriched.getValue(), is(value));
    }

    @Test
    public void initiateHearingOffence_withNoPreviousPlea() {

        InitiateHearingOffenceCommand initiateHearingOffenceCommand = new InitiateHearingOffenceCommand(randomUUID(), randomUUID(), randomUUID(), randomUUID());

        OffenceAggregate offenceAggregate = new OffenceAggregate();

        List<Object> events = offenceAggregate.initiateHearingOffence(initiateHearingOffenceCommand).collect(Collectors.toList());

        assertThat(events.get(0), not(offenceAggregate.getPlea()));
    }

    @Test
    public void updatePlea() {

        UUID offenceId = randomUUID();
        UUID hearingId = randomUUID();
        LocalDate pleaDate = PAST_LOCAL_DATE.next();
        String value = STRING.next();

        OffenceAggregate offenceAggregate = new OffenceAggregate();

        List<Object> events = offenceAggregate.updatePlea(hearingId, offenceId, pleaDate, value).collect(Collectors.toList());

        assertThat(events.get(0), is(offenceAggregate.getPlea()));

        assertThat(offenceAggregate.getPlea().getHearingId(), is(hearingId));
        assertThat(offenceAggregate.getPlea().getOffenceId(), is(offenceId));
        assertThat(offenceAggregate.getPlea().getPleaDate(), is(pleaDate));
        assertThat(offenceAggregate.getPlea().getValue(), is(value));
    }

}
