package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
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
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

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
public class HearingDetailChangeCommandHandlerTest {

    public static final String ARBITRARY_TRIAL = RandomGenerator.STRING.next();
    public static final String ARBITRARY_COURT_NAME = RandomGenerator.STRING.next();
    public static final String ARBITRARY_HEARING_START_DATE = "2016-06-01T10:00:00Z";
    private static final String PRIVATE_HEARING_LISTENER_HEARING_DETAIL_CHANGED = "hearing.event.detail-changed";
    private static final String PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE = "hearing.change-hearing-detail";
    private static final String PRIVATE_HEARING_EVENT_IGNORED = "hearing.hearing-event-ignored";
    private static final String ARBITRARY_HEARING_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_COURT_ROOM_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_TITLE = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_FIRST_NAME = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_LAST_NAME = RandomGenerator.STRING.next();

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingDetailChanged.class,
            HearingEventIgnored.class
    );

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private HearingDetailChangeCommandHandler testObj;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventHearingDetailChangedShouldCreated() throws Exception {
        //Given
        setupMockedEventStream(UUID.fromString(ARBITRARY_HEARING_ID), this.hearingEventStream, with(new NewModelHearingAggregate(), a -> {
            a.apply(new HearingInitiated(null,Hearing.builder().withId(UUID.fromString(ARBITRARY_HEARING_ID)).build()));
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID(PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE), commandHearingChangedEvent());

        //when
        testObj.changeHearingDetail(command);

        //then
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(PRIVATE_HEARING_LISTENER_HEARING_DETAIL_CHANGED),

                        payloadIsJson(allOf(
                                withJsonPath("$.id", equalTo(ARBITRARY_HEARING_ID)),
                                withJsonPath("$.type", equalTo(ARBITRARY_TRIAL)),
                                withJsonPath("$.courtRoomName", equalTo(ARBITRARY_COURT_NAME)),
                                withJsonPath("$.courtRoomId", equalTo(ARBITRARY_HEARING_COURT_ROOM_ID)),
                                withJsonPath("$.hearingDays[0]", equalTo(ZonedDateTimes.toString(ZonedDateTimes.fromString(ARBITRARY_HEARING_START_DATE)))),
                                withJsonPath("$.judge.id", equalTo(ARBITRARY_HEARING_JUDGE_ID)),
                                withJsonPath("$.judge.firstName", equalTo(ARBITRARY_HEARING_JUDGE_FIRST_NAME)),
                                withJsonPath("$.judge.lastName", equalTo(ARBITRARY_HEARING_JUDGE_LAST_NAME)),
                                withJsonPath("$.judge.title", equalTo(ARBITRARY_HEARING_JUDGE_TITLE))
                        ))).thatMatchesSchema()
        ));


    }

    @Test
    public void eventHearingDetailChangedShouldIgnored() throws Exception {
        setupMockedEventStream(UUID.fromString(ARBITRARY_HEARING_ID), this.hearingEventStream, new NewModelHearingAggregate());
        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID(PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE), commandHearingChangedEvent());

        testObj.changeHearingDetail(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(PRIVATE_HEARING_EVENT_IGNORED),

                        payloadIsJson(allOf(

                        )))
        ));


    }

    private JsonObject commandHearingChangedEvent() {
        return Json.createObjectBuilder()
                .add("hearing", Json.createObjectBuilder()
                        .add("id", ARBITRARY_HEARING_ID)
                        .add("type", ARBITRARY_TRIAL)
                        .add("judge", getJudge())
                        .add("courtRoomId", ARBITRARY_HEARING_COURT_ROOM_ID)
                        .add("courtRoomName", ARBITRARY_COURT_NAME)
                        .add("hearingDays", Json.createArrayBuilder().add(ARBITRARY_HEARING_START_DATE).build())
                        .build())
                .build();
    }

    private JsonObject getJudge() {
        final JsonObject judgeJsonObject = Json.createObjectBuilder()
                .add("id", ARBITRARY_HEARING_JUDGE_ID)
                .add("firstName", ARBITRARY_HEARING_JUDGE_FIRST_NAME)
                .add("lastName", ARBITRARY_HEARING_JUDGE_LAST_NAME)
                .add("title", ARBITRARY_HEARING_JUDGE_TITLE)
                .build();
        return judgeJsonObject;
    }

    private <T extends Aggregate> void setupMockedEventStream(final UUID id, final EventStream eventStream, final T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        final Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}