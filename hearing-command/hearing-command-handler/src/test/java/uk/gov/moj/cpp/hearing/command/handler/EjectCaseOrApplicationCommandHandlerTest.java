package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseEjected;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationEjected;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EjectCaseOrApplicationCommandHandlerTest {
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CaseEjected.class,
            CourtApplicationEjected.class);
    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventStream applicationEventStream;
    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private EjectCaseOrApplicationCommandHandler handler;

    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> streamArgumentCaptor;

    @Test
    public void shouldEjectCase() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        JsonObject payload = Json.createObjectBuilder()
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()))
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .build();
        final JsonEnvelope envelope =
                envelopeFrom(metadataWithRandomUUID("hearing.command.eject-case-or-application"), payload);
        when(this.eventSource.getStreamById(prosecutionCaseId)).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(new CaseAggregate());
        handler.ejectCaseOrApplication(envelope);
        assertThat(verifyAppendAndGetArgumentFrom(this.caseEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.case-ejected"),
                        payloadIsJson(allOf(
                                withJsonPath("$.prosecutionCaseId", is(prosecutionCaseId.toString())),
                                withJsonPath("$.hearingIds[0]", is(hearingId.toString()))
                        ))
                )));
    }

    @Test
    public void shouldEjectCaseWhenHearingIdsNotPresent() throws EventStreamException {
        final UUID prosecutionCaseId = randomUUID();
        JsonObject payload = Json.createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .build();
        final JsonEnvelope envelope =
                envelopeFrom(metadataWithRandomUUID("hearing.command.eject-case-or-application"), payload);
        when(this.eventSource.getStreamById(prosecutionCaseId)).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(new CaseAggregate());
        handler.ejectCaseOrApplication(envelope);
        verify(caseEventStream, times(1)).append(streamArgumentCaptor.capture());
        final List<JsonEnvelope> jsonEnvelopeList = convertStreamToEventList(streamArgumentCaptor.getAllValues());
        assertThat("Empty Stream returned",jsonEnvelopeList.size(), is(0));

    }

    @Test
    public void shouldEjectApplicationWhenHearingIdsNotPresent() throws EventStreamException {
        final UUID applicationId = randomUUID();
        JsonObject payload = Json.createObjectBuilder()
                .add("applicationId", applicationId.toString())
                .build();
        final JsonEnvelope envelope =
                envelopeFrom(metadataWithRandomUUID("hearing.command.eject-case-or-application"), payload);
        when(this.eventSource.getStreamById(applicationId)).thenReturn(this.applicationEventStream);
        when(this.aggregateService.get(this.applicationEventStream, ApplicationAggregate.class)).thenReturn(new ApplicationAggregate());
        handler.ejectCaseOrApplication(envelope);
        verify(applicationEventStream, times(1)).append(streamArgumentCaptor.capture());
        final List<JsonEnvelope> jsonEnvelopeList = convertStreamToEventList(streamArgumentCaptor.getAllValues());
        assertThat("Empty Stream returned",jsonEnvelopeList.size(), is(0));

    }

    @Test
    public void shouldRaiseCourtApplicationEjectedWhenProsecutionCaseIdIsNullButHearingIdsIsNotNull() throws EventStreamException {
        final UUID applicationId = randomUUID();
        JsonObject payload = Json.createObjectBuilder()
                .add("applicationId", applicationId.toString())
                .build();
        final JsonEnvelope envelope =
                envelopeFrom(metadataWithRandomUUID("hearing.command.eject-case-or-application"), payload);
        when(this.eventSource.getStreamById(applicationId)).thenReturn(this.applicationEventStream);
        final ApplicationAggregate applicationAggregate = new ApplicationAggregate();
        ReflectionUtil.setField(applicationAggregate, "hearingIds", Arrays.asList(randomUUID(), randomUUID()));
        when(this.aggregateService.get(this.applicationEventStream, ApplicationAggregate.class)).thenReturn(applicationAggregate);
        handler.ejectCaseOrApplication(envelope);
        verify(applicationEventStream, times(1)).append(streamArgumentCaptor.capture());
        final List<JsonEnvelope> jsonEnvelopeList = convertStreamToEventList(streamArgumentCaptor.getAllValues());
        assertThat(jsonEnvelopeList.size(), is(1));
        //assertThat(jsonEnvelopeList.get(0).payloadAsJsonObject().getString("applicationId"), is(applicationId));

    }

    @Test
    public void shouldEjectCourtApplication() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();
        JsonObject payload = Json.createObjectBuilder()
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()))
                .add("applicationId", applicationId.toString())
                .build();
        final JsonEnvelope envelope =
                envelopeFrom(metadataWithRandomUUID("hearing.command.eject-case-or-application"), payload);
        when(this.eventSource.getStreamById(applicationId)).thenReturn(this.applicationEventStream);
        when(this.aggregateService.get(this.applicationEventStream, ApplicationAggregate.class)).thenReturn(new ApplicationAggregate());
        handler.ejectCaseOrApplication(envelope);
        assertThat(verifyAppendAndGetArgumentFrom(this.applicationEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.court-application-ejected"),
                        payloadIsJson(allOf(
                                withJsonPath("$.applicationId", is(applicationId.toString())),
                                withJsonPath("$.hearingIds[0]", is(hearingId.toString()))
                        ))
                )));
    }

    private List<JsonEnvelope> convertStreamToEventList(final List<Stream<JsonEnvelope>> listOfStreams) {
        return listOfStreams.stream()
                .flatMap(jsonEnvelopeStream -> jsonEnvelopeStream).collect(toList());
    }
}
