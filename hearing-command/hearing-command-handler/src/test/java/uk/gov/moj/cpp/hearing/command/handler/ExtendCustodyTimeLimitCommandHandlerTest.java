package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import javax.json.JsonObjectBuilder;
import java.time.LocalDate;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ExtendCustodyTimeLimitCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CustodyTimeLimitExtended.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private ExtendCustodyTimeLimitCommandHandler handler;

    @Test
    public void shouldCreateCustodyTimeLimitExtendedEvent() throws EventStreamException {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate extendedTimeLimit = LocalDate.now();

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("offenceId", offenceId.toString())
                .add("extendedTimeLimit", extendedTimeLimit.toString());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.extend-custody-time-limit"), payloadBuilder.build());

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        handler.extendCustodyTimeLimit(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.custody-time-limit-extended"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.extendedTimeLimit", is(extendedTimeLimit.toString()))
                        ))
                )));
    }


}