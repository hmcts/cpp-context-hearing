package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Asserts the public event name and that the payload is passed through unchanged — the
 * contract consumers rely on. A test that only verified {@code send} was called would pass
 * even if the processor published the wrong event or dropped the tier.
 */
@ExtendWith(MockitoExtension.class)
public class PtphDetailEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private PtphDetailEventProcessor processor;

    @Test
    public void shouldPublishPublicPtphDetailSavedEventCarryingAllValues() {
        final UUID hearingId = randomUUID();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.ptph-detail-saved"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_3")
                        .add("listType", "TYPE_1_FIXED")
                        .add("keyReason", "Trial fixed date required by court order")
                        .build());

        processor.publishPublicPtphDetailSavedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope published = this.envelopeArgumentCaptor.getValue();
        final JsonObject payload = published.payloadAsJsonObject();

        assertThat(published.metadata().name(), is("public.hearing.ptph-detail-saved"));
        assertThat(payload.getString("hearingId"), is(hearingId.toString()));
        assertThat(payload.getString("tier"), is("TIER_3"));
        assertThat(payload.getString("listType"), is("TYPE_1_FIXED"));
        assertThat(payload.getString("keyReason"), is("Trial fixed date required by court order"));
    }

    /**
     * A tier-only save is valid, and a flexible list type has its key reason discarded before
     * the event is emitted, so the public event must tolerate the optional fields being absent.
     */
    @Test
    public void shouldPublishPublicPtphDetailSavedEventWhenOnlyTierIsPresent() {
        final UUID hearingId = randomUUID();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.ptph-detail-saved"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_1")
                        .build());

        processor.publishPublicPtphDetailSavedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final JsonObject payload = this.envelopeArgumentCaptor.getValue().payloadAsJsonObject();

        assertThat(this.envelopeArgumentCaptor.getValue().metadata().name(), is("public.hearing.ptph-detail-saved"));
        assertThat(payload.getString("tier"), is("TIER_1"));
        assertThat(payload.get("listType"), is(nullValue()));
        assertThat(payload.get("keyReason"), is(nullValue()));
    }

    @Test
    public void shouldPublishPublicPtphDetailFinalisedEvent() {
        final UUID hearingId = randomUUID();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.ptph-detail-finalised"),
                createObjectBuilder().add("hearingId", hearingId.toString()).build());

        processor.publishPublicPtphDetailFinalisedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope published = this.envelopeArgumentCaptor.getValue();

        assertThat(published.metadata().name(), is("public.hearing.ptph-detail-finalised"));
        assertThat(published.payloadAsJsonObject().getString("hearingId"), is(hearingId.toString()));
    }

    @Test
    public void shouldPublishPublicPtphDetailDeletedEvent() {
        final UUID hearingId = randomUUID();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.ptph-detail-deleted"),
                createObjectBuilder().add("hearingId", hearingId.toString()).build());

        processor.publishPublicPtphDetailDeletedEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope published = this.envelopeArgumentCaptor.getValue();

        assertThat(published.metadata().name(), is("public.hearing.ptph-detail-deleted"));
        assertThat(published.payloadAsJsonObject().getString("hearingId"), is(hearingId.toString()));
    }
}
