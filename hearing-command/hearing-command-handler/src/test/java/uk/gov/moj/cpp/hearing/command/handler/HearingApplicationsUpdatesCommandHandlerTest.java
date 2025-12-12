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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingApplicationsTobeAddedCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingBreachApplicationsToBeAddedReceived;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingApplicationsUpdatesCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingBreachApplicationsToBeAddedReceived.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private HearingApplicationsUpdatesCommandHandler handler;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();
    @InjectMocks
    private HearingEventCommandHandler hearingEventCommandHandler;

    @Test
    public void shouldCreateHearingBreachApplicationsToBeAddedReceivedEvent() throws EventStreamException {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate extendedTimeLimit = LocalDate.now();

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};



       final UUID applicationId = randomUUID();

       final HearingApplicationsTobeAddedCommand hearingApplicationsTobeAddedCommand = new HearingApplicationsTobeAddedCommand(Arrays.asList(applicationId), hearingId);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.breach-applications-to-be-added"), objectToJsonObjectConverter.convert(hearingApplicationsTobeAddedCommand));

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        handler.recordApplicationsToBeAddedToHearing(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.events.hearing-breach-Applications-to-be-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.courtApplications[0]", is(applicationId.toString()))
                        ))
                )));
    }
}
