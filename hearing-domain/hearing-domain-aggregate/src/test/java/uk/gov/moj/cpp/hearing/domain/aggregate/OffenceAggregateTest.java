package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Test;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.moj.cpp.hearing.command.initiate.LookupPleaOnOffenceForHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OffenceAggregateTest {

    OffenceAggregate offenceAggregate = new OffenceAggregate();

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(offenceAggregate);
        } catch (SerializationException e) {
            e.printStackTrace();
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void initiateHearingOffence_withPreviousPlea() {

        LookupPleaOnOffenceForHearingCommand lookupPleaOnOffenceForHearingCommand = new LookupPleaOnOffenceForHearingCommand(randomUUID(), randomUUID(), randomUUID(), randomUUID());
        UUID originHearingId = randomUUID();
        LocalDate pleaDate = PAST_LOCAL_DATE.next();
        String value = STRING.next();

        offenceAggregate.apply(new OffencePleaUpdated(originHearingId, lookupPleaOnOffenceForHearingCommand.getOffenceId(), pleaDate, value));

        FoundPleaForHearingToInherit foundPleaForHearingToInherit = (FoundPleaForHearingToInherit) offenceAggregate.lookupPleaForHearing(lookupPleaOnOffenceForHearingCommand).collect(Collectors.toList()).get(1);
        assertThat(foundPleaForHearingToInherit.getOriginHearingId(), is(originHearingId));
        assertThat(foundPleaForHearingToInherit.getOffenceId(), is(lookupPleaOnOffenceForHearingCommand.getOffenceId()));
        assertThat(foundPleaForHearingToInherit.getDefendantId(), is(lookupPleaOnOffenceForHearingCommand.getDefendantId()));
        assertThat(foundPleaForHearingToInherit.getCaseId(), is(lookupPleaOnOffenceForHearingCommand.getCaseId()));
        assertThat(foundPleaForHearingToInherit.getHearingId(), is(lookupPleaOnOffenceForHearingCommand.getHearingId()));
        assertThat(foundPleaForHearingToInherit.getPleaDate(), is(pleaDate));
        assertThat(foundPleaForHearingToInherit.getValue(), is(value));
    }

    @Test
    public void initiateHearingOffence_withNoPreviousPlea() {

        LookupPleaOnOffenceForHearingCommand lookupPleaOnOffenceForHearingCommand = new LookupPleaOnOffenceForHearingCommand(randomUUID(), randomUUID(), randomUUID(), randomUUID());

        List<Object> events = offenceAggregate.lookupPleaForHearing(lookupPleaOnOffenceForHearingCommand).collect(Collectors.toList());

        assertThat(events.get(0), not(offenceAggregate.getPlea()));
    }

    @Test
    public void updatePlea() {

        UUID offenceId = randomUUID();
        UUID hearingId = randomUUID();
        LocalDate pleaDate = PAST_LOCAL_DATE.next();
        String value = STRING.next();
        DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(UUID.randomUUID()).build();

        OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withPleaDate(pleaDate)
                .withValue(value)
                .withDelegatedPowers(delegatedPowers)
                .build();

        List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated).collect(Collectors.toList());

        assertThat(events.get(0), is(offenceAggregate.getPlea()));

        assertThat(offenceAggregate.getPlea().getHearingId(), is(hearingId));
        assertThat(offenceAggregate.getPlea().getOffenceId(), is(offenceId));
        assertThat(offenceAggregate.getPlea().getPleaDate(), is(pleaDate));
        assertThat(offenceAggregate.getPlea().getValue(), is(value));
        assertThat(offenceAggregate.getPlea().getDelegatedPowers().getLastName(), is(delegatedPowers.getLastName()));
    }

}
