package uk.gov.moj.cpp.hearing.query.view;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonassert.impl.matcher.IsEmptyCollection.empty;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@SuppressWarnings({"unchecked", "unused"})
@RunWith(MockitoJUnitRunner.class)
public class HearingEventQueryViewTest {

    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS_VERSION_TWO = "hearing.get-hearing-event-definitions.v2";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_WITNESS_ID = "witnessId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_HEARING_EVENTS = "events";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_CASE_ATTRIBUTES = "caseAttributes";
    private static final String FIELD_DEFENDANT_NAME = "defendant.name";
    private static final String FIELD_COUNSEL_NAME = "counsel.name";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_SEQUENCE_TYPE = "type";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";
    private static final String FIELD_ALTERABLE = "alterable";

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID WITNESS_ID = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime LAST_MODIFIED_TIME = PAST_ZONED_DATE_TIME.next();
    private static final String ACTION_LABEL = STRING.next();
    private static final Integer SEQUENCE = 1;
    private static final boolean ALTERABLE = BOOLEAN.next();

    private static final UUID HEARING_EVENT_ID_2 = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID_2 = randomUUID();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final ZonedDateTime EVENT_TIME_2 = EVENT_TIME.plusMinutes(1);
    private static final ZonedDateTime LAST_MODIFIED_TIME_2 = PAST_ZONED_DATE_TIME.next();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final Integer SEQUENCE_2 = 2;
    private static final boolean ALTERABLE_2 = BOOLEAN.next();

    private static final List<String> CASE_ATTRIBUTES = newArrayList("defendant.name", "counsel.name");
    private static final UUID PERSON_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID PERSON_ID_2 = randomUUID();
    private static final UUID DEFENDANT_ID_2 = randomUUID();

    private static final UUID HEARING_EVENT_DEFINITION_ID_3 = randomUUID();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final String ACTION_LABEL_3 = STRING.next();
    private static final boolean ALTERABLE_3 = BOOLEAN.next();

    private static final String GROUP_TYPE = STRING.next();
    private static final String GROUP_LABEL = STRING.next();
    private static final String ACTION_LABEL_EXTENSION = STRING.next();

