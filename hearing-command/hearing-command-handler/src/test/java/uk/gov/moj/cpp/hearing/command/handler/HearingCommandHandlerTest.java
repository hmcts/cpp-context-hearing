package uk.gov.moj.cpp.hearing.command.handler;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.ResultLine;
import uk.gov.moj.cpp.hearing.domain.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingsPleaAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CaseCreated;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.JudgeAssigned;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.PleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.domain.event.VerdictAdded;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Resources.getResource;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_UTC_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandHandlerTest {

    private static final String INITIATE_HEARING_COMMAND = "hearing.initiate-hearing";
    private static final String ALLOCATE_COURT_COMMAND = "hearing.allocate-court";
    private static final String BOOK_ROOM_COMMAND = "hearing.book-room";
    private static final String ADD_CASE_COMMAND = "hearing.add-case";
    private static final String ADD_PROSECUTION_COUNSEL_COMMAND = "hearing.add-prosecution-counsel";
    private static final String ADD_DEFENCE_COUNSEL_COMMAND = "hearing.add-defence-counsel";
    private static final String ADJOURN_DATE_COMMAND = "hearing.adjourn-date";
    private static final String SAVE_DRAFT_RESULT_COMMAND = "hearing.save-draft-result";
    private static final String HEARING_SHARE_RESULTS_COMMAND = "hearing.share-results";

    private static final String HEARING_INITIATED_EVENT = "hearing.hearing-initiated";
    private static final String CASE_ASSOCIATED_EVENT = "hearing.case-associated";
    private static final String COURT_ASSIGNED_EVENT = "hearing.court-assigned";
    private static final String JUDGE_ASSIGNED_EVENT = "hearing.judge-assigned";
    private static final String ROOM_BOOKED_EVENT = "hearing.room-booked";
    private static final String PROSECUTION_COUNSEL_ADDED_EVENT = "hearing.prosecution-counsel-added";
    private static final String NEWPROSECUTION_COUNSEL_ADDED_EVENT = "hearing.newprosecution-counsel-added";
    private static final String DEFENCE_COUNSEL_ADDED_EVENT = "hearing.defence-counsel-added";
    private static final String NEWDEFENCE_COUNSEL_ADDED_EVENT = "hearing.newdefence-counsel-added";
    private static final String ADJOURN_DATE_UPDATED_EVENT = "hearing.adjourn-date-updated";
    private static final String HEARING_DRAFT_RESULT_SAVED_EVENT = "hearing.draft-result-saved";
    private static final String HEARING_RESULTS_SHARED_EVENT = "hearing.results-shared";
    private static final String HEARING_RESULT_AMENDED_EVENT = "hearing.result-amended";
    private static final String HEARING_UPDATE_PLEA = "hearing.update-plea";
    private static final String HEARING_UPDATE_VERDICT = "hearing.update-verdict";
    private static final String HEARING_CONVICTION_DATE_ADDED = "hearing.conviction-date-added";
    private static final String HEARING_VERDICT_ADDED = "hearing.verdict-added";
    private static final String HEARING_PLEA_ADD = "hearing.plea-add";
    private static final String HEARING_PLEA_CHANGE = "hearing.plea-change";
    private static final String HEARING_PLEA_ADDED = "hearing.plea-added";
    private static final String HEARING_PLEA_CHANGED = "hearing.plea-changed";
    private static final String HEARING_CONVICTION_DATE_REMOVED = "hearing.conviction-date-removed";
    private static final String PLEA_ADDED = "hearing.case.plea-added";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING = "hearing";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_URN = "urn";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_ATTENDEE_ID = "attendeeId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_TITLE= "title";
    private static final String FIELD_FIRST_NAME="firstName";
    private static final String FIELD_LAST_NAME="lastName";
    private static final String FIELD_DEFENDANT_IDS = "defendantIds";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
    private static final String FIELD_START_DATE = "startDate";
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
    private static final String FIELD_JUDGE_ID = "judgeId";
    private static final String FIELD_JUDGE_TITLE = "judgeTitle";
    private static final String FIELD_JUDGE_FIRST_NAME = "judgeFirstName";
    private static final String FIELD_JUDGE_LAST_NAME = "judgeLastName";
    private static final String FIELD_CONVICTION_DATE = "convictionDate";

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
    private static final String FIRST_NAME = STRING.next();
    private static final String LAST_NAME = STRING.next();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID TARGET_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();
    private static final String ARBITRARY_STRING_IMP_2_YRS = "imp 2 yrs";
    private static final UUID DEFENDANT_ID_2 = randomUUID();

    private static final UUID PERSON_ID = randomUUID();
    private static final UUID ATTENDEE_ID = randomUUID();
    private static final String STATUS = STRING.next();
    private static final String TITLE = STRING.next();

    private static final ZonedDateTime START_DATE_TIME = PAST_UTC_DATE_TIME.next();
    private static final ZonedDateTime SHARED_TIME = PAST_UTC_DATE_TIME.next();
    private static final ZonedDateTime SHARED_TIME_2 = SHARED_TIME.plusMinutes(5);
    private static final Integer DURATION = INTEGER.next();
    private static final String HEARING_TYPE = STRING.next();
    private static final UUID CASE_ID = randomUUID();
    private static final UUID ROOM_ID = randomUUID();
    private static final String ROOM_NAME = STRING.next();
    private static final UUID COURT_CENTRE_ID = randomUUID();
    private static final String COURT_CENTRE_NAME = STRING.next();
    private static final String START_DATE = PAST_LOCAL_DATE.next().toString();
    private static final String JUDGE_ID = STRING.next();
    private static final String JUDGE_TITLE = STRING.next();
    private static final String JUDGE_FIRST_NAME = STRING.next();
    private static final String JUDGE_LAST_NAME = STRING.next();


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

    private static final String PLEA_ID = UUID.randomUUID().toString();
    private static final String VERDICT_ID = UUID.randomUUID().toString();
    private static final String VERDICT_CATEGORY = "GUILTY";
    private static final String PLEA_DATE = "2017-02-02";
    private static final String PLEA_GUILTY = "GUILTY";
    private static final String PLEA_NOT_GUILTY = "NOT GUILTY";
    public static final String HEARING_HEARING_PLEA_UPDATED = "hearing.hearing-plea-updated";
    public static final String HEARING_HEARING_VERDICT_UPDATED = "hearing.hearing-verdict-updated";
    private static final String VERDICT_DATE = "2017-02-02";

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
            //new events.
            Initiated.class,
            CaseCreated.class,
            OffencePleaUpdated.class,
            //TODO - GPE-3032 CLEANUP - remove old events.
            DraftResultSaved.class, HearingInitiated.class, CaseAssociated.class, CourtAssigned.class,
            RoomBooked.class, ProsecutionCounselAdded.class, NewProsecutionCounselAdded.class,
            DefenceCounselAdded.class, NewDefenceCounselAdded.class,
            HearingAdjournDateUpdated.class, ResultsShared.class, ResultAmended.class, PleaAdded.class, PleaChanged.class,
            HearingPleaAdded.class, HearingPleaChanged.class, HearingPleaUpdated.class, JudgeAssigned.class,
            VerdictAdded.class, ConvictionDateAdded.class, HearingVerdictUpdated.class, ConvictionDateRemoved.class);

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
        when(this.aggregateService.get(this.hearingPleaEventStream, HearingsPleaAggregate.class)).thenReturn(new HearingsPleaAggregate());
    }

    @Test
    public void shouldRaiseHearingInitiatedEventWhenOnlyRequiredFieldsAreAvailable() throws Exception {
        final JsonEnvelope command = createInitiateHearingCommandWithOnlyRequiredFields();

        this.hearingCommandHandler.initiateHearing(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_INITIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startDateTime", is(ZonedDateTimes.toString(START_DATE_TIME))),
                                withJsonPath("$.duration", equalTo(DURATION)),
                                withJsonPath("$.hearingType", equalTo(HEARING_TYPE))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseMultipleEventsWhenAllTheFieldsAreAvailableForInitiateHearing() throws Exception {
        final JsonEnvelope command = createInitiateHearingCommand();

        this.hearingCommandHandler.initiateHearing(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_INITIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startDateTime", is(ZonedDateTimes.toString(START_DATE_TIME))),
                                withJsonPath("$.duration", equalTo(DURATION)),
                                withJsonPath("$.hearingType", equalTo(HEARING_TYPE))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(CASE_ASSOCIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.caseId", equalTo(CASE_ID.toString()))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(COURT_ASSIGNED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.courtCentreName", equalTo(COURT_CENTRE_NAME)),
                                withJsonPath("$.courtCentreId", equalTo(COURT_CENTRE_ID.toString()))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ROOM_BOOKED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.roomName", equalTo(ROOM_NAME)),
                                withJsonPath("$.roomId", equalTo(ROOM_ID.toString()))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(JUDGE_ASSIGNED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.id", equalTo(JUDGE_ID)),
                                withJsonPath("$.firstName", equalTo(JUDGE_FIRST_NAME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseCourtAssignedEvent() throws Exception {
        final JsonEnvelope command = createAllocateCourtCommand();

        this.hearingCommandHandler.allocateCourt(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(COURT_ASSIGNED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.courtCentreName", equalTo(COURT_CENTRE_NAME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseRoomBookedEvent() throws Exception {
        final JsonEnvelope command = createBookRoomCommand();

        this.hearingCommandHandler.bookRoom(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ROOM_BOOKED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.roomName", equalTo(ROOM_NAME))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseHearingAdjournDateUpdatedEvent() throws Exception {
        final JsonEnvelope command = createAdjournHearingDateCommand();

        this.hearingCommandHandler.adjournHearingDate(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ADJOURN_DATE_UPDATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.startDate", is(START_DATE))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseCaseAssociatedEvent() throws Exception {
        final JsonEnvelope command = createAddCaseCommand();

        this.hearingCommandHandler.addCase(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(CASE_ASSOCIATED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.caseId", equalTo(CASE_ID.toString()))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseProsecutionCounselAddedEvent() throws Exception {
        final JsonEnvelope command = createAddProsecutionCounselCommand();

        this.hearingCommandHandler.addProsecutionCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(PROSECUTION_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", equalTo(PERSON_ID.toString())),
                                withJsonPath("$.attendeeId", equalTo(ATTENDEE_ID.toString())),
                                withJsonPath("$.status", equalTo(STATUS)),
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString()))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(NEWPROSECUTION_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.personId", equalTo(PERSON_ID.toString())),
                                withJsonPath("$.attendeeId", equalTo(ATTENDEE_ID.toString())),
                                withJsonPath("$.status", equalTo(STATUS)),
                                withJsonPath("$." + FIELD_FIRST_NAME, equalTo(FIRST_NAME)),
                                withJsonPath("$." + FIELD_FIRST_NAME, equalTo(FIRST_NAME))
                                //withJsonPath("$.lastName", equalTo(LAST_NAME.toString()))
                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseDefenceCounselAddedEvent() throws Exception {
        final JsonEnvelope command = createAddDefenceCounselCommand();

        this.hearingCommandHandler.addDefenceCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(DEFENCE_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", equalTo(PERSON_ID.toString())),
                                withJsonPath("$.attendeeId", equalTo(ATTENDEE_ID.toString())),
                                withJsonPath("$.status", equalTo(STATUS)),
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.defendantIds", hasSize(2)),
                                withJsonPath("$.defendantIds", hasItems(DEFENDANT_ID.toString(), DEFENDANT_ID_2.toString()))
                        ))).thatMatchesSchema(),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(NEWDEFENCE_COUNSEL_ADDED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath("$.personId", equalTo(PERSON_ID.toString())),
                                withJsonPath("$.attendeeId", equalTo(ATTENDEE_ID.toString())),
                                withJsonPath("$.status", equalTo(STATUS)),
                                withJsonPath("$.hearingId", equalTo(HEARING_ID.toString())),
                                withJsonPath("$.defendantIds", hasSize(2)),
                                withJsonPath("$.defendantIds", hasItems(DEFENDANT_ID.toString(), DEFENDANT_ID_2.toString()))
                        ))).thatMatchesSchema()

        ));
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
        hearingAggregate.apply(new ResultsShared(HEARING_ID, SHARED_TIME, prepareResultLines()));
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
        hearingAggregate.apply(new ResultsShared(HEARING_ID, SHARED_TIME, prepareResultLines()));
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

    @Test
    public void shouldRaisePleaAdded() throws Exception {

        final JsonObject publicHearingAddedPayload = getSendCaseForListingPayload(
                "hearing.update-plea.json", PLEA_GUILTY);
        final JsonEnvelope addPleaCommand = envelopeFrom(metadataWithRandomUUID(HEARING_UPDATE_PLEA),
                publicHearingAddedPayload);
        // When
        this.hearingCommandHandler.updatePlea(addPleaCommand);
        // Then
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingPleaEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addPleaCommand)
                                .withName(PLEA_ADDED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s.%s", "plea", "value"), equalTo(PLEA_GUILTY))
                        ))),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addPleaCommand)
                                .withName(HEARING_HEARING_PLEA_UPDATED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString()))
                        )))
        ));
    }

    @Test
    public void shouldRaiseVerdictUpdated() throws Exception {
        // Given

        final JsonObject updateVerdictPayload = getHearingUpdateVerdictPayload(
                "hearing.update-verdict.json");
        final JsonEnvelope updateVerdictCommand = envelopeFrom(metadataWithRandomUUID(HEARING_UPDATE_VERDICT),
                updateVerdictPayload);
        // When
        this.hearingCommandHandler.updateVerdict(updateVerdictCommand);
        // Then
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(updateVerdictCommand)
                                .withName(HEARING_CONVICTION_DATE_ADDED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString()))
                        ))),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(updateVerdictCommand)
                                .withName(HEARING_VERDICT_ADDED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s.%s.%s", "verdict", "value", "category"), equalTo(VERDICT_CATEGORY)),
                                withJsonPath(format("$.%s.%s", "verdict", "verdictDate"), equalTo(VERDICT_DATE))
                        ))),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(updateVerdictCommand)
                                .withName(HEARING_HEARING_VERDICT_UPDATED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString()))
                        )))
        ));
    }

    @Test
    public void shouldRaiseHearingPleaAdded() throws Exception {
        // Given

        final JsonObject publicHearingAddedPayload = getSendCaseForListingPayload(
                "hearing.plea.json", PLEA_GUILTY);
        final JsonEnvelope addPleaCommand = envelopeFrom(metadataWithRandomUUID(HEARING_PLEA_ADD),
                publicHearingAddedPayload);
        // When
        this.hearingCommandHandler.pleaAdd(addPleaCommand);
        // Then
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addPleaCommand)
                                .withName(HEARING_CONVICTION_DATE_ADDED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_DEFENDANT_ID), equalTo(DEFENDANT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_CONVICTION_DATE), equalTo(PLEA_DATE))
                        ))),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addPleaCommand)
                                .withName(HEARING_PLEA_ADDED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s.%s", "plea", "value"), equalTo(PLEA_GUILTY))
                        )))
        ));

    }

    @Test
    public void shouldRaiseHearingPleaChanged() throws Exception {
        // Given

        final JsonObject publicHearingAddedPayload = getSendCaseForListingPayload(
                "hearing.plea.json", PLEA_NOT_GUILTY);
        final JsonEnvelope changePleaCommand = envelopeFrom(metadataWithRandomUUID(HEARING_PLEA_CHANGE),
                publicHearingAddedPayload);
        // When
        this.hearingCommandHandler.pleaChange(changePleaCommand);
        // Then
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(changePleaCommand)
                                .withName(HEARING_CONVICTION_DATE_REMOVED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_DEFENDANT_ID), equalTo(DEFENDANT_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_PERSON_ID), equalTo(PERSON_ID.toString()))
                        ))),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(changePleaCommand)
                                .withName(HEARING_PLEA_CHANGED),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                                withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s.%s", "plea", "value"), equalTo(PLEA_NOT_GUILTY))
                        )))
        ));

    }


    private JsonEnvelope createInitiateHearingCommandWithOnlyRequiredFields() {
        return envelope()
                .with(metadataWithRandomUUID(INITIATE_HEARING_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(ZonedDateTimes.toString(START_DATE_TIME), FIELD_START_DATE_TIME)
                .withPayloadOf(DURATION, FIELD_DURATION)
                .withPayloadOf(HEARING_TYPE, FIELD_HEARING_TYPE)
                .build();
    }

    private JsonEnvelope createInitiateHearingCommand() {
        return envelope()
                .with(metadataWithRandomUUID(INITIATE_HEARING_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(ZonedDateTimes.toString(START_DATE_TIME), FIELD_START_DATE_TIME)
                .withPayloadOf(DURATION, FIELD_DURATION)
                .withPayloadOf(HEARING_TYPE, FIELD_HEARING_TYPE)
                .withPayloadOf(CASE_ID, FIELD_CASE_ID)
                .withPayloadOf(COURT_CENTRE_ID, FIELD_COURT_CENTRE_ID)
                .withPayloadOf(COURT_CENTRE_NAME, FIELD_COURT_CENTRE_NAME)
                .withPayloadOf(ROOM_ID, FIELD_ROOM_ID)
                .withPayloadOf(ROOM_NAME, FIELD_ROOM_NAME)
                .withPayloadOf(JUDGE_ID, FIELD_JUDGE_ID)
                .withPayloadOf(JUDGE_FIRST_NAME, FIELD_JUDGE_FIRST_NAME)
                .withPayloadOf(JUDGE_LAST_NAME, FIELD_JUDGE_LAST_NAME)
                .withPayloadOf(JUDGE_TITLE, FIELD_JUDGE_TITLE)
                .build();
    }

    private JsonEnvelope createAllocateCourtCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ALLOCATE_COURT_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(COURT_CENTRE_NAME, FIELD_COURT_CENTRE_NAME)
                .build();
    }

    private JsonEnvelope createBookRoomCommand() {
        return envelope()
                .with(metadataWithRandomUUID(BOOK_ROOM_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(ROOM_NAME, FIELD_ROOM_NAME)
                .build();
    }

    private JsonEnvelope createAdjournHearingDateCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADJOURN_DATE_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(START_DATE, FIELD_START_DATE)
                .build();
    }

    private JsonEnvelope createAddCaseCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADD_CASE_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(CASE_ID, FIELD_CASE_ID)
                .build();
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

    private JsonEnvelope createAddProsecutionCounselCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADD_PROSECUTION_COUNSEL_COMMAND))
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(PERSON_ID, FIELD_PERSON_ID)
                .withPayloadOf(ATTENDEE_ID, FIELD_ATTENDEE_ID)
                .withPayloadOf(STATUS, FIELD_STATUS)
                .withPayloadOf(TITLE, FIELD_TITLE)
                .withPayloadOf(FIRST_NAME, FIELD_FIRST_NAME)
                .withPayloadOf(LAST_NAME, FIELD_LAST_NAME)
                .build();
    }

    private JsonEnvelope createAddDefenceCounselCommand() {
        final JsonArrayBuilder defendantIdsBuilder = createArrayBuilder()
                .add(createObjectBuilder().add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString()))
                .add(createObjectBuilder().add(FIELD_DEFENDANT_ID, DEFENDANT_ID_2.toString()));

        return envelopeFrom(metadataWithRandomUUID(ADD_DEFENCE_COUNSEL_COMMAND),
                createObjectBuilder()
                        .add(FIELD_HEARING_ID, HEARING_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_ATTENDEE_ID, ATTENDEE_ID.toString())
                        .add(FIELD_STATUS, STATUS)
                        .add(FIELD_DEFENDANT_IDS, defendantIdsBuilder)
                        .build());
    }

    private JsonObject getSendCaseForListingPayload(final String resource, final String pleaValue) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString =
                sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", CASE_ID.toString()).
                        replace("RANDOM_HEARING_ID", HEARING_ID.toString()).
                        replace("RANDOM_DEFENDANT_ID", DEFENDANT_ID.toString()).
                        replace("RANDOM_PERSON_ID", PERSON_ID.toString()).
                        replace("RANDOM_OFFENCE_ID", OFFENCE_ID.toString()).
                        replace("RANDOM_PLEA_ID", PLEA_ID).
                        replace("RANDOM_PLEA_DATE", PLEA_DATE).
                        replace("PLEA_VALUE", pleaValue);

        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }

    private JsonObject getSendCaseForListingVerdictPayload(final String resource, final String pleaValue) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString =
                sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", CASE_ID.toString()).
                        replace("RANDOM_HEARING_ID", HEARING_ID.toString()).
                        replace("RANDOM_DEFENDANT_ID", DEFENDANT_ID.toString()).
                        replace("RANDOM_PERSON_ID", PERSON_ID.toString()).
                        replace("RANDOM_OFFENCE_ID", OFFENCE_ID.toString()).
                        replace("RANDOM_VERDICT_ID", VERDICT_ID).
                        replace("RANDOM_VERDICT_DATE", VERDICT_DATE).
                        replace("VALUE", pleaValue);

        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }


    private JsonObject getHearingUpdateVerdictPayload(final String resource) throws IOException {
        String sendCaseForListingEventPayloadString = getStringFromResource(resource);
        sendCaseForListingEventPayloadString =
                sendCaseForListingEventPayloadString.replace("RANDOM_CASE_ID", CASE_ID.toString()).
                        replace("RANDOM_HEARING_ID", HEARING_ID.toString()).
                        replace("RANDOM_DEFENDANT_ID", DEFENDANT_ID.toString()).
                        replace("RANDOM_PERSON_ID", PERSON_ID.toString()).
                        replace("RANDOM_OFFENCE_ID", OFFENCE_ID.toString()).
                        replace("RANDOM_VERDICT_ID", VERDICT_ID);

        return new StringToJsonObjectConverter().convert(sendCaseForListingEventPayloadString);
    }


    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }
}

