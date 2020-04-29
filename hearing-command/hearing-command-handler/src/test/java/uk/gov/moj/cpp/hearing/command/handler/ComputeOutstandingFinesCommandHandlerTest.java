package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesQueried;

import java.time.LocalDate;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComputeOutstandingFinesCommandHandlerTest {

    private static final String COURT_ROOM_ID1 = "2181fbc2-f4f9-495b-a648-9bb45e752302";
    private static final String COURT_ROOM_ID2 = "872f4cce-6be1-45c8-a520-604828c96cbd";
    private static final String COURT_ROOM_ID3 = "d16499b0-28ca-48e2-960a-c443123af9c3";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(OutstandingFinesQueried.class);

    @InjectMocks
    private ComputeOutstandingFinesCommandHandler computeOutstandingFinesCommandHandler;


    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void computeOutstandingFines() throws EventStreamException {


        final JsonObject payload = createCourtRoomsOutstandingFInesQuery();

        final MetadataBuilder metadataBuilder = metadataWithRandomUUID("hearing.command.compute-outstanding-fines")
                .withClientCorrelationId("0d5d72c0-7987-4677-8ce7-69fab7031f9b");

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataBuilder, payload);

        when(eventSource.getStreamById(any())).thenReturn(eventStream);


        computeOutstandingFinesCommandHandler.computeOutstandingFines(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList()).get(0);
        assertThat(actualEventProduced.metadata().name(), is("hearing.compute-outstanding-fines-requested"));
        assertThat(actualEventProduced.metadata().clientCorrelationId().get(), is("0d5d72c0-7987-4677-8ce7-69fab7031f9b"));
        assertThat(asPojo(actualEventProduced, OutstandingFinesQueried.class),
                isBean(OutstandingFinesQueried.class)
                        .with(OutstandingFinesQueried::getCourtCentreId, is(fromString("88abd281-8c52-4171-aca1-740f734b43d7")))
                        .with(OutstandingFinesQueried::getCourtRoomIds, containsInAnyOrder(
                                fromString(COURT_ROOM_ID1),
                                fromString(COURT_ROOM_ID2),
                                fromString(COURT_ROOM_ID3)
                        ))
                        .with(OutstandingFinesQueried::getHearingDate, is(LocalDate.parse("2019-12-18")))
        );
    }

    private JsonObject createCourtRoomsOutstandingFInesQuery() {
        return Json.createObjectBuilder()
                .add("courtCentreId", "88abd281-8c52-4171-aca1-740f734b43d7")
                .add("courtRoomIds", Json.createArrayBuilder()
                        .add(COURT_ROOM_ID1)
                        .add(COURT_ROOM_ID2)
                        .add(COURT_ROOM_ID3))
                .add("hearingDate", "2019-12-18")
                .build();
    }

}