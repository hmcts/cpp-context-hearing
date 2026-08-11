package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
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
            PtphDetailSaved.class, PtphDetailFinalised.class, PtphDetailDeleted.class);

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
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(new HearingAggregate());
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
        assertThat(!payload.containsKey("keyReason") || payload.isNull("keyReason"), org.hamcrest.CoreMatchers.is(true));
    }

    @Test
    void rejectsFixedListTypeWithoutReason() {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .build());

        assertThat(assertThrows(RuntimeException.class, () -> handler.savePtphDetail(envelope)),
                instanceOf(RuntimeException.class));
    }

    @Test
    void finalises() throws EventStreamException {
        // aggregate has tier+listType applied first via a save so finalise passes
        final HearingAggregate aggregate = new HearingAggregate();
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
}
