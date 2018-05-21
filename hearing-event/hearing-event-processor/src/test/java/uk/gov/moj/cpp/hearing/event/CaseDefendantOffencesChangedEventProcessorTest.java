package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.progression.events.CaseDefendantOffencesChanged;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantOffenceWithHearingIds;
import uk.gov.moj.cpp.hearing.domain.event.DeleteOffenceFromHearings;
import uk.gov.moj.cpp.hearing.domain.event.UpdateOffenceOnHearings;

import java.time.LocalDate;
import java.util.Collections;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantOffencesChangedEventProcessorTest {

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
    private CaseDefendantOffencesChangedEventProcessor caseDefendantOffencesChangedEventProcessor;

    @Test
    public void processPublicCaseDefendantOffencesChanged() {

        final CaseDefendantOffencesChanged caseDefendantOffencesChanged = CaseDefendantOffencesChanged.builder()
                .withModifiedDate(LocalDate.now())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.defendant-offences-changed"),
                objectToJsonObjectConverter.convert(caseDefendantOffencesChanged));

        caseDefendantOffencesChangedEventProcessor.processPublicCaseDefendantOffencesChanged(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.update-case-defendant-offences"),
                        payloadIsJson(allOf(
                                withJsonPath("$.modifiedDate", is(caseDefendantOffencesChanged.getModifiedDate().toString()))
                                )
                        )
                )
        );

    }

    @Test
    public void addCaseDefendantOffence() {

        CaseDefendantOffenceWithHearingIds offence = CaseDefendantOffenceWithHearingIds.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(randomUUID()))
                .withDefendantId(randomUUID())
                .withCaseId(randomUUID())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.add-case-defendant-offence-enriched-with-hearing-ids"),
                objectToJsonObjectConverter.convert(offence));

        caseDefendantOffencesChangedEventProcessor.addCaseDefendantOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.add-case-defendant-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(offence.getId().toString())),
                                withJsonPath("$.hearingIds[0]", is(offence.getHearingIds().get(0).toString())),
                                withJsonPath("$.defendantId", is(offence.getDefendantId().toString())),
                                withJsonPath("$.caseId", is(offence.getCaseId().toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void updateCaseDefendantOffence() {

        UpdateOffenceOnHearings offence = UpdateOffenceOnHearings.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(randomUUID()))
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-offence-enriched-with-hearing-ids"),
                objectToJsonObjectConverter.convert(offence));

        caseDefendantOffencesChangedEventProcessor.updateCaseDefendantOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.update-case-defendant-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(offence.getId().toString())),
                                withJsonPath("$.hearingIds[0]", is(offence.getHearingIds().get(0).toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void deleteCaseDefendantOffence() {

        DeleteOffenceFromHearings offence = DeleteOffenceFromHearings.builder()
                .withId(randomUUID())
                .withHearingIds(Collections.singletonList(randomUUID()))
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.delete-case-defendant-offence-enriched-with-hearing-id"),
                objectToJsonObjectConverter.convert(offence));

        caseDefendantOffencesChangedEventProcessor.deleteCaseDefendantOffence(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.delete-case-defendant-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id", is(offence.getId().toString())),
                                withJsonPath("$.hearingIds[0]", is(offence.getHearingIds().get(0).toString()))
                                )
                        )
                )
        );
    }
}