package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
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
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @InjectMocks
    private ApplicationDetailChangeCommandHandler applicationDetailChangeCommandHandler;

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
        when(this.eventSource.getStreamById(arbitraryHearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(new HearingAggregate());
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

        when(this.eventSource.getStreamById(arbitraryHearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(this.eventSource.getStreamById(courtApplication.getId())).thenReturn(this.applicationEventStream);
        when(this.aggregateService.get(this.applicationEventStream, ApplicationAggregate.class)).thenReturn(applicationAggregate);


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
                                        withJsonPath("$.courtApplication.id", is(courtApplication.getId().toString()))
                                ))
                )));
    }
}