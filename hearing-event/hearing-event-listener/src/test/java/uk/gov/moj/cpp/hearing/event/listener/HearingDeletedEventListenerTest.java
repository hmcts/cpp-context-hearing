package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingDeletedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ProsecutionCaseRepository pcRepository;

    @InjectMocks
    private HearingDeletedEventListener hearingDeletedEventListener;

    @Test
    public void shouldDeleteHearingWhenExistsInViewStore() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        hearingDeletedEventListener.hearingDeleted(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository).remove(hearing);
    }

    @Test
    public void shouldNotDeleteHearingWhenHearingNotExistsInViewStore() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();

        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingDeletedEventListener.hearingDeleted(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository, never()).remove(hearing);
    }

    @Test
    public void shouldDeleteHearingBdfWhenExistsInViewStore() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();

        final ProsecutionCase pc = new ProsecutionCase();

        when(hearingRepository.findProsecutionCasesByHearingId(hearingId)).thenReturn(List.of(pc));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        hearingDeletedEventListener.hearingDeletedBdf(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository).remove(hearing);
        verify(pcRepository).remove(pc);
        verify(pcRepository).flush();
    }

    @Test
    public void shouldDeleteHearingBdfWhenPcDontExists() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();

        when(hearingRepository.findProsecutionCasesByHearingId(hearingId)).thenReturn(Collections.emptyList());
        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        hearingDeletedEventListener.hearingDeletedBdf(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository).remove(hearing);
        verifyNoInteractions(pcRepository);
    }

    @Test
    public void shouldNotDeleteHearingBdfWhenHearingNotExistsInViewStore() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();

        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingDeletedEventListener.hearingDeletedBdf(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository, never()).remove(hearing);
    }

    @Test
    public void shouldProcessCourtApplicationDeleted() {
        Envelope<CourtApplicationHearingDeleted> envelope = (Envelope<CourtApplicationHearingDeleted>) mock(Envelope.class);

        final UUID hearingId = randomUUID();
        final CourtApplicationHearingDeleted courtApplicationHearingDeleted = CourtApplicationHearingDeleted.courtApplicationHearingDeleted()
                .withHearingId(hearingId)
                .build();
        given(envelope.payload()).willReturn(courtApplicationHearingDeleted);
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        when(hearingRepository.findBy(any())).thenReturn(hearing);
        hearingDeletedEventListener.processCourtApplicationDeleted(envelope);

        verify(hearingRepository).remove(hearing);
    }
}
