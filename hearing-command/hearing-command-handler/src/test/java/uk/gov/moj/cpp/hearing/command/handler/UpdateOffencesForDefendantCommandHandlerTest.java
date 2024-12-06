package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.addOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.deleteOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffenceV2;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffenceV2;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffenceV2;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAddedV2;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdatedV2;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.domain.event.RemoveOffencesFromExistingHearing;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UpdateOffencesForDefendantCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            FoundHearingsForNewOffenceV2.class,
            FoundHearingsForEditOffence.class,
            FoundHearingsForEditOffenceV2.class,
            FoundHearingsForDeleteOffence.class,
            FoundHearingsForDeleteOffenceV2.class,
            OffenceAdded.class,
            OffenceAddedV2.class,
            OffenceUpdated.class,
            OffenceUpdatedV2.class,
            OffenceDeleted.class,
            OffenceDeletedV2.class,
            OffencesRemovedFromExistingHearing.class
    );

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private UpdateOffencesForDefendantCommandHandler updateOffencesForDefendantCommandHandler;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceAddEvent() throws EventStreamException {

        UUID defendantId = randomUUID();

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(addOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), defendantId).setOffencesToAdd(singletonList(randomUUID()))), u -> {
                    u.setUpdatedOffences(emptyList()).setDeletedOffences(emptyList());
                }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-offences-for-defendant"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));

        final DefendantAggregate defendantAggregate = new DefendantAggregate();
        final UUID hearingId = randomUUID();
        setField(defendantAggregate, "hearingIds", ImmutableSet.of(hearingId));
        setupMockedEventStream(defendantId, this.eventStream, defendantAggregate);

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-new-offence-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offences.length()", is(1)),
                                withJsonPath("$.offences[*].id", hasItem(caseDefendantOffencesChanged.getFirstOffenceFromAddedOffences().getId().toString())),
                                withJsonPath("$.defendantId", is(caseDefendantOffencesChanged.getFirstAddedOffences().getDefendantId().toString())),
                                withJsonPath("$.prosecutionCaseId", is(caseDefendantOffencesChanged.getFirstAddedOffences().getProsecutionCaseId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_DoNotSend_OffenceAddEvent() throws EventStreamException {

        UUID defendantId = randomUUID();

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(addOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), defendantId).setOffencesToAdd(singletonList(randomUUID()))), u -> {
                    u.setUpdatedOffences(emptyList()).setDeletedOffences(emptyList());
                }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-offences-for-defendant"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));

        final DefendantAggregate defendantAggregate = new DefendantAggregate();
        setupMockedEventStream(defendantId, this.eventStream, defendantAggregate);

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(this.eventStream.size(), is(0L));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceUpdateAddEvent() throws EventStreamException {
        UUID defendantId = randomUUID();
        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(updateOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), defendantId).setOffencesToUpdate(singletonList(randomUUID()))), u -> {
                    u.setAddedOffences(emptyList()).setDeletedOffences(emptyList());
                }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offences"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));

        final DefendantAggregate defendantAggregate = new DefendantAggregate();
        final UUID hearingId = randomUUID();
        ReflectionUtil.setField(defendantAggregate, "hearingIds", Set.of(hearingId));
        setupMockedEventStream(defendantId, this.eventStream, defendantAggregate);

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-edit-offence-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offences[*].id", hasItem(caseDefendantOffencesChanged.getFirstOffenceFromUpdatedOffences().getId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_FoundHearingsForEditOffenceEvent() throws EventStreamException {
        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(updateOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), randomUUID()).setOffencesToUpdate(singletonList(randomUUID()))), u -> {
                    u.setAddedOffences(emptyList()).setDeletedOffences(emptyList());
                }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-offences-for-defendant"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));

        final DefendantAggregate defendantAggregate = new DefendantAggregate();
        final UUID hearingId = randomUUID();
        ReflectionUtil.setField(defendantAggregate, "hearingIds", Set.of(hearingId));
        setupMockedEventStream(caseDefendantOffencesChanged.it().getUpdatedOffences().get(0).getDefendantId(), this.eventStream, defendantAggregate);

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-edit-offence-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offences[*].id", hasItem(caseDefendantOffencesChanged.getFirstOffenceFromUpdatedOffences().getId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceDeleteEvent() throws EventStreamException {
        UUID defendantId = randomUUID();
        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(deleteOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), defendantId).setOffenceToDelete(singletonList(randomUUID()))), u -> {
                    u.setAddedOffences(emptyList()).setUpdatedOffences(emptyList());
                }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offences"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));
        final DefendantAggregate defendantAggregate = new DefendantAggregate();
        final UUID hearingId = randomUUID();
        ReflectionUtil.setField(defendantAggregate, "hearingIds", Set.of(hearingId));

        setupMockedEventStream(defendantId, this.eventStream, defendantAggregate);

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-delete-offence-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.ids[*]", hasItem(caseDefendantOffencesChanged.getFirstOffenceIdFromDeletedOffences().toString()))
                        )))));
    }

    @Test
    public void testAddOffenceForExistingHearing() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForNewOffence foundHearingsForNewOffence = FoundHearingsForNewOffence.foundHearingsForNewOffence()
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .withDefendantId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId())
                .withProsecutionCaseId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId())
                .withOffence(Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.add-new-offence-to-hearings"), objectToJsonObjectConverter.convert(foundHearingsForNewOffence));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.addOffenceForExistingHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(foundHearingsForNewOffence.getHearingIds().get(0).toString())),
                                withJsonPath("$.defendantId", is(foundHearingsForNewOffence.getDefendantId().toString())),
                                withJsonPath("$.prosecutionCaseId", is(foundHearingsForNewOffence.getProsecutionCaseId().toString())),
                                withJsonPath("$.offence.id", is(foundHearingsForNewOffence.getOffence().getId().toString()))
                        )))
        ));
    }

    @Test
    public void testAddOffenceForExistingHearingV2() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForNewOffenceV2 foundHearingsForNewOffence = FoundHearingsForNewOffenceV2.foundHearingsForNewOffenceV2()
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .withDefendantId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId())
                .withProsecutionCaseId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId())
                .withOffences(singletonList(Offence.offence()
                        .withId(randomUUID())
                        .build()));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.add-new-offence-to-hearings-v2"), objectToJsonObjectConverter.convert(foundHearingsForNewOffence));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.addOffenceForExistingHearingV2(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-added-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(foundHearingsForNewOffence.getHearingIds().get(0).toString())),
                                withJsonPath("$.defendantId", is(foundHearingsForNewOffence.getDefendantId().toString())),
                                withJsonPath("$.prosecutionCaseId", is(foundHearingsForNewOffence.getProsecutionCaseId().toString())),
                                withJsonPath("$.offences[*].id", hasItem(foundHearingsForNewOffence.getOffences().get(0).getId().toString()))
                        )))
        ));
    }

    @Test
    public void testUpdateOffence() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForEditOffence foundHearingsForEditOffence = FoundHearingsForEditOffence.foundHearingsForEditOffence()
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .withDefendantId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offence"),
                objectToJsonObjectConverter.convert(foundHearingsForEditOffence));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.updateOffence(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.defendantId", is(foundHearingsForEditOffence.getDefendantId().toString())),
                                withJsonPath("$.offence.id", is(foundHearingsForEditOffence.getOffence().getId().toString()))
                        )))));
    }

    @Test
    public void testUpdateOffenceV2() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForEditOffenceV2 foundHearingsForEditOffence = FoundHearingsForEditOffenceV2.foundHearingsForEditOffenceV2()
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .withDefendantId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId())
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build(), uk.gov.justice.core.courts.Offence.offence()
                        .withId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId())
                        .build()));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offence-v2"),
                objectToJsonObjectConverter.convert(foundHearingsForEditOffence));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.updateOffenceV2(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-updated-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.defendantId", is(foundHearingsForEditOffence.getDefendantId().toString())),
                                withJsonPath("$.offences[0].id", is(foundHearingsForEditOffence.getOffences().get(1).getId().toString()))
                        )))));
    }

    @Test
    public void testDeleteOffence() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForDeleteOffence foundHearingsForDeleteOffence = FoundHearingsForDeleteOffence.builder()
                .withId(randomUUID())
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.delete-case-defendant-offence"),
                objectToJsonObjectConverter.convert(foundHearingsForDeleteOffence));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.deleteOffence(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-deleted"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(foundHearingsForDeleteOffence.getId().toString()))
                        )))));
    }

    @Test
    public void testDeleteOffenceV2() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForDeleteOffenceV2 foundHearingsForDeleteOffence = FoundHearingsForDeleteOffenceV2.builder()
                .withIds(singletonList(randomUUID()))
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.delete-case-defendant-offence-v2"),
                objectToJsonObjectConverter.convert(foundHearingsForDeleteOffence));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.deleteOffenceV2(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-deleted-v2"),
                        payloadIsJson(allOf(
                                withJsonPath("$.ids[*]", hasItem(foundHearingsForDeleteOffence.getIds().get(0).toString()))
                        )))));
    }

    @Test
    public void shouldRaiseOffencesRemovedFromExistingHearingEvent() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing =initiateHearingCommand.getHearing();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID offence2Id = randomUUID();
        final Offence offence2 =Offence.offence().withId(offence2Id).build();
        ofNullable(hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> {
                    prosecutionCase.getDefendants().forEach(defendant -> {
                        defendant.getOffences().add(offence2);
                    });
                });

        final RemoveOffencesFromExistingHearing removeOffencesFromExistingHearing = new RemoveOffencesFromExistingHearing(hearingId,asList(offence2Id));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.remove-offences-from-existing-hearing"),
                objectToJsonObjectConverter.convert(removeOffencesFromExistingHearing));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.removeOffencesFromExistingHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offences-removed-from-existing-hearing"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(removeOffencesFromExistingHearing.getHearingId().toString())),
                                withJsonPath("$.sourceContext", is("Hearing")),
                                withJsonPath("$.offenceIds[0]", is(removeOffencesFromExistingHearing.getOffenceIds().get(0).toString()))
                        )))));
    }

    @Test
    public void shouldRaiseOffencesRemovedFromExistingAllocatedHearingEvent() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing =initiateHearingCommand.getHearing();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID offence2Id = randomUUID();
        final Offence offence2 =Offence.offence().withId(offence2Id).build();
        ofNullable(hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> {
                    prosecutionCase.getDefendants().forEach(defendant -> {
                        defendant.getOffences().add(offence2);
                    });
                });

        final RemoveOffencesFromExistingHearing removeOffencesFromExistingHearing = new RemoveOffencesFromExistingHearing(hearingId,asList(offence2Id));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.remove-offences-from-existing-allocated-hearing"),
                objectToJsonObjectConverter.convert(removeOffencesFromExistingHearing));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.removedOffencesFromAllocatedHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offences-removed-from-existing-hearing"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(removeOffencesFromExistingHearing.getHearingId().toString())),
                                withJsonPath("$.sourceContext", is("Listing")),
                                withJsonPath("$.offenceIds[0]", is(removeOffencesFromExistingHearing.getOffenceIds().get(0).toString()))
                        )))));
    }

    @Test
    public void shouldNotRaiseOffencesNotRemovedFromExistingHearingEvent() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final List<UUID> offenceIds = Arrays.asList(randomUUID(), randomUUID());

        final RemoveOffencesFromExistingHearing removeOffencesFromExistingHearing = new RemoveOffencesFromExistingHearing(hearingId, offenceIds);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.remove-offences-from-existing-hearing"),
                objectToJsonObjectConverter.convert(removeOffencesFromExistingHearing));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.eventStream, hearingAggregate);

        updateOffencesForDefendantCommandHandler.removeOffencesFromExistingHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream).collect(Collectors.toList()).isEmpty(), is(true));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        System.out.println(id);
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

}