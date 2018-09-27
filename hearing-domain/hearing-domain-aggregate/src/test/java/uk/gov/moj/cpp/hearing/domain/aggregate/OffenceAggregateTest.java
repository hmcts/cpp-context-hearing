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
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.PleaValue;
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
        LocalDate pleaDate = PAST_LOCAL_DATE.next();
        String value = PleaValue.GUILTY.toString();

        offenceAggregate.apply(
                new OffencePleaUpdated(
                        lookupPleaOnOffenceForHearingCommand.getHearingId(),
                        Plea.plea()
                                .withOffenceId(lookupPleaOnOffenceForHearingCommand.getOffenceId())
                                .withPleaDate(pleaDate)
                                .withPleaValue(PleaValue.GUILTY)
                                .build()));

        FoundPleaForHearingToInherit foundPleaForHearingToInherit =
                (FoundPleaForHearingToInherit) offenceAggregate.lookupPleaForHearing(lookupPleaOnOffenceForHearingCommand)
                        .collect(Collectors.toList()).get(1);

        assertThat(foundPleaForHearingToInherit.getHearingId(), is(lookupPleaOnOffenceForHearingCommand.getHearingId()));
        assertThat(foundPleaForHearingToInherit.getPlea().getOffenceId(), is(lookupPleaOnOffenceForHearingCommand.getOffenceId()));
        assertThat(foundPleaForHearingToInherit.getHearingId(), is(lookupPleaOnOffenceForHearingCommand.getHearingId()));
        assertThat(foundPleaForHearingToInherit.getPlea().getPleaDate(), is(pleaDate));
        assertThat(foundPleaForHearingToInherit.getPlea().getPleaValue().toString(), is(value));
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
        String value = PleaValue.GUILTY.toString();
        DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(UUID.randomUUID()).build();

        OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.GUILTY)
                        .withPleaDate(pleaDate)
                        .withOffenceId(offenceId)
                        .withDelegatedPowers(delegatedPowers)
                        .build())
                .build();

        List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPlea()).collect(Collectors.toList());

        assertThat(events.get(0), is(offenceAggregate.getPlea()));

        assertThat(offenceAggregate.getPlea().getHearingId(), is(hearingId));
        assertThat(offenceAggregate.getPlea().getPlea().getOffenceId(), is(offenceId));
        assertThat(offenceAggregate.getPlea().getPlea().getPleaDate(), is(pleaDate));
        assertThat(offenceAggregate.getPlea().getPlea().getPleaValue().toString(), is(value));
        assertThat(offenceAggregate.getPlea().getPlea().getDelegatedPowers().getLastName(), is(delegatedPowers.getLastName()));
    }

}