    @Spy
    private Enveloper enveloper = createEnveloper();

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
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID)).thenReturn(hearingEvents());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString()).build());

        final JsonEnvelope actualHearingEventLog = hearingEventQueryView.getHearingEventLog(query);
        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_EVENT_TIME), equalTo(ZonedDateTimes.toString(EVENT_TIME))),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_LAST_MODIFIED_TIME), equalTo(ZonedDateTimes.toString(LAST_MODIFIED_TIME))),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_ALTERABLE), equalTo(ALTERABLE)),
                        withJsonPath(format("$.%s[0].%s", FIELD_HEARING_EVENTS, FIELD_WITNESS_ID), equalTo(WITNESS_ID.toString())),

                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_EVENT_TIME), equalTo(ZonedDateTimes.toString(EVENT_TIME_2))),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_LAST_MODIFIED_TIME), equalTo(ZonedDateTimes.toString(LAST_MODIFIED_TIME_2))),
                        withJsonPath(format("$.%s[1].%s", FIELD_HEARING_EVENTS, FIELD_ALTERABLE), equalTo(ALTERABLE_2))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldReturnEmptyEventLogWhenThereAreNoEventsByHearingId() {
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID)).thenReturn(emptyList());

        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUIDAndName(),
                createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString()).build());

        final JsonEnvelope actualHearingEventLog = hearingEventQueryView.getHearingEventLog(query);

        assertThat(actualHearingEventLog, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_LOG),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENTS), empty())
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetAllHearingEventDefinitionsWhichDoNotHaveCaseAttributes() {
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionsWithoutCaseAttributes());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .build());

        final JsonEnvelope actualHearingEventDefinitions = hearingEventQueryView.getHearingEventDefinitions(query);

        assertThat(actualHearingEventDefinitions, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetAllHearingEventDefinitionsWithCaseAttributes() {

        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionsWithCaseAttributes());

        Hearing hearing = Hearing.builder()
                .withId(HEARING_ID)
                .addAttendee(DefenceAdvocate.builder()
                        .withId(new HearingSnapshotKey(randomUUID(), HEARING_ID))
                        .withPersonId(PERSON_ID)
                        .addDefendant(Defendant.builder()
                                .withId(new HearingSnapshotKey(DEFENDANT_ID, HEARING_ID))
                                .build())
                        .build())
                .addAttendee(DefenceAdvocate.builder()
                        .withId(new HearingSnapshotKey(randomUUID(), HEARING_ID))
                        .withPersonId(PERSON_ID_2)
                        .addDefendant(Defendant.builder()
                                .withId(new HearingSnapshotKey(DEFENDANT_ID_2, HEARING_ID))
                                .build())
                        .build())
                .build();
        when(hearingRepository.findById(HEARING_ID)).thenReturn(hearing);

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .build());

        final JsonEnvelope actualHearingEventDefinitions = hearingEventQueryView.getHearingEventDefinitions(query);

        assertThat(actualHearingEventDefinitions, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES), hasSize(2)),
                        withJsonPath(format("$.%s[1].%s[*].['%s']", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES, FIELD_DEFENDANT_NAME), hasItems(DEFENDANT_ID.toString(), DEFENDANT_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s[*].['%s']", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES, FIELD_COUNSEL_NAME), hasItems(PERSON_ID.toString(), PERSON_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION), is(ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingEventDefinitionsWithOutOfSequenceEvents() {
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionsWithOutOfSequenceEvent());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .build());

        final JsonEnvelope actualHearingEventDefinitions = hearingEventQueryView.getHearingEventDefinitions(query);

        assertThat(actualHearingEventDefinitions, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(3)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2)),

                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_3.toString())),
                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_3)),
                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_3)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_3))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingEventDefinitionById() {
        when(hearingEventDefinitionRepository.findBy(HEARING_EVENT_DEFINITION_ID)).thenReturn(prepareHearingEventDefinition());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, HEARING_EVENT_DEFINITION_ID.toString())
                .build());

        final JsonEnvelope actualHearingEventDefinition = hearingEventQueryView.getHearingEventDefinition(query);

        assertThat(actualHearingEventDefinition, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITION),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s.%s", FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s.%s", FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s", FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s", FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s", FIELD_ACTION_LABEL_EXTENSION)),
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
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS_VERSION_TWO),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
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
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS_VERSION_TWO),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(2)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES), hasSize(2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL), is(GROUP_LABEL)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION), is(ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2))
                ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldGetHearingEventDefinitionsVersionTwoWithOutOfSequenceEvents() {
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionsWithOutOfSequenceEvent());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder()
                .build());

        final JsonEnvelope actualHearingEventDefinitions = hearingEventQueryView.getHearingEventDefinitionsVersionTwo(query);

        assertThat(actualHearingEventDefinitions, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query)
                        .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS_VERSION_TWO),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_EVENT_DEFINITIONS), hasSize(3)),

                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE)),
                        withJsonPath(format("$.%s[0].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[0].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE)),

                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_2.toString())),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_GENERIC_ID), is(SEQUENCE_2)),
                        withJsonPath(format("$.%s[1].%s.%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE, FIELD_SEQUENCE_TYPE), is(GROUP_TYPE)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[1].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_2)),

                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_GENERIC_ID), is(HEARING_EVENT_DEFINITION_ID_3.toString())),
                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL), is(ACTION_LABEL_3)),
                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_RECORDED_LABEL), is(RECORDED_LABEL_3)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_SEQUENCE)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_CASE_ATTRIBUTES)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_GROUP_LABEL)),
                        withoutJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ACTION_LABEL_EXTENSION)),
                        withJsonPath(format("$.%s[2].%s", FIELD_EVENT_DEFINITIONS, FIELD_ALTERABLE), is(ALTERABLE_3))
                ))).thatMatchesSchema()
        ));
    }


    private HearingEventDefinition prepareHearingEventDefinition() {
        return new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, ACTION_LABEL, SEQUENCE, GROUP_TYPE, null, null, null, ALTERABLE);
    }

    private List<HearingEvent> hearingEvents() {
        final List<HearingEvent> hearingEvents = new ArrayList<>();
        hearingEvents.add(
                HearingEvent.hearingEvent()
                .setId(HEARING_EVENT_ID)
                .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID)
                .setHearingId(HEARING_ID)
                .setRecordedLabel(RECORDED_LABEL)
                .setEventTime(EVENT_TIME)
                .setLastModifiedTime(LAST_MODIFIED_TIME)
                .setAlterable(ALTERABLE)
                .setWitnessId(WITNESS_ID)
        );
        hearingEvents.add(
                HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID_2)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID_2)
                        .setHearingId(HEARING_ID)
                        .setRecordedLabel(RECORDED_LABEL_2)
                        .setEventTime(EVENT_TIME_2)
                        .setLastModifiedTime(LAST_MODIFIED_TIME_2)
                        .setAlterable(ALTERABLE_2)
                        .setWitnessId(WITNESS_ID)
        );

        return hearingEvents;
    }

    private List<HearingEventDefinition> hearingEventDefinitionsWithoutCaseAttributes() {
        return newArrayList(
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, ACTION_LABEL, SEQUENCE, GROUP_TYPE, null, null, null, ALTERABLE),
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID_2, RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_2, GROUP_TYPE, null, null, null, ALTERABLE_2)
        );
    }

    private List<HearingEventDefinition> hearingEventDefinitionsWithCaseAttributes() {
        return newArrayList(
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, ACTION_LABEL, SEQUENCE, GROUP_TYPE, null, null, null, ALTERABLE),
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID_2, RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_2, GROUP_TYPE, CASE_ATTRIBUTES.stream().collect(joining(",")), GROUP_LABEL, ACTION_LABEL_EXTENSION, ALTERABLE_2)
        );
    }


    private List<HearingEventDefinition> hearingEventDefinitionsWithOutOfSequenceEvent() {
        return newArrayList(
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, ACTION_LABEL, SEQUENCE, GROUP_TYPE, null, null, null, ALTERABLE),
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID_2, RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_2, GROUP_TYPE, null, null, null, ALTERABLE_2),
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID_3, RECORDED_LABEL_3, ACTION_LABEL_3, null, null, null, null, null, ALTERABLE_3)
        );
    }

}