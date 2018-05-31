package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

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

        RegisterHearingAgainstDefendantCommand expected = RegisterHearingAgainstDefendantCommand.builder()
                .withDefendantId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        RegisteredHearingAgainstDefendant result = (RegisteredHearingAgainstDefendant) defendantAggregate.registerHearing(expected).collect(Collectors.toList()).get(0);

        assertThat(result.getDefendantId(), is(expected.getDefendantId()));
        assertThat(result.getHearingId(), is(expected.getHearingId()));
    }

    @Test
    public void testRecordCaseDefendantDetailsWithHearings() {

        final UUID previousHearingId = randomUUID();

        final Address.Builder address = Address.address()
                .withAddress1(STRING.next())
                .withAddress2(STRING.next())
                .withAddress3(STRING.next())
                .withAddress4(STRING.next())
                .withPostcode(STRING.next());

        final CaseDefendantDetailsCommand command = CaseDefendantDetailsCommand.builder()
                .withCaseId(randomUUID())
                .withDefendant(Defendant.builder()
                        .withId(randomUUID())
                        .withPerson(Person.builder().withId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .withNationality(STRING.next())
                        .withGender(STRING.next())
                        .withAddress(address)
                        .withDateOfBirth(PAST_LOCAL_DATE.next()))
                        .withBailStatus(STRING.next())
                        .withCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                        .withDefenceOrganisation(STRING.next())
                        .withInterpreter(Interpreter.builder(STRING.next())))
                .build();

        RegisteredHearingAgainstDefendant registerDefendantWithHearingCommand = RegisteredHearingAgainstDefendant.builder()
                .withDefendantId(randomUUID())
                .withHearingId(previousHearingId)
                .build();

        defendantAggregate.apply(registerDefendantWithHearingCommand);

        CaseDefendantDetailsWithHearings result = (CaseDefendantDetailsWithHearings) defendantAggregate.enrichCaseDefendantDetailsWithHearingIds(command).collect(Collectors.toList()).get(0);

        assertThat(result.getHearingIds(), hasItems(previousHearingId));
    }
}