package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingEventDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String HEARING_EVENTS = "hearingEvents";
    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";
    private static final String HEARING_EVENT_ID = "hearingEventId";
    private static final String RECORDED_LABEL = "recordedLabel";
    private static final String HEARING_ID = "hearingId";

    private final transient HearingAggregateMomento momento;

    public HearingEventDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingEventLogged(HearingEventLogged hearingEventLogged) {
        this.momento.getHearingEvents().put(hearingEventLogged.getHearingEventId(), new HearingEvent(hearingEventLogged));
    }

    public void handleHearingEventDeleted(HearingEventDeleted hearingEventDeleted) {
        this.momento.getHearingEvents().get(hearingEventDeleted.getHearingEventId()).setDeleted(true);
    }

    public Stream<Object> logHearingEvent(final LogEventCommand logEventCommand) {

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage(REASON_HEARING_NOT_FOUND, logEventCommand));
        }

        if (this.momento.getHearingEvents().containsKey(logEventCommand.getHearingEventId())) {
            if (this.momento.getHearingEvents().get(logEventCommand.getHearingEventId()).isDeleted()) {
                return Stream.of(generateHearingIgnoredMessage(REASON_ALREADY_DELETED, logEventCommand));
            }

            return Stream.of(generateHearingIgnoredMessage(REASON_ALREADY_LOGGED, logEventCommand));
        }

        return Stream.of(new HearingEventLogged(
                logEventCommand.getHearingEventId(),
                null,
                logEventCommand.getHearingId(),
                logEventCommand.getHearingEventDefinitionId(),
                logEventCommand.getRecordedLabel(),
                logEventCommand.getEventTime(),
                logEventCommand.getLastModifiedTime(),
                logEventCommand.getAlterable(),
                this.momento.getHearing().getCourtCentreId(),
                this.momento.getHearing().getCourtCentreName(),
                this.momento.getHearing().getCourtRoomId(),
                this.momento.getHearing().getCourtRoomName(),
                this.momento.getHearing().getType(),
                this.momento.getCases().get(0).getUrn(),
                this.momento.getCases().get(0).getCaseId(),
                logEventCommand.getWitnessId(), logEventCommand.getCounselId()));
    }

    public Stream<Object> updateHearingEvents(final JsonObject payload) {
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage(REASON_HEARING_NOT_FOUND,
                    hearingId));
        }

        final List<uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent> hearingEvents = new ArrayList<>();
        final JsonArray hearingEventsArray = payload.getJsonArray(HEARING_EVENTS);
        hearingEventsArray.getValuesAs(JsonObject.class).forEach(hearingEvent ->

                hearingEvents.add(new uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent(
                        fromString(hearingEvent.getString(HEARING_EVENT_ID)),
                        hearingEvent.getString(RECORDED_LABEL)))
        );
        return hearingEvents.isEmpty() ? Stream.empty()
                : Stream.of(new HearingEventsUpdated(hearingId, hearingEvents));
    }

    public Stream<Object> correctHearingEvent(final CorrectLogEventCommand logEventCommand) {

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage(REASON_HEARING_NOT_FOUND, logEventCommand));
        }

        if (!this.momento.getHearingEvents().containsKey(logEventCommand.getHearingEventId())) {

            return Stream.of(generateHearingIgnoredMessage(REASON_EVENT_NOT_FOUND, logEventCommand));
        }

        if (this.momento.getHearingEvents().get(logEventCommand.getHearingEventId()).isDeleted()) {
            return Stream.of(generateHearingIgnoredMessage(REASON_ALREADY_DELETED, logEventCommand));
        }

        return Stream.of(
                new HearingEventDeleted(logEventCommand.getHearingEventId()),
                new HearingEventLogged(
                        logEventCommand.getLatestHearingEventId(),
                        logEventCommand.getHearingEventId(),
                        logEventCommand.getHearingId(),
                        logEventCommand.getHearingEventDefinitionId(),
                        logEventCommand.getRecordedLabel(),
                        logEventCommand.getEventTime(),
                        logEventCommand.getLastModifiedTime(),
                        logEventCommand.getAlterable(),
                        this.momento.getHearing().getCourtCentreId(),
                        this.momento.getHearing().getCourtCentreName(),
                        this.momento.getHearing().getCourtRoomId(),
                        this.momento.getHearing().getCourtRoomName(),
                        this.momento.getHearing().getType(),
                        this.momento.getCases().get(0).getUrn(),
                        this.momento.getCases().get(0).getCaseId(),
                        logEventCommand.getWitnessId(),
                        logEventCommand.getCounselId()
                ));
    }

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason, final CorrectLogEventCommand logEventCommand) {
        return new HearingEventIgnored(
                logEventCommand.getHearingEventId(),
                logEventCommand.getHearingId(),
                logEventCommand.getHearingEventDefinitionId(),
                logEventCommand.getRecordedLabel(),
                logEventCommand.getEventTime(),
                reason,
                logEventCommand.getAlterable()
        );
    }

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason, final LogEventCommand logEventCommand) {
        return new HearingEventIgnored(
                logEventCommand.getHearingEventId(),
                logEventCommand.getHearingId(),
                logEventCommand.getHearingEventDefinitionId(),
                logEventCommand.getRecordedLabel(),
                logEventCommand.getEventTime(),
                reason,
                logEventCommand.getAlterable()
        );
    }

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason, final UUID hearingId) {
        return new HearingEventIgnored(
                hearingId,
                reason
        );
    }

    public static final class HearingEvent implements Serializable {

        private static final long serialVersionUID = 1L;
        private final HearingEventLogged hearingEventLogged;
        private boolean deleted;

        public HearingEvent(final HearingEventLogged hearingEventLogged) {
            this.hearingEventLogged = hearingEventLogged;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(final boolean deleted) {
            this.deleted = deleted;
        }

        public HearingEventLogged getHearingEventLogged() {
            return hearingEventLogged;
        }
    }
}
