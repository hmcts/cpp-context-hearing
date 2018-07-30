package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class HearingDetailChangeCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingDetailChangeCommandHandler.class.getName());

    private static final String FIELD_HEARING = "hearing";

    @Handles("hearing.change-hearing-detail")
    public void changeHearingDetail(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.change-hearing-detail event received {}", envelope.toObfuscatedDebugString());
        }

        final HearingJsonPayloadReader hr = new HearingJsonPayloadReader(envelope.payloadAsJsonObject().getJsonObject(FIELD_HEARING));
        aggregate(NewModelHearingAggregate.class, hr.getId(), envelope, a -> a.updateHearingDetails(hr.getId(), hr.getType(), hr.getCourtRoomId(), hr.getCourtRoomName(), hr.getJudge(), hr.getHearingDays()));
    }

    private class HearingJsonPayloadReader {
        private static final String FIELD_HEARING_ID = "id";
        private static final String FIELD_HEARING_TYPE = "type";
        private static final String FIELD_HEARING_COURT_ROOM_ID = "courtRoomId";
        private static final String FIELD_HEARING_COURT_ROOM_NAME = "courtRoomName";
        private static final String FIELD_HEARING_JUDGE_ID = "id";
        private static final String FIELD_HEARING_JUDGE_TITLE = "title";
        private static final String FIELD_HEARING_JUDGE_FIRST_NAME = "firstName";
        private static final String FIELD_HEARING_JUDGE_LAST_NAME = "lastName";
        private static final String FIELD_HEARING_HEARING_DAYS = "hearingDays";
        private final JsonObject hearingPayload;

        public HearingJsonPayloadReader(
                final JsonObject hearingPayload) {
            this.hearingPayload = hearingPayload;
        }

        public UUID getId() {
            return UUID.fromString(hearingPayload.getString(FIELD_HEARING_ID));
        }

        public String getType() {
            return hearingPayload.getString(FIELD_HEARING_TYPE);
        }

        public UUID getCourtRoomId() {
            return UUID.fromString(hearingPayload.getString(FIELD_HEARING_COURT_ROOM_ID));
        }

        public String getCourtRoomName() {
            return hearingPayload.getString(FIELD_HEARING_COURT_ROOM_NAME);
        }

        public Judge getJudge() {
            final JsonObject judge = hearingPayload.getJsonObject("judge");

            return Judge.judge()
                    .setId(UUID.fromString(judge.getString(FIELD_HEARING_JUDGE_ID)))
                    .setTitle(judge.getString(FIELD_HEARING_JUDGE_TITLE))
                    .setFirstName(judge.getString(FIELD_HEARING_JUDGE_FIRST_NAME))
                    .setLastName(judge.getString(FIELD_HEARING_JUDGE_LAST_NAME));
        }

        public List<ZonedDateTime> getHearingDays() {
            return hearingPayload
                    .getJsonArray(FIELD_HEARING_HEARING_DAYS).getValuesAs(JsonString.class)
                    .stream()
                    .map(value -> ZonedDateTimes.fromString(value.getString()))
                    .collect(toList());
        }
    }
}

