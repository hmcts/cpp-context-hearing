package uk.gov.moj.cpp.hearing.query.view;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonassert.impl.matcher.IsEmptyCollection.empty;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.time.ZonedDateTime.parse;
import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings({"unchecked", "unused"})
@RunWith(MockitoJUnitRunner.class)
public class HearingEventQueryViewTest {

    public static final ZonedDateTime HEARING_DATE = parse("2018-02-22T10:30:00Z");
    private static final String ID_1 = "b71e7d2a-d3b3-4a55-a393-6d451767fc05";
    private static final String RECORDED_LABEL_1 = "Hearing Started";
    private static final String ACTION_LABEL_1 = "Start";
    private static final Integer ACTION_SEQUENCE_1 = 1;
    private static final String GROUP_LABEL_1 = "Recording";
    private static final Integer GROUP_SEQUENCE_1 = 1;
    private static final String ID_2 = "0df93f18-0a21-40f5-9fb3-da4749cd70fe";
    private static final String RECORDED_LABEL_2 = "Hearing Ended";
    private static final String ACTION_LABEL_2 = "End";
    private static final Integer ACTION_SEQUENCE_2 = 2;
    private static final String ID_3 = "160ecb51-29ee-4954-bbbf-daab18a24fbb";
    private static final String RECORDED_LABEL_3 = "Hearing Paused";
    private static final String ACTION_LABEL_3 = "Pause";
    private static final Integer ACTION_SEQUENCE_3 = 3;
    private static final String ID_5 = "ffd6bb0d-8702-428c-a7bd-570570fa8d0a";
    private static final String RECORDED_LABEL_5 = "Proceedings in chambers";
    private static final String ACTION_LABEL_5 = "In chambers";
    private static final Integer ACTION_SEQUENCE_5 = 5;
    private static final String ID_6 = "c0b15e38-52ce-4d9d-9ffa-d76c7793cff6";
    private static final String RECORDED_LABEL_6 = "Open Court";
    private static final String ACTION_LABEL_6 = "Open court";
    private static final Integer ACTION_SEQUENCE_6 = 6;
    private static final String ID_7 = "c3edf650-13c4-4ecb-9f85-6100ad8e4ffc";
    private static final String RECORDED_LABEL_7 = "Defendant Arraigned";
    private static final String ACTION_LABEL_7 = "Arraign defendant.name";
    private static final Integer ACTION_SEQUENCE_7 = 1;
    private static final String GROUP_LABEL_2 = "Defendant";
    private static final Integer GROUP_SEQUENCE_2 = 2;
    private static final String ID_8 = "75c8c5eb-c661-40be-a5bf-07b7b8c0463a";
    private static final String RECORDED_LABEL_8 = "Defendant Rearraigned";
    private static final String ACTION_LABEL_8 = "Rearraign defendant.name";
    private static final Integer ACTION_SEQUENCE_8 = 2;
    private static final boolean ALTERABLE = BOOLEAN.next();
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";
    private static final String RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM = "hearing.get-active-hearings-for-court-room";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_DEFENCE_COUNSEL_ID = "defenceCounselId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_HEARING_EVENTS = "events";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_ACTIVE_HEARINGS = "activeHearings";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_ACTION_SEQUENCE = "actionSequence";
    private static final String FIELD_CASE_ATTRIBUTES = "caseAttributes";
    private static final String FIELD_DEFENDANT_NAME = "defendant.name";
    private static final String FIELD_COUNSEL_NAME = "counsel.name";
    private static final String FIELD_GROUP_SEQUENCE = "groupSequence";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_HAS_ACTIVE_HEARING = "hasActiveHearing";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_EVENT_DATE = "eventDate";
    private static final UUID HEARING_ID_1 = randomUUID();
    private static final UUID HEARING_ID_2 = randomUUID();
    private static final UUID HEARING_EVENT_ID_1 = randomUUID();
    private static final UUID DEFENCE_COUNSEL_ID = randomUUID();
    private static final UUID START_HEARING_EVENT_DEFINITION_ID = fromString("b71e7d2a-d3b3-4a55-a393-6d451767fc05");
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime LAST_MODIFIED_TIME = PAST_ZONED_DATE_TIME.next();
    private static final UUID HEARING_EVENT_ID_2 = randomUUID();
    private static final UUID HEARING_EVENT_ID_3 = randomUUID();
    private static final UUID HEARING_EVENT_ID_4 = randomUUID();
    private static final UUID HEARING_EVENT_ID_5 = randomUUID();
    private static final UUID RESUME_HEARING_EVENT_DEFINITION_ID = fromString("64476e43-2138-46d5-b58b-848582cf9b07");
    private static final UUID PAUSE_HEARING_EVENT_DEFINITION_ID = fromString("160ecb51-29ee-4954-bbbf-daab18a24fbb");
    private static final UUID END_HEARING_EVENT_DEFINITION_ID = fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe");
    private static final ZonedDateTime EVENT_TIME_2 = EVENT_TIME.plusMinutes(1);
    private static final ZonedDateTime LAST_MODIFIED_TIME_2 = PAST_ZONED_DATE_TIME.next();
    private static final boolean ALTERABLE_2 = BOOLEAN.next();
    private static final List<String> CASE_ATTRIBUTES = newArrayList("defendant.name", "counsel.name");
    private static final UUID PERSON_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID PERSON_ID_2 = randomUUID();
    private static final UUID DEFENDANT_ID_2 = randomUUID();
    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final UUID COURT_ROOM_ID = randomUUID();
    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private HearingEventRepository hearingEventRepository;

