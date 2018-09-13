package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class DefendantAggregateTest {

    @InjectMocks
    private DefendantAggregate defendantAggregate;

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(defendantAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void testRegisteringHearing() {

        final RegisterHearingAgainstDefendantCommand expected = RegisterHearingAgainstDefendantCommand.builder()
                .withDefendantId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        final RegisteredHearingAgainstDefendant result = (RegisteredHearingAgainstDefendant) defendantAggregate.registerHearing(expected).collect(Collectors.toList()).get(0);

        assertThat(result.getDefendantId(), is(expected.getDefendantId()));
        assertThat(result.getHearingId(), is(expected.getHearingId()));
    }

    @Test
    public void testRecordCaseDefendantDetailsWithHearings() {

        final UUID previousHearingId = randomUUID();

        final CaseDefendantDetailsCommand command = CaseDefendantDetailsCommand.caseDefendantDetailsCommand()
                .setDefendant(defendantTemplate());

        final RegisteredHearingAgainstDefendant registerDefendantWithHearingCommand = RegisteredHearingAgainstDefendant.builder()
                .withDefendantId(randomUUID())
                .withHearingId(previousHearingId)
                .build();

        defendantAggregate.apply(registerDefendantWithHearingCommand);

        final CaseDefendantDetailsWithHearings result =
                (CaseDefendantDetailsWithHearings) defendantAggregate.enrichCaseDefendantDetailsWithHearingIds(command).collect(Collectors.toList()).get(0);

        assertThat(result.getHearingIds(), hasItems(previousHearingId));
    }
}