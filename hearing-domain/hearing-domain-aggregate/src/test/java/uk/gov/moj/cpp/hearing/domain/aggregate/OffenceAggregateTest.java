package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static uk.gov.justice.core.courts.Plea.plea;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated.builder;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.hearing.domain.event.EnrichAssociatedHearingsWithIndicatedPlea;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdatePleaWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdateVerdictWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForOffence;
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
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OffenceAggregateTest {

    private static final String GUILTY = "GUILTY";

    private OffenceAggregate offenceAggregate;

    @BeforeEach
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

        final List<Object> events = offenceAggregate.lookupOffenceForHearing(
                registeredHearingAgainstOffence.getHearingId(),
                registeredHearingAgainstOffence.getOffenceId())
                .collect(Collectors.toList());

        assertThat(events.get(0), not(offenceAggregate.getVerdict()));
    }

    @Test
    public void initiateHearingOffence_withNoPreviousPlea() {
        final RegisteredHearingAgainstOffence registeredHearingAgainstOffence = RegisteredHearingAgainstOffence.builder()
                .withHearingId(randomUUID())
                .withOffenceId(randomUUID()).build();

        final List<Object> events = offenceAggregate.lookupOffenceForHearing(
                registeredHearingAgainstOffence.getHearingId(),
                registeredHearingAgainstOffence.getOffenceId()).collect(Collectors.toList());

        assertThat(events.size(), is(1));
        assertThat(events.get(0), instanceOf(RegisteredHearingAgainstOffence.class));
    }

    @Test
    public void updatePlea() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(UUID.randomUUID()).build();

        final OffencePleaUpdated offencePleaUpdated = builder()
                .withHearingId(hearingId)
                .withPleaModel(pleaModel().withPlea(plea()
                        .withPleaValue(GUILTY)
                        .withPleaDate(pleaDate)
                        .withOffenceId(offenceId)
                        .withDelegatedPowers(delegatedPowers)
                        .build()).build())
                .build();

        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));

        final List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPleaModel()).collect(Collectors.toList());

        assertThat(events.get(0), is(offenceAggregate.getPlea()));
        assertThat(events.size(), is(2));
        assertThat(events.get(0), is(instanceOf(OffencePleaUpdated.class)));
        assertThat(events.get(1), is(instanceOf(EnrichUpdatePleaWithAssociatedHearings.class)));
        assertThat(offenceAggregate.getPlea().getHearingId(), is(hearingId));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getOffenceId(), is(offenceId));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getPleaDate(), is(pleaDate));
        assertThat(offenceAggregate.getPlea().getPleaModel().getPlea().getPleaValue().toString(), is(GUILTY));
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
    public void shouldReturn_stream_whenDeleteOffence_hearingIds_found() {
        final UUID offenceId = randomUUID();
        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));
        final Stream<Object> objectStream = offenceAggregate.lookupHearingsForDeleteOffenceOnOffence(offenceId);
        final List<Object> event = objectStream.collect(toList());
        assertThat(event, is(notNullValue()));
        assertThat(event.size(), is(1));
        final FoundHearingsForDeleteOffence foundHearingsForDeleteOffence = (FoundHearingsForDeleteOffence) event.get(0);
        assertThat(foundHearingsForDeleteOffence, is(notNullValue()));
        assertThat(foundHearingsForDeleteOffence.getId(), is(offenceId));
    }

    @Test
    public void shouldReturn_stream_whenDeleteOffence_hearingIds_areFound() {
        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));
        Stream<Object> objectStream = offenceAggregate.lookupHearingsForEditOffenceOnOffence(randomUUID(), Offence.offence().build());
        assertTrue(objectStream.findAny().isPresent());
    }

    @Test
    public void shouldRaiseEventHearingDeletedForOffence() {
        setField(offenceAggregate, "hearingIds", singletonList(randomUUID()));
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final List<Object> eventStream = offenceAggregate.deleteHearingForOffence(offenceId, hearingId).collect(toList());
        assertThat(eventStream.size(), CoreMatchers.is(1));
        final HearingDeletedForOffence hearingDeleted = (HearingDeletedForOffence) eventStream.get(0);
        assertThat(hearingDeleted.getHearingId(), CoreMatchers.is(hearingId));
        assertThat(hearingDeleted.getOffenceId(), CoreMatchers.is(offenceId));
    }

    @Test
    public void updatePlea_shouldNotEnrichPleaWithAssociatedHearings() {

        final UUID hearingId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = builder()
                .withHearingId(hearingId)
                .withPleaModel(pleaModel().build())
                .build();

        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));

        final List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPleaModel()).collect(Collectors.toList());

        assertNotNull(events.get(0));
        assertThat(events.size(), is(1));
        assertThat(events.get(0), is(instanceOf(OffencePleaUpdated.class)));
    }


    @Test
    public void updatePlea_shouldEnrichPleaWithAssociatedHearings_WhenPleaValueIsPresent() {

        final UUID hearingId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = builder()
                .withHearingId(hearingId)
                .withPleaModel(pleaModel().withPlea(plea()
                        .withPleaValue(GUILTY)
                        .build()).build())
                .build();

        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));

        final List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPleaModel()).collect(Collectors.toList());

        assertNotNull(events.get(0));
        assertThat(events.size(), is(2));
        assertThat(events.get(0), is(instanceOf(OffencePleaUpdated.class)));
        assertThat(events.get(1), is(instanceOf(EnrichUpdatePleaWithAssociatedHearings.class)));

    }

    @Test
    public void updatePlea_shouldNotEnrichPleaWithAssociatedHearings_WhenPleaValueIsNull() {

        final UUID hearingId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = builder()
                .withHearingId(hearingId)
                .withPleaModel(pleaModel().withPlea(plea()
                        .build()).build())
                .build();

        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));

        final List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPleaModel()).collect(Collectors.toList());

        assertNotNull(events.get(0));
        assertThat(events.size(), is(1));
        assertThat(events.get(0), is(instanceOf(OffencePleaUpdated.class)));
    }


    @Test
    public void updateIndicatedPlea_shouldEnrichIndicatedPleaWithAssociatedHearings_WhenPleaValueIsPresent() {

        final UUID hearingId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = builder()
                .withHearingId(hearingId)
                .withPleaModel(pleaModel().withIndicatedPlea(IndicatedPlea.indicatedPlea().withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY)
                        .build()).build())
                .build();

        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(randomUUID()));

        final List<Object> events = offenceAggregate.updatePlea(offencePleaUpdated.getHearingId(), offencePleaUpdated.getPleaModel()).collect(Collectors.toList());

        assertNotNull(events.get(0));
        assertThat(events.size(), is(2));
        assertThat(events.get(0), is(instanceOf(OffencePleaUpdated.class)));
        assertThat(events.get(1), is(instanceOf(EnrichAssociatedHearingsWithIndicatedPlea.class)));
    }

    @Test
    public void shouldUpdateVerdict(){
        final boolean unanimous = BOOLEAN.next();
        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID connectedHearingId = randomUUID();
        ReflectionUtil.setField(offenceAggregate, "hearingIds", Collections.singletonList(connectedHearingId));
        final Verdict verdict = Verdict.verdict()
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
                .build();
        final Stream<Object> objectStream = offenceAggregate.updateVerdict(hearingId, verdict);
        final List<Object> verdictStream = objectStream.collect(toList());
        assertThat(verdictStream.size(), is(2));

        OffenceVerdictUpdated offenceVerdictUpdated = (OffenceVerdictUpdated) verdictStream.get(0);
        EnrichUpdateVerdictWithAssociatedHearings enrichUpdateVerdictWithAssociatedHearings =
                (EnrichUpdateVerdictWithAssociatedHearings) verdictStream.get(1);
        assertThat(offenceVerdictUpdated.getHearingId(), is(hearingId));
        assertThat(enrichUpdateVerdictWithAssociatedHearings.getHearingIds().contains(connectedHearingId), is(true));
    }

    @Test
    public void shouldMarkHearingAsDuplicate(){
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final Stream<Object> objectStream = offenceAggregate.markHearingAsDuplicate(offenceId, hearingId);
        final List<Object> events = objectStream.collect(toList());
        assertThat(events.size(), is(1));
        HearingMarkedAsDuplicateForOffence hearingMarkedAsDuplicateForOffence = (HearingMarkedAsDuplicateForOffence) events.get(0);
        assertThat(hearingMarkedAsDuplicateForOffence.getHearingId(), is(hearingId));
        assertThat(hearingMarkedAsDuplicateForOffence.getOffenceId(), is(offenceId));
    }

    @Test
    public void shouldRemoveHearingForOffence(){
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final Stream<Object> objectStream = offenceAggregate.removeHearingForOffence(offenceId, hearingId);
        final List<Object> events = objectStream.collect(toList());
        assertThat(events.size(), is(1));
        HearingRemovedForOffence removedForOffence = (HearingRemovedForOffence) events.get(0);
        assertThat(removedForOffence.getHearingId(), is(hearingId));
        assertThat(removedForOffence.getOffenceId(), is(offenceId));
    }

}
