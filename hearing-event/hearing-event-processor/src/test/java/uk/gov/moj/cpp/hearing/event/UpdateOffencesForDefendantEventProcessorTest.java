package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import javax.json.JsonObject;
import javax.json.JsonString;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOffencesForDefendantEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> commandEnvelopeArgumentCaptor;

    @InjectMocks
    private UpdateOffencesForDefendantEventProcessor updateOffencesForDefendantEventProcessor;

    @Test
    public void processPublicCaseDefendantOffencesChanged() {

        final UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand = updateOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), randomUUID())
                .setOffencesToUpdate(asList(randomUUID())));

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.defendant-offences-changed"),
                objectToJsonObjectConverter.convert(updateOffencesForDefendantCommand));

        updateOffencesForDefendantEventProcessor.onPublicProgressionEventsOffencesForDefendantUpdated(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.command.update-offences-for-defendant"),
                        payloadIsJson(allOf(
                                withJsonPath("$.modifiedDate", is(updateOffencesForDefendantCommand.getModifiedDate().toString())),
                                withJsonPath("$.updatedOffences[0].defendantId", is(updateOffencesForDefendantCommand.getUpdatedOffences()
                                        .get(0).getDefendantId().toString())),
                                withJsonPath("$.updatedOffences[0].prosecutionCaseId", is(updateOffencesForDefendantCommand.getUpdatedOffences()
                                        .get(0).getProsecutionCaseId().toString())),
                                withJsonPath("$.updatedOffences[0].offences[0].reportingRestrictions[0]", notNullValue()))
                        )
                )
        );
    }

    @Test
    public void addCaseDefendantOffence() {

        final FoundHearingsForNewOffence foundHearingsForNewOffence = FoundHearingsForNewOffence.foundHearingsForNewOffence()
                .withHearingIds(Collections.singletonList(randomUUID()))
                .withDefendantId(randomUUID())
                .withProsecutionCaseId(randomUUID())
                .withOffence(Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.found-hearings-for-new-offence"),
                objectToJsonObjectConverter.convert(foundHearingsForNewOffence));

        updateOffencesForDefendantEventProcessor.addCaseDefendantOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.command.add-new-offence-to-hearings"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingIds[0]", is(foundHearingsForNewOffence.getHearingIds().get(0).toString())),
                                withJsonPath("$.defendantId", is(foundHearingsForNewOffence.getDefendantId().toString())),
                                withJsonPath("$.prosecutionCaseId", is(foundHearingsForNewOffence.getProsecutionCaseId().toString())),
                                withJsonPath("$.offence.id", is(foundHearingsForNewOffence.getOffence().getId().toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void updateCaseDefendantOffence() {

        final FoundHearingsForEditOffence foundHearingsForEditOffence = FoundHearingsForEditOffence.foundHearingsForEditOffence()
                .withHearingIds(Collections.singletonList(randomUUID()))
                .withDefendantId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.found-hearings-for-edit-offence"),
                objectToJsonObjectConverter.convert(foundHearingsForEditOffence));

        updateOffencesForDefendantEventProcessor.updateCaseDefendantOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.command.update-offence-on-hearings"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingIds[0]", is(foundHearingsForEditOffence.getHearingIds().get(0).toString())),
                                withJsonPath("$.defendantId", is(foundHearingsForEditOffence.getDefendantId().toString())),
                                withJsonPath("$.offence.id", is(foundHearingsForEditOffence.getOffence().getId().toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void deleteCaseDefendantOffence() {

        FoundHearingsForDeleteOffence offence = FoundHearingsForDeleteOffence.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(randomUUID()))
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.found-hearings-for-delete-offence"),
                objectToJsonObjectConverter.convert(offence));

        updateOffencesForDefendantEventProcessor.deleteCaseDefendantOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.command.delete-offence-on-hearings"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(offence.getId().toString())),
                                withJsonPath("$.hearingIds[0]", is(offence.getHearingIds().get(0).toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void offenceAdded() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();

        OffenceAdded offenceAdded = OffenceAdded.offenceAdded()
                .withOffence(Offence.offence().withId(offenceId).build())
                .withDefendantId(randomUUID())
                .withHearingId(hearingId)
                .withProsecutionCaseId(randomUUID());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.offence-added"),
                objectToJsonObjectConverter.convert(offenceAdded));

        updateOffencesForDefendantEventProcessor.addOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope( this.envelopeArgumentCaptor.getValue()), jsonEnvelope(
                        metadata().withName("hearing.command.register-hearing-against-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void shouldRaiseRemoveOffencesFromExistingAllocatedHearingCommand() {

        final UUID hearingId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final JsonObject envelopePayload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("offenceIds", createArrayBuilder()
                        .add(offenceId1.toString())
                        .add(offenceId2.toString())
                        .build())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.events.listing.offences-removed-from-existing-allocated-hearing"), envelopePayload);

        updateOffencesForDefendantEventProcessor.handleOffencesRemovedFromExistingAllocatedHearingPublicEvent(event);

        verify(this.sender, times(1)).send(this.commandEnvelopeArgumentCaptor.capture());
        final Envelope<JsonObject> command = this.commandEnvelopeArgumentCaptor.getAllValues().get(0);
        assertThat(command.metadata().name(), is("hearing.command.remove-offences-from-existing-allocated-hearing"));
        assertThat(command.payload().getString("hearingId"), is(hearingId.toString()));
        final List<JsonString> offenceIds = command.payload().getJsonArray("offenceIds").getValuesAs(JsonString.class);
        assertThat(offenceIds.get(0).getString(), is(offenceId1.toString()));
        assertThat(offenceIds.get(1).getString(), is(offenceId2.toString()));

    }

    @Test
    public void ShouldRaisePublicEventIfSourceIsHearingContext(){
        final UUID hearingId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final JsonObject envelopePayload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("sourceContext", "Hearing")
                .add("offenceIds", createArrayBuilder()
                        .add(offenceId1.toString())
                        .add(offenceId2.toString())
                        .build())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.offences-removed-from-existing-hearing"), envelopePayload);
        updateOffencesForDefendantEventProcessor.handleOffenceOrDefendantRemovalToListAssist(event);

        verify(this.sender, times(1)).send(this.commandEnvelopeArgumentCaptor.capture());
        final Envelope<JsonObject> command = this.commandEnvelopeArgumentCaptor.getAllValues().get(0);
        assertThat(command.metadata().name(), is("public.hearing.selected-offences-removed-from-existing-hearing"));
        assertThat(command.payload().getString("hearingId"), is(hearingId.toString()));
        final List<JsonString> offenceIds = command.payload().getJsonArray("offenceIds").getValuesAs(JsonString.class);
        assertThat(offenceIds.get(0).getString(), is(offenceId1.toString()));
        assertThat(offenceIds.get(1).getString(), is(offenceId2.toString()));
    }

    @Test
    public void ShouldNotRaisePublicEventIfSourceIsListingContext(){
        final UUID hearingId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final JsonObject envelopePayload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("sourceContext", "Listing")
                .add("offenceIds", createArrayBuilder()
                        .add(offenceId1.toString())
                        .add(offenceId2.toString())
                        .build())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.offences-removed-from-existing-hearing"), envelopePayload);
        updateOffencesForDefendantEventProcessor.handleOffenceOrDefendantRemovalToListAssist(event);

        verify(this.sender, times(0)).send(this.commandEnvelopeArgumentCaptor.capture());

    }

    @Test
    public void shouldDeleteHearingFromDefendantAggregateWhenDefendantDeletedFromHearing(){
        final UUID hearingId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();

        final JsonObject envelopePayload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("sourceContext", "Listing")
                .add("defendantIds", createArrayBuilder()
                        .add(defendantId1.toString())
                        .add(defendantId2.toString())
                        .build())
                .add("offenceIds", createArrayBuilder()
                        .add(offenceId1.toString())
                        .add(offenceId2.toString())
                        .build())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.offences-removed-from-existing-hearing"), envelopePayload);
        updateOffencesForDefendantEventProcessor.handleOffenceOrDefendantRemovalToListAssist(event);

        verify(this.sender, times(1)).send(this.commandEnvelopeArgumentCaptor.capture());
        final Envelope<JsonObject> command = this.commandEnvelopeArgumentCaptor.getAllValues().get(0);
        assertThat(command.metadata().name(), is("hearing.command.delete-hearing-for-defendants"));
        assertThat(command.payload().getString("hearingId"), is(hearingId.toString()));
        final List<JsonString> defendantIds = command.payload().getJsonArray("defendantIds").getValuesAs(JsonString.class);
        assertThat(defendantIds.get(0).getString(), is(defendantId1.toString()));
        assertThat(defendantIds.get(1).getString(), is(defendantId2.toString()));
    }

}
