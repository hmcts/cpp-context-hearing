package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantOffencesChangedCommandTemplates.updateOffencesForDefendantTemplate;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;

import java.util.Collections;

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
public class UpdateOffencesForDefendantEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private UpdateOffencesForDefendantEventProcessor updateOffencesForDefendantEventProcessor;

    @Test
    public void processPublicCaseDefendantOffencesChanged() {

        final UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand = updateOffencesForDefendantTemplate(updateOffencesForDefendantArguments(randomUUID(), randomUUID()));

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
                                        .get(0).getProsecutionCaseId().toString()))
                                )
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
}
