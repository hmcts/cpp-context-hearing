package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesRequested;

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
public class AddRequestForOutstandingFinesCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(OutstandingFinesRequested.class);

    @InjectMocks
    private AddRequestForOutstandingFinesCommandHandler addRequestForOutstandingFinesCommandHandler;

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

        final MetadataBuilder metadataBuilder = metadataWithRandomUUID("hearing.command.add-request-for-outstanding-fines");

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataBuilder, payload);

        when(eventSource.getStreamById(any())).thenReturn(eventStream);


        addRequestForOutstandingFinesCommandHandler.addRequestForOutstandingFines(envelope);

        final JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList()).get(0);
        assertThat(actualEventProduced.metadata().name(), is("hearing.outstanding-fines-requested"));
        assertThat(asPojo(actualEventProduced, OutstandingFinesRequested.class),
                isBean(OutstandingFinesRequested.class)
                        .with(OutstandingFinesRequested::getHearingDate, is(LocalDate.parse("2020-03-26")))
        );
    }

    private JsonObject createCourtRoomsOutstandingFInesQuery() {
        return Json.createObjectBuilder()
                .add("hearingDate", "2020-03-26")
                .build();
    }

}