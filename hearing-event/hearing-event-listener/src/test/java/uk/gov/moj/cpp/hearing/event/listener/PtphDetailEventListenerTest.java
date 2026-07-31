package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PtphDetailEventListenerTest {

    @Mock
    private PtphDetailRepository repository;

    @Captor
    private ArgumentCaptor<PtphDetail> captor;

    @InjectMocks
    private PtphDetailEventListener listener;

    @Test
    void savedUpsertsRow() {
        final UUID hearingId = randomUUID();
        when(repository.findBy(hearingId)).thenReturn(null);

        listener.ptphDetailSaved(envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-saved"),
                Json.createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .add("keyReason", "reason")
                        .build()));

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTier(), is("TIER_2"));
        assertThat(captor.getValue().getKeyReason(), is("reason"));
    }

    @Test
    void savedUpsertsRowWithTierOnly() {
        final UUID hearingId = randomUUID();
        when(repository.findBy(hearingId)).thenReturn(null);

        listener.ptphDetailSaved(envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-saved"),
                Json.createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_3")
                        .build()));

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTier(), is("TIER_3"));
        assertThat(captor.getValue().getListType(), is((Object) null));
        assertThat(captor.getValue().getKeyReason(), is((Object) null));
    }

    @Test
    void finalisedSetsFlag() {
        final UUID hearingId = randomUUID();
        final PtphDetail existing = new PtphDetail();
        existing.setHearingId(hearingId);
        when(repository.findBy(hearingId)).thenReturn(existing);

        listener.ptphDetailFinalised(envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-finalised"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build()));

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isFinalised(), is(true));
    }

    @Test
    void deletedRemovesRow() {
        final UUID hearingId = randomUUID();
        final PtphDetail existing = new PtphDetail();
        existing.setHearingId(hearingId);
        when(repository.findBy(hearingId)).thenReturn(existing);

        listener.ptphDetailDeleted(envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-deleted"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build()));

        verify(repository).removeAndFlush(existing);
    }
}
