package uk.gov.moj.cpp.hearing.command.handler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingResultLineSharedDatesUpdated;
import javax.json.Json;
import javax.json.JsonObject;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;

@RunWith(MockitoJUnitRunner.class)
public class UpdateResultLineSharedDatesCommandHandlerTest {
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingResultLineSharedDatesUpdated.class);

    @InjectMocks
    private UpdateResultLineSharedDatesCommandHandler updateResultLineSharedDatesCommandHandler;


    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private Set<String> guiltyPleaTypes;

    @Before
    public void setup() {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        setField(this.jsonObjectToObjectConverter, "objectMapper", objectMapper);
        setField(this.objectToJsonObjectConverter, "mapper", objectMapper);
    }


    @Test
    public void updateResultLineSharedDates() throws EventStreamException {
        final UUID hearingId = UUID.randomUUID();
        final UUID resultLineId1 = UUID.randomUUID();
        final String sharedDate = "2020-02-05";

        final JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("resultLinesToBeUpdated", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("resultLineId", resultLineId1.toString())
                                .add("sharedDate", sharedDate)
                        )).build();

        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.command.update-resultline-shared-dates"), payload);
        updateResultLineSharedDatesCommandHandler.updateResultLineSharedDates(jsonEnvelop);

        final List<?> events = verifyAppendAndGetArgumentFrom(eventStream)
                .collect(Collectors.toList());

        final HearingResultLineSharedDatesUpdated eventObj = asPojo((JsonEnvelope) events.get(0), HearingResultLineSharedDatesUpdated.class);
        assertThat(eventObj.getHearingId(), is(hearingId));
        assertThat(eventObj.getResultLinesToBeUpdated().size(), is(1));
        assertThat(eventObj.getResultLinesToBeUpdated().get(0).getResultLineId(), is(resultLineId1));
        assertThat(eventObj.getResultLinesToBeUpdated().get(0).getSharedDate(), is(LocalDate.parse(sharedDate)));

    }
}