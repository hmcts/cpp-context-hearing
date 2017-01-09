package uk.gov.moj.cpp.hearing.query.view;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonassert.impl.matcher.IsEmptyCollection.empty;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.query.view.ListToJsonArrayConverterFactory.createListToJsonArrayConverter;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Todo test cases in the class needs to be re-factored to use less mocks and instead use real objects
 * E.g. {@link HearingQueryViewTest#shouldGetHearingEventLogByHearingId()}
 */
@RunWith(MockitoJUnitRunner.class)
public class HearingQueryViewTest {

    private static final String NAME_RESPONSE_HEARING_LIST = "hearing.get.hearings-by-startdate-response";
    private static final String NAME_RESPONSE_HEARING = "hearing.get.hearing-response";
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_HEARING_EVENT_ID = "id";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_HEARING_EVENTS = "events";

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID HEARING_ID_2 = randomUUID();
    private static final LocalDate START_DATE = LocalDate.now();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime TIMESTAMP = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_ID_2 = randomUUID();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final ZonedDateTime TIMESTAMP_2 = TIMESTAMP.plusMinutes(1);

    private static final UUID HEARING_EVENT_ID_3 = randomUUID();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final ZonedDateTime TIMESTAMP_3 = TIMESTAMP.plusMinutes(2);

    @Mock
    private JsonEnvelope query;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Spy
    private ListToJsonArrayConverter listToJsonArrayConverter = createListToJsonArrayConverter();

    @Mock
    private List<HearingView> hearings;

    @Mock
    private HearingView hearingView;

    @Mock
    private HearingService hearingService;

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonEnvelope responseJson;

    @Mock
    private HearingEventRepository hearingEventRepository;

    @InjectMocks
    private HearingQueryView hearingsQueryView;

    @Test
    public void shouldGetHearingsByStartDate() {
        when(hearingService.getHearingsByStartDate(START_DATE)).thenReturn(getHearings());

        final JsonEnvelope query = envelopeFrom(
                metadataWithDefaults(),
                createObjectBuilder().add(FIELD_START_DATE, LocalDates.to(START_DATE)).build());

        final JsonEnvelope actualHearings = hearingsQueryView.findHearings(query);

        assertThat(actualHearings, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName(NAME_RESPONSE_HEARING_LIST),
                payloadIsJson(allOf(
                        withJsonPath("$.hearings", hasSize(2)),
                        withJsonPath("$.hearings[0].hearingId", equalTo(HEARING_ID.toString())),
                        withJsonPath("$.hearings[1].hearingId", equalTo(HEARING_ID_2.toString()))
                ))
        )));
    }

    @Test
    public void shouldFindHearing() {
        UUID hearingId = randomUUID();
        String now = LocalDate.now().toString();
        JsonObject jsonObject = createObjectBuilder()
                .add(FIELD_HEARING_ID,
                        hearingId.toString())
                .add(FIELD_START_DATE, now).build();
        when(query.payloadAsJsonObject()).thenReturn(jsonObject);

        when(hearingService.getHearingById(hearingId)).thenReturn(hearingView);

        when(enveloper.withMetadataFrom(query, NAME_RESPONSE_HEARING))
                .thenReturn(function);

        when(function.apply(hearingView)).thenReturn(responseJson);

        assertThat(hearingsQueryView.findHearing(query), equalTo(responseJson));
    }

    @Test
    public void shouldGetHearingEventLogByHearingId() {
        when(hearingEventRepository.findByHearingId(HEARING_ID)).thenReturn(hearingEvents());

        final JsonEnvelope query = envelopeFrom(
                metadataWithDefaults(),
                createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString()).build());

        final JsonEnvelope actualHearingEventLog = hearingsQueryView.getHearingEventLog(query);

        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_TIMESTAMP), equalTo(ZonedDateTimes.toString(TIMESTAMP))),

                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_TIMESTAMP), equalTo(ZonedDateTimes.toString(TIMESTAMP_2)))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingEventLogByHearingIdInOrder() {
        when(hearingEventRepository.findByHearingId(HEARING_ID)).thenReturn(unorderedHearingEvents());

        final JsonEnvelope query = envelopeFrom(
                metadataWithDefaults(),
                createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString()).build());

        final JsonEnvelope actualHearingEventLog = hearingsQueryView.getHearingEventLog(query);

        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), hasSize(3)),

                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_TIMESTAMP), equalTo(ZonedDateTimes.toString(TIMESTAMP))),

                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_TIMESTAMP), equalTo(ZonedDateTimes.toString(TIMESTAMP_2))),

                        withJsonPath(format("$.%s[2].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_3.toString())),
                        withJsonPath(format("$.%s[2].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_3)),
                        withJsonPath(format("$.%s[2].%s", FIELD_HEARING_EVENTS, FIELD_TIMESTAMP), equalTo(ZonedDateTimes.toString(TIMESTAMP_3)))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldReturnEmptyEventLogWhenThereAreNoEventsByHearingId() {
        when(hearingEventRepository.findByHearingId(HEARING_ID)).thenReturn(emptyList());

        final JsonEnvelope query = envelopeFrom(
                metadataWithDefaults(),
                createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString()).build());

        final JsonEnvelope actualHearingEventLog = hearingsQueryView.getHearingEventLog(query);

        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), empty())
                ))).thatMatchesSchema()
        ));
    }

    private List<HearingView> getHearings() {
        final List<HearingView> hearings = new ArrayList<>();
        final HearingView hearing1 = new HearingView();
        hearing1.setHearingId(HEARING_ID.toString());
        hearing1.setDuration(INTEGER.next());
        hearing1.setStartDate(PAST_LOCAL_DATE.next());
        hearing1.setStartTime(LocalTime.now());
        hearing1.setCourtCentreName(STRING.next());
        hearing1.setRoomName(STRING.next());
        hearing1.setStartedAt(PAST_ZONED_DATE_TIME.next());
        hearing1.setEndedAt(null);
        hearing1.setHearingType(STRING.next());
        hearing1.setCaseIds(newArrayList(randomUUID().toString()));

        final HearingView hearing2 = new HearingView();
        hearing2.setHearingId(HEARING_ID_2.toString());
        hearing2.setDuration(INTEGER.next());
        hearing2.setStartDate(PAST_LOCAL_DATE.next());
        hearing2.setStartTime(LocalTime.now());
        hearing2.setCourtCentreName(STRING.next());
        hearing2.setRoomName(STRING.next());
        hearing2.setStartedAt(PAST_ZONED_DATE_TIME.next());
        hearing2.setEndedAt(null);
        hearing2.setHearingType(STRING.next());
        hearing2.setCaseIds(newArrayList(randomUUID().toString(), randomUUID().toString()));

        hearings.add(hearing1);
        hearings.add(hearing2);

        return hearings;
    }

    private List<HearingEvent> hearingEvents() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();

        hearingEvents.add(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP));
        hearingEvents.add(new HearingEvent(HEARING_EVENT_ID_2, HEARING_ID, RECORDED_LABEL_2, TIMESTAMP_2));
        return hearingEvents;
    }

    private List<HearingEvent> unorderedHearingEvents() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();

        hearingEvents.add(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP));
        hearingEvents.add(new HearingEvent(HEARING_EVENT_ID_3, HEARING_ID, RECORDED_LABEL_3, TIMESTAMP_3));
        hearingEvents.add(new HearingEvent(HEARING_EVENT_ID_2, HEARING_ID, RECORDED_LABEL_2, TIMESTAMP_2));
        return hearingEvents;
    }

}
