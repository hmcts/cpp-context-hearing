package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate.computeAllOffencesWithdrawnOrDismissed;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.DefendantCaseWithdrawnOrDismissed;
import uk.gov.moj.cpp.hearing.domain.event.DefendantOffenceResultsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

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

        final RegisteredHearingAgainstDefendant result = (RegisteredHearingAgainstDefendant) defendantAggregate.registerHearing(expected.getDefendantId(), expected.getHearingId()).collect(Collectors.toList()).get(0);

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
                (CaseDefendantDetailsWithHearings) defendantAggregate.enrichCaseDefendantDetailsWithHearingIds(command.getDefendant()).collect(Collectors.toList()).get(0);

        assertThat(result.getHearingIds(), hasItems(previousHearingId));
    }


    @Test
    public void testUpdateOffenceResults_WhenResultsAreSharedInTwoHearings() {

        //test setup
        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();
        List<UUID> offences = Arrays.asList(randomUUID(), randomUUID(), randomUUID());

        //when
        Stream<Object> returnedEvents = defendantAggregate.updateOffenceResults(defendantId, caseId, offences,
                mapBuilder()
                        .put(offences.get(0), OffenceResult.WITHDRAWN)
                        .put(offences.get(1), OffenceResult.ADJOURNED)
                        .put(offences.get(2), OffenceResult.WITHDRAWN)
                        .build());
        //then
        List<Object> listOfEvents = assertStreamHasSize(returnedEvents, 1);
        final DefendantOffenceResultsUpdated event1 = (DefendantOffenceResultsUpdated) listOfEvents.stream()
                .filter(o -> o instanceof DefendantOffenceResultsUpdated)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        verifyDefendantOffenceResultsUpdated(
                event1,
                defendantId, offences,
                allOf(
                        mapHasSize(3),
                        hasEntry(offences.get(0), OffenceResult.WITHDRAWN),
                        hasEntry(offences.get(1), OffenceResult.ADJOURNED),
                        hasEntry(offences.get(2), OffenceResult.WITHDRAWN)
                )
        );

        //when
        returnedEvents = defendantAggregate.updateOffenceResults(defendantId, caseId, offences,
                mapBuilder()
                        .put(offences.get(1), OffenceResult.DISMISSED)
                        .build());
        //then
        listOfEvents = assertStreamHasSize(returnedEvents, 2);
        assertThat(listOfEvents, containsInAnyOrder(instanceOf(DefendantOffenceResultsUpdated.class), instanceOf(DefendantCaseWithdrawnOrDismissed.class)));
        final DefendantOffenceResultsUpdated event2 = (DefendantOffenceResultsUpdated) listOfEvents.stream()
                .filter(o -> o instanceof DefendantOffenceResultsUpdated)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        verifyDefendantOffenceResultsUpdated(
                event2,
                defendantId, offences,
                allOf(
                        mapHasSize(1),
                        hasEntry(offences.get(1), OffenceResult.DISMISSED))
        );
        final DefendantCaseWithdrawnOrDismissed event3 = (DefendantCaseWithdrawnOrDismissed) listOfEvents.stream()
                .filter(o -> o instanceof DefendantCaseWithdrawnOrDismissed)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        verifyDefendantCaseWithdrawnOrDismissed(event3, defendantId, caseId, allOf(
                hasEntry(offences.get(0), OffenceResult.WITHDRAWN),
                hasEntry(offences.get(2), OffenceResult.WITHDRAWN),
                hasEntry(offences.get(1), OffenceResult.DISMISSED)
        ));


    }

    @Test
    public void testUpdateOffenceResults_WhenAllResultsAreSharedInASingleHearing_AndFoundGuiltyOfOneOffence() {

        //test setup
        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();
        List<UUID> offences = Arrays.asList(randomUUID(), randomUUID(), randomUUID());

        //when
        Stream<Object> returnedEvents = defendantAggregate.updateOffenceResults(defendantId, caseId, offences,
                mapBuilder()
                        .put(offences.get(0), OffenceResult.WITHDRAWN)
                        .put(offences.get(2), OffenceResult.GUILTY)
                        .put(offences.get(1), OffenceResult.DISMISSED)
                        .build());
        //then
        List<Object> listOfEvents = assertStreamHasSize(returnedEvents, 1);
        assertThat(listOfEvents, contains(instanceOf(DefendantOffenceResultsUpdated.class)));
        final DefendantOffenceResultsUpdated event = (DefendantOffenceResultsUpdated) listOfEvents.stream()
                .filter(o -> o instanceof DefendantOffenceResultsUpdated)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        verifyDefendantOffenceResultsUpdated(
                event,
                defendantId, offences,
                allOf(
                        mapHasSize(3),
                        hasEntry(offences.get(0), OffenceResult.WITHDRAWN),
                        hasEntry(offences.get(2), OffenceResult.GUILTY),
                        hasEntry(offences.get(1), OffenceResult.DISMISSED))
        );

    }

    @Test
    public void testUpdateOffenceResults_WhenAllResultsAreSharedInASingleHearing() {

        //test setup
        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();
        List<UUID> offences = Arrays.asList(randomUUID(), randomUUID(), randomUUID());

        //when
        Stream<Object> returnedEvents = defendantAggregate.updateOffenceResults(defendantId, caseId, offences,
                mapBuilder()
                        .put(offences.get(0), OffenceResult.WITHDRAWN)
                        .put(offences.get(2), OffenceResult.WITHDRAWN)
                        .put(offences.get(1), OffenceResult.DISMISSED)
                        .build());
        //then
        List<Object> listOfEvents = assertStreamHasSize(returnedEvents, 2);
        assertThat(listOfEvents, containsInAnyOrder(instanceOf(DefendantOffenceResultsUpdated.class), instanceOf(DefendantCaseWithdrawnOrDismissed.class)));
        final DefendantOffenceResultsUpdated event1 = (DefendantOffenceResultsUpdated) listOfEvents.stream()
                .filter(o -> o instanceof DefendantOffenceResultsUpdated)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        verifyDefendantOffenceResultsUpdated(
                event1,
                defendantId, offences,
                allOf(
                        mapHasSize(3),
                        hasEntry(offences.get(0), OffenceResult.WITHDRAWN),
                        hasEntry(offences.get(2), OffenceResult.WITHDRAWN),
                        hasEntry(offences.get(1), OffenceResult.DISMISSED))
        );
        final DefendantCaseWithdrawnOrDismissed event2 = (DefendantCaseWithdrawnOrDismissed) listOfEvents.stream()
                .filter(o -> o instanceof DefendantCaseWithdrawnOrDismissed)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        verifyDefendantCaseWithdrawnOrDismissed(event2, defendantId, caseId, allOf(
                hasEntry(offences.get(0), OffenceResult.WITHDRAWN),
                hasEntry(offences.get(2), OffenceResult.WITHDRAWN),
                hasEntry(offences.get(1), OffenceResult.DISMISSED)
        ));

    }

    private void verifyDefendantCaseWithdrawnOrDismissed(final DefendantCaseWithdrawnOrDismissed event, final UUID defendantId, final UUID caseId, final Matcher resultedOffencesMatcher) {
        assertThat(event.getDefendantId(), is(defendantId));
        assertThat(event.getCaseId(), is(caseId));
        assertThat(event.getResultedOffences(), resultedOffencesMatcher);
    }

    private static List<Object> assertStreamHasSize(final Stream<Object> returnedEvents, final int size) {
        final List<Object> collection = returnedEvents.collect(toList());
        assertThat(collection, hasSize(size));
        return collection;
    }

    private void verifyDefendantOffenceResultsUpdated(final DefendantOffenceResultsUpdated event, final UUID defendantId, final List<UUID> offences, final Matcher resultedOffencesMatcher) {
        assertThat(event.getDefendantId(), is(defendantId));
        assertThat(event.getOffenceIds(), containsInAnyOrder(offences.toArray()));
        assertThat(event.getResultedOffences(),
                resultedOffencesMatcher);
    }

    @Test
    public void testComputeAllOffencesWithdrawnOrDismissed() {
        testMapWithdrawnDismissed(new HashMap<>(), is(false));
        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.GUILTY)
                        .build(),
                is(false));
        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.ADJOURNED)
                        .build(),
                is(false));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.WITHDRAWN)
                        .build(),
                is(true));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.DISMISSED)
                        .build(),
                is(true));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.GUILTY)
                        .put(randomUUID(), OffenceResult.ADJOURNED)
                        .build(),
                is(false));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.GUILTY)
                        .put(randomUUID(), OffenceResult.WITHDRAWN)
                        .build(),
                is(false));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.GUILTY)
                        .put(randomUUID(), OffenceResult.DISMISSED)
                        .build(),
                is(false));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.WITHDRAWN)
                        .put(randomUUID(), OffenceResult.DISMISSED)
                        .build(),
                is(true));

        testMapWithdrawnDismissed(mapBuilder()
                        .put(randomUUID(), OffenceResult.WITHDRAWN)
                        .put(randomUUID(), OffenceResult.DISMISSED)
                        .put(randomUUID(), OffenceResult.ADJOURNED)
                        .build(),
                is(false));

    }

    public void testMapWithdrawnDismissed(final Map map, final Matcher matcher) {
        assertThat(computeAllOffencesWithdrawnOrDismissed(map),
                matcher);
    }

    private ImmutableMap.Builder<UUID, OffenceResult> mapBuilder() {
        return ImmutableMap.<UUID, OffenceResult>builder();
    }

    public static Matcher<Map> mapHasSize(final int size) {
        return new TypeSafeMatcher<Map>() {
            @Override
            public boolean matchesSafely(Map map) {
                return map.size() == size;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" has ").appendValue(size).appendText(" key/value pairs");
            }
        };
    }

}