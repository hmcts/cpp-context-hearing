package uk.gov.moj.cpp.hearing.event;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

public class DefendantLegalAidStatusUpdatedProcessorTest {

    private static final String DEFENDANT_ID = "defendantId";
    private static final String HEARING_ID = "hearingId";
    private static final String CASE_ID = "caseId";
    private static final String LEGAL_AID_STATUS = "legalAidStatus";
    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Mock
    private Sender sender;

    @Mock
    private DefendantRepository defendantRepository;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private DefendantLegalAidStatusUpdatedProcessor defendantLegalAidStatusUpdatedProcessor;

    private String caseId;
    private String defendantId;

    @BeforeEach
    public void initMocks() {
        this.defendantId = randomUUID().toString();
        this.caseId = randomUUID().toString();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDefendantLegalAidStatusUpdate() {
        final JsonObject eventPayload = Json.createObjectBuilder()
                .add(DEFENDANT_ID, defendantId)
                .add(CASE_ID, caseId)
                .add(LEGAL_AID_STATUS, "Granted")
                .build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("progression.defendant-legalaid-status-updated"),
                eventPayload);

        defendantLegalAidStatusUpdatedProcessor.defendantLegalStatusUpdate(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-defendant-legalaid-status"));

        assertThat(events.get(0).payloadAsJsonObject().getString(DEFENDANT_ID), is(defendantId));

        assertThat(events.get(0).payloadAsJsonObject().getString(CASE_ID), is(caseId));

        assertThat(events.get(0).payloadAsJsonObject().getString(LEGAL_AID_STATUS), is("Granted"));

    }

    @Test
    public void testHandleDefendantLegalStatusUpdateForHearings() {

        final String LEGAL_AID_STATUS = "legalAidStatus";
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();

        final JsonObject eventPayload = Json.createObjectBuilder()
                .add(DEFENDANT_ID, defendantId.toString())
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()).build())
                .add(LEGAL_AID_STATUS, "Granted")
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.defendant-legalaid-status-updated"),
                eventPayload);

        when(defendantRepository.findBy(any(HearingSnapshotKey.class))).thenReturn(new Defendant());
        defendantLegalAidStatusUpdatedProcessor.handleDefendantLegalStatusUpdateForHearings(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        MatcherAssert.assertThat(events.get(0).metadata().name(), is("hearing.command.update-defendant-legalaid-status-for-hearing"));

        MatcherAssert.assertThat(events.get(0).payloadAsJsonObject().getString(DEFENDANT_ID), is(defendantId.toString()));

        MatcherAssert.assertThat(events.get(0).payloadAsJsonObject().getString(HEARING_ID), is(hearingId.toString()));

        MatcherAssert.assertThat(events.get(0).payloadAsJsonObject().getString(LEGAL_AID_STATUS), is("Granted"));

    }
}
