package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;

import java.util.Arrays;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateCaseDefendantsHandlerTest {
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CaseDefendantsUpdated.class,
            CaseDefendantsUpdatedForHearing.class,
            ApplicationDefendantsUpdated.class,
            ApplicationDefendantsUpdatedForHearing.class);

    @Mock
    private EventStream eventStream;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private UpdateCaseDefendantsHandler updateCaseDefendantsHandler;


    private HearingAggregate hearingAggregate;

    private CaseAggregate caseAggregate;

    private ApplicationAggregate applicationAggregate;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static InitiateHearingCommand initiateHearingCommand;


    @BeforeEach
    public void setup() {
        caseAggregate = new CaseAggregate();
        applicationAggregate = new ApplicationAggregate();
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        initiateHearingCommand = standardInitiateHearingTemplate();
        hearingAggregate = new HearingAggregate() ;
    }

    @Test
    public void testCaseDefendantsUpdated() throws EventStreamException{
        final UUID caseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        setupMockedEventStream(caseId, this.eventStream, caseAggregate);

        caseAggregate.registerHearingId(caseId, hearingId);

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withCaseStatus("CLOSED")
                .withId(caseId)
                .withDefendants(Arrays.asList(Defendant.defendant()
                        .withId(defendantId)
                        .withProceedingsConcluded(true).build()))
                .build();
        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("prosecutionCase",objectToJsonObjectConverter.convert(prosecutionCase))
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-case-defendants"), commandPayload);
        updateCaseDefendantsHandler.updateCaseDefendants(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.case-defendants-updated"),
                        payloadIsJson(allOf(withJsonPath("$.hearingIds[0]", is(hearingId.toString()))))
                )));

    }

    @Test
    public void testCaseDefendantsForHearingUpdated() throws EventStreamException{
        final UUID caseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        setupMockedEventStream(hearingId, this.hearingEventStream, hearingAggregate);
        hearingAggregate.initiate(initiateHearingCommand.getHearing());

        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("prosecutionCase",Json.createObjectBuilder()
                    .add("caseStatus", "CLOSED")
                    .add("id", caseId.toString())
                    .add("defendants", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                          .add("id",defendantId.toString())
                          .add("proceedingsConcluded", true)
                          .build()).build())
                .build())
                .add("hearingId",hearingId.toString())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-case-defendants-for-hearing"), commandPayload);
        updateCaseDefendantsHandler.updateCaseDefendantsForHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.case-defendants-updated-for-hearing"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString()))))
                )));

    }

    @Test
    public void testApplicationDefendantsUpdated() throws EventStreamException{
        final UUID applicationId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        setupMockedEventStream(applicationId, this.eventStream, applicationAggregate);

        applicationAggregate.registerHearingId(applicationId, hearingId);

        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withId(defendantId)
                        .build())
                .build();
        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("courtApplication",objectToJsonObjectConverter.convert(courtApplication))
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-application-defendants"), commandPayload);
        updateCaseDefendantsHandler.updateApplicationDefendants(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.application-defendants-updated"),
                        payloadIsJson(allOf(withJsonPath("$.hearingIds[0]", is(hearingId.toString()))))
                )));

    }


  

    @Test
    public void testApplicationDefendantsForHearingUpdated() throws EventStreamException{
        final UUID applicationId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        setupMockedEventStream(hearingId, this.hearingEventStream, hearingAggregate);
        hearingAggregate.initiate(initiateHearingCommand.getHearing());

        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("courtApplication", Json.createObjectBuilder()
                        .add("id", applicationId.toString())
                        .add("applicant", Json.createObjectBuilder()
                                .add("id", defendantId.toString())
                                .build()).build())
                .add("hearingId", hearingId.toString())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-application-defendants-for-hearing"), commandPayload);
        updateCaseDefendantsHandler.updateApplicationDefendantsForHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.application-defendants-updated-for-hearing"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString()))))
                )));

    }


    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}
