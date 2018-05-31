package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
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
import uk.gov.moj.cpp.hearing.command.offence.AddedOffence;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffence;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class ChangeCaseDefendantOffencesCommandHandlerTest {

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
    private ChangeCaseDefendantOffencesCommandHandler changeCaseDefendantOffencesCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceAddEvent() throws EventStreamException {

        final List<UpdatedOffence> offences = Collections.singletonList(UpdatedOffence.builder().withId(randomUUID()).build());

        final List<AddedOffence> addedOffences = Collections.singletonList(
                AddedOffence.builder()
                        .withDefendantId(randomUUID())
                        .withCaseId(randomUUID())
                        .withAddedOffences(offences)
                        .build());

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = CaseDefendantOffencesChangedCommand.builder()
                .withModifiedDate(LocalDate.now())
                .withAddedOffences(addedOffences)
                .build();

        setupMockedEventStream(addedOffences.get(0).getDefendantId(), this.eventStream, new DefendantAggregate());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.defendant-offences-changed"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged));

        changeCaseDefendantOffencesCommandHandler.updateCaseDefendantOffences(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-new-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(caseDefendantOffencesChanged.getAddedOffences().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.defendantId", is(caseDefendantOffencesChanged.getAddedOffences().get(0).getDefendantId().toString())),
                                withJsonPath("$.caseId", is(caseDefendantOffencesChanged.getAddedOffences().get(0).getCaseId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceUpdateAddEvent() throws EventStreamException {

        final List<UpdatedOffence> offences = Collections.singletonList(UpdatedOffence.builder().withId(randomUUID()).build());

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = CaseDefendantOffencesChangedCommand.builder()
                .withModifiedDate(LocalDate.now())
                .withUpdateOffences(offences)
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offences"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged));

        setupMockedEventStream(offences.get(0).getId(), this.eventStream, new OffenceAggregate());

        changeCaseDefendantOffencesCommandHandler.updateCaseDefendantOffences(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-edit-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(caseDefendantOffencesChanged.getUpdatedOffences().get(0).getId().toString()))
                        )))));
    }

    @Test
    public void testUpdateCaseDefendantOffences_Sends_OffenceDeleteEvent() throws EventStreamException {

        final List<DeletedOffence> offences = Collections.singletonList(DeletedOffence.builder().withId(randomUUID()).build());

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = CaseDefendantOffencesChangedCommand.builder()
                .withModifiedDate(LocalDate.now())
                .withDeletedOffences(offences)
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offences"), objectToJsonObjectConverter.convert(caseDefendantOffencesChanged));

        setupMockedEventStream(offences.get(0).getId(), this.eventStream, new OffenceAggregate());

        changeCaseDefendantOffencesCommandHandler.updateCaseDefendantOffences(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.found-hearings-for-delete-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(caseDefendantOffencesChanged.getDeletedOffences().get(0).getId().toString()))
                        )))));
    }

    @Test
    public void testAddOffenceForExistingHearing() throws EventStreamException {

        final UUID hearingId = randomUUID();

        final FoundHearingsForNewOffence foundHearingsForNewOffence = FoundHearingsForNewOffence.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(hearingId))
                .withDefendantId(randomUUID())
                .withCaseId(randomUUID())
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.add-case-defendant-offence"), objectToJsonObjectConverter.convert(foundHearingsForNewOffence));

        setupMockedEventStream(hearingId, this.eventStream, new NewModelHearingAggregate());

        changeCaseDefendantOffencesCommandHandler.addOffenceForExistingHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(foundHearingsForNewOffence.getId().toString())),
                                withJsonPath("$.hearingId", is(foundHearingsForNewOffence.getHearingIds().get(0).toString())),
                                withJsonPath("$.defendantId", is(foundHearingsForNewOffence.getDefendantId().toString())),
                                withJsonPath("$.caseId", is(foundHearingsForNewOffence.getCaseId().toString()))
                        )))));
    }

    @Test
    public void testUpdateOffence() throws EventStreamException {

        final UUID hearingId = randomUUID();

        final FoundHearingsForEditOffence foundHearingsForEditOffence = FoundHearingsForEditOffence.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(hearingId))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offence"), objectToJsonObjectConverter.convert(foundHearingsForEditOffence));

        setupMockedEventStream(hearingId, this.eventStream, new NewModelHearingAggregate());

        changeCaseDefendantOffencesCommandHandler.updateOffence(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.offence-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(foundHearingsForEditOffence.getId().toString()))
                        )))));
    }

    @Test
    public void testDeleteOffence() throws EventStreamException {

        final UUID hearingId = randomUUID();

        final FoundHearingsForDeleteOffence foundHearingsForDeleteOffence = FoundHearingsForDeleteOffence.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(hearingId))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.delete-case-defendant-offence"), objectToJsonObjectConverter.convert(foundHearingsForDeleteOffence));

        setupMockedEventStream(hearingId, this.eventStream, new NewModelHearingAggregate());

        changeCaseDefendantOffencesCommandHandler.deleteOffence(envelope);

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