package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
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
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.CourtApplication;
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
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDetailChangeCommandHandlerTest {
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            ApplicationDetailChanged.class,
            HearingChangeIgnored.class);

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventStream applicationEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private ApplicationDetailChangeCommandHandler applicationDetailChangeCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateExistingCourtApplicationShouldIgnored_When_Hearing_Not_Found() throws EventStreamException {
        //Given
        final UUID arbitraryHearingId = UUID.randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        final CourtApplication courtApplication = hearingOne.getHearing().getCourtApplications().get(0);
        final ApplicationAggregate applicationAggregate = new ApplicationAggregate() {{
            apply(RegisteredHearingAgainstApplication.builder().withApplicationId(courtApplication.getId()).withHearingId(arbitraryHearingId).build());
        }};
        setupMockedEventStream(arbitraryHearingId, this.hearingEventStream, new HearingAggregate());
        setupMockedEventStream(courtApplication.getId(), this.applicationEventStream, applicationAggregate);
        when(this.eventSource.getStreamById(courtApplication.getId())).thenReturn(this.applicationEventStream);
        when(this.aggregateService.get(this.applicationEventStream, ApplicationAggregate.class)).thenReturn(applicationAggregate);

        JsonObject payload = Json.createObjectBuilder()
                .add("courtApplication", objectToJsonObjectConverter.convert(courtApplication))
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-court-application"), payload);

        applicationDetailChangeCommandHandler.updateExistingCourtApplication(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.hearing-change-ignored"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(arbitraryHearingId.toString()))))
                )));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateExistingCourtApplicationShouldApply_When_Application_Already_Added() throws EventStreamException {
        //Given
        final UUID arbitraryHearingId = UUID.randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper arbitraryHearingObject = h(standardInitiateHearingTemplate());
        final CourtApplication courtApplication = arbitraryHearingObject.getHearing().getCourtApplications().get(0);
        final ApplicationAggregate applicationAggregate = new ApplicationAggregate() {{
            apply(RegisteredHearingAgainstApplication.builder().withApplicationId(courtApplication.getId()).withHearingId(arbitraryHearingId).build());
        }};
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(arbitraryHearingObject.getHearing()));
        }};

        setupMockedEventStream(arbitraryHearingId, this.hearingEventStream, new HearingAggregate());
        setupMockedEventStream(courtApplication.getId(), this.applicationEventStream, applicationAggregate);
        when(this.eventSource.getStreamById(courtApplication.getId())).thenReturn(this.applicationEventStream);
        when(this.aggregateService.get(this.applicationEventStream, ApplicationAggregate.class)).thenReturn(applicationAggregate);
        when(this.eventSource.getStreamById(arbitraryHearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        //Update Application any field
        courtApplication.setBreachedOrder("DUMMY_TEXT");

        JsonObject payload = Json.createObjectBuilder()
                .add("courtApplication", objectToJsonObjectConverter.convert(courtApplication))
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-court-application"), payload);

        applicationDetailChangeCommandHandler.updateExistingCourtApplication(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.application-detail-changed"),
                        payloadIsJson(
                                allOf(
                                        withJsonPath("$.hearingId", is(arbitraryHearingId.toString())),
                                        withJsonPath("$.courtApplication.id", is(courtApplication.getId().toString())),
                                        withJsonPath("$.courtApplication.breachedOrder", is(courtApplication.getBreachedOrder())
                                        )
                                ))
                )));
    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}