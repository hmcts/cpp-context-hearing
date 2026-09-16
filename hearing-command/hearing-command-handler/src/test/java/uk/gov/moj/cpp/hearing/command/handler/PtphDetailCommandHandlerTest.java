package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PtphDetailCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            PtphDetailSaved.class, PtphDetailFinalised.class, PtphDetailDeleted.class,
            HearingChangeIgnored.class);

    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter =
            new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Mock
    private EventSource eventSource;
    @Mock
    private EventStream eventStream;
    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private PtphDetailCommandHandler handler;

    private final UUID hearingId = randomUUID();

    @BeforeEach
    void setUp() {
        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(initiatedHearingAggregate());
    }

    /**
     * An aggregate for a hearing that actually exists. A bare {@code new HearingAggregate()}
     * represents a hearing that was never initiated, and every PTPH command is now rejected in
     * that state — the framework creates an event stream for any UUID, so the aggregate is the
     * only place that knows whether the hearing is real.
     */
    /**
     * A Crown Court PTPH — the only kind of hearing eligible for a tier and list type. A
     * hearing without a jurisdiction and PTPH type is rejected by the aggregate guard, so
     * these tests would otherwise assert against a rejection event.
     */
    private HearingAggregate initiatedHearingAggregate() {
        final HearingAggregate aggregate = new HearingAggregate();
        aggregate.apply(new HearingInitiated(Hearing.hearing()
                .withId(hearingId)
                .withJurisdictionType(JurisdictionType.CROWN)
                .withType(HearingType.hearingType()
                        .withId(UUID.fromString("06b0c2bf-3f98-46ed-ab7e-56efaf9ecced"))
                        .withDescription("Plea and Trial Preparation")
                        .build())
                .build()));
        return aggregate;
    }

    @Test
    void savesTierOnly() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .build());

        handler.savePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(appended.get(0).payloadAsJsonObject().getString("tier"), org.hamcrest.CoreMatchers.is("TIER_2"));
    }

    @Test
    void savesFixedListTypeWithReason() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .add("keyReason", "court ordered")
                        .build());

        handler.savePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(appended.get(0).payloadAsJsonObject().getString("keyReason"), org.hamcrest.CoreMatchers.is("court ordered"));
    }

    @Test
    void savesFlexibleListTypeIgnoringKeyReason() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_2_FLEXIBLE")
                        .add("keyReason", "ignored")
                        .build());

        handler.savePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        final javax.json.JsonObject payload = appended.get(0).payloadAsJsonObject();
        // Definite, not "absent or null": the framework's ObjectMapperProducer sets
        // JsonInclude.Include.NON_ABSENT, so a null field is omitted rather than serialised as
        // JSON null. That matters because hearing.ptph-detail-saved declares keyReason as
        // {"type":"string"} with additionalProperties:false — an emitted null would fail schema
        // validation on the listener and processor queues and dead-letter the event.
        assertThat(payload.containsKey("keyReason"), org.hamcrest.CoreMatchers.is(false));
    }

    /**
     * A fixed list type with no reason must not make the handler throw.
     */
    @Test
    void doesNotThrowOnFixedListTypeWithoutReason() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .build());

        handler.savePtphDetail(envelope);

        // Passed straight through. The rule belongs to HearingCommandApi, which rejects it with a
        // 400 before dispatch, so by the time a command reaches the handler it has been checked.
        // What matters here is only that the handler does not throw: it runs inside the JMS
        // transaction for the hearing stream, so an exception would roll back, redeliver and
        // dead-letter the queue every hearing command shares.
        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(appended.size(), org.hamcrest.CoreMatchers.is(1));
        assertThat(appended.get(0).metadata().name(), org.hamcrest.CoreMatchers.is("hearing.ptph-detail-saved"));
    }

    @Test
    void finalises() throws EventStreamException {
        // an initiated hearing, with tier+listType applied first so finalise passes
        final HearingAggregate aggregate = initiatedHearingAggregate();
        aggregate.savePtphDetail(new PtphDetailSaved(hearingId, "TIER_2", "TYPE_2_FLEXIBLE", null))
                .forEach(aggregate::apply);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.finalise-ptph-detail"),
                createObjectBuilder().add("hearingId", hearingId.toString()).build());

        handler.finalisePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(appended.size(), org.hamcrest.CoreMatchers.is(1));
    }

    @Test
    void deletes() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.delete-ptph-detail"),
                createObjectBuilder().add("hearingId", hearingId.toString()).build());

        handler.deletePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(appended.size(), org.hamcrest.CoreMatchers.is(1));
    }

    /**
     * A hearing id that was never initiated must not create PTPH state. The framework's
     * getStreamById opens a stream for any UUID, so the command would otherwise record tier and
     * list type against a hearing that does not exist.
     */
    @Test
    void rejectsSaveWhenHearingDoesNotExist() throws EventStreamException {
        // its own stream mock, so this stubbing cannot collide with setUp's
        final UUID unknownHearingId = randomUUID();
        final EventStream unknownHearingStream = org.mockito.Mockito.mock(EventStream.class);
        when(eventSource.getStreamById(unknownHearingId)).thenReturn(unknownHearingStream);
        when(aggregateService.get(unknownHearingStream, HearingAggregate.class)).thenReturn(new HearingAggregate());

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", unknownHearingId.toString())
                        .add("tier", "TIER_2")
                        .build());

        handler.savePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(unknownHearingStream).collect(Collectors.toList());
        assertThat(appended.size(), org.hamcrest.CoreMatchers.is(1));
        assertThat(appended.get(0).metadata().name(), org.hamcrest.CoreMatchers.is("hearing.hearing-change-ignored"));
        assertThat(appended.get(0).payloadAsJsonObject().getString("reason"),
                org.hamcrest.CoreMatchers.is("Rejecting 'hearing.save-ptph-detail' event as hearing not found"));
    }
}
