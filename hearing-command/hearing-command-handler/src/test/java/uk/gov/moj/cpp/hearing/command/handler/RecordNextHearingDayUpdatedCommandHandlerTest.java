package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.NextHearingStartDateRecorded;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import javax.json.Json;
import javax.json.JsonObject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class RecordNextHearingDayUpdatedCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(NextHearingStartDateRecorded.class, EarliestNextHearingDateChanged.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @InjectMocks
    private RecordNextHearingDayUpdatedCommandHandler handler;

    @Test
    public void shouldCreateNextHearingDayChangedEvent() throws EventStreamException {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        final UUID hearingId = hearing.getHearingId();
        final UUID seedingHearingId = randomUUID();
        final ZonedDateTime earliestNextHearingDate = ZonedDateTime.of(2022, 01, 01, 0, 0, 0, 0, ZoneId.of("UTC"));

        final JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("seedingHearingId", seedingHearingId.toString())
                .add("hearingStartDate", "2021-06-20T00:00:00.000Z")
                .build();

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.record-next-hearing-day-updated"), payload);

        when(this.eventSource.getStreamById(seedingHearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        handler.recordNextHearingDayUpdated(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(hearingEventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.next-hearing-start-date-recorded"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.seedingHearingId", is(seedingHearingId.toString())),
                                withJsonPath("$.nextHearingStartDate", is("2021-06-20T00:00:00.000Z"))
                        ))
                ),
                jsonEnvelope(
                        metadata()
                                .withName("hearing.events.earliest-next-hearing-date-changed"),
                        JsonEnvelopePayloadMatcher.payload().isJson(Matchers.allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.seedingHearingId", is(seedingHearingId.toString())),
                                withJsonPath("$.earliestNextHearingDate", is("2021-06-20T00:00:00.000Z"))
                        ))
                )));
    }


}