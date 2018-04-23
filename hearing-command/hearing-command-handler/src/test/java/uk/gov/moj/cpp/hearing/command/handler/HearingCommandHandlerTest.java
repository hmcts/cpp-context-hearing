package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.ResultLine;
import uk.gov.moj.cpp.hearing.domain.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
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
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_UTC_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@Ignore("GPE-3390") //FIXME: GPE-3390 refactor
@RunWith(MockitoJUnitRunner.class)
public class HearingCommandHandlerTest {

    private static final String SAVE_DRAFT_RESULT_COMMAND = "hearing.save-draft-result";
    private static final String HEARING_SHARE_RESULTS_COMMAND = "hearing.share-results";

    private static final String HEARING_DRAFT_RESULT_SAVED_EVENT = "hearing.draft-result-saved";
    private static final String HEARING_RESULTS_SHARED_EVENT = "hearing.results-shared";
    private static final String HEARING_RESULT_AMENDED_EVENT = "hearing.result-amended";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_PROMPTS = "prompts";
    private static final String FIELD_PROMPT_LABEL = "label";
    private static final String FIELD_PROMPT_VALUE = "value";
    private static final String FIELD_SHARED_TIME = "sharedTime";
    private static final String FIELD_COURT = "court";
    private static final String FIELD_COURT_ROOM = "courtRoom";
    private static final String FIELD_CLERK_OF_THE_COURT_ID = "clerkOfTheCourtId";
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME = "clerkOfTheCourtFirstName";
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME = "clerkOfTheCourtLastName";

    private static final String FIELD_COURT_VALUE = STRING.next();
    private static final String FIELD_COURT_ROOM_VALUE = STRING.next();
    private static final UUID FIELD_CLERK_OF_THE_COURT_ID_VALUE = randomUUID();
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE = STRING.next();
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE = STRING.next();

    private static final UUID GENERIC_ID = randomUUID();
    private static final UUID GENERIC_ID_2 = randomUUID();
    private static final UUID GENERIC_ID_3 = randomUUID();
    private static final UUID GENERIC_ID_4 = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID TARGET_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();
    private static final String ARBITRARY_STRING_IMP_2_YRS = "imp 2 yrs";

    private static final UUID PERSON_ID = randomUUID();

    private static final ZonedDateTime SHARED_TIME = PAST_UTC_DATE_TIME.next();
    private static final ZonedDateTime SHARED_TIME_2 = SHARED_TIME.plusMinutes(5);
    private static final UUID CASE_ID = randomUUID();

    private static final String LEVEL = "OFFENCE";
    private static final String RESULT_LABEL = "Imprisonment";
    private static final String PROMPT_LABEL_1 = "Imprisonment duration";
    private static final String PROMPT_VALUE_1 = "1 year 6 months";
    private static final String PROMPT_LABEL_2 = "Prison";
    private static final String PROMPT_VALUE_2 = "Wormwood Scrubs";

    private static final String RESULT_LABEL_2 = "Compensation";
    private static final String PROMPT_LABEL_3 = "Amount of compensation";
    private static final String PROMPT_VALUE_3 = "£100";
    private static final String PROMPT_LABEL_4 = "Creditor name";
    private static final String PROMPT_VALUE_4 = "Roger Stokes";

