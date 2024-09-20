package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate.computeAllOffencesWithdrawnOrDismissed;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.NCESDecisionConstants;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.DefendantCaseWithdrawnOrDismissed;
import uk.gov.moj.cpp.hearing.domain.event.DefendantOffenceResultsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.nces.Defendant;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nces.DocumentContent;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    public void testUpdateDefendantWithFinancialOrderDetails_whenFirstShare() {

        runAndVerifyUpdateDefendantWithFinancialOrderDetails(
                FinancialOrderForDefendant.newBuilder().withResultDefinitionIds(Arrays.asList(
                        randomUUID(), NCESDecisionConstants.RD_FIDICI, randomUUID()
                )).build(), true);

        runAndVerifyUpdateDefendantWithFinancialOrderDetails(
                FinancialOrderForDefendant.newBuilder().withResultDefinitionIds(
                        null
                ).build(), false);

        runAndVerifyUpdateDefendantWithFinancialOrderDetails(
                FinancialOrderForDefendant.newBuilder().withResultDefinitionIds(Collections.emptyList()).build()
                , false);

        runAndVerifyUpdateDefendantWithFinancialOrderDetails(
                FinancialOrderForDefendant.newBuilder().withResultDefinitionIds(Arrays.asList(
                        UUID.randomUUID(), UUID.randomUUID()
                )).build(), false);

        runAndVerifyUpdateDefendantWithFinancialOrderDetails(
                FinancialOrderForDefendant.newBuilder().withResultDefinitionIds(Arrays.asList(
                        NCESDecisionConstants.RD_FIDICI, NCESDecisionConstants.RD_FIDICTI, NCESDecisionConstants.RD_FIDIPI
                )).build(), true);
    }

    private void runAndVerifyUpdateDefendantWithFinancialOrderDetails(final FinancialOrderForDefendant financialOrderForDefendant, boolean shouldGenerateMail) {
        // need junit5
        runAndVerifyUpdateDefendantWithFinancialOrderDetails(new DefendantAggregate(), financialOrderForDefendant, shouldGenerateMail);
    }

    @Test
    public void testUpdateDefendantWithFinancialOrderDetails_whenReShare_shouldMatch() {

        DefendantAggregate defendantAggregate = new DefendantAggregate();
        Defendant defendant = Defendant.defendant().build();
        UUID hearingId = randomUUID();
        defendantAggregate.apply(
                DefendantUpdateWithFinancialOrderDetails.newBuilder()
                        .withFinancialOrderForDefendant(FinancialOrderForDefendant.newBuilder()
                                .withHearingId(hearingId)
                                .withResultDefinitionIds(Arrays.asList(
                                        randomUUID(), randomUUID()
                                ))
                                .withDocumentContent(DocumentContent.documentContent()
                                        .withDefendant(defendant)
                                        .withGobAccountNumber("gobAccountNumber1")
                                        .withDivisionCode("divisionCode1")
                                        .withAmendmentType("type1")
                                        .build())
                                .build())
                        .build());

        FinancialOrderForDefendant financialOrderDetailsReshared = FinancialOrderForDefendant.newBuilder()
                .withHearingId(hearingId)
                .withResultDefinitionIds(Arrays.asList(
                        randomUUID(), randomUUID()
                ))
                .withDocumentContent(DocumentContent.documentContent()
                        .withDefendant(defendant)
                        .withGobAccountNumber("gobAccountNumber2")
                        .withDivisionCode("divisionCode2")
                        .withAmendmentType("type2")
                        .build())
                .build();

        DefendantUpdateWithFinancialOrderDetails result = runUpdateDefendantWithFinancialOrderDetails(defendantAggregate, financialOrderDetailsReshared);

//        assertEquals(result.isShouldGenerateMail(), true);
        FinancialOrderForDefendant resultFinancialOrder = result.getFinancialOrderForDefendant();
        assertEquals(hearingId, resultFinancialOrder.getHearingId());
        assertEquals("gobAccountNumber1", resultFinancialOrder.getDocumentContent().getOldGobAccountNumber());
        assertEquals("gobAccountNumber2", resultFinancialOrder.getDocumentContent().getGobAccountNumber());
        assertEquals("divisionCode1", resultFinancialOrder.getDocumentContent().getOldDivisionCode());
        assertEquals("divisionCode2", resultFinancialOrder.getDocumentContent().getDivisionCode());
        assertEquals("Amend result", resultFinancialOrder.getDocumentContent().getAmendmentType());
        assertEquals(defendant, resultFinancialOrder.getDocumentContent().getDefendant());
    }


    @Test
    public void testUpdateDefendantWithFinancialOrderDetails_whenReShare_shouldNotMatch() {

        DefendantAggregate defendantAggregate = new DefendantAggregate();
        Defendant defendant = Defendant.defendant().build();
        UUID hearingId = randomUUID();
        defendantAggregate.apply(
                DefendantUpdateWithFinancialOrderDetails.newBuilder()
                        .withFinancialOrderForDefendant(FinancialOrderForDefendant.newBuilder()
                                .withHearingId(hearingId)
                                .withResultDefinitionIds(Arrays.asList(
                                        randomUUID(), randomUUID()
                                ))
                                .withDocumentContent(DocumentContent.documentContent()
                                        .withDefendant(defendant)
                                        .withGobAccountNumber("gobAccountNumber1")
                                        .withDivisionCode("divisionCode1")
                                        .withAmendmentType("type1")
                                        .build())
                                .build())
                        .build());

        FinancialOrderForDefendant financialOrderDetailsReshared = FinancialOrderForDefendant.newBuilder()
                .withHearingId(hearingId)
                .withResultDefinitionIds(Arrays.asList(
                        randomUUID(), NCESDecisionConstants.RD_FIDIPI, randomUUID()
                ))
                .withDocumentContent(DocumentContent.documentContent()
                        .withDefendant(defendant)
                        .withGobAccountNumber("gobAccountNumber2")
                        .withDivisionCode("divisionCode2")
                        .withAmendmentType("type2")
                        .build())
                .build();

        DefendantUpdateWithFinancialOrderDetails result = runUpdateDefendantWithFinancialOrderDetails(defendantAggregate, financialOrderDetailsReshared);

        FinancialOrderForDefendant resultFinancialOrder = result.getFinancialOrderForDefendant();
        assertEquals(hearingId, resultFinancialOrder.getHearingId());
        assertEquals("gobAccountNumber1", resultFinancialOrder.getDocumentContent().getOldGobAccountNumber());
        assertEquals("gobAccountNumber2", resultFinancialOrder.getDocumentContent().getGobAccountNumber());
        assertEquals("divisionCode1", resultFinancialOrder.getDocumentContent().getOldDivisionCode());
        assertEquals("divisionCode2", resultFinancialOrder.getDocumentContent().getDivisionCode());
        assertEquals("Amend result", resultFinancialOrder.getDocumentContent().getAmendmentType());
        assertEquals(defendant, resultFinancialOrder.getDocumentContent().getDefendant());
    }


    private void runAndVerifyUpdateDefendantWithFinancialOrderDetails(DefendantAggregate defendantAggregate, FinancialOrderForDefendant financialOrderForDefendant, boolean expectedShouldGenerateMail) {

        DefendantUpdateWithFinancialOrderDetails result = runUpdateDefendantWithFinancialOrderDetails(defendantAggregate, financialOrderForDefendant);
//        assertEquals(expectedShouldGenerateMail, result.isShouldGenerateMail());
        if(expectedShouldGenerateMail)
            financialOrderForDefendant = FinancialOrderForDefendant.newBuilderFrom(financialOrderForDefendant)
                    .withDocumentContent(DocumentContent.newBuilderFrom(financialOrderForDefendant.getDocumentContent())
                            .withAmendmentType("Write off one day deemed served")
                            .build())
                    .build();
        assertEquals(financialOrderForDefendant, result.getFinancialOrderForDefendant());
    }

    private DefendantUpdateWithFinancialOrderDetails runUpdateDefendantWithFinancialOrderDetails(DefendantAggregate defendantAggregate, final FinancialOrderForDefendant financialOrderForDefendant) {
        List<Object> results = defendantAggregate.updateDefendantWithFinancialOrder(financialOrderForDefendant)
                .collect(Collectors.toList());

        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(defendantAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
        return (DefendantUpdateWithFinancialOrderDetails) results.get(0);

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

    @Test
    public void shouldNotRaiseFoundHearingIdForNewOffences_when_hearingIdIsNotAssociatedWithCase() {
        final List<Object> collect = defendantAggregate.lookupHearingsForNewOffenceOnDefendant(randomUUID(), randomUUID(), Offence.offence().build()).collect(toList());
        assertTrue(collect.isEmpty());
    }

    @Test
    public void shouldRaiseFoundHearingIdForNewOffences_when_hearingIdIsNotAssociatedWithCase() {
        Set<UUID> hearingIds = new HashSet<>();
        hearingIds.add(randomUUID());
        setField(defendantAggregate, "hearingIds", hearingIds);

        final List<Object> collect = defendantAggregate.lookupHearingsForNewOffenceOnDefendant(randomUUID(), randomUUID(), Offence.offence().build()).collect(toList());
        assertThat(collect.size(), is(1));
    }

    @Test
    public void shouldRaiseEventHearingDeletedForDefendant() {
        Set<UUID> hearingIds = new HashSet<>();
        hearingIds.add(randomUUID());
        setField(defendantAggregate, "hearingIds", hearingIds);
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final List<Object> eventStream = defendantAggregate.deleteHearingForDefendant(defendantId, hearingId).collect(toList());
        assertThat(eventStream.size(), is(1));
        final HearingDeletedForDefendant hearingDeletedForDefendant = (HearingDeletedForDefendant) eventStream.get(0);
        assertThat(hearingDeletedForDefendant.getHearingId(), is(hearingId));
        assertThat(hearingDeletedForDefendant.getDefendantId(), is(defendantId));
    }

    @Test
    public void shouldRaiseEventHearingAllocatedForDefendant() {
        Set<UUID> hearingIds = new HashSet<>();
        hearingIds.add(randomUUID());
        setField(defendantAggregate, "hearingIds", hearingIds);
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final List<Object> eventStream = defendantAggregate.removeHearingForDefendant(defendantId, hearingId).collect(toList());
        assertThat(eventStream.size(), is(1));
        final HearingRemovedForDefendant hearingRemovedForDefendant = (HearingRemovedForDefendant) eventStream.get(0);
        assertThat(hearingRemovedForDefendant.getHearingId(), is(hearingId));
        assertThat(hearingRemovedForDefendant.getDefendantId(), is(defendantId));
    }

    @Test
    public void shouldRemoveGrantedApplicationDetailsForDefendant() {
        Set<UUID> hearingIds = new HashSet<>();
        hearingIds.add(randomUUID());
        setField(defendantAggregate, "hearingIds", hearingIds);
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final List<Object> eventStream = defendantAggregate.removeHearingForDefendant(defendantId, hearingId).collect(toList());
        assertThat(eventStream.size(), is(1));
        final HearingRemovedForDefendant hearingRemovedForDefendant = (HearingRemovedForDefendant) eventStream.get(0);
        assertThat(hearingRemovedForDefendant.getHearingId(), is(hearingId));
        assertThat(hearingRemovedForDefendant.getDefendantId(), is(defendantId));
    }

    @Test
    public void testHearingIdsRemovedWhenMarkedForDuplicateOrRemoveOrDelete() {

        final UUID defendantId = randomUUID();
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();

        Set<UUID> hearingIds = new HashSet<>();
        hearingIds.add(hearingId1);
        hearingIds.add(hearingId2);
        hearingIds.add(hearingId1);

        setField(defendantAggregate, "hearingIds", hearingIds);

        final HearingMarkedAsDuplicateForDefendant hearingMarkedAsDuplicateForDefendant =
                new HearingMarkedAsDuplicateForDefendant(defendantId, hearingId1);

        defendantAggregate.apply(hearingMarkedAsDuplicateForDefendant);

        assertThat(getValueOfField(defendantAggregate, "hearingIds", HashSet.class).size(), is(1));
        assertThat(getValueOfField(defendantAggregate, "hearingIds", HashSet.class).stream().findFirst().get(), is(hearingId2));
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
