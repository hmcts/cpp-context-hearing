package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.addOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.deleteOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class) @SuppressWarnings("unchecked")
public class UpdateOffencesForDefendantCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            FoundHearingsForNewOffence.class,
            FoundHearingsForEditOffence.class,
            FoundHearingsForDeleteOffence.class,
            OffenceAdded.class,
            OffenceUpdated.class,
            OffenceDeleted.class
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

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
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

        setupMockedEventStream(defendantId, this.eventStream, new DefendantAggregate());

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-new-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offence.id", is(caseDefendantOffencesChanged.getFirstOffenceFromAddedOffences().getId().toString())),
                                withJsonPath("$.defendantId", is(caseDefendantOffencesChanged.getFirstAddedOffences().getDefendantId().toString())),
                                withJsonPath("$.prosecutionCaseId", is(caseDefendantOffencesChanged.getFirstAddedOffences().getProsecutionCaseId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceUpdateAddEvent() throws EventStreamException {

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(updateOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), randomUUID()).setOffencesToUpdate(singletonList(randomUUID()))), u -> {
            u.setAddedOffences(emptyList()).setDeletedOffences(emptyList());
        }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offences"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));

        setupMockedEventStream(caseDefendantOffencesChanged.getFirstOffenceFromUpdatedOffences().getId(), this.eventStream, new OffenceAggregate());

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-edit-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offence.id", is(caseDefendantOffencesChanged.getFirstOffenceFromUpdatedOffences().getId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceDeleteEvent() throws EventStreamException {

        final CommandHelpers.UpdateOffencesForDefendantCommandHelper caseDefendantOffencesChanged =
                h(with(deleteOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), randomUUID()).setOffenceToDelete(singletonList(randomUUID()))), u -> {
            u.setAddedOffences(emptyList()).setUpdatedOffences(emptyList());
        }));

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offences"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged.it()));

        setupMockedEventStream(caseDefendantOffencesChanged.getFirstOffenceIdFromDeletedOffences(), this.eventStream, new OffenceAggregate());

        updateOffencesForDefendantCommandHandler.updateOffencesForDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-delete-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(caseDefendantOffencesChanged.getFirstOffenceIdFromDeletedOffences().toString()))
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
    public void testUpdateOffence() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final FoundHearingsForEditOffence foundHearingsForEditOffence = FoundHearingsForEditOffence.foundHearingsForEditOffence()
                .withHearingIds(singletonList(initiateHearingCommand.getHearing().getId()))
                .withDefendantId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId())
                .withOffence(uk.gov.justice.json.schemas.core.Offence.offence()
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

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

}