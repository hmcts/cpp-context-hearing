package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;

import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonString;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateHearingEventProcessorTest {

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> publicEventArgumentCaptor;

    @Mock
    private Sender sender;

    @InjectMocks
    private DuplicateHearingEventProcessor duplicateHearingEventProcessor;

    @Test
    public void shouldHandleHearingMarkedAsDuplicate() {
        final UUID hearingId = randomUUID();
        final UUID caseId1 = randomUUID();
        final UUID caseId2 = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        this.duplicateHearingEventProcessor.handleHearingMarkedAsDuplicate(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("prosecutionCaseIds", createArrayBuilder()
                        .add(caseId1.toString())
                        .add(caseId2.toString())
                        .build())
                .add("defendantIds", createArrayBuilder()
                        .add(defendantId1.toString())
                        .add(defendantId2.toString())
                        .build())
                .add("offenceIds", createArrayBuilder()
                        .add(offenceId1.toString())
                        .add(offenceId2.toString())
                        .build())
                .build()));

        verify(this.sender, times(4)).send(this.publicEventArgumentCaptor.capture());

        final Envelope<JsonObject> caseCommand = this.publicEventArgumentCaptor.getAllValues().get(0);
        assertThat(caseCommand.metadata().name(), is("hearing.command.mark-as-duplicate-for-cases"));
        final List<JsonString> actualProsecutionCaseIds = caseCommand.payload().getJsonArray("prosecutionCaseIds").getValuesAs(JsonString.class);
        assertThat(actualProsecutionCaseIds.get(0).getString(), is(caseId1.toString()));
        assertThat(actualProsecutionCaseIds.get(1).getString(), is(caseId2.toString()));

        final Envelope<JsonObject> defendantCommand = this.publicEventArgumentCaptor.getAllValues().get(1);
        assertThat(defendantCommand.metadata().name(), is("hearing.command.mark-as-duplicate-for-defendants"));
        final List<JsonString> actualDefendantIds = defendantCommand.payload().getJsonArray("defendantIds").getValuesAs(JsonString.class);
        assertThat(actualDefendantIds.get(0).getString(), is(defendantId1.toString()));
        assertThat(actualDefendantIds.get(1).getString(), is(defendantId2.toString()));

        final Envelope<JsonObject> offenceCommand = this.publicEventArgumentCaptor.getAllValues().get(2);
        assertThat(offenceCommand.metadata().name(), is("hearing.command.mark-as-duplicate-for-offences"));
        final List<JsonString> actualOffenceIds = offenceCommand.payload().getJsonArray("offenceIds").getValuesAs(JsonString.class);
        assertThat(actualOffenceIds.get(0).getString(), is(offenceId1.toString()));
        assertThat(actualOffenceIds.get(1).getString(), is(offenceId2.toString()));

        final Envelope<JsonObject> publicEvent = this.publicEventArgumentCaptor.getAllValues().get(3);
        assertThat(publicEvent.metadata().name(), is("public.events.hearing.marked-as-duplicate"));
        assertThat(publicEvent.payload().getString("hearingId"), is(hearingId.toString()));
    }

}