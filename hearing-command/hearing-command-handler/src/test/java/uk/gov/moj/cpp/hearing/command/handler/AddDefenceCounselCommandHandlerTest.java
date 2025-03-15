package uk.gov.moj.cpp.hearing.command.handler;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
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
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class AddDefenceCounselCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefenceCounselAdded.class,
            DefenceCounselRemoved.class,
            DefenceCounselUpdated.class
    );
    @InjectMocks
    private AddDefenceCounselCommandHandler defenceCounselCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;

    @Mock
    private HearingAggregateMomento hearingAggregateMomento ;
    @Mock
    private AggregateService aggregateService;

    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperProducer().objectMapper();

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }

    @Test
    public void addDefenceCounsel() throws EventStreamException, IOException {
        UUID hearingId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        UUID defendantId1 = UUID.randomUUID();
        UUID defendantId2 = UUID.randomUUID();

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

        final String defenceCounselString = getStringFromResource("add-defence-counsel.json")
                .replace("HEARING_ID", hearingId.toString())
                .replace("DEFENDANT_ID1", defendantId1.toString())
                .replace("DEFENDANT_ID2", defendantId2.toString());

        final AddDefenceCounsel addDefenceCounsel = new JsonObjectToObjectConverter(OBJECT_MAPPER).convert(new StringToJsonObjectConverter().convert(defenceCounselString), AddDefenceCounsel.class);

        final Metadata metadata = metadataFor("hearing.add-defence-counsel", UUID.randomUUID());
        final Envelope<AddDefenceCounsel> envelope = envelopeFrom(metadata, addDefenceCounsel);

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        defenceCounselCommandHandler.addDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.defence-counsel-added",actualEventProduced.metadata().name());
    }

    @Test
    public void removeDefenceCounsel() throws EventStreamException, IOException {

        final RemoveDefenceCounsel removeDefenceCounsel = fileResourceObjectMapper.convertFromFile("remove-defence-counsel.json", RemoveDefenceCounsel.class);

        final UUID streamId = UUID.fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d");
        final Metadata metadata = metadataFor("hearing.remove-defence-counsel", UUID.randomUUID());
        final Envelope<RemoveDefenceCounsel> envelope = envelopeFrom(metadata, removeDefenceCounsel);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate());

        defenceCounselCommandHandler.removeDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.defence-counsel-removed",actualEventProduced.metadata().name());

    }

    @Test
    public void updateDefenceCounsel() throws EventStreamException, IOException {

        final UpdateDefenceCounsel updateDefenceCounsel = fileResourceObjectMapper.convertFromFile("update-defence-counsel.json", UpdateDefenceCounsel.class);

        final UUID streamId = UUID.fromString("029034d9-0f54-43c5-ba36-e5deadd62474");
        final Metadata metadata = metadataFor("hearing.update-defence-counsel", UUID.randomUUID());
        final Envelope<UpdateDefenceCounsel> envelope = envelopeFrom(metadata, updateDefenceCounsel);
        final DefenceCounsel defenceCounselTest = mock(DefenceCounsel.class);
        final HearingAggregate hearingAggregate = new HearingAggregate();

        DefenceCounselAdded defenceCounselAdded = mock(DefenceCounselAdded.class);

        when(defenceCounselTest.getId()).thenReturn(envelope.payload().getDefenceCounsel().getId());
        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);

        when(defenceCounselAdded.getDefenceCounsel()).thenReturn(defenceCounselTest);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        hearingAggregate.apply(defenceCounselAdded);

        defenceCounselCommandHandler.updateDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.defence-counsel-updated",actualEventProduced.metadata().name());

    }

    @Test
    public void addDefenceCounselToSingleDefendantIsOnHearing() throws EventStreamException, IOException {
        UUID hearingId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        UUID defendantId1 = UUID.randomUUID();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase().withId(caseId)
                        .withDefendants(asList(Defendant.defendant().withId(defendantId1)
                                .withOffences(asList(Offence.offence().withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final String defenceCounselString = getStringFromResource("add-defence-counsel-defendant-not-in-hearing.json")
                .replace("HEARING_ID", hearingId.toString())
                .replace("DEFENDANT_ID1", defendantId1.toString());

        final AddDefenceCounsel addDefenceCounsel = new JsonObjectToObjectConverter(OBJECT_MAPPER).convert(new StringToJsonObjectConverter().convert(defenceCounselString), AddDefenceCounsel.class);

        final Metadata metadata = metadataFor("hearing.add-defence-counsel", UUID.randomUUID());
        final Envelope<AddDefenceCounsel> envelope = envelopeFrom(metadata, addDefenceCounsel);

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        defenceCounselCommandHandler.addDefenceCounsel(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        assertEquals( "hearing.defence-counsel-added",actualEventProduced.metadata().name());
    }

    @Test
    public void shouldNotRaiseEventAddDefenceCounsel_WhenDefendantIsNotOnHearing() throws EventStreamException, IOException {
        UUID hearingId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        UUID defendantId1 = UUID.randomUUID();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase().withId(caseId)
                        .withDefendants(asList(Defendant.defendant().withId(defendantId1)
                                .withOffences(asList(Offence.offence().withId(UUID.randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final String defenceCounselString = getStringFromResource("add-defence-counsel-defendant-not-in-hearing.json")
                .replace("HEARING_ID", hearingId.toString())
                .replace("DEFENDANT_ID1", UUID.randomUUID().toString()); // Defendant not on hearing

        final AddDefenceCounsel addDefenceCounsel = new JsonObjectToObjectConverter(OBJECT_MAPPER).convert(new StringToJsonObjectConverter().convert(defenceCounselString), AddDefenceCounsel.class);

        final Metadata metadata = metadataFor("hearing.add-defence-counsel", UUID.randomUUID());
        final Envelope<AddDefenceCounsel> envelope = envelopeFrom(metadata, addDefenceCounsel);

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(hearingAggregate);

        defenceCounselCommandHandler.addDefenceCounsel(envelope);
        final List<Object> events = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList());
        assertThat(events, empty());
    }
}