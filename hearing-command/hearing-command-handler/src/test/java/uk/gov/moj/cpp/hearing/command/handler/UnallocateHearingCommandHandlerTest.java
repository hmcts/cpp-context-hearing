package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnallocated;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UnallocateHearingCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingUnallocated.class,
            HearingRemovedForProsecutionCase.class,
            HearingRemovedForDefendant.class,
            HearingRemovedForOffence.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventStream defendantEventStream;

    @Mock
    private EventStream offenceEventStream;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private UnallocateHearingCommandHandler handler;

    @Test
    public void shouldCreateHearingUnallocatedEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId1 = randomUUID();
        final UUID prosecutionCaseId2 = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.unallocate-hearing", "offenceIds",
                Arrays.asList(offenceId1));
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        final HearingAggregate hearingAggregate = setInitialHearingDataIntoAggregate(hearingId, prosecutionCaseId1, prosecutionCaseId2, defendantId1, defendantId2, offenceId1, offenceId2);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        handler.handleUnallocateHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.hearing-unallocated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.prosecutionCaseIds[0]", is(prosecutionCaseId1.toString())),
                                withJsonPath("$.defendantIds[0]", is(defendantId1.toString())),
                                withJsonPath("$.offenceIds[0]", is(offenceId1.toString()))

                        ))
                )));
    }

    @Test
    public void shouldCreateHearingRemovedForProsecutionCaseEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId1 = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.remove-hearing-for-prosecution-cases",
                "prosecutionCaseIds", Arrays.asList(prosecutionCaseId1));

        when(this.eventSource.getStreamById(prosecutionCaseId1)).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(new CaseAggregate());

        handler.handleRemoveHearingForProsecutionCases(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(caseEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.hearing-removed-for-prosecution-case"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.prosecutionCaseId", is(prosecutionCaseId1.toString()))
                                )
                        )
                )
        ));
    }


    @Test
    public void shouldCreateHearingRemovedForDefendantEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.remove-hearing-for-defendant",
                "defendantIds", Arrays.asList(defendantId));

        when(this.eventSource.getStreamById(defendantId)).thenReturn(this.defendantEventStream);
        when(this.aggregateService.get(this.defendantEventStream, DefendantAggregate.class)).thenReturn(new DefendantAggregate());

        handler.handleRemoveHearingForDefendants(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(defendantEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.hearing-removed-for-defendant"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.defendantId", is(defendantId.toString()))
                                )
                        )
                )
        ));
    }

    @Test
    public void shouldCreateHearingRemovedForOffenceEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.remove-hearing-for-offence",
                "offenceIds", Arrays.asList(offenceId));

        when(this.eventSource.getStreamById(offenceId)).thenReturn(this.offenceEventStream);
        when(this.aggregateService.get(this.offenceEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        handler.handleRemoveHearingForOffences(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(offenceEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.hearing-removed-for-offence"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString()))
                                )
                        )
                )
        ));
    }

    private JsonEnvelope buildCommandEnvelope(final UUID hearingId, final String commandName, final String idsFieldName, final List<UUID> ids) {
        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearingId.toString());

        final JsonArrayBuilder arrayBuilder = createArrayBuilder();
        for (final UUID id : ids) {
            arrayBuilder.add(id.toString());
        }
        payloadBuilder.add(idsFieldName, arrayBuilder.build());

        return envelopeFrom(metadataWithRandomUUID(commandName), payloadBuilder.build());
    }

    private HearingAggregate setInitialHearingDataIntoAggregate(final UUID hearingId, final UUID caseId1, final UUID caseId2,
                                                                final UUID defendantId1, final UUID defendantId2, final UUID offenceId1, final UUID offenceId2) {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.initiate(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                                .withId(caseId1)
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withId(defendantId1)
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withId(offenceId1)
                                                .build()))
                                        .build()))
                                .build(),
                        ProsecutionCase.prosecutionCase()
                                .withId(caseId2)
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withId(defendantId2)
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withId(offenceId2)
                                                .build()))
                                        .build()))
                                .build()
                ))
                .build());
        return hearingAggregate;
    }
}