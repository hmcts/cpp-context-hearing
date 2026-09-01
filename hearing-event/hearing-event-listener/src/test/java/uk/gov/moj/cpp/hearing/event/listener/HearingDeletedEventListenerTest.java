package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
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
import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

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

    @Mock
    private PtphDetailRepository ptphDetailRepository;

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

    // ---------------------------------------------------------------------------------
    // LPT-2400-2404: ha_ptph_detail is keyed by hearing id but has no foreign key to the
    // hearing table, so nothing cascades. Without explicit cleanup the row outlives the
    // hearing and the ptph-detail query keeps answering for a hearing that no longer exists.
    // ---------------------------------------------------------------------------------

    @Test
    public void shouldRemovePtphDetailWhenHearingIsDeleted() {
        final UUID hearingId = randomUUID();
        final PtphDetail ptphDetail = new PtphDetail();

        when(hearingRepository.findBy(hearingId)).thenReturn(new Hearing());
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(ptphDetail);

        hearingDeletedEventListener.hearingDeleted(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(ptphDetailRepository).removeAndFlush(ptphDetail);
    }

    @Test
    public void shouldRemovePtphDetailWhenHearingIsDeletedByBdf() {
        final UUID hearingId = randomUUID();
        final PtphDetail ptphDetail = new PtphDetail();

        when(hearingRepository.findProsecutionCasesByHearingId(hearingId)).thenReturn(Collections.emptyList());
        when(hearingRepository.findBy(hearingId)).thenReturn(new Hearing());
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(ptphDetail);

        hearingDeletedEventListener.hearingDeletedBdf(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(ptphDetailRepository).removeAndFlush(ptphDetail);
    }

    /**
     * The court-application deletion path deletes the same hearing row as the other two handlers,
     * so it must clean up ha_ptph_detail too. It was the one handler that did not, leaving an
     * orphan row that the ptph-detail query would keep answering from.
     */
    @Test
    public void shouldRemovePtphDetailWhenCourtApplicationHearingIsDeleted() {
        final Envelope<CourtApplicationHearingDeleted> envelope = (Envelope<CourtApplicationHearingDeleted>) mock(Envelope.class);
        final UUID hearingId = randomUUID();
        final PtphDetail ptphDetail = new PtphDetail();

        given(envelope.payload()).willReturn(CourtApplicationHearingDeleted.courtApplicationHearingDeleted()
                .withHearingId(hearingId)
                .build());
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(ptphDetail);

        hearingDeletedEventListener.processCourtApplicationDeleted(envelope);

        verify(ptphDetailRepository).removeAndFlush(ptphDetail);
        verify(hearingRepository).remove(hearing);
    }

    /** Same rule on the plain deletion path: no hearing removed, no tier removed. */
    @Test
    public void shouldLeavePtphDetailAloneWhenTheHearingIsNotInTheViewStore() {
        final UUID hearingId = randomUUID();

        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingDeletedEventListener.hearingDeleted(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(ptphDetailRepository, never()).removeAndFlush(any());
    }

    /** And on the BDF path. */
    @Test
    public void shouldLeavePtphDetailAloneWhenTheBdfHearingIsNotInTheViewStore() {
        final UUID hearingId = randomUUID();

        when(hearingRepository.findProsecutionCasesByHearingId(hearingId)).thenReturn(Collections.emptyList());
        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingDeletedEventListener.hearingDeletedBdf(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(ptphDetailRepository, never()).removeAndFlush(any());
    }

    /**
     * The cleanup is deliberately tied to the hearing actually being removed, not run
     * unconditionally: the record exists only to describe a hearing, so it loses its reason to
     * exist exactly when the hearing does and not before. If the upstream command is ever fixed to
     * keep a hearing that still has other applications, the tier survives with it.
     */
    @Test
    public void shouldLeavePtphDetailAloneWhenTheCourtApplicationHearingIsNotInTheViewStore() {
        final Envelope<CourtApplicationHearingDeleted> envelope = (Envelope<CourtApplicationHearingDeleted>) mock(Envelope.class);
        final UUID hearingId = randomUUID();

        given(envelope.payload()).willReturn(CourtApplicationHearingDeleted.courtApplicationHearingDeleted()
                .withHearingId(hearingId)
                .build());
        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingDeletedEventListener.processCourtApplicationDeleted(envelope);

        verify(ptphDetailRepository, never()).removeAndFlush(any());
    }

    /** Most hearings have no ptph detail, so deletion must not fail for them. */
    @Test
    public void shouldDeleteHearingWithoutErrorWhenThereIsNoPtphDetail() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(null);

        hearingDeletedEventListener.hearingDeleted(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(ptphDetailRepository, never()).removeAndFlush(any());
        verify(hearingRepository).remove(hearing);
    }
}
