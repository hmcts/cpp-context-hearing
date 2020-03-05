package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.core.courts.Plea.plea;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated.builder;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.FoundVerdictForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OffenceAggregateTest {

    private OffenceAggregate offenceAggregate;

    @Before
    public void setUp() {
        offenceAggregate = new OffenceAggregate();
    }

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

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final PleaValue value = PleaValue.GUILTY;

        final RegisteredHearingAgainstOffence registeredHearingAgainstOffence = RegisteredHearingAgainstOffence.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId).build();

        offenceAggregate.apply(
                new OffencePleaUpdated(
                        registeredHearingAgainstOffence.getHearingId(),
                        pleaModel().withPlea(plea()
                                .withOffenceId(registeredHearingAgainstOffence.getOffenceId())
                                .withPleaDate(pleaDate)
                                .withPleaValue(PleaValue.GUILTY)
                                .build()).build()));

        final FoundPleaForHearingToInherit foundPleaForHearingToInherit =
                (FoundPleaForHearingToInherit) offenceAggregate.lookupOffenceForHearing(
                        registeredHearingAgainstOffence.getHearingId(),
                        registeredHearingAgainstOffence.getOffenceId())
                        .collect(Collectors.toList()).get(1);

        assertThat(foundPleaForHearingToInherit.getHearingId(), is(registeredHearingAgainstOffence.getHearingId()));
        assertThat(foundPleaForHearingToInherit.getPlea().getOffenceId(), is(registeredHearingAgainstOffence.getOffenceId()));
        assertThat(foundPleaForHearingToInherit.getPlea().getPleaDate(), is(pleaDate));
        assertThat(foundPleaForHearingToInherit.getPlea().getPleaValue(), is(value));
    }

    @Test
    public void initiateHearingOffence_withPreviousVerdict() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final boolean unanimous = BOOLEAN.next();
        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        final RegisteredHearingAgainstOffence registeredHearingAgainstOffence = RegisteredHearingAgainstOffence.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId).build();

        offenceAggregate.apply(
                new OffenceVerdictUpdated(registeredHearingAgainstOffence.getHearingId(),
                        Verdict.verdict()
                                .withVerdictDate(PAST_LOCAL_DATE.next())
                                .withVerdictType(VerdictType.verdictType()
                                        .withId(randomUUID())
                                        .withCategory(STRING.next())
                                        .withCategoryType(TestTemplates.VerdictCategoryType.GUILTY.name())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                                .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                        .withOffenceDefinitionId(randomUUID())
                                        .withOffenceCode(STRING.next())
                                        .withOffenceTitle(STRING.next())
                                        .withOffenceTitleWelsh(STRING.next())
                                        .withOffenceLegislation(STRING.next())
                                        .withOffenceLegislationWelsh(STRING.next())
                                        .build())
                                .withJurors(Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(numberOfSplitJurors)
                                        .withUnanimous(unanimous)
                                        .build())
                                .withOffenceId(offenceId)
                                .withOriginatingHearingId(hearingId)
                                .build()
                ));

        final FoundVerdictForHearingToInherit foundVerdictForHearingToInherit =
                (FoundVerdictForHearingToInherit) offenceAggregate.lookupOffenceForHearing(
                        registeredHearingAgainstOffence.getHearingId(),
                        registeredHearingAgainstOffence.getOffenceId())
                        .collect(Collectors.toList()).get(1);

        assertThat(foundVerdictForHearingToInherit.getHearingId(), is(registeredHearingAgainstOffence.getHearingId()));
        assertThat(foundVerdictForHearingToInherit.getVerdict().getOffenceId(), is(registeredHearingAgainstOffence.getOffenceId()));
    }

    @Test
    public void initiateHearingOffence_withNoPreviousPlea() {
        final RegisteredHearingAgainstOffence registeredHearingAgainstOffence = RegisteredHearingAgainstOffence.builder()
                .withHearingId(randomUUID())
                .withOffenceId(randomUUID()).build();

        final List<Object> events = offenceAggregate.lookupOffenceForHearing(
                registeredHearingAgainstOffence.getHearingId(),
                registeredHearingAgainstOffence.getOffenceId()).collect(Collectors.toList());

        assertThat(events.get(0), not(offenceAggregate.getPlea()));
    }

    @Test
    public void updatePlea() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final String value = PleaValue.GUILTY.toString();
        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(UUID.randomUUID()).build();

        final OffencePleaUpdated offencePleaUpdated = builder()
                .withHearingId(hearingId)
                .withPleaModel(pleaModel().withPlea(plea()
                        .withPleaValue(PleaValue.GUILTY)
                        .withPleaDate(pleaDate)
                        .withOffenceId(offenceId)
                        .withDelegatedPowers(delegatedPowers)
                        .build()).build())
                .build();

        final List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPleaModel()).collect(Collectors.toList());

        assertThat(events.get(0), is(offenceAggregate.getPlea()));

        assertThat(offenceAggregate.getPlea().getHearingId(), is(hearingId));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getOffenceId(), is(offenceId));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getPleaDate(), is(pleaDate));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getPleaValue().toString(), is(value));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getDelegatedPowers().getLastName(), is(delegatedPowers.getLastName()));
    }

    @Test
    public void shouldReturn_emptyStream_whenEditOffence_hearingIds_notFound() {
        Stream<Object> objectStream = offenceAggregate.lookupHearingsForEditOffenceOnOffence(randomUUID(), Offence.offence().build());
        assertThat(objectStream.findAny(), is(Optional.empty()));
    }

    @Test
    public void shouldReturn_stream_whenEditOffence_hearingIds_areFound() {
        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));
        Stream<Object> objectStream = offenceAggregate.lookupHearingsForEditOffenceOnOffence(randomUUID(), Offence.offence().build());
        assertTrue(objectStream.findAny().isPresent());
    }

    @Test
    public void shouldReturn_emptyStream_whenDeleteOffence_hearingIds_notFound() {
        Stream<Object> objectStream = offenceAggregate.lookupHearingsForDeleteOffenceOnOffence(randomUUID());
        assertThat(objectStream.findAny(), is(Optional.empty()));
    }

    @Test
    public void shouldReturn_stream_whenDeleteOffence_hearingIds_areFound() {
        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));
        Stream<Object> objectStream = offenceAggregate.lookupHearingsForEditOffenceOnOffence(randomUUID(), Offence.offence().build());
        assertTrue(objectStream.findAny().isPresent());
    }
}
