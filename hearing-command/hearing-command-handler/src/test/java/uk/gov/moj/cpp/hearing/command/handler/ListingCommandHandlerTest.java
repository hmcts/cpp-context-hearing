package uk.gov.moj.cpp.hearing.command.handler;

import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingsPleaAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingConfirmedRecorded;

import java.io.IOException;
import java.util.UUID;

import javax.json.JsonObject;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListingCommandHandlerTest {

    private static final String RECORD_HEARING_CONFIRMED = "hearing.confirmed-recorded";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_DURATION = "estimateMinutes";
    private static final String FIELD_HEARING_TYPE = "type";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_ROOM_ID = "courtRoomId";
    private static final String FIELD_ROOM_NAME = "courtRoomName";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String CASE_ID = UUID.randomUUID().toString();
    private static final String HEARING_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String COURT_CENTRE_ID = UUID.randomUUID().toString();
    private static final String ROOM_ID = UUID.randomUUID().toString();
    private static final String COURT_CENTRE_NAME = "Liverppol Crown Court";
    private static final int DURATION = 15;
    private static final String HEARING_TYPE = "PTP";
    private static final String ROOM_NAME = "3";

    @InjectMocks
    private ListingCommandHandler listingCommandHandler;

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;


    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectConverter, "mapper",
                new ObjectMapperProducer().objectMapper());

        when(this.eventSource.getStreamById(Mockito.any(UUID.class))).thenReturn(this.eventStream);
        when(this.eventSource.getStreamById(Mockito.any(UUID.class))).thenReturn(this.eventStream);
        when(this.aggregateService.get(this.eventStream, HearingsPleaAggregate.class))
                .thenReturn(new HearingsPleaAggregate());
    }

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(HearingConfirmedRecorded.class);


    @Test
    public void shouldRaiseRecordHearingConfirmed() throws Exception {
        // Given

        final JsonObject publicHearingAddedPayload = getSendCaseForListingPayload(
                "record-hearing-confirmed.json");
        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID(RECORD_HEARING_CONFIRMED),
                publicHearingAddedPayload);
        // When
        this.listingCommandHandler.recordHearingConfirmed(command);
        // Then
        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(jsonEnvelope(
                withMetadataEnvelopedFrom(command).withName(RECORD_HEARING_CONFIRMED),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_CASE_ID),
                                equalTo(CASE_ID)),
                        withJsonPath(format("$.%s.%s", "hearing", FIELD_GENERIC_ID),
                                equalTo(HEARING_ID)),
                        withJsonPath(format("$.%s.%s", "hearing", FIELD_HEARING_TYPE),
                                equalTo(HEARING_TYPE)),
                        withJsonPath(format("$.%s.%s", "hearing", FIELD_COURT_CENTRE_ID),
                                equalTo(COURT_CENTRE_ID)),
                        withJsonPath(format("$.%s.%s", "hearing", FIELD_COURT_CENTRE_NAME),
                                equalTo(COURT_CENTRE_NAME)),
                        withJsonPath(format("$.%s.%s", "hearing", FIELD_DURATION),
                                equalTo(DURATION))
                )))
                .thatMatchesSchema()));

    }


    private JsonObject getSendCaseForListingPayload(final String resource) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString =
                sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", CASE_ID).
                        replace("RANDOM_HEARING_ID", HEARING_ID).
                        replace("RANDOM_HEARING_TYPE", HEARING_TYPE).
                        replace("RANDOM_COURT_CENTRE_ID", COURT_CENTRE_ID).
                        replace("RANDOM_COURT_CENTRE_NAME", COURT_CENTRE_NAME).replace("RANDOM_COURT_ROOM_ID", ROOM_ID).
                        replace("RANDOM_COURT_ROOM_NAME", ROOM_NAME);

        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }
}
