package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.matchEvent;

import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.CourtApplicationResponseType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.application.SaveApplicationResponseCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.application.ApplicationResponseSaved;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SaveApplicationResponseCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            ApplicationResponseSaved.class
    );
    @InjectMocks
    private SaveApplicationResponseCommandHandler saveApplicationResponseCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    @Before
    public void setup() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldSaveApplicationResponse() throws Exception {

        //Given
        final SaveApplicationResponseCommand saveApplicationResponseCommand = fileResourceObjectMapper.convertFromFile("save-application-response.json", SaveApplicationResponseCommand.class);
        final UUID streamId = UUID.fromString("60d46e6a-0248-4a8d-96a2-8dcd2814dc17");
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataOf(UUID.randomUUID(), "hearing.command.save-application-response"), objectToJsonObjectConverter.convert(saveApplicationResponseCommand));
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new HearingAggregate());

        //When
        saveApplicationResponseCommandHandler.saveApplicationResponse(envelope);


        //Then
        matchEvent(verifyAppendAndGetArgumentFrom(hearingEventStream),
                "hearing.application-response-saved",
                fileResourceObjectMapper.convertFromFile("application-response-saved.json", JsonValue.class));
    }

    @Test
    public void shouldSaveApplicationResponseWhenItAlreadySaved() throws Exception {

        //Given
        final SaveApplicationResponseCommand saveApplicationResponseCommand = fileResourceObjectMapper.convertFromFile("save-application-response.json", SaveApplicationResponseCommand.class);
        final UUID streamId = UUID.fromString("60d46e6a-0248-4a8d-96a2-8dcd2814dc17");
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataOf(UUID.randomUUID(), "hearing.command.save-application-response"), objectToJsonObjectConverter.convert(saveApplicationResponseCommand));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(Stream.of(standardInitiateHearingTemplate().getHearing()));
            apply(Stream.of(getApplicationResponseSaved()));
        }};
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        //When
        saveApplicationResponseCommandHandler.saveApplicationResponse(envelope);

        //Then
        matchEvent(verifyAppendAndGetArgumentFrom(hearingEventStream),
                "hearing.application-response-saved",
                fileResourceObjectMapper.convertFromFile("application-response-saved.json", JsonValue.class));


    }

    private ApplicationResponseSaved getApplicationResponseSaved() {
        return ApplicationResponseSaved.applicationResponseSaved()
                .setApplicationPartyId(randomUUID())
                .setCourtApplicationResponse(CourtApplicationResponse.courtApplicationResponse()
                        .withApplicationResponseDate(LocalDate.now())
                        .withOriginatingHearingId(randomUUID())
                        .withApplicationId(randomUUID())
                        .withApplicationResponseType(CourtApplicationResponseType.courtApplicationResponseType()
                                .withDescription("Admitted")
                                .withId(UUID.randomUUID())
                                .withSequence(1).build())
                        .build());
    }
}