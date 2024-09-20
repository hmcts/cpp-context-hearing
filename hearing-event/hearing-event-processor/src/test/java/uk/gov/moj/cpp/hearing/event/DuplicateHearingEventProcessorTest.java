package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicateHearingEventProcessorTest {

    private final UUID hearingId = randomUUID();
    private final UUID caseId1 = randomUUID();
    private final UUID caseId2 = randomUUID();
    private final UUID defendantId1 = randomUUID();
    private final UUID defendantId2 = randomUUID();
    private final UUID offenceId1 = randomUUID();
    private final UUID offenceId2 = randomUUID();
    private final UUID courtCentreId = randomUUID();

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> publicEventArgumentCaptor;

    @Mock
    private Sender sender;

    @InjectMocks
    private DuplicateHearingEventProcessor duplicateHearingEventProcessor;

    @Test
    public void shouldHandleHearingMarkedAsDuplicate() {

        this.duplicateHearingEventProcessor.handleHearingMarkedAsDuplicate(buildJsonEnvelope());

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
        assertThat(publicEvent.payload().toString(), isJson(allOf(
                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                withoutJsonPath("$.courtCentreId")
        )));
    }

    @Test
    public void shouldHandleHearingMarkedAsDuplicateAndCourtCentreIdIsNotExist() {
        this.duplicateHearingEventProcessor.handleHearingMarkedAsDuplicate(buildJsonEnvelopeWithOutCourtCentreId());

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
        assertThat(publicEvent.payload().toString(), isJson(allOf(
                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                withoutJsonPath("$.courtCentreId")
        )));
    }

    private JsonEnvelope buildJsonEnvelope() {
        return envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
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
                .add("courtCentreId", courtCentreId.toString())
                .build());
    }

    private JsonEnvelope buildJsonEnvelopeWithOutCourtCentreId() {
        return envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
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
                .build());
    }
}