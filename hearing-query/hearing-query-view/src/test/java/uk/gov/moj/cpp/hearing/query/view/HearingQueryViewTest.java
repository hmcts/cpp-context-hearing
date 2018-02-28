package uk.gov.moj.cpp.hearing.query.view;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.format.DateTimeFormatter.ISO_TIME;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import java.time.LocalDate;
import java.time.LocalTime;
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

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryViewTest {

    private static final String RESPONSE_NAME_HEARINGS = "hearing.get.hearings";
    private static final String RESPONSE_NAME_HEARING = "hearing.get-hearing";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_NUMBEROFJURORS = "numberOfJurors";

    private static final UUID HEARING_ID = randomUUID();
    private static final LocalDate START_DATE = PAST_LOCAL_DATE.next();
    private static final LocalTime START_TIME = LocalTime.now();
    private static final Integer DURATION = INTEGER.next();
    private static final UUID ROOM_ID = randomUUID();
    private static final String ROOM_NAME = STRING.next();
    private static final String HEARING_TYPE = STRING.next();
    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final String COURT_CENTRE_NAME = STRING.next();
    private static final String DEFAULT_COURT_CENTRE_NAME = "Liverpool";

    private static final UUID HEARING_ID_2 = randomUUID();
    private static final LocalTime START_TIME_2 = START_TIME.plusHours(2);
    private static final Integer DURATION_2 = INTEGER.next();
    private static final UUID ROOM_ID_2 = randomUUID();
    private static final String ROOM_NAME_2 = STRING.next();
    private static final String DEFAULT_ROOM_NAME = "Room 1";

    private static final UUID CASE_ID = randomUUID();
    private static final UUID CASE_ID_2 = randomUUID();
    private static final UUID CASE_ID_3 = randomUUID();
    private static final UUID CASE_ID_4 = randomUUID();
    private static final UUID CASE_ID_5 = randomUUID();

    private static final UUID HEARING_CASE_ID = randomUUID();
    private static final UUID HEARING_CASE_ID_2 = randomUUID();
    private static final UUID HEARING_CASE_ID_3 = randomUUID();
    private static final UUID HEARING_CASE_ID_4 = randomUUID();
    private static final UUID HEARING_CASE_ID_5 = randomUUID();

    @Mock
    private JsonEnvelope query;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private HearingView hearingView;

    @Mock
    private HearingService hearingService;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingCaseRepository hearingCaseRepository;

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonEnvelope responseJson;

    @InjectMocks
    private HearingQueryView hearingsQueryView;

    @Test
    public void shouldGetHearingsByStartDate() {
        when(hearingRepository.findByStartDate(START_DATE)).thenReturn(hearings());
        when(hearingCaseRepository.findByHearingIds(newArrayList(HEARING_ID, HEARING_ID_2))).thenReturn(hearingCases());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_START_DATE, LocalDates.to(START_DATE)).build());

        final JsonEnvelope actualHearings = hearingsQueryView.findHearingsByStartDate(query);

        assertThat(actualHearings, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName(RESPONSE_NAME_HEARINGS),
                payloadIsJson(allOf(
                        withJsonPath("$.hearings", hasSize(2)),

                        withJsonPath("$.hearings[0].hearingId", equalTo(HEARING_ID.toString())),
                        withJsonPath("$.hearings[0].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[0].startTime", equalTo(START_TIME.format(ISO_TIME))),
                        withJsonPath("$.hearings[0].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[0].duration", equalTo(DURATION)),
                        withJsonPath("$.hearings[0].courtCentreName", equalTo(COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[0].roomName", equalTo(ROOM_NAME)),
                        withJsonPath("$.hearings[0].caseIds", hasSize(2)),
                        withJsonPath("$.hearings[0].caseIds[*]", containsInAnyOrder(CASE_ID.toString(), CASE_ID_2.toString())),
                        withJsonPath("$.hearings[0].courtCentreId", equalTo(COURT_CENTRE_ID.toString())),
                        withJsonPath("$.hearings[0].roomId", equalTo(ROOM_ID.toString())),

                        withJsonPath("$.hearings[1].hearingId", equalTo(HEARING_ID_2.toString())),
                        withJsonPath("$.hearings[1].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[1].startTime", equalTo(START_TIME_2.format(ISO_TIME))),
                        withJsonPath("$.hearings[1].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[1].duration", equalTo(DURATION_2)),
                        withJsonPath("$.hearings[1].courtCentreName", equalTo(COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[1].roomName", equalTo(ROOM_NAME_2)),
                        withJsonPath("$.hearings[1].caseIds", hasSize(3)),
                        withJsonPath("$.hearings[1].caseIds[*]", containsInAnyOrder(CASE_ID_3.toString(), CASE_ID_4.toString(), CASE_ID_5.toString())),
                        withJsonPath("$.hearings[1].courtCentreId", equalTo(COURT_CENTRE_ID.toString())),
                        withJsonPath("$.hearings[1].roomId", equalTo(ROOM_ID_2.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingsByStartDateWithDefaultCourtCenterNameAndRoomNameWhenTheyAreMissing() {
        when(hearingRepository.findByStartDate(START_DATE)).thenReturn(hearingsWithMissingCourtCenterNameAndRoomName());
        when(hearingCaseRepository.findByHearingIds(newArrayList(HEARING_ID, HEARING_ID_2))).thenReturn(hearingCases());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_START_DATE, LocalDates.to(START_DATE)).build());

        final JsonEnvelope actualHearings = hearingsQueryView.findHearingsByStartDate(query);

        assertThat(actualHearings, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName(RESPONSE_NAME_HEARINGS),
                payloadIsJson(allOf(
                        withJsonPath("$.hearings", hasSize(2)),

                        withJsonPath("$.hearings[0].hearingId", equalTo(HEARING_ID.toString())),
                        withJsonPath("$.hearings[0].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[0].startTime", equalTo(START_TIME.format(ISO_TIME))),
                        withJsonPath("$.hearings[0].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[0].duration", equalTo(DURATION)),
                        withJsonPath("$.hearings[0].courtCentreName", equalTo(DEFAULT_COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[0].roomName", equalTo(DEFAULT_ROOM_NAME)),
                        withJsonPath("$.hearings[0].caseIds", hasSize(2)),
                        withJsonPath("$.hearings[0].caseIds[*]", containsInAnyOrder(CASE_ID.toString(), CASE_ID_2.toString())),

                        withJsonPath("$.hearings[1].hearingId", equalTo(HEARING_ID_2.toString())),
                        withJsonPath("$.hearings[1].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[1].startTime", equalTo(START_TIME_2.format(ISO_TIME))),
                        withJsonPath("$.hearings[1].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[1].duration", equalTo(DURATION_2)),
                        withJsonPath("$.hearings[1].courtCentreName", equalTo(DEFAULT_COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[1].roomName", equalTo(DEFAULT_ROOM_NAME)),
                        withJsonPath("$.hearings[1].caseIds", hasSize(3)),
                        withJsonPath("$.hearings[1].caseIds[*]", containsInAnyOrder(CASE_ID_3.toString(), CASE_ID_4.toString(), CASE_ID_5.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingsByCaseId() {
        when(hearingCaseRepository.findByCaseId(CASE_ID)).thenReturn(caseHearings());
        when(hearingRepository.findByHearingIds(newArrayList(HEARING_ID, HEARING_ID_2))).thenReturn(hearings());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString()).build());

        final JsonEnvelope actualHearings = hearingsQueryView.findHearingsByCaseId(query);

        assertThat(actualHearings, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName(RESPONSE_NAME_HEARINGS),
                payloadIsJson(allOf(
                        withJsonPath("$.hearings", hasSize(2)),

                        withJsonPath("$.hearings[0].hearingId", equalTo(HEARING_ID.toString())),
                        withJsonPath("$.hearings[0].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[0].startTime", equalTo(START_TIME.format(ISO_TIME))),
                        withJsonPath("$.hearings[0].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[0].duration", equalTo(DURATION)),
                        withJsonPath("$.hearings[0].courtCentreName", equalTo(COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[0].roomName", equalTo(ROOM_NAME)),
                        withJsonPath("$.hearings[0].caseIds", hasSize(1)),
                        withJsonPath("$.hearings[0].caseIds[*]", containsInAnyOrder(CASE_ID.toString())),
                        withJsonPath("$.hearings[0].courtCentreId", equalTo(COURT_CENTRE_ID.toString())),
                        withJsonPath("$.hearings[0].roomId", equalTo(ROOM_ID.toString())),

                        withJsonPath("$.hearings[1].hearingId", equalTo(HEARING_ID_2.toString())),
                        withJsonPath("$.hearings[1].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[1].startTime", equalTo(START_TIME_2.format(ISO_TIME))),
                        withJsonPath("$.hearings[1].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[1].duration", equalTo(DURATION_2)),
                        withJsonPath("$.hearings[1].courtCentreName", equalTo(COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[1].roomName", equalTo(ROOM_NAME_2)),
                        withJsonPath("$.hearings[1].caseIds", hasSize(1)),
                        withJsonPath("$.hearings[1].caseIds[*]", containsInAnyOrder(CASE_ID.toString())),
                        withJsonPath("$.hearings[1].courtCentreId", equalTo(COURT_CENTRE_ID.toString())),
                        withJsonPath("$.hearings[1].roomId", equalTo(ROOM_ID_2.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingsByCaseIdWithDefaultCourtCenterNameAndRoomNameWhenTheyAreMissing() {
        when(hearingCaseRepository.findByCaseId(CASE_ID)).thenReturn(caseHearings());
        when(hearingRepository.findByHearingIds(newArrayList(HEARING_ID, HEARING_ID_2))).thenReturn(hearingsWithMissingCourtCenterNameAndRoomName());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString()).build());

        final JsonEnvelope actualHearings = hearingsQueryView.findHearingsByCaseId(query);

        assertThat(actualHearings, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName(RESPONSE_NAME_HEARINGS),
                payloadIsJson(allOf(
                        withJsonPath("$.hearings", hasSize(2)),

                        withJsonPath("$.hearings[0].hearingId", equalTo(HEARING_ID.toString())),
                        withJsonPath("$.hearings[0].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[0].startTime", equalTo(START_TIME.format(ISO_TIME))),
                        withJsonPath("$.hearings[0].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[0].duration", equalTo(DURATION)),
                        withJsonPath("$.hearings[0].courtCentreName", equalTo(DEFAULT_COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[0].roomName", equalTo(DEFAULT_ROOM_NAME)),
                        withJsonPath("$.hearings[0].caseIds", hasSize(1)),
                        withJsonPath("$.hearings[0].caseIds[*]", containsInAnyOrder(CASE_ID.toString())),

                        withJsonPath("$.hearings[1].hearingId", equalTo(HEARING_ID_2.toString())),
                        withJsonPath("$.hearings[1].startDate", equalTo(LocalDates.to(START_DATE))),
                        withJsonPath("$.hearings[1].startTime", equalTo(START_TIME_2.format(ISO_TIME))),
                        withJsonPath("$.hearings[1].hearingType", equalTo(HEARING_TYPE)),
                        withJsonPath("$.hearings[1].duration", equalTo(DURATION_2)),
                        withJsonPath("$.hearings[1].courtCentreName", equalTo(DEFAULT_COURT_CENTRE_NAME)),
                        withJsonPath("$.hearings[1].roomName", equalTo(DEFAULT_ROOM_NAME)),
                        withJsonPath("$.hearings[1].caseIds", hasSize(1)),
                        withJsonPath("$.hearings[1].caseIds[*]", containsInAnyOrder(CASE_ID.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldFindHearing() {
        final UUID hearingId = randomUUID();
        final String now = LocalDate.now().toString();
        final JsonObject jsonObject = createObjectBuilder()
                .add(FIELD_HEARING_ID,
                        hearingId.toString())
                .add(FIELD_START_DATE, now)
                .build();

        when(query.payloadAsJsonObject()).thenReturn(jsonObject);

        when(hearingService.getHearingById(hearingId)).thenReturn(hearingView);

        when(enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING))
                .thenReturn(function);

        when(function.apply(hearingView)).thenReturn(responseJson);

        assertThat(hearingsQueryView.findHearing(query), equalTo(responseJson));
    }

    private List<Hearing> hearings() {
        return newArrayList(
                new Hearing.Builder().withHearingId(HEARING_ID).withStartDate(START_DATE)
                        .withStartTime(START_TIME)
                        .withDuration(DURATION)
                        .withRoomId(ROOM_ID)
                        .withRoomName(ROOM_NAME)
                        .withHearingType(HEARING_TYPE)
                        .withCourtCentreId(COURT_CENTRE_ID)
                        .withCourtCentreName(COURT_CENTRE_NAME)
                        .build(),
                new Hearing.Builder().withHearingId(HEARING_ID_2).withStartDate(START_DATE)
                        .withStartTime(START_TIME_2).withDuration(DURATION_2)
                        .withRoomId(ROOM_ID_2).withRoomName(ROOM_NAME_2)
                        .withHearingType(HEARING_TYPE)
                        .withCourtCentreId(COURT_CENTRE_ID)
                        .withCourtCentreName(COURT_CENTRE_NAME)
                        .build()
        );
    }

    private List<Hearing> hearingsWithMissingCourtCenterNameAndRoomName() {
        return newArrayList(
                new Hearing.Builder().withHearingId(HEARING_ID).withStartDate(START_DATE)
                        .withStartTime(START_TIME).withDuration(DURATION)
                        .withHearingType(HEARING_TYPE).build(),
                new Hearing.Builder().withHearingId(HEARING_ID_2)
                        .withStartDate(START_DATE).withStartTime(START_TIME_2)
                        .withDuration(DURATION_2).withHearingType(HEARING_TYPE)
                        .build()
        );
    }

    private List<HearingCase> hearingCases() {
        return newArrayList(
                new HearingCase(HEARING_CASE_ID, HEARING_ID, CASE_ID),
                new HearingCase(HEARING_CASE_ID_2, HEARING_ID, CASE_ID_2),
                new HearingCase(HEARING_CASE_ID_3, HEARING_ID_2, CASE_ID_3),
                new HearingCase(HEARING_CASE_ID_4, HEARING_ID_2, CASE_ID_4),
                new HearingCase(HEARING_CASE_ID_5, HEARING_ID_2, CASE_ID_5)
        );
    }

    private List<HearingCase> caseHearings() {
        return newArrayList(
                new HearingCase(HEARING_CASE_ID, HEARING_ID, CASE_ID),
                new HearingCase(HEARING_CASE_ID_2, HEARING_ID_2, CASE_ID)
        );
    }
}
