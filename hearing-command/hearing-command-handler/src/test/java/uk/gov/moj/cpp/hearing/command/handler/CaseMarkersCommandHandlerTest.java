package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersEnrichedWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class CaseMarkersCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CaseMarkersEnrichedWithAssociatedHearings.class,
            CaseMarkersUpdated.class
    );

    @InjectMocks
    private CaseMarkersCommandHandler caseMarkersCommandHandler;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventSource eventSource;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private AggregateService aggregateService;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void updateCaseMarkers() throws EventStreamException {

        CaseAggregate aggregate = new CaseAggregate();
        aggregate.registerHearingId(fromString("34d07e81-9770-4d23-af6f-84f1d7571bd3"), fromString("581767a1-22af-408a-92f0-20837846cc6f"));
        aggregate.registerHearingId(fromString("34d07e81-9770-4d23-af6f-84f1d7571bd3"), fromString("123767a1-22af-408a-92f0-20837846cc67"));

        final JsonObject payload = Json.createObjectBuilder()
                .add("prosecutionCaseId", "34d07e81-9770-4d23-af6f-84f1d7571bd3")
                .add("hearingId", "581767a1-22af-408a-92f0-20837846cc6f")
                .add("caseMarkers", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                .add("markerTypeid", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                .add("markerTypeCode", "WP")
                                .add("markerTypeDescription", "Prohibited Weapons"))
                        .build())
                .build();

        final UUID streamId = fromString("34d07e81-9770-4d23-af6f-84f1d7571bd3");

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.update-case-markers"), payload);

        when(eventSource.getStreamById(streamId)).thenReturn(caseEventStream);
        when(aggregateService.get(eq(caseEventStream), any()))
                .thenReturn(aggregate);

        caseMarkersCommandHandler.updateCaseMarkers(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(caseEventStream).collect(Collectors.toList()).get(0);
        assertThat(actualEventProduced.metadata().name(), Matchers.is("hearing.events.case-markers-enriched-with-associated-hearings"));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionCaseId"), Matchers.is("34d07e81-9770-4d23-af6f-84f1d7571bd3"));
        assertThat(actualEventProduced.asJsonObject().getJsonArray("hearingIds").size(), Matchers.is(2));
        assertThat(actualEventProduced.asJsonObject().getJsonArray("caseMarkers").size(), Matchers.is(1));
    }

    @Test
    public void updateCaseMarkersForAssociatedHearings() throws EventStreamException {

        final JsonObject payload = Json.createObjectBuilder()
                .add("prosecutionCaseId", "34d07e81-9770-4d23-af6f-84f1d7571bd3")
                .add("hearingIds", Json.createArrayBuilder()
                        .add("581767a1-22af-408a-92f0-20837846cc6f")
                        .build())
                .add("caseMarkers", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                .add("markerTypeid", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                .add("markerTypeCode", "WP")
                                .add("markerTypeDescription", "Prohibited Weapons"))
                        .build())
                .build();

        final UUID streamId = fromString("581767a1-22af-408a-92f0-20837846cc6f");

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.update-case-markers-with-associated-hearings"), payload);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        caseMarkersCommandHandler.updateCaseMarkersForAssociatedHearings(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertThat(actualEventProduced.metadata().name(), Matchers.is("hearing.events.case-markers-updated"));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionCaseId"), Matchers.is("34d07e81-9770-4d23-af6f-84f1d7571bd3"));
        assertThat(actualEventProduced.asJsonObject().getString("hearingId"), Matchers.is("581767a1-22af-408a-92f0-20837846cc6f"));
        assertThat(actualEventProduced.asJsonObject().getJsonArray("caseMarkers").size(), Matchers.is(1));
    }


}