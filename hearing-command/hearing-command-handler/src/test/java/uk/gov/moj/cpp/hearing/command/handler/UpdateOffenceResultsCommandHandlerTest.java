package uk.gov.moj.cpp.hearing.command.handler;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefendantCaseWithdrawnOrDismissed;
import uk.gov.moj.cpp.hearing.domain.event.DefendantOffenceResultsUpdated;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOffenceResultsCommandHandlerTest {

    public static final String HEARING_COMMAND_HANDLER_UPDATE_OFFENCE_RESULTS = "hearing.command.handler.update-offence-results";
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefendantOffenceResultsUpdated.class,
            DefendantCaseWithdrawnOrDismissed.class
    );

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private UpdateOffenceResultsCommandHandler updateOffenceResultsCommandHandler;

    @Test
    public void testUpdateOffenceResults_WhenCaseResultedInOneHearing() throws IOException, EventStreamException {

        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID offence1 = randomUUID();
        final UUID offence2 = randomUUID();
        final UUID offence3 = randomUUID();
        final UUID offence4 = randomUUID();
        final List<UUID> offenceIds = asList(offence1, offence2, offence3, offence4);
        final Map<UUID, OffenceResult> offenceResults = mapOf(
                entry(offence1, OffenceResult.WITHDRAWN),
                entry(offence2, OffenceResult.WITHDRAWN),
                entry(offence3, OffenceResult.WITHDRAWN),
                entry(offence4, OffenceResult.DISMISSED)
        );

        final JsonObject payload = createPayload(defendantId, caseId, offenceIds, offenceResults);

        final JsonEnvelope envelope = EnvelopeFactory.createEnvelope(HEARING_COMMAND_HANDLER_UPDATE_OFFENCE_RESULTS, payload);

        setupMockedEventStream(defendantId, this.eventStream, new DefendantAggregate());

        updateOffenceResultsCommandHandler.updateOffenceResults(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream),
                streamContaining(
                        matchDefendantOffenceUpdated(envelope, defendantId, offenceIds, offenceResults),
                        matchDefendantCaseWithDrawnDismissed(envelope, defendantId, caseId, offenceResults)
                ));
    }

    @Test
    public void testUpdateOffenceResults_WhenCaseResultedInMultipleHearings() throws IOException, EventStreamException {

        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID offence1 = randomUUID();
        final UUID offence2 = randomUUID();
        final UUID offence3 = randomUUID();
        final UUID offence4 = randomUUID();
        final List<UUID> offenceIds = asList(offence1, offence2, offence3, offence4);
        final Map<UUID, OffenceResult> offenceResults1 = mapOf(
                entry(offence1, OffenceResult.WITHDRAWN),
                entry(offence2, OffenceResult.WITHDRAWN),
                entry(offence3, OffenceResult.WITHDRAWN)
        );

        final JsonObject payload = createPayload(defendantId, caseId, offenceIds, offenceResults1);

        setupMockedEventStream(defendantId, this.eventStream, new DefendantAggregate());

        final JsonEnvelope envelope1 = EnvelopeFactory.createEnvelope(HEARING_COMMAND_HANDLER_UPDATE_OFFENCE_RESULTS, payload);

        updateOffenceResultsCommandHandler.updateOffenceResults(envelope1);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                matchDefendantOffenceUpdated(envelope1, defendantId, offenceIds, offenceResults1)
        ));


        //update the results from the second hearing

        final Map<UUID, OffenceResult> offenceResults2 = mapOf(
                entry(offence4, OffenceResult.DISMISSED)
        );

        final JsonEnvelope envelope2 = EnvelopeFactory.createEnvelope(HEARING_COMMAND_HANDLER_UPDATE_OFFENCE_RESULTS,
                createPayload(defendantId, caseId, offenceIds, offenceResults2));

        updateOffenceResultsCommandHandler.updateOffenceResults(envelope2);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream, 2), streamContaining(
                matchDefendantOffenceUpdated(envelope2, defendantId, offenceIds, offenceResults2),
                matchDefendantCaseWithDrawnDismissed(envelope2, defendantId, caseId, mapOf(offenceResults1, offenceResults2))
        ));
    }

    @Test
    public void testUpdateOffenceResults_NoCaseWithdrawnDismissedEventRaised_whenGultyOfAtLeastOneOffence() throws IOException, EventStreamException {

        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID offence1 = randomUUID();
        final UUID offence2 = randomUUID();
        final UUID offence3 = randomUUID();
        final UUID offence4 = randomUUID();
        final List<UUID> offenceIds = asList(offence1, offence2, offence3, offence4);
        final Map<UUID, OffenceResult> offenceResults = mapOf(
                entry(offence1, OffenceResult.WITHDRAWN),
                entry(offence2, OffenceResult.WITHDRAWN),
                entry(offence3, OffenceResult.WITHDRAWN),
                entry(offence4, OffenceResult.GUILTY)
        );

        final JsonObject payload = createPayload(defendantId, caseId, offenceIds, offenceResults);

        setupMockedEventStream(defendantId, this.eventStream, new DefendantAggregate());

        final JsonEnvelope envelope1 = EnvelopeFactory.createEnvelope(HEARING_COMMAND_HANDLER_UPDATE_OFFENCE_RESULTS, payload);

        updateOffenceResultsCommandHandler.updateOffenceResults(envelope1);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream),
                streamContaining(
                        matchDefendantOffenceUpdated(envelope1, defendantId, offenceIds, offenceResults)
                ));

    }

    public JsonObject createPayload(final UUID defendantId, final UUID caseId, final List<UUID> offenceIds, final Map<UUID, OffenceResult> offenceResults) {

        final JsonArrayBuilder offenceArray = createArrayBuilder();
        offenceIds.stream().map(UUID::toString).forEach(s -> offenceArray.add(s));

        final JsonArrayBuilder results = createArrayBuilder();
        offenceResults.entrySet().stream().forEach(entry ->
                results.add(
                        createObjectBuilder()
                                .add("offenceId", entry.getKey().toString())
                                .add("offenceResult", entry.getValue().name())
                ));

        return createObjectBuilder()
                .add("defendantId", defendantId.toString())
                .add("caseId", caseId.toString())
                .add("offenceIds", offenceArray)
                .add("resultedOffences", results)
                .build();

    }

    private JsonEnvelopeMatcher matchDefendantCaseWithDrawnDismissed(final JsonEnvelope envelope,
                                                                     final UUID defendantId,
                                                                     final UUID caseId,
                                                                     final Map<UUID, OffenceResult> offenceResults) {
        return jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.defendant-case-withdrawn-or-dismissed"),
                payloadIsJson(allOf(
                        withJsonPath("$.defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.caseId", equalTo(caseId.toString())),
                        withJsonPath("$.resultedOffences", allOf(toKeyMatcher(offenceResults)))
                ))).thatMatchesSchema();
    }

    private JsonEnvelopeMatcher matchDefendantOffenceUpdated(final JsonEnvelope envelope,
                                                             final UUID defendantId,
                                                             final List<UUID> offenceIds,
                                                             final Map<UUID, OffenceResult> offenceResults) {

        return jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.defendant-offence-results-updated"),
                payloadIsJson(allOf(
                        withJsonPath("$.defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offenceIds", hasSize(offenceIds.size())),
                        withJsonPath("$.offenceIds", containsInAnyOrder(toStringArray(offenceIds))),
                        withJsonPath("$.resultedOffences", allOf(toKeyMatcher(offenceResults))))
                )).thatMatchesSchema();
    }

    private String[] toStringArray(final List<UUID> offenceIds) {
        return offenceIds.stream().map(UUID::toString).collect(toList()).toArray(new String[0]);
    }

    private List<Matcher<? super Map<? extends String, ?>>> toKeyMatcher(final Map<UUID, OffenceResult> offenceResults) {
        return offenceResults.entrySet().stream().map(entry -> hasKey(entry.getKey().toString())).collect(toList());
    }

    private <K, V> Map<K, V> mapOf(final Map<K, V>... maps) {
        return stream(maps)
                .flatMap(kvMap -> kvMap.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private <K, V> Map<K, V> mapOf(final Map.Entry<K, V>... entries) {
        return stream(entries).collect(toMap(o -> o.getKey(), o -> o.getValue()));
    }

    private <K, V> Map.Entry<K, V> entry(final K key, final V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Stream<JsonEnvelope> verifyAppendAndGetArgumentFrom(final EventStream eventStream) throws EventStreamException {
        return verifyAppendAndGetArgumentFrom(eventStream, 1);
    }

    public static Stream<JsonEnvelope> verifyAppendAndGetArgumentFrom(final EventStream eventStream, final int times) throws EventStreamException {
        ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
        ((EventStream) Mockito.verify(eventStream, times(times))).append((Stream) argumentCaptor.capture());
        return (Stream) argumentCaptor.getValue();
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}