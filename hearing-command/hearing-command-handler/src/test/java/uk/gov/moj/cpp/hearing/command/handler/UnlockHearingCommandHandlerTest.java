package uk.gov.moj.cpp.hearing.command.handler;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.ResultsError;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnlocked;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ManageResultsFailed;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UnlockHearingCommandHandlerTest {

    private static final String HEARING_MANAGE_RESULTS_FAILED = "hearing.manage-results-failed";
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(HearingUnlocked.class, ManageResultsFailed.class);

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

    @InjectMocks
    private UnlockHearingCommandHandler unlockHearingCommandHandler;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void hearingUnlocked() throws EventStreamException {

        //Given
        final UUID hearingId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.parse("2023-10-10");
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 initialDraftResultsDay1 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, userId, 1);
        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId, HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final ApprovalRequestedV2 approvalRequested = new ApprovalRequestedV2(hearingId, userId);
        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(standardInitiateHearingTemplate().getHearing()));
            apply(initialDraftResultsDay1);
            apply(hearingAmended);
            apply(approvalRequested);
        }};

        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonObject hearingUnlocked = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("userId", userId.toString())
                .add("hearingDay", hearingDay.toString())
                .build();

        final JsonEnvelope commandEnvelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.unlock-hearing")
                .withUserId(UUID.randomUUID().toString()), objectToJsonObjectConverter.convert(hearingUnlocked));

        unlockHearingCommandHandler.unlockHearing(commandEnvelope);

        final Stream<JsonEnvelope> eventStream = verifyAppendAndGetArgumentFrom(this.hearingEventStream);
        final List<JsonEnvelope> events = eventStream.toList();
        assertThat(events.stream().anyMatch(e -> HEARING_MANAGE_RESULTS_FAILED.equals(e.metadata().name())), Matchers.is(true));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(events.get(0), ManageResultsFailed.class), isBean(ManageResultsFailed.class)
                .with(ManageResultsFailed::getHearingId, Matchers.is(hearingId))
                .with(ManageResultsFailed::getHearingDay, Matchers.is(hearingDay))
                .with(ManageResultsFailed::getLastUpdatedVersion, Matchers.is(1))
                .with(ManageResultsFailed::getLastUpdatedByUserId, Matchers.is(userId))
                .with(ManageResultsFailed::getResultsError, isBean(ResultsError.class)
                        .with(ResultsError::getType, Matchers.is(ResultsError.ErrorType.STATE))
                        .with(ResultsError::getCode, Matchers.is("204"))
                        .with(ResultsError::getReason, Matchers.is("Unlock hearing not permitted! Hearing is in APPROVAL_REQUESTED state")))
        );
    }
}