    @Mock
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private HearingEventQueryView hearingEventQueryView;

    @Test
    public void shouldGetHearingEventLogByHearingId() {
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID_1, EVENT_TIME.toLocalDate())).thenReturn(hearingEvents());
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEvents(HEARING_ID_2));

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualHearingEventLog = hearingEventQueryView.getHearingEventLog(query);

        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID_1.toString())),
                        withJsonPath(format("$.%s", FIELD_HAS_ACTIVE_HEARING), equalTo(TRUE)),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_1.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(START_HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_EVENT_TIME), equalTo(ZonedDateTimes.toString(EVENT_TIME))),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_LAST_MODIFIED_TIME), equalTo(ZonedDateTimes.toString(LAST_MODIFIED_TIME))),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_ALTERABLE), equalTo(ALTERABLE)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_DEFENCE_COUNSEL_ID), equalTo(DEFENCE_COUNSEL_ID.toString())),

                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(START_HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_EVENT_TIME), equalTo(ZonedDateTimes.toString(EVENT_TIME_2))),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_LAST_MODIFIED_TIME), equalTo(ZonedDateTimes.toString(LAST_MODIFIED_TIME_2))),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_ALTERABLE), equalTo(ALTERABLE_2)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_DEFENCE_COUNSEL_ID), equalTo(DEFENCE_COUNSEL_ID.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldReturnEmptyEventLogWhenThereAreNoEventsByHearingId() {
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID_1)).thenReturn(emptyList());
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEvents());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualHearingEventLog = hearingEventQueryView.getHearingEventLog(query);

        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID_1.toString())),
                        withJsonPath(format("$.%s", FIELD_HAS_ACTIVE_HEARING), equalTo(FALSE)),

                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), empty())
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingEventDefinitionById() {
        when(hearingEventDefinitionRepository.findBy(START_HEARING_EVENT_DEFINITION_ID)).thenReturn(prepareHearingEventDefinition());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, START_HEARING_EVENT_DEFINITION_ID.toString())
                .build());

        final JsonEnvelope actualHearingEventDefinition = hearingEventQueryView.getHearingEventDefinition(query);

        assertThat(actualHearingEventDefinition, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITION),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_GENERIC_ID), is(ID_1)),
                        withJsonPath(format("$.%s", FIELD_ACTION_LABEL), is(ACTION_LABEL_1)),
                        withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), is(RECORDED_LABEL_1)),
                        withJsonPath(format("$.%s", FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_1)),
                        withoutJsonPath(format("$.%s", FIELD_CASE_ATTRIBUTES)),
                        withJsonPath(format("$.%s", FIELD_GROUP_LABEL), is(GROUP_LABEL_1)),
                        withJsonPath(format("$.%s", FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                        withJsonPath(format("$.%s", FIELD_ALTERABLE), is(ALTERABLE))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetAllHearingEventDefinitionsVersionTwoWhichDoNotHaveCaseAttributes() {
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionsWithoutCaseAttributes());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .build());

        final JsonEnvelope actualHearingEventDefinitions = hearingEventQueryView.getHearingEventDefinitionsVersionTwo(query);

        assertThat(actualHearingEventDefinitions, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL_1)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetAllHearingEventDefinitionsVersionTwoWithCaseAttributes() {
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionsWithCaseAttributes());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .build());

        final JsonEnvelope actualHearingEventDefinitions = hearingEventQueryView.getHearingEventDefinitionsVersionTwo(query);

        assertThat(actualHearingEventDefinitions, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES), hasSize(2)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES), hasSize(2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetActiveHearingIdsWhenAnotherHearingIsActiveInTheSameCourtRoom() {
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEvents(HEARING_ID_2));

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_EVENT_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualActiveHearingIdsForCourtRoom = hearingEventQueryView.getActiveHearingsForCourtRoom(query);

        assertThat(actualActiveHearingIdsForCourtRoom, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(1)),
                        withJsonPath(format("$.%s[0]", FIELD_ACTIVE_HEARINGS), equalTo(HEARING_ID_2.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetActiveHearingIdsInCaseOfSamePauseAndResumeEventsRecorded() {
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEventsWithSameNumberOfPausesAndResumes());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_EVENT_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualActiveHearingIdsForCourtRoom = hearingEventQueryView.getActiveHearingsForCourtRoom(query);

        assertThat(actualActiveHearingIdsForCourtRoom, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(1)),
                        withJsonPath(format("$.%s[0]", FIELD_ACTIVE_HEARINGS), equalTo(HEARING_ID_2.toString()))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldNotGetActiveHearingIdsInCaseOfMorePausesThanResumeEvents() {
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEventsWithMoreNumberOfPausesThanResumes());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_EVENT_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualActiveHearingIdsForCourtRoom = hearingEventQueryView.getActiveHearingsForCourtRoom(query);

        assertThat(actualActiveHearingIdsForCourtRoom, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(0))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldNotGetActiveHearingIdsInCaseOfEndEventsRecorded() {
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEventsWithEndHearingEvents());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_EVENT_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualActiveHearingIdsForCourtRoom = hearingEventQueryView.getActiveHearingsForCourtRoom(query);

        assertThat(actualActiveHearingIdsForCourtRoom, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(0))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldNotGetActiveHearingIdsWhenThereIsNoActiveHearingForTheSameCourtRoom() {
        when(hearingRepository.findBy(HEARING_ID_1)).thenReturn(mockHearing());
        when(hearingEventRepository
                .findHearingEvents(
                        COURT_CENTRE_ID,
                        COURT_ROOM_ID,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEventsWithStartAndEndEvent());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_EVENT_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final JsonEnvelope actualActiveHearingIdsForCourtRoom = hearingEventQueryView.getActiveHearingsForCourtRoom(query);

        assertThat(actualActiveHearingIdsForCourtRoom, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(0))
                ))).thatMatchesSchema()
        ));
    }

    private HearingEventDefinition prepareHearingEventDefinition() {
        return new HearingEventDefinition(java.util.UUID.fromString(ID_1), RECORDED_LABEL_1, ACTION_LABEL_1, ACTION_SEQUENCE_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE);
    }

    private List<HearingEvent> hearingEvents() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();
        hearingEvents.add(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID)
                        .setHearingId(HEARING_ID_1)
                        .setRecordedLabel(RECORDED_LABEL_1)
                        .setEventDate(EVENT_TIME.toLocalDate())
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE)
                        .setDefenceCounselId(DEFENCE_COUNSEL_ID)
        );
        hearingEvents.add(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_2)
                        .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID)
                        .setHearingId(HEARING_ID_1)
                        .setRecordedLabel(RECORDED_LABEL_2)
                        .setEventDate(EVENT_TIME_2.toLocalDate())
                        .setEventTime(EVENT_TIME_2)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_2)
                        .setAlterable(ALTERABLE_2)
                        .setDefenceCounselId(DEFENCE_COUNSEL_ID)
        );

        return hearingEvents;
    }

    private List<HearingEventDefinition> hearingEventDefinitionsWithoutCaseAttributes() {
        return newArrayList(
                new HearingEventDefinition(java.util.UUID.fromString(ID_1), RECORDED_LABEL_1, ACTION_LABEL_1, ACTION_SEQUENCE_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE),
                new HearingEventDefinition(java.util.UUID.fromString(ID_2), RECORDED_LABEL_2, ACTION_LABEL_2, ACTION_SEQUENCE_2, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE_2)
        );
    }

    private List<HearingEventDefinition> hearingEventDefinitionsWithCaseAttributes() {
        return newArrayList(
                new HearingEventDefinition(java.util.UUID.fromString(ID_1), RECORDED_LABEL_1, ACTION_LABEL_1, ACTION_SEQUENCE_1, CASE_ATTRIBUTES.stream().collect(joining(",")), GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE),
                new HearingEventDefinition(java.util.UUID.fromString(ID_2), RECORDED_LABEL_2, ACTION_LABEL_2, ACTION_SEQUENCE_2, CASE_ATTRIBUTES.stream().collect(joining(",")), GROUP_LABEL_2, GROUP_SEQUENCE_2, ALTERABLE_2)
        );
    }


    private List<HearingEventDefinition> hearingEventDefinitionsWithOutOfSequenceEvent() {
        return newArrayList(
                new HearingEventDefinition(java.util.UUID.fromString(ID_1), RECORDED_LABEL_1, ACTION_LABEL_1, ACTION_SEQUENCE_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, false),
                new HearingEventDefinition(java.util.UUID.fromString(ID_2), RECORDED_LABEL_2, ACTION_LABEL_2, ACTION_SEQUENCE_2, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, false),
                new HearingEventDefinition(java.util.UUID.fromString(ID_3), RECORDED_LABEL_3, ACTION_LABEL_3, ACTION_SEQUENCE_3, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, false),
                new HearingEventDefinition(java.util.UUID.fromString(ID_6), RECORDED_LABEL_6, ACTION_LABEL_6, ACTION_SEQUENCE_6, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, false),
                new HearingEventDefinition(java.util.UUID.fromString(ID_5), RECORDED_LABEL_5, ACTION_LABEL_5, ACTION_SEQUENCE_5, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, false),
                new HearingEventDefinition(java.util.UUID.fromString(ID_7), RECORDED_LABEL_7, ACTION_LABEL_7, ACTION_SEQUENCE_7, null, GROUP_LABEL_2, GROUP_SEQUENCE_2, false),
                new HearingEventDefinition(java.util.UUID.fromString(ID_8), RECORDED_LABEL_8, ACTION_LABEL_8, ACTION_SEQUENCE_8, null, GROUP_LABEL_2, GROUP_SEQUENCE_2, false)
        );
    }

    private List<HearingEvent> mockActiveHearingEvents(final UUID... ids) {
        final List<HearingEvent> hearingEvents = new ArrayList<>();
        return Arrays.stream(ids)
                .map(id -> new HearingEvent()
                        .setId(HEARING_EVENT_ID_1)
                        .setHearingId(id)
                        .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID))
                .collect(Collectors.toList());
    }

    private List<HearingEvent> mockActiveHearingEventsWithSameNumberOfPausesAndResumes() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();

        final HearingEvent hearingEvent1 = new HearingEvent()
                .setId(HEARING_EVENT_ID_1)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent1);

        final HearingEvent hearingEvent2 = new HearingEvent()
                .setId(HEARING_EVENT_ID_2)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(PAUSE_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent2);

        final HearingEvent hearingEvent3 = new HearingEvent()
                .setId(HEARING_EVENT_ID_3)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(RESUME_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent3);

        final HearingEvent hearingEvent4 = new HearingEvent()
                .setId(HEARING_EVENT_ID_4)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(PAUSE_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent4);

        final HearingEvent hearingEvent5 = new HearingEvent()
                .setId(HEARING_EVENT_ID_5)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(RESUME_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent5);

        return hearingEvents;
    }

    private List<HearingEvent> mockActiveHearingEventsWithMoreNumberOfPausesThanResumes() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();

        final HearingEvent hearingEvent1 = new HearingEvent()
                .setId(HEARING_EVENT_ID_1)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent1);

        final HearingEvent hearingEvent2 = new HearingEvent()
                .setId(HEARING_EVENT_ID_2)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(PAUSE_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent2);

        final HearingEvent hearingEvent3 = new HearingEvent()
                .setId(HEARING_EVENT_ID_3)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(RESUME_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent3);

        final HearingEvent hearingEvent4 = new HearingEvent()
                .setId(HEARING_EVENT_ID_4)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(PAUSE_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent4);

        return hearingEvents;
    }

    private List<HearingEvent> mockActiveHearingEventsWithStartAndEndEvent() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();

        final HearingEvent hearingEvent1 = new HearingEvent()
                .setId(HEARING_EVENT_ID_1)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent1);

        final HearingEvent hearingEvent2 = new HearingEvent()
                .setId(HEARING_EVENT_ID_2)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(END_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent2);

        return hearingEvents;
    }

    private List<HearingEvent> mockActiveHearingEventsWithEndHearingEvents() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();

        final HearingEvent hearingEvent1 = new HearingEvent()
                .setId(HEARING_EVENT_ID_1)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(START_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent1);

        final HearingEvent hearingEvent2 = new HearingEvent()
                .setId(HEARING_EVENT_ID_2)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(PAUSE_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent2);

        final HearingEvent hearingEvent3 = new HearingEvent()
                .setId(HEARING_EVENT_ID_3)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(RESUME_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent3);

        final HearingEvent hearingEvent4 = new HearingEvent()
                .setId(HEARING_EVENT_ID_4)
                .setHearingId(HEARING_ID_2)
                .setHearingEventDefinitionId(END_HEARING_EVENT_DEFINITION_ID);
        hearingEvents.add(hearingEvent4);

        return hearingEvents;
    }

    private Hearing mockHearing() {
        final Hearing hearing = new Hearing();
        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setId(COURT_CENTRE_ID);
        courtCentre.setRoomId(COURT_ROOM_ID);
        hearing.setId(HEARING_ID_1);
        hearing.setCourtCentre(courtCentre);

        return hearing;
    }
}