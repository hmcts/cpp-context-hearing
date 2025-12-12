package uk.gov.moj.cpp.hearing.command.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.matchEvent;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.casenote.SaveHearingCaseNote;
import uk.gov.moj.cpp.hearing.domain.event.HearingCaseNoteSaved;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.util.UUID;

import javax.json.JsonValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SaveHearingCaseNoteCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingCaseNoteSaved.class
    );
    @InjectMocks
    private SaveHearingCaseNoteCommandHandler saveHearingCaseNoteCommandHandler;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    private FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    @Test
    public void shouldSaveNoteReceived() throws Exception {

        final SaveHearingCaseNote hearingCaseNote = fileResourceObjectMapper.convertFromFile("save-hearing-case-note.json", SaveHearingCaseNote.class);

        final UUID streamId = UUID.fromString("e9b2ef55-5dc0-4eb1-b0e3-f22f31a0e5cb");
        final Metadata metadata = metadataFor("hearing.command.save-hearing-case-note", UUID.randomUUID());
        final Envelope<SaveHearingCaseNote> envelope = envelopeFrom(metadata, hearingCaseNote);

        when(eventSource.getStreamById(streamId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any()))
                .thenReturn(new uk.gov.moj.cpp.hearing.domain.aggregate.HearingCaseNote());

        saveHearingCaseNoteCommandHandler.saveHearingCaseNote(envelope);

//        Stream<JsonEnvelope>  sEnvelopes = verifyAppendAndGetArgumentFrom(hearingEventStream);
//        List<JsonEnvelope> envelopes = sEnvelopes.collect(Collectors.toList());
//        Assert.assertEquals(envelopes.size(), 1);



        matchEvent(verifyAppendAndGetArgumentFrom(hearingEventStream),
                "hearing.hearing-case-note-saved",
                fileResourceObjectMapper.convertFromFile("hearing-case-note-saved.json", JsonValue.class));


    }
}