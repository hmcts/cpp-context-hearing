package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.ResultLine;
import uk.gov.moj.cpp.hearing.domain.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;

@SuppressWarnings({"WeakerAccess", "CdiInjectionPointsInspection"})
@ServiceComponent(COMMAND_HANDLER)
public class HearingCommandHandler {

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_GENERIC_ID = "id";

    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_SHARED_TIME = "sharedTime";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_LEVEL = "level";
    private static final String RESULT_LABEL = "resultLabel";
    private static final String FIELD_RESULT_PROMPTS = "prompts";
    private static final String FIELD_RESULT_LABEL = "label";
    private static final String FIELD_RESULT_VALUE = "value";
    private static final String FIELD_COURT = "court";
    private static final String FIELD_COURT_ROOM = "courtRoom";
    private static final String FIELD_CLERK_OF_THE_COURT_ID = "clerkOfTheCourtId";
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME = "clerkOfTheCourtFirstName";
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME = "clerkOfTheCourtLastName";

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID defendantId = fromString(payload.getString(FIELD_DEFENDANT_ID));
        final UUID targetId = fromString(payload.getString("targetId"));
        final UUID offenceId = fromString(payload.getString(FIELD_OFFENCE_ID));
        final String draftResult = payload.getString("draftResult");
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));

        final Stream<Object> events = streamOf(new DraftResultSaved(targetId, defendantId, offenceId, draftResult, hearingId));
        this.eventSource.getStreamById(hearingId).append(events.map(this.enveloper.withMetadataFrom(command)));
    }

    @Handles("hearing.command.share-results")
    public void shareResult(final JsonEnvelope command) throws EventStreamException {
        final JsonObject payload = command.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(FIELD_HEARING_ID));
        final ZonedDateTime sharedTime = ZonedDateTimes.fromJsonString(payload.getJsonString(FIELD_SHARED_TIME));

        final List<ResultLine> resultLines = payload.getJsonArray(FIELD_RESULT_LINES)
                .getValuesAs(JsonObject.class).stream()
                .map(this::extractResultLine)
                .collect(toList());

        final EventStream eventStream = this.eventSource.getStreamById(hearingId);
        final HearingAggregate aggregate = this.aggregateService.get(eventStream, HearingAggregate.class);
        final Stream<Object> events = aggregate.shareResults(hearingId, sharedTime, resultLines);

        eventStream.append(events.map(this.enveloper.withMetadataFrom(command)));
    }

    private ResultLine extractResultLine(final JsonObject resultLine) {
        return new ResultLine(fromString(resultLine.getString(FIELD_GENERIC_ID)),
                resultLine.containsKey(FIELD_LAST_SHARED_RESULT_ID) ? fromString(resultLine.getString(FIELD_LAST_SHARED_RESULT_ID)) : null,
                fromString(resultLine.getString(FIELD_CASE_ID)),
                fromString(resultLine.getString(FIELD_PERSON_ID)),
                fromString(resultLine.getString(FIELD_OFFENCE_ID)),
                resultLine.getString(FIELD_LEVEL),
                resultLine.getString(RESULT_LABEL),
                resultLine.getJsonArray(FIELD_RESULT_PROMPTS).getValuesAs(JsonObject.class).stream()
                        .map(prompt -> new ResultPrompt(prompt.getString(FIELD_RESULT_LABEL), prompt.getString(FIELD_RESULT_VALUE)))
                        .collect(toList()),
                resultLine.getString(FIELD_COURT),
                resultLine.getString(FIELD_COURT_ROOM),
                fromString(resultLine.getString(FIELD_CLERK_OF_THE_COURT_ID)),
                resultLine.getString(FIELD_CLERK_OF_THE_COURT_FIRST_NAME),
                resultLine.getString(FIELD_CLERK_OF_THE_COURT_LAST_NAME));
    }
}
