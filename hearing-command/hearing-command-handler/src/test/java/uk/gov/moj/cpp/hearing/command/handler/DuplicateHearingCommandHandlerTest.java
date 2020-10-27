package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
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
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicate;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForCase;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForOffence;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateHearingCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingMarkedAsDuplicate.class,
            HearingMarkedAsDuplicateForCase.class,
            HearingMarkedAsDuplicateForDefendant.class,
            HearingMarkedAsDuplicateForOffence.class);

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
    private DuplicateHearingCommandHandler handler;

    @Test
    public void shouldCreateMarkAsDuplicateEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID caseId1 = randomUUID();
        final UUID caseId2 = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final JsonEnvelope envelope = createMarkAsDuplicateCommandEnvelopeForHearing(hearingId, Arrays.asList(caseId1, caseId2), Arrays.asList(defendantId1, defendantId2), Arrays.asList(offenceId1, offenceId2));
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        final HearingAggregate hearingAggregate = setInitialHearingDataIntoAggregate(hearingId, caseId1, caseId2, defendantId1, defendantId2, offenceId1, offenceId2);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        handler.markAsDuplicateHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.marked-as-duplicate"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.prosecutionCaseIds[0]", is(caseId1.toString())),
                                withJsonPath("$.prosecutionCaseIds[1]", is(caseId2.toString())),
                                withJsonPath("$.defendantIds[0]", is(defendantId1.toString())),
                                withJsonPath("$.defendantIds[1]", is(defendantId2.toString())),
                                withJsonPath("$.offenceIds[0]", is(offenceId1.toString())),
                                withJsonPath("$.offenceIds[1]", is(offenceId2.toString()))
                        ))
                )));
    }

    @Test
    public void shouldCreateCaseMarkedAsDuplicateEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID caseId1 = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.mark-as-duplicate-for-cases", "prosecutionCaseIds", Arrays.asList(caseId1));
        when(this.eventSource.getStreamById(caseId1)).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(new CaseAggregate());

        handler.markAsDuplicateHearingForCases(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(caseEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.marked-as-duplicate-for-case"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.prosecutionCaseId", is(caseId1.toString()))
                                )
                        )
                )
        ));
    }

    @Test
    public void shouldCreateDefendantMarkedAsDuplicateEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.mark-as-duplicate-for-defendant", "defendantIds", Arrays.asList(defendantId));
        when(this.eventSource.getStreamById(defendantId)).thenReturn(this.defendantEventStream);
        when(this.aggregateService.get(this.defendantEventStream, DefendantAggregate.class)).thenReturn(new DefendantAggregate());

        handler.markAsDuplicateHearingForDefendants(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(defendantEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.marked-as-duplicate-for-defendant"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.defendantId", is(defendantId.toString()))
                                )
                        )
                )
        ));
    }

    @Test
    public void shouldCreateOffenceMarkedAsDuplicateEvent() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();

        final JsonEnvelope envelope = buildCommandEnvelope(hearingId, "hearing.command.mark-as-duplicate-for-offence", "offenceIds", Arrays.asList(offenceId));
        when(this.eventSource.getStreamById(offenceId)).thenReturn(this.offenceEventStream);
        when(this.aggregateService.get(this.offenceEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        handler.markAsDuplicateHearingForOffences(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(offenceEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.marked-as-duplicate-for-offence"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString()))
                                )
                        )
                )
        ));
    }

    private JsonEnvelope createMarkAsDuplicateCommandEnvelopeForHearing(final UUID hearingId, final List<UUID> prosecutionCaseIds, final List<UUID> defendantIds, final List<UUID> offenceIds) {
        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearingId.toString());

        for (final UUID prosecutionCaseId : prosecutionCaseIds) {
            payloadBuilder.add("prosecutionCaseIds", createArrayBuilder().add(prosecutionCaseId.toString()));
        }

        for (final UUID defendantId : defendantIds) {
            payloadBuilder.add("defendantIds", createArrayBuilder().add(defendantId.toString()));
        }

        for (final UUID offenceId : offenceIds) {
            payloadBuilder.add("offenceIds", createArrayBuilder().add(offenceId.toString()));
        }

        return envelopeFrom(metadataWithRandomUUID("hearing.command.mark-as-duplicate"), payloadBuilder.build());
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

    private HearingAggregate setInitialHearingDataIntoAggregate(final UUID hearingId, final UUID caseId1, final UUID caseId2, final UUID defendantId1, final UUID defendantId2, final UUID offenceId1, final UUID offenceId2) {
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