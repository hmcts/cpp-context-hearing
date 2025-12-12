package uk.gov.moj.cpp.hearing.command.handler;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import uk.gov.justice.core.courts.*;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.RemoveProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.*;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class ProsecutionCounselCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            ProsecutionCounselAdded.class,
            ProsecutionCounselRemoved.class,
            ProsecutionCounselUpdated.class
    );
    @InjectMocks
    private ProsecutionCounselCommandHandler prosecutionCounselCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperProducer().objectMapper();

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }


    @Test
    public void addProsecutionCounsel() throws EventStreamException, IOException {
        UUID hearingId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        UUID defendantId1 = UUID.randomUUID();
        UUID defendantId2 = UUID.randomUUID();
        final UUID prosecutionCounselId = UUID.randomUUID();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase().withId(caseId)
                        .withDefendants(asList(Defendant.defendant().withId(defendantId1)
                                        .withOffences(asList(Offence.offence().withId(UUID.randomUUID())
                                                .build()))
                                        .build(),
                                Defendant.defendant().withId(defendantId2)
                                        .withOffences(asList(Offence.offence().withId(UUID.randomUUID())
                                                .build()))
                                        .build()))
                        .build()))
                .build();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};



        final String prosecutionCounselString = getStringFromResource("add-prosecution-counsel.json")
                .replace("HEARING_ID", hearingId.toString())
                .replace("CASE_ID", caseId.toString())
                .replace("PROSECUTION_COUNSEL_ID", prosecutionCounselId.toString());

        final AddProsecutionCounsel addProsecutionCounsel = new JsonObjectToObjectConverter(OBJECT_MAPPER).convert(new StringToJsonObjectConverter().convert(prosecutionCounselString), AddProsecutionCounsel.class);



        final UUID streamId = hearingId;
        final Metadata metadata = metadataFor("hearing.add-prosecution-counsel", UUID.randomUUID());
        final Envelope<AddProsecutionCounsel> envelope = envelopeFrom(metadata, addProsecutionCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        prosecutionCounselCommandHandler.addProsecutionCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.prosecution-counsel-added",actualEventProduced.metadata().name());

    }

    @Test
    public void removeProsecutionCounsel() throws EventStreamException, IOException {

        UUID hearingId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        UUID defendantId1 = UUID.randomUUID();
        UUID defendantId2 = UUID.randomUUID();
        final UUID prosecutionCounselId = UUID.randomUUID();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase().withId(caseId)
                        .withDefendants(asList(Defendant.defendant().withId(defendantId1)
                                        .withOffences(asList(Offence.offence().withId(UUID.randomUUID())
                                                .build()))
                                        .build(),
                                Defendant.defendant().withId(defendantId2)
                                        .withOffences(asList(Offence.offence().withId(UUID.randomUUID())
                                                .build()))
                                        .build()))
                        .build()))
                .build();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final String prosecutionCounselString = getStringFromResource("add-prosecution-counsel.json")
                .replace("HEARING_ID", hearingId.toString())
                .replace("CASE_ID", caseId.toString())
                .replace("PROSECUTION_COUNSEL_ID", prosecutionCounselId.toString());

        final AddProsecutionCounsel addProsecutionCounsel = new JsonObjectToObjectConverter(OBJECT_MAPPER).convert(new StringToJsonObjectConverter().convert(prosecutionCounselString), AddProsecutionCounsel.class);

        hearingAggregate.apply(new ProsecutionCounselAdded(addProsecutionCounsel.getProsecutionCounsel(), hearingId));


        final String removeProsecutionCounselString = getStringFromResource("remove-prosecution-counsel.json")
                .replace("HEARING_ID", hearingId.toString())
                .replace("PROSECUTION_COUNSEL_ID", prosecutionCounselId.toString());

        final RemoveProsecutionCounsel removeProsecutionCounsel = new JsonObjectToObjectConverter(OBJECT_MAPPER).convert(new StringToJsonObjectConverter().convert(removeProsecutionCounselString), RemoveProsecutionCounsel.class);


        final UUID streamId = hearingId;
        final Metadata metadata = metadataFor("hearing.remove-prosecution-counsel", UUID.randomUUID());
        final Envelope<RemoveProsecutionCounsel> envelope = envelopeFrom(metadata, removeProsecutionCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        prosecutionCounselCommandHandler.removeProsecutionCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.prosecution-counsel-removed",actualEventProduced.metadata().name());

    }

    @Test
    public void updateProsecutionCounsel() throws EventStreamException, IOException {

        final UpdateProsecutionCounsel updateProsecutionCounsel = fileResourceObjectMapper.convertFromFile("update-prosecution-counsel.json", UpdateProsecutionCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.update-prosecution-counsel", UUID.randomUUID());
        final Envelope<UpdateProsecutionCounsel> envelope = envelopeFrom(metadata, updateProsecutionCounsel);
        final ProsecutionCounsel prosecutionCounselTest = mock(ProsecutionCounsel.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();

        ProsecutionCounselAdded prosecutionCounselAdded = mock(ProsecutionCounselAdded.class);

        when(prosecutionCounselTest.getId()).thenReturn(envelope.payload().getProsecutionCounsel().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);

        when(prosecutionCounselAdded.getProsecutionCounsel()).thenReturn(prosecutionCounselTest);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        hearingAggregate.apply(prosecutionCounselAdded);

        prosecutionCounselCommandHandler.updateProsecutionCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.prosecution-counsel-updated",actualEventProduced.metadata().name());

    }
}