package uk.gov.moj.cpp.hearing.query.view;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonassert.impl.matcher.IsEmptyCollection.empty;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Person.person;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory;
import uk.gov.justice.services.test.utils.core.random.Generator;
import uk.gov.justice.services.test.utils.core.random.StringGenerator;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.query.view.helper.TimeZoneHelper;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ProgressionService;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.view.service.userdata.UserDataService;
import uk.gov.moj.cpp.hearing.test.FileUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"unchecked", "unused"})
@ExtendWith(MockitoExtension.class)
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
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final DateTimeFormatter reportTimeFormatter = DateTimeFormatter.ofPattern("HH:mm 'on' dd MMM yyyy");
    private static final DateTimeFormatter eventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final Generator<String> STRING = new StringGenerator();

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private HearingService hearingService;

    @Mock
    private ProgressionService progressionService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private HearingEventQueryView target;

    @Spy
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    private TimeZoneHelper timeZone;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
        setField(this.courtApplicationsSerializer, "objectToJsonObjectConverter", objectToJsonObjectConverter);
    }

    @Test
    public void shouldGetHearingEventLogByHearingId() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService.getHearingEvents(HEARING_ID_1, EVENT_TIME.toLocalDate())).thenReturn(hearingEvents());
        when(hearingService
                .getHearingEvents(
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

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLog(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(HEARING_ID_1, EVENT_TIME.toLocalDate());
        verify(hearingService).getHearingEvents(COURT_CENTRE_ID, COURT_ROOM_ID, EVENT_TIME.toLocalDate());

        assertThat(actualHearingEventLog.metadata().name(), is(RESPONSE_NAME_HEARING_EVENT_LOG));
        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID_1.toString())),
                hasJsonPath(format("$.%s", FIELD_HAS_ACTIVE_HEARING), equalTo(TRUE)),
                hasJsonPath(format("$.%s", FIELD_HEARING_EVENTS), hasSize(2)),

                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_1.toString())),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(START_HEARING_EVENT_DEFINITION_ID.toString())),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_EVENT_TIME), equalTo(ZonedDateTimes.toString(EVENT_TIME))),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_LAST_MODIFIED_TIME), equalTo(ZonedDateTimes.toString(LAST_MODIFIED_TIME))),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_ALTERABLE), equalTo(ALTERABLE)),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_DEFENCE_COUNSEL_ID), equalTo(DEFENCE_COUNSEL_ID.toString())),

                hasJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_2.toString())),
                hasJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(START_HEARING_EVENT_DEFINITION_ID.toString())),
                hasJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_EVENT_TIME), equalTo(ZonedDateTimes.toString(EVENT_TIME_2))),
                hasJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_LAST_MODIFIED_TIME), equalTo(ZonedDateTimes.toString(LAST_MODIFIED_TIME_2))),
                hasJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_ALTERABLE), equalTo(ALTERABLE_2)),
                hasJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_DEFENCE_COUNSEL_ID), equalTo(DEFENCE_COUNSEL_ID.toString()))
        ));
    }

    @Test
    public void shouldReturnEmptyEventLogWhenThereAreNoEventsByHearingId() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService
                .getHearingEvents(
                        HEARING_ID_1,
                        EVENT_TIME.toLocalDate())
        ).thenReturn(mockActiveHearingEvents());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                        .add(FIELD_DATE, EVENT_TIME.toLocalDate().toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLog(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(HEARING_ID_1,
                EVENT_TIME.toLocalDate());

        assertThat(actualHearingEventLog.metadata().name(), is(RESPONSE_NAME_HEARING_EVENT_LOG));
        assertThat(actualHearingEventLog.payload().toString(), allOf(
                hasJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID_1.toString())),
                hasJsonPath(format("$.%s", FIELD_HAS_ACTIVE_HEARING), equalTo(FALSE)),
                hasJsonPath(format("$.%s", FIELD_HEARING_EVENTS), empty())
        ));
    }

    @Test
    public void shouldGetHearingEventDefinitionById() {
        when(hearingService.getHearingEventDefinition(START_HEARING_EVENT_DEFINITION_ID)).thenReturn(Optional.of(prepareHearingEventDefinition()));

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, START_HEARING_EVENT_DEFINITION_ID.toString())
                .build());

        final Envelope<JsonObject> actualHearingEventDefinition = target.getHearingEventDefinition(query);

        verify(hearingService).getHearingEventDefinition(START_HEARING_EVENT_DEFINITION_ID);
        assertThat(actualHearingEventDefinition.metadata().name(), is(RESPONSE_NAME_HEARING_EVENT_DEFINITION));
        assertThat(actualHearingEventDefinition.payload().toString(), allOf(
                hasJsonPath(format("$.%s", FIELD_GENERIC_ID), is(ID_1)),
                hasJsonPath(format("$.%s", FIELD_ACTION_LABEL), is(ACTION_LABEL_1)),
                hasJsonPath(format("$.%s", FIELD_RECORDED_LABEL), is(RECORDED_LABEL_1)),
                hasJsonPath(format("$.%s", FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_1)),
                hasNoJsonPath(format("$.%s", FIELD_CASE_ATTRIBUTES)),
                hasJsonPath(format("$.%s", FIELD_GROUP_LABEL), is(GROUP_LABEL_1)),
                hasJsonPath(format("$.%s", FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                hasJsonPath(format("$.%s", FIELD_ALTERABLE), is(ALTERABLE))
        ));
    }

    @Test
    public void shouldGetAllHearingEventDefinitionsVersionTwoWhichDoNotHaveCaseAttributes() {
        when(hearingService.getHearingEventDefinitions()).thenReturn(hearingEventDefinitionsWithoutCaseAttributes());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .build());

        final Envelope<JsonObject> actualHearingEventDefinitions = target.getHearingEventDefinitions(query);

        verify(hearingService).getHearingEventDefinitions();
        assertThat(actualHearingEventDefinitions.metadata().name(), is(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS));
        assertThat(actualHearingEventDefinitions.payload().toString(), allOf(
                hasJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                hasNoJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL_1)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                hasNoJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
        ));
    }

    @Test
    public void shouldGetAllHearingEventDefinitionsVersionTwoWithCaseAttributes() {
        when(hearingService.getHearingEventDefinitions()).thenReturn(hearingEventDefinitionsWithCaseAttributes());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .build());

        final Envelope<JsonObject> actualHearingEventDefinitions = target.getHearingEventDefinitions(query);

        verify(hearingService).getHearingEventDefinitions();
        assertThat(actualHearingEventDefinitions.metadata().name(), is(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS));
        assertThat(actualHearingEventDefinitions.payload().toString(), allOf(
                hasJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_1)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES), hasSize(2)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                hasJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(ID_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_SEQUENCE), is(ACTION_SEQUENCE_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_SEQUENCE), is(GROUP_SEQUENCE_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES), hasSize(2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL_2)),
                hasJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
        ));
    }

    @Test
    public void shouldGetActiveHearingIdsWhenAnotherHearingIsActiveInTheSameCourtRoom() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService.getHearingEvents(
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

        final Envelope<JsonObject> actualActiveHearingIdsForCourtRoom = target.getActiveHearingsForCourtRoom(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(COURT_CENTRE_ID,
                COURT_ROOM_ID,
                EVENT_TIME.toLocalDate());
        assertThat(actualActiveHearingIdsForCourtRoom.metadata().name(), is(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM));
        assertThat(actualActiveHearingIdsForCourtRoom.payload().toString(), (allOf(
                hasJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(1)),
                hasJsonPath(format("$.%s[0]", FIELD_ACTIVE_HEARINGS), equalTo(HEARING_ID_2.toString()))
        )));
    }

    @Test
    public void shouldGetActiveHearingIdsInCaseOfSamePauseAndResumeEventsRecorded() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService
                .getHearingEvents(
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

        final Envelope<JsonObject> actualActiveHearingIdsForCourtRoom = target.getActiveHearingsForCourtRoom(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(COURT_CENTRE_ID,
                COURT_ROOM_ID,
                EVENT_TIME.toLocalDate());
        assertThat(actualActiveHearingIdsForCourtRoom.metadata().name(), is(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM));
        assertThat(actualActiveHearingIdsForCourtRoom.payload().toString(), allOf(
                hasJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(1)),
                hasJsonPath(format("$.%s[0]", FIELD_ACTIVE_HEARINGS), equalTo(HEARING_ID_2.toString()))
        ));
    }

    @Test
    public void shouldNotGetActiveHearingIdsInCaseOfMorePausesThanResumeEvents() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService
                .getHearingEvents(
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

        final Envelope<JsonObject> actualActiveHearingIdsForCourtRoom = target.getActiveHearingsForCourtRoom(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(COURT_CENTRE_ID,
                COURT_ROOM_ID,
                EVENT_TIME.toLocalDate());
        assertThat(actualActiveHearingIdsForCourtRoom.metadata().name(), is(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM));
        assertThat(actualActiveHearingIdsForCourtRoom.payload().toString(),
                hasJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), Matchers.empty())
        );
    }

    @Test
    public void shouldNotGetActiveHearingIdsInCaseOfEndEventsRecorded() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService
                .getHearingEvents(
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

        final Envelope<JsonObject> actualActiveHearingIdsForCourtRoom = target.getActiveHearingsForCourtRoom(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(COURT_CENTRE_ID,
                COURT_ROOM_ID,
                EVENT_TIME.toLocalDate());
        assertThat(actualActiveHearingIdsForCourtRoom.metadata().name(), is(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM));
        assertThat(actualActiveHearingIdsForCourtRoom.payload().toString(),
                hasJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), Matchers.empty())
        );
    }

    @Test
    public void shouldNotGetActiveHearingIdsWhenThereIsNoActiveHearingForTheSameCourtRoom() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.of(mockHearing().getCourtCentre()));
        when(hearingService
                .getHearingEvents(
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

        final Envelope<JsonObject> actualActiveHearingIdsForCourtRoom = target.getActiveHearingsForCourtRoom(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        verify(hearingService).getHearingEvents(COURT_CENTRE_ID,
                COURT_ROOM_ID,
                EVENT_TIME.toLocalDate());
        assertThat(actualActiveHearingIdsForCourtRoom.metadata().name(), is(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM));
        assertThat(actualActiveHearingIdsForCourtRoom.payload().toString(),
                hasJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), Matchers.empty())
        );
    }

    @Test
    public void shouldNotGetHearingEventDefinitionByNonExistingId() {
        when(hearingService.getHearingEventDefinition(START_HEARING_EVENT_DEFINITION_ID)).thenReturn(Optional.empty());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, START_HEARING_EVENT_DEFINITION_ID.toString())
                .build());

        final Envelope<JsonObject> actualHearingEventDefinition = target.getHearingEventDefinition(query);

        verify(hearingService).getHearingEventDefinition(START_HEARING_EVENT_DEFINITION_ID);
        assertThat(actualHearingEventDefinition.payload(), is((JsonObject) null));
        assertThat(actualHearingEventDefinition.metadata().name(), is(RESPONSE_NAME_HEARING_EVENT_DEFINITION));
    }

    @Test
    public void shouldNotGetActiveHearingsByNonExistingId() {
        when(hearingService.getCourtCenterByHearingId(HEARING_ID_1)).thenReturn(Optional.empty());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID_1.toString())
                .add(FIELD_EVENT_DATE, EVENT_TIME.toLocalDate().toString())
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, START_HEARING_EVENT_DEFINITION_ID.toString())
                .build());

        final Envelope<JsonObject> activeHearingsForCourtRoom = target.getActiveHearingsForCourtRoom(query);

        verify(hearingService).getCourtCenterByHearingId(HEARING_ID_1);
        assertThat(activeHearingsForCourtRoom.metadata().name(), is(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM));
        assertThat(activeHearingsForCourtRoom.payload().toString(),
                hasJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), Matchers.empty())
        );
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingWhenDateTimeBST() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing(hearingId1, 2);
        HearingEvent hearingEventInUTC = mockHearingEvent();
        HearingEvent hearingEventInBST = mockHearingEvent();;


        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEventInUTC));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(timeZone.isDayLightSavingOn()).thenReturn(true);

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);


        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEventInUTC.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEventInUTC.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEventInUTC.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
        assertEquals(hearingEventInUTC.getEventTime().truncatedTo(ChronoUnit.SECONDS), hearingEventInBST.getEventTime().plusHours(1).truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingWhenDateTimeUTC() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing(hearingId1, 2);
        HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(timeZone.isDayLightSavingOn()).thenReturn(false);

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);


        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingForSingleDay() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing(hearingId1, 2);
        HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);


        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s", "hearings", "startDate"), notNullValue()),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingWithMutliDays() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing1(hearingId1, 2019, 7, 1, 3);
        HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);


        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s", "hearings", "startDate"), equalTo("2019-07-01")),
                hasJsonPath(format("$.%s[0].%s", "hearings", "endDate"), equalTo("2019-07-05")),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingNoUserIdAndNoCourtClerks() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing(hearingId1,2);
        HearingEvent hearingEvent = mockHearingEventNoUserId();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());
        if(nonNull(query.metadata().userId())) {
            when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        }

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);


        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingWithUserIdAndClerk() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing(hearingId1,2);
        HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldReturnEmptyPayloadIfNoEventLogForHeraing() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        UUID hearingId1 = randomUUID();
        Hearing hearing = mockHearing(hearingId1,2);
        HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(new ArrayList<>());
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().get("hearings"),  is(JsonValue.NULL));

    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingAndDate() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final  LocalDate hearingDate = LocalDate.now();

        Hearing hearing = mockHearing(hearingId1,2);
        final HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, hearingDate)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .add("hearingDate", hearingDate.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, hearingDate);

        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldGetHearingEventReportForDocumentByHearingAndDateWithDefendantAndProsecutionAttendees() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final  LocalDate hearingDate = LocalDate.of(2022,10,12);

        Hearing hearing = mockHearing(hearingId1,2);
        final HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(hearing);
        when(hearingService.getHearingEvents(hearingId1, hearingDate)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId1.toString())
                        .add("hearingDate", hearingDate.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId1);
        verify(hearingService).getHearingEvents(hearingId1, hearingDate);

        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "defendantAttendees", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "prosecutionAttendees", hasSize(1))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
                ));
    }


    @Test
    public void shouldGetHearingEventReportForDocumentByCaseWithSingleHearing() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final LocalDate hearingDate = LocalDate.now();
        final UUID caseId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        final HearingEvent hearingEvent = mockHearingEvent();

        when(hearingService.getHearingDetailsByCaseForDocuments(any())).thenReturn(asList(hearing));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("caseId", caseId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByCaseForDocuments(caseId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));

    }


    @Test
    public void shouldGetHearingEventReportForDocumentByCaseWithOrderedSingleAndMultiDayHearings() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID hearingId3 = randomUUID();
        final UUID hearingId4 = randomUUID();

        final LocalDate hearingDate = LocalDate.now();
        final UUID caseId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        Hearing hearing1 = mockHearing1(hearingId2, 2020, 7, 1, 3);
        Hearing hearing2 = mockHearing1(hearingId3,2023, 2, 21, 5);
        Hearing hearing3 = mockHearing1(hearingId4,2019, 5, 2, 4);

        final HearingEvent hearingEvent = mockHearingEvent();
        final HearingEvent hearingEvent1 = mockHearingEvent();
        final HearingEvent hearingEvent2 = mockHearingEvent();
        final HearingEvent hearingEvent3 = mockHearingEvent();


        when(hearingService.getHearingDetailsByCaseForDocuments(any())).thenReturn(asList(hearing,hearing1,hearing2,hearing3));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(hearingEvent1));
        when(hearingService.getHearingEvents(hearingId3, null)).thenReturn(asList(hearingEvent2));
        when(hearingService.getHearingEvents(hearingId4, null)).thenReturn(asList(hearingEvent3));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("caseId", caseId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByCaseForDocuments(caseId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(4))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId4.toString())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[3].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));

    }

     @Test
        public void shouldGetHearingEventReportForDocumentByCaseWithOrderedSingleDayHearings() throws IOException{
            setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
            final UUID hearingId1 = randomUUID();
            final UUID hearingId2 = randomUUID();
            final UUID hearingId3 = randomUUID();
            final UUID hearingId4 = randomUUID();

            final LocalDate hearingDate = LocalDate.now();
            final UUID caseId = randomUUID();

            Hearing hearing = mockHearing(hearingId1,5);
            Hearing hearing1 = mockHearing(hearingId2,4);
            Hearing hearing2 = mockHearing(hearingId3,3);
            Hearing hearing3 = mockHearing(hearingId4,2);

            final HearingEvent hearingEvent = mockHearingEvent();
            final HearingEvent hearingEvent1 = mockHearingEvent();
            final HearingEvent hearingEvent2 = mockHearingEvent();
            final HearingEvent hearingEvent3 = mockHearingEvent();


            when(hearingService.getHearingDetailsByCaseForDocuments(any())).thenReturn(asList(hearing,hearing1,hearing2,hearing3));
            when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
            when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(hearingEvent1));
            when(hearingService.getHearingEvents(hearingId3, null)).thenReturn(asList(hearingEvent2));
            when(hearingService.getHearingEvents(hearingId4, null)).thenReturn(asList(hearingEvent3));
            when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
            when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

            final JsonEnvelope query = envelopeFrom(
                    JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                    createObjectBuilder().add("caseId", caseId.toString())
                            .build());

            final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


            verify(hearingService).getHearingDetailsByCaseForDocuments(caseId);
            verify(hearingService).getHearingEvents(hearingId1, null);

            assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(4))),
                    hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId4.toString())),
                    hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                    hasJsonPath(format("$.%s[1].%s", "hearings", "hearingId"), equalTo(hearingId3.toString())),
                    hasJsonPath(format("$.%s[2].%s", "hearings", "hearingId"), equalTo(hearingId2.toString())),
                    hasJsonPath(format("$.%s[3].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                    hasJsonPath(format("$.%s[0].%s[0]", "hearings", "caseUrns"), equalTo("TFL102345")),
                    hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                    hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                    hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
            ));

        }

    @Test
    public void shouldGetHearingEventReportForDocumentByCaseWithMultipleHearing() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID caseId = randomUUID();

        when(hearingService.getHearingDetailsByCaseForDocuments(any())).thenReturn(asList(mockHearing(hearingId1,2), mockHearing1(hearingId2,2019, 7, 1, 3)));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(mockHearingEvents());
        when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(mockHearingEvent()));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("caseId", caseId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByCaseForDocuments(caseId);
        verify(hearingService).getHearingEvents(hearingId1, null);
        verify(hearingService).getHearingEvents(hearingId2, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(2))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))));
    }

    @Test
    public void shouldGetHearingEventReportForCdesDocumentByCaseWithSingleHearingAndMultipleCourtClerks() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId = randomUUID();

        when(hearingService.getHearingDetailsByHearingForDocuments(any())).thenReturn(mockHearing(hearingId, 2));
        when(hearingService.getHearingEvents(hearingId, null)).thenReturn(mockHearingEvents());
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));

        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("hearingId", hearingId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByHearingForDocuments(hearingId);
        verify(hearingService).getHearingEvents(hearingId, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf(hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks", hasSize(2))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))

        ));
    }

    @Test
    public void shouldGetHearingEventReportForCdesDocumentByApplication() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final LocalDate hearingDate = LocalDate.now();
        final UUID applicationId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        final HearingEvent hearingEvent = mockHearingEvent();
        final JsonObject courtApplicationResponse = stringToJsonObjectConverter.convert(Resources.toString(getResource("progression.query.application.aaag.json"), defaultCharset()));
        final CourtApplication courtApplication = getCourtApplication(applicationId);

        when(hearingService.getHearingDetailsByApplicationForDocuments(any())).thenReturn(asList(hearing));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(progressionService.retrieveApplication(any(JsonEnvelope.class), any())).thenReturn(courtApplicationResponse);

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(courtApplication));


        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("applicationId", applicationId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);


        verify(hearingService).getHearingDetailsByApplicationForDocuments(applicationId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationReferences"), equalTo(courtApplication.getApplicationReference())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationIds"), equalTo(courtApplication.getId().toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "thirdParties"), equalTo(courtApplication.getThirdParties().get(0).getPersonDetails().getFirstName()+" "+ courtApplication.getThirdParties().get(0).getPersonDetails().getLastName())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));

    }

     @Test
    public void shouldGetHearingEventReportForCdesDocumentByApplicationInOrder() throws IOException{
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID hearingId3 = randomUUID();
        final UUID hearingId4 = randomUUID();

        final LocalDate hearingDate = LocalDate.now();
        final UUID caseId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        Hearing hearing1 = mockHearing1(hearingId2, 2021, 7, 1, 3);
        Hearing hearing2 = mockHearing1(hearingId3,2023, 6, 3, 5);
        Hearing hearing3 = mockHearing1(hearingId4,2020, 5, 2, 4);

        final HearingEvent hearingEvent = mockHearingEvent();
        final HearingEvent hearingEvent1 = mockHearingEvent();
        final HearingEvent hearingEvent2 = mockHearingEvent();
        final HearingEvent hearingEvent3 = mockHearingEvent();
        final UUID applicationId = randomUUID();
         final JsonObject courtApplicationResponse = stringToJsonObjectConverter.convert(Resources.toString(getResource("progression.query.application.aaag.json"), defaultCharset()));

        final CourtApplication courtApplication = getCourtApplication(applicationId);

        when(hearingService.getHearingDetailsByApplicationForDocuments(any())).thenReturn(asList(hearing, hearing1, hearing2, hearing3));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(hearingEvent1));
        when(hearingService.getHearingEvents(hearingId3, null)).thenReturn(asList(hearingEvent2));
        when(hearingService.getHearingEvents(hearingId4, null)).thenReturn(asList(hearingEvent3));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(progressionService.retrieveApplication(any(JsonEnvelope.class), any())).thenReturn(courtApplicationResponse);

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(courtApplication));


        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("applicationId", applicationId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByApplicationForDocuments(applicationId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId4.toString())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[3].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationReferences"), equalTo(courtApplication.getApplicationReference())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationIds"), equalTo(courtApplication.getId().toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "respondents"), equalTo("WBHE1n0bUr")),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicants"), equalTo("IuXmrISMSm zKUPL1pbbN")),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "thirdParties"), equalTo(courtApplication.getThirdParties().get(0).getPersonDetails().getFirstName()+" "+ courtApplication.getThirdParties().get(0).getPersonDetails().getLastName())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));

    }

    @Test
    public void shouldGetHearingEventLogCount() throws IOException{
        final UUID hearingId1 = randomUUID();
        final LocalDate hearingDate = LocalDate.now();

        JsonObject jsonObject = createObjectBuilder()
                .add("eventLogCountByHearingIdAndDate",1)
                .add("eventLogCountByHearingId",2)
                .build();

        when(hearingService.getHearingEventLogCount(any(), any())).thenReturn(jsonObject);

        final JsonEnvelope query = EnvelopeFactory.createEnvelope("hearing.get-hearing-event-log-count", createObjectBuilder()
                .add("hearingId", hearingId1.toString())
                .add("hearingDate", LocalDate.now().toString())
                .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogCount(query);

        verify(hearingService).getHearingEventLogCount(hearingId1, hearingDate);

        assertThat(actualHearingEventLog.payload().toString(),
                allOf(hasJsonPath("$.eventLogCountByHearingIdAndDate", is(1)),
                hasJsonPath("$.eventLogCountByHearingId", is(2))

        ));

    }

    @Test
    public void shouldProcesAaagHearingLogDocumentNoApplicantRespondent() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID hearingId3 = randomUUID();
        final UUID hearingId4 = randomUUID();

        final LocalDate hearingDate = LocalDate.now();
        final UUID caseId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        Hearing hearing1 = mockHearing1(hearingId2, 2021, 7, 1, 3);
        Hearing hearing2 = mockHearing1(hearingId3,2023, 6, 3, 5);
        Hearing hearing3 = mockHearing1(hearingId4,2020, 5, 2, 4);

        final HearingEvent hearingEvent = mockHearingEvent();
        final HearingEvent hearingEvent1 = mockHearingEvent();
        final HearingEvent hearingEvent2 = mockHearingEvent();
        final HearingEvent hearingEvent3 = mockHearingEvent();
        final UUID applicationId = randomUUID();
        final JsonObject courtApplicationResponse = stringToJsonObjectConverter.convert(Resources.toString(getResource("progression.query.application.aaag-no-applicant-respondent.json"), defaultCharset()));

        final CourtApplication courtApplication = getCourtApplication(applicationId);

        when(hearingService.getHearingDetailsByApplicationForDocuments(any())).thenReturn(asList(hearing, hearing1, hearing2, hearing3));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(hearingEvent1));
        when(hearingService.getHearingEvents(hearingId3, null)).thenReturn(asList(hearingEvent2));
        when(hearingService.getHearingEvents(hearingId4, null)).thenReturn(asList(hearingEvent3));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(progressionService.retrieveApplication(any(JsonEnvelope.class), any())).thenReturn(courtApplicationResponse);

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(courtApplication));


        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("applicationId", applicationId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query); //failed

        verify(hearingService).getHearingDetailsByApplicationForDocuments(applicationId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId4.toString())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[3].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationReferences"), equalTo(courtApplication.getApplicationReference())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationIds"), equalTo(courtApplication.getId().toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "thirdParties"), equalTo(courtApplication.getThirdParties().get(0).getPersonDetails().getFirstName()+" "+ courtApplication.getThirdParties().get(0).getPersonDetails().getLastName())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldGetHearingLogDocumentWithNoRespondent() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID hearingId3 = randomUUID();
        final UUID hearingId4 = randomUUID();

        final LocalDate hearingDate = LocalDate.now();
        final UUID caseId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        Hearing hearing1 = mockHearing1(hearingId2, 2021, 7, 1, 3);
        Hearing hearing2 = mockHearing1(hearingId3,2023, 6, 3, 5);
        Hearing hearing3 = mockHearing1(hearingId4,2020, 5, 2, 4);

        final HearingEvent hearingEvent = mockHearingEvent();
        final HearingEvent hearingEvent1 = mockHearingEvent();
        final HearingEvent hearingEvent2 = mockHearingEvent();
        final HearingEvent hearingEvent3 = mockHearingEvent();
        final UUID applicationId = randomUUID();
        final JsonObject courtApplicationResponse = stringToJsonObjectConverter.convert(Resources.toString(getResource("progression.query.application.aaag-no-respondent.json"), defaultCharset()));

        final CourtApplication courtApplication = getCourtApplication(applicationId);

        when(hearingService.getHearingDetailsByApplicationForDocuments(any())).thenReturn(asList(hearing, hearing1, hearing2, hearing3));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(hearingEvent1));
        when(hearingService.getHearingEvents(hearingId3, null)).thenReturn(asList(hearingEvent2));
        when(hearingService.getHearingEvents(hearingId4, null)).thenReturn(asList(hearingEvent3));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(progressionService.retrieveApplication(any(JsonEnvelope.class), any())).thenReturn(courtApplicationResponse);

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(courtApplication));


        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("applicationId", applicationId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);//failed

        verify(hearingService).getHearingDetailsByApplicationForDocuments(applicationId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId4.toString())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[3].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationReferences"), equalTo(courtApplication.getApplicationReference())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationIds"), equalTo(courtApplication.getId().toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicants"), equalTo("IuXmrISMSm zKUPL1pbbN")),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "thirdParties"), equalTo(courtApplication.getThirdParties().get(0).getPersonDetails().getFirstName()+" "+ courtApplication.getThirdParties().get(0).getPersonDetails().getLastName())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
        ));
    }

    @Test
    public void shouldProcesAaagHearingLogDocumentNoApplicant() throws IOException {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final UUID hearingId3 = randomUUID();
        final UUID hearingId4 = randomUUID();

        final LocalDate hearingDate = LocalDate.now();
        final UUID caseId = randomUUID();

        Hearing hearing = mockHearing(hearingId1,2);
        Hearing hearing1 = mockHearing1(hearingId2, 2021, 7, 1, 3);
        Hearing hearing2 = mockHearing1(hearingId3,2023, 6, 3, 5);
        Hearing hearing3 = mockHearing1(hearingId4,2020, 5, 2, 4);

        final HearingEvent hearingEvent = mockHearingEvent();
        final HearingEvent hearingEvent1 = mockHearingEvent();
        final HearingEvent hearingEvent2 = mockHearingEvent();
        final HearingEvent hearingEvent3 = mockHearingEvent();
        final UUID applicationId = randomUUID();
        final JsonObject courtApplicationResponse = stringToJsonObjectConverter.convert(Resources.toString(getResource("progression.query.application.aaag-no-applicant.json"), defaultCharset()));

        final CourtApplication courtApplication = getCourtApplication(applicationId);

        when(hearingService.getHearingDetailsByApplicationForDocuments(any())).thenReturn(asList(hearing, hearing1, hearing2, hearing3));
        when(hearingService.getHearingEvents(hearingId1, null)).thenReturn(asList(hearingEvent));
        when(hearingService.getHearingEvents(hearingId2, null)).thenReturn(asList(hearingEvent1));
        when(hearingService.getHearingEvents(hearingId3, null)).thenReturn(asList(hearingEvent2));
        when(hearingService.getHearingEvents(hearingId4, null)).thenReturn(asList(hearingEvent3));
        when(userDataService.getUserDetails(any(), any())).thenReturn(asList("Jacob John"));
        when(referenceDataService.getJudiciaryTitle(any(), any())).thenReturn(asList("Martin Thomas"));
        when(progressionService.retrieveApplication(any(JsonEnvelope.class), any())).thenReturn(courtApplicationResponse);

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(courtApplication));


        final JsonEnvelope query = envelopeFrom(
                JsonEnvelope.metadataBuilder().withUserId(randomUUID().toString()).withId(randomUUID()).withName("hearing.get-hearing-event-log-for-cdes-document").build(),
                createObjectBuilder().add("applicationId", applicationId.toString())
                        .build());

        final Envelope<JsonObject> actualHearingEventLog = target.getHearingEventLogForDocuments(query);

        verify(hearingService).getHearingDetailsByApplicationForDocuments(applicationId);
        verify(hearingService).getHearingEvents(hearingId1, null);

        assertThat(actualHearingEventLog.payload().toString(), allOf( hasJsonPath(format("$.%s[0]", "hearings", hasSize(1))),
                hasJsonPath(format("$.%s[0].%s", "hearings", "hearingId"), equalTo(hearingId4.toString())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[3].%s", "hearings", "hearingId"), equalTo(hearingId1.toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationReferences"), equalTo(courtApplication.getApplicationReference())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "applicationIds"), equalTo(courtApplication.getId().toString())),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "respondents"), equalTo("WBHE1n0bUr")),
                hasJsonPath(format("$.%s[0].%s[0]", "hearings", "thirdParties"), equalTo(courtApplication.getThirdParties().get(0).getPersonDetails().getFirstName()+" "+ courtApplication.getThirdParties().get(0).getPersonDetails().getLastName())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0]", "hearings", "loggedHearingEvents", "courtClerks"), equalTo("Jacob John")),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "label"), equalTo(hearingEvent.getRecordedLabel())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "note"), equalTo(hearingEvent.getNote())),
                hasJsonPath(format("$.%s[0].%s[0].%s[0].%s", "hearings", "loggedHearingEvents", "eventLogs", "time"), equalTo(hearingEvent.getEventTime().format(eventTimeFormatter))),
                hasJsonPath(format("$.%s", "requestedUser", equalTo("Jacob John"))),
                hasJsonPath(format("$.%s", "requestedTime", equalTo(now().format(formatter))))
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

    private List<HearingEvent> mockHearingEvents(){
        final HearingEvent hearingEvent1 = new HearingEvent();
        hearingEvent1.setUserId(randomUUID());
        hearingEvent1.setRecordedLabel("hearing started-1");
        hearingEvent1.setEventTime(now());
        hearingEvent1.setNote("note1");

        final HearingEvent hearingEvent2 = new HearingEvent();
        hearingEvent2.setUserId(randomUUID());
        hearingEvent2.setRecordedLabel("hearing started-2");
        hearingEvent2.setEventTime(now());
        hearingEvent2.setNote("note2");

        return Arrays.asList(hearingEvent1, hearingEvent2);
    }

    private Hearing mockHearing(final UUID hearingId, final int days) throws IOException {

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet(Arrays.asList(mockProsecutionCase())));
        hearing.setCourtCentre(mockCourtCentre());
        hearing.setHearingType(mockHearingType());
        hearing.setHearingDays(new HashSet(Arrays.asList(mockHearingDay(days))));
        hearing.setJudicialRoles(new HashSet(Arrays.asList(mockJudicialRole())));
        hearing.setDefenceCounsels(new HashSet(Arrays.asList(mockDefenceCounsel())));
        hearing.setProsecutionCounsels(new HashSet(Arrays.asList(mockProsecutionCounsel())));
        hearing.setCourtApplicationsJson("{}");

        return hearing;
    }

    private Hearing mockHearing1(final UUID hearingId, final int year, final int month, final int day, final int sequence) throws IOException {

        final Hearing hearing = new Hearing();
        hearing.setId(randomUUID());
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet(Arrays.asList(mockProsecutionCase())));
        hearing.setCourtCentre(mockCourtCentre());
        hearing.setHearingType(mockHearingType());
        final Set<HearingDay> hearingDays1 = generateHearingDays(hearing.getId(), year, month, day, sequence);
        hearing.setHearingDays(hearingDays1);
        hearing.setJudicialRoles(new HashSet(Arrays.asList(mockJudicialRole())));
        hearing.setDefenceCounsels(new HashSet(Arrays.asList(mockDefenceCounsel())));
        hearing.setProsecutionCounsels(new HashSet(Arrays.asList(mockProsecutionCounsel())));
        hearing.setCourtApplicationsJson("{}");
        return hearing;
    }

    private ProsecutionCase mockProsecutionCase(){
        final HearingSnapshotKey hearingSnapshotKey = new HearingSnapshotKey();
        hearingSnapshotKey.setId(randomUUID());

        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = new ProsecutionCaseIdentifier();
        prosecutionCaseIdentifier.setCaseURN("TFL102345");

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setProsecutionCaseIdentifier(prosecutionCaseIdentifier);
        prosecutionCase.setId(hearingSnapshotKey);

        Person person = new Person();
        person.setFirstName("Mark");
        person.setLastName("Taylor");
        PersonDefendant personDefendant = new PersonDefendant();
        personDefendant.setPersonDetails(person);

        Defendant defendant = new Defendant();
        defendant.setPersonDefendant(personDefendant);

        prosecutionCase.setDefendants(new HashSet(Arrays.asList(defendant)));

        return prosecutionCase;
    }

    private HearingDay mockHearingDay(final int days){
        final HearingDay hearingDay = new HearingDay();
        hearingDay.setDate(LocalDate.now().plusDays(days));
        return hearingDay;
    }

    private CourtCentre mockCourtCentre(){
        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setId(COURT_CENTRE_ID);
        courtCentre.setRoomId(COURT_ROOM_ID);
        return courtCentre;
    }

    private JudicialRole mockJudicialRole(){
        final JudicialRole judicialRole = new JudicialRole();
        judicialRole.setJudicialId(randomUUID());
        return judicialRole;
    }

    private HearingType mockHearingType(){
        final HearingType hearingType = new HearingType();
        hearingType.setDescription("sentence");
        return hearingType;
    }

    private HearingDefenceCounsel mockDefenceCounsel() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        HearingDefenceCounsel hearingDefenceCounsel = new HearingDefenceCounsel();
        String jsonStr = "{ \"firstName\" : \"James\", \"lastName\" : \"Peter\"," +
                " \"attendanceDays\" : [\"2022-10-12\", \"2022-10-13\", \"2023-07-24\"] }";
        hearingDefenceCounsel.setPayload(objectMapper.readValue(jsonStr, JsonNode.class));
        return hearingDefenceCounsel;
    }

    private HearingProsecutionCounsel mockProsecutionCounsel() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        HearingProsecutionCounsel hearingProsecutionCounsel = new HearingProsecutionCounsel();
        String jsonStr = "{ \"firstName\" : \"James\", \"lastName\" : \"Peter\"," +
                " \"attendanceDays\" : [\"2022-10-12\", \"2022-10-13\", \"2023-07-24\"] }";
        hearingProsecutionCounsel.setPayload(objectMapper.readValue(jsonStr, JsonNode.class));
        return hearingProsecutionCounsel;
    }


    private HearingEvent mockHearingEvent(){
        final HearingEvent hearingEvent = new HearingEvent();
        hearingEvent.setUserId(randomUUID());
        hearingEvent.setRecordedLabel("hearing started-3");
        hearingEvent.setEventTime(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")));
        hearingEvent.setNote("note3");
        return hearingEvent;
    }

    private HearingEvent mockHearingEventNoUserId(){
        final HearingEvent hearingEvent = new HearingEvent();
        hearingEvent.setRecordedLabel("hearing started-3");
        hearingEvent.setEventTime(now());
        hearingEvent.setNote("note3");
        return hearingEvent;
    }


    private CourtApplication getCourtApplication(UUID applicationId) {

        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicationReference(STRING.next())
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withPersonDetails(person().withFirstName(STRING.next()).withLastName(STRING.next()).build())
                        .build())
                .withRespondents(ImmutableList.of(CourtApplicationParty.courtApplicationParty()
                        .withPersonDetails(person().withFirstName(STRING.next()).withLastName(STRING.next()).build())
                        .build()))
                .withThirdParties(ImmutableList.of(CourtApplicationParty.courtApplicationParty()
                        .withPersonDetails(person().withFirstName(STRING.next()).withLastName(STRING.next()).build())
                        .build()))
                .build();
    }

    private Hearing mockHearingv1(final UUID hearingId, final int days) throws IOException {

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet(Arrays.asList(mockProsecutionCase())));
        hearing.setCourtCentre(mockCourtCentre());
        hearing.setHearingType(mockHearingType());
        hearing.setHearingDays(new HashSet(Arrays.asList(mockHearingDay(days))));
        hearing.setJudicialRoles(new HashSet(Arrays.asList(mockJudicialRole())));
        hearing.setDefenceCounsels(new HashSet(Arrays.asList(mockDefenceCounsel())));
        hearing.setProsecutionCounsels(new HashSet(Arrays.asList(mockProsecutionCounsel())));
        hearing.setCourtApplicationsJson((FileUtil.getPayload("court-applications-v1.json")));

        return hearing;
    }

    private static Set<HearingDay> generateHearingDays(final UUID hearingId, final int year, final int month, int day, final int sequence) {

        final Set<HearingDay> hearingDays = new HashSet<>(); //add 5 days

        final HearingDay hearingDay1 = getHearingDay(hearingId, year, month, day++, sequence);
        final HearingDay hearingDay2 = getHearingDay(hearingId, year, month, day++, sequence);
        final HearingDay hearingDay3 = getHearingDay(hearingId, year, month, day++, sequence);
        final HearingDay hearingDay4 = getHearingDay(hearingId, year, month, day++, sequence);
        final HearingDay hearingDay5 = getHearingDay(hearingId, year, month, day++, sequence);

        hearingDays.add(hearingDay1);
        hearingDays.add(hearingDay2);
        hearingDays.add(hearingDay3);
        hearingDays.add(hearingDay4);
        hearingDays.add(hearingDay5);

        return hearingDays;
    }

    private static HearingDay getHearingDay(final UUID hearingId, final int year, final int month, final int day, final int sequence) {
        final HearingDay hearingDay = new HearingDay();
        hearingDay.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        hearingDay.setListingSequence(sequence);

        final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.parse("11:00:11.297"), ZoneId.of("UTC"));
        hearingDay.setDate(zonedDateTime.toLocalDate());
        hearingDay.setSittingDay(zonedDateTime);
        hearingDay.setDateTime(zonedDateTime);

        return hearingDay;
    }

}