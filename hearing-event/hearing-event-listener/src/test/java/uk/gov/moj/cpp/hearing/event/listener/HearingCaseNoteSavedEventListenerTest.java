package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import uk.gov.justice.json.schemas.core.HearingCaseNote;
import uk.gov.justice.json.schemas.core.NoteType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.mapping.HearingCaseNoteJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingCaseNoteRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingCaseNoteSavedEventListenerTest {

    @InjectMocks
    private HearingCaseNoteSavedEventListener hearingCaseNoteSavedEventListener;

    @Mock
    private HearingCaseNoteRepository hearingCaseNoteRepository;

    @Mock
    private HearingRepository hearingRepository;

    @Captor
    private ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote> hearingCaseNoteEntityArgumentCaptor;

    @Mock
    private HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Test
    public void shouldSaveHearingCaseNote() {

        final Metadata metadata = metadataFor("hearing.command.save-hearing-case-note", randomUUID());
        final UUID hearingId = randomUUID();

        final JsonObjectBuilder envelopeData = createObjectBuilder()
                .add("hearingCaseNote", createObjectBuilder()
                        .add("id", randomUUID().toString())
                        .add("originatingHearingId", hearingId.toString())
                        .add("courtClerk", createObjectBuilder()
                                .add("id", randomUUID().toString())
                                .add("firstName", STRING.next())
                                .add("lastName", STRING.next())
                        )
                        .add("prosecutionCases", createArrayBuilder().add(randomUUID().toString()))
                        .add("noteDateTime", ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next()))
                        .add("noteType", randomEnum(NoteType.class).next().toString())
                        .add("note", STRING.next())
                );

        final JsonEnvelope envelope = envelopeFrom(metadata, envelopeData.build());

//        final HearingCaseNoteSaved hearingCaseNoteSaved = HearingCaseNoteSaved.hearingCaseNoteSaved()
//                .withHearingCaseNote(HearingCaseNote.hearingCaseNote()
//                        .withCourtClerk(CourtClerk.courtClerk()
//                                .withId(randomUUID())
//                                .withLastName(STRING.next())
//                                .withFirstName(STRING.next())
//                                .build())
//                        .withId(randomUUID())
//                        .withOriginatingHearingId(hearingId)
//                        .withProsecutionCases(asList(randomUUID(), randomUUID()))
//                        .withNoteDateTime(ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next()))
//                        .withNoteType(randomEnum(NoteType.class).next())
//                        .withNote(STRING.next())
//                        .build())
//                .build();
//
//        final Envelope<HearingCaseNoteSaved> envelope = envelopeFrom(metadata, hearingCaseNoteSaved);

        final Hearing hearingEntity = new Hearing();
        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote caseNoteEntityToBeSaved = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote.class);
        when(hearingCaseNoteJPAMapper.toJPA(eq(hearingEntity), any(HearingCaseNote.class))).thenReturn(caseNoteEntityToBeSaved);

        hearingCaseNoteSavedEventListener.saveHearingCaseNoteListener(envelope);

        verify(hearingCaseNoteRepository).save(hearingCaseNoteEntityArgumentCaptor.capture());
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote savedEntity = hearingCaseNoteEntityArgumentCaptor.getValue();
        assertThat(savedEntity, is(caseNoteEntityToBeSaved));
    }

    public static Metadata metadataFor(final String commandName, final UUID commandId) {
        return metadataBuilder()
                .withName(commandName)
                .withId(commandId)
                .build();
    }

}