    @Mock
    private EventStream hearingPleaEventStream;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DraftResultSaved.class,
            ResultsShared.class,
            ResultAmended.class
    );

    @InjectMocks
    private HearingCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper",
                new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper",
                new ObjectMapperProducer().objectMapper());

        when(this.eventSource.getStreamById(HEARING_ID)).thenReturn(this.hearingEventStream);
        when(this.eventSource.getStreamById(CASE_ID)).thenReturn(this.hearingPleaEventStream);
        //TODO - GPE-3032 CLEANUP - when we get rid of hearingPlea, we can revert this to use the original CASE_ID
        when(this.eventSource.getStreamById(new UUID(CASE_ID.getLeastSignificantBits(), CASE_ID.getMostSignificantBits()))).thenReturn(this.caseEventStream);

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(new HearingAggregate());
        when(this.aggregateService.get(this.hearingPleaEventStream, CaseAggregate.class)).thenReturn(new CaseAggregate());
    }


    @Test
    public void shouldRaiseDraftResultSaved() throws Exception {
        final JsonEnvelope command = createSaveDraftResultCommand();

        this.hearingCommandHandler.saveDraftResult(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_DRAFT_RESULT_SAVED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_DEFENDANT_ID), equalTo(DEFENDANT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_DRAFT_RESULT), equalTo(ARBITRARY_STRING_IMP_2_YRS)),
                                withJsonPath(format("$.%s", FIELD_TARGET_ID), equalTo(TARGET_ID.toString()))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseResultsSharedEventIfSharingForTheFirstTime() throws Exception {
        final JsonEnvelope command = prepareResultsToShareCommand();

        this.hearingCommandHandler.shareResult(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_RESULTS_SHARED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(SHARED_TIME))),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_GENERIC_ID), equalTo(GENERIC_ID.toString())),
                                withoutJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_LAST_SHARED_RESULT_ID)),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_LEVEL), equalTo(LEVEL)),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_COURT), equalTo(FIELD_COURT_VALUE)),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_COURT_ROOM), equalTo(FIELD_COURT_ROOM_VALUE)),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CLERK_OF_THE_COURT_ID), equalTo(FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_RESULT_LABEL), equalTo(RESULT_LABEL)),
                                withJsonPath(format("$.%s[0].%s[0].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_1)),
                                withJsonPath(format("$.%s[0].%s[0].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_1)),
                                withJsonPath(format("$.%s[0].%s[1].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_2)),
                                withJsonPath(format("$.%s[0].%s[1].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_2))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseResultAmendedEventsForNewAndUpdatedResultsOnlyOnSubsequentSharingOfResults() throws Exception {
        final JsonEnvelope command = prepareAmendedResultsToShareCommand();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new ResultsShared(HEARING_ID, SHARED_TIME, prepareResultLines(), null, null, null, null)); //FIXME: GPE-3390
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        this.hearingCommandHandler.shareResult(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_RESULT_AMENDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(SHARED_TIME_2))),
                                withJsonPath(format("$.%s", FIELD_GENERIC_ID), equalTo(GENERIC_ID_2.toString())),
                                withJsonPath(format("$.%s", FIELD_LAST_SHARED_RESULT_ID), equalTo(GENERIC_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_LEVEL), equalTo(LEVEL)),
                                withJsonPath(format("$.%s", FIELD_COURT), equalTo(FIELD_COURT_VALUE)),
                                withJsonPath(format("$.%s", FIELD_COURT_ROOM), equalTo(FIELD_COURT_ROOM_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_ID), equalTo(FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())),
                                withJsonPath(format("$.%s", FIELD_RESULT_LABEL), equalTo(RESULT_LABEL)),
                                withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_1)),
                                withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_3)),
                                withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_2)),
                                withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_4))
                        ))
                ).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_RESULT_AMENDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(SHARED_TIME_2))),
                                withJsonPath(format("$.%s", FIELD_GENERIC_ID), equalTo(GENERIC_ID_3.toString())),
                                withoutJsonPath(format("$.%s", FIELD_LAST_SHARED_RESULT_ID)),
                                withJsonPath(format("$.%s", FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_LEVEL), equalTo(LEVEL)),
                                withJsonPath(format("$.%s", FIELD_RESULT_LABEL), equalTo(RESULT_LABEL_2)),
                                withJsonPath(format("$.%s", FIELD_COURT), equalTo(FIELD_COURT_VALUE)),
                                withJsonPath(format("$.%s", FIELD_COURT_ROOM), equalTo(FIELD_COURT_ROOM_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_ID), equalTo(FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_3)),
                                withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_3)),
                                withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_4)),
                                withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_4))
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldFilterEarlierSharedResultsBeforeRaisingAnyResultAmendedEvents() throws Exception {
        final JsonEnvelope command = prepareAmendedResultsToShareCommand();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new ResultsShared(HEARING_ID, SHARED_TIME, prepareResultLines(), null, null, null, null)); //FIXME: GPE-3390
        prepareAmendedResults().forEach(hearingAggregate::apply);

        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        this.hearingCommandHandler.shareResult(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_RESULT_AMENDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(SHARED_TIME_2))),
                                withJsonPath(format("$.%s", FIELD_GENERIC_ID), equalTo(GENERIC_ID_3.toString())),
                                withoutJsonPath(format("$.%s", FIELD_LAST_SHARED_RESULT_ID)),
                                withJsonPath(format("$.%s", FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_LEVEL), equalTo(LEVEL)),
                                withJsonPath(format("$.%s", FIELD_RESULT_LABEL), equalTo(RESULT_LABEL_2)),
                                withJsonPath(format("$.%s", FIELD_COURT), equalTo(FIELD_COURT_VALUE)),
                                withJsonPath(format("$.%s", FIELD_COURT_ROOM), equalTo(FIELD_COURT_ROOM_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)),
                                withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_ID), equalTo(FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())),
                                withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_3)),
                                withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_3)),
                                withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_4)),
                                withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_4))
                        ))
                ).thatMatchesSchema()
        ));
    }

    private JsonEnvelope createSaveDraftResultCommand() {
        return envelope()
                .with(metadataWithRandomUUID(SAVE_DRAFT_RESULT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(DEFENDANT_ID, FIELD_DEFENDANT_ID)
                .withPayloadOf(TARGET_ID, FIELD_TARGET_ID)
                .withPayloadOf(OFFENCE_ID, FIELD_OFFENCE_ID)
                .withPayloadOf(ARBITRARY_STRING_IMP_2_YRS, FIELD_DRAFT_RESULT)
                .build();
    }

    private JsonEnvelope prepareResultsToShareCommand() {
        final JsonArrayBuilder resultLines = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, GENERIC_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_COURT, FIELD_COURT_VALUE)
                        .add(FIELD_COURT_ROOM, FIELD_COURT_ROOM_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_ID, FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_1))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_2))));

        final JsonObject shareResult = createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(SHARED_TIME))
                .add(FIELD_RESULT_LINES, resultLines)
                .build();

        return envelopeFrom(metadataWithRandomUUID(HEARING_SHARE_RESULTS_COMMAND), shareResult);
    }

    private JsonEnvelope prepareAmendedResultsToShareCommand() {
        final JsonArrayBuilder resultLines = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, GENERIC_ID_2.toString())
                        .add(FIELD_LAST_SHARED_RESULT_ID, GENERIC_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_COURT, FIELD_COURT_VALUE)
                        .add(FIELD_COURT_ROOM, FIELD_COURT_ROOM_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_ID, FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_3))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_4))))
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, GENERIC_ID_3.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL_2)
                        .add(FIELD_COURT, FIELD_COURT_VALUE)
                        .add(FIELD_COURT_ROOM, FIELD_COURT_ROOM_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_ID, FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_3)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_3))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_4)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_4))))
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, GENERIC_ID_4.toString())
                        .add(FIELD_LAST_SHARED_RESULT_ID, GENERIC_ID_4.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_COURT, FIELD_COURT_VALUE)
                        .add(FIELD_COURT_ROOM, FIELD_COURT_ROOM_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_ID, FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_1))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_2))));

        final JsonObject shareResult = createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(SHARED_TIME_2))
                .add(FIELD_RESULT_LINES, resultLines)
                .build();

        return envelopeFrom(metadataWithRandomUUID(HEARING_SHARE_RESULTS_COMMAND), shareResult);
    }

    private List<ResultLine> prepareResultLines() {
        return newArrayList(
                new ResultLine(GENERIC_ID, null, CASE_ID, PERSON_ID, OFFENCE_ID, LEVEL, RESULT_LABEL,
                        newArrayList(new ResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1),
                                new ResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2)),
                        FIELD_COURT_VALUE, FIELD_COURT_ROOM_VALUE, FIELD_CLERK_OF_THE_COURT_ID_VALUE, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE),
                new ResultLine(GENERIC_ID_4, null, CASE_ID, PERSON_ID, OFFENCE_ID, LEVEL, RESULT_LABEL,
                        newArrayList(new ResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1),
                                new ResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2)),
                        FIELD_COURT_VALUE, FIELD_COURT_ROOM_VALUE, FIELD_CLERK_OF_THE_COURT_ID_VALUE, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
        );
    }


    private List<ResultAmended> prepareAmendedResults() {
        return newArrayList(new ResultAmended(GENERIC_ID_2, GENERIC_ID, SHARED_TIME_2, HEARING_ID, CASE_ID, PERSON_ID, OFFENCE_ID, LEVEL, RESULT_LABEL,
                newArrayList(
                        new ResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1),
                        new ResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2)), FIELD_COURT_VALUE, FIELD_COURT_ROOM_VALUE, FIELD_CLERK_OF_THE_COURT_ID_VALUE, FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE, FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
        );
    }
}

