package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;
import uk.gov.moj.cpp.hearing.domain.HearingType;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingEventDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";
    private static final String REASON_ONLY_CROWN_COURT_HEARINGS_CAN_LOG_EVENTS = "Only crown court hearings can log events";

    private final HearingAggregateMomento momento;

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
            return Stream.of(raiseHearingEventIgnored(REASON_HEARING_NOT_FOUND, logEventCommand));
        }

        if (this.momento.getHearingEvents().containsKey(logEventCommand.getHearingEventId())) {
            if (this.momento.getHearingEvents().get(logEventCommand.getHearingEventId()).isDeleted()) {
                return Stream.of(raiseHearingEventIgnored(REASON_ALREADY_DELETED, logEventCommand));
            }
            return Stream.of(raiseHearingEventIgnored(REASON_ALREADY_LOGGED, logEventCommand));
        }

        if (this.momento.getHearing().getJurisdictionType() == JurisdictionType.MAGISTRATES) {
            return Stream.of(raiseHearingEventIgnored(REASON_ONLY_CROWN_COURT_HEARINGS_CAN_LOG_EVENTS, logEventCommand));
        }

        return Stream.of(new HearingEventLogged(
                logEventCommand.getHearingEventId(),
                null,
                logEventCommand.getHearingId(),
                logEventCommand.getHearingEventDefinitionId(),
                logEventCommand.getDefenceCounselId(),
                logEventCommand.getRecordedLabel(),
                logEventCommand.getEventTime(),
                logEventCommand.getLastModifiedTime(),
                logEventCommand.getAlterable(),
                CourtCentre.courtCentre()
                    .withId(momento.getHearing().getCourtCentre().getId())
                    .withName(momento.getHearing().getCourtCentre().getName())
                    .withRoomId(momento.getHearing().getCourtCentre().getRoomId())
                    .withRoomName(momento.getHearing().getCourtCentre().getRoomName())
                    .withWelshName(momento.getHearing().getCourtCentre().getWelshName())
                    .withWelshRoomName(momento.getHearing().getCourtCentre().getWelshRoomName())
                    .build(),
                HearingType.hearingType()
                    .withId(momento.getHearing().getType().getId())
                    .withDescription(momento.getHearing().getType().getDescription())
                    .build(),
                this.momento.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN())); //TODO: GPE-5657 Which case URN is expected to be set?
    }

    public Stream<Object> updateHearingEvents(final UpdateHearingEventsCommand updateHearingEventsCommand) {

        if (this.momento.getHearing() == null) {
            return Stream.of(raiseHearingEventIgnored(REASON_HEARING_NOT_FOUND, updateHearingEventsCommand.getHearingId()));
        }

        if (this.momento.getHearing().getJurisdictionType() == JurisdictionType.MAGISTRATES) {
            return Stream.of(raiseHearingEventIgnored(REASON_ONLY_CROWN_COURT_HEARINGS_CAN_LOG_EVENTS, updateHearingEventsCommand.getHearingId()));
        }

        if (updateHearingEventsCommand.getHearingEvents().isEmpty()) {
            return Stream.empty();
        }

        return Stream.of(new HearingEventsUpdated(updateHearingEventsCommand.getHearingId(), updateHearingEventsCommand.getHearingEvents()));
    }

    public Stream<Object> correctHearingEvent(final CorrectLogEventCommand logEventCommand) {

        if (this.momento.getHearing() == null) {
            return Stream.of(raiseHearingEventIgnored(REASON_HEARING_NOT_FOUND, logEventCommand));
        }

        if (!this.momento.getHearingEvents().containsKey(logEventCommand.getHearingEventId())) {
            return Stream.of(raiseHearingEventIgnored(REASON_EVENT_NOT_FOUND, logEventCommand));
        }

        if (this.momento.getHearingEvents().get(logEventCommand.getHearingEventId()).isDeleted()) {
            return Stream.of(raiseHearingEventIgnored(REASON_ALREADY_DELETED, logEventCommand));
        }

        if (this.momento.getHearing().getJurisdictionType() == JurisdictionType.MAGISTRATES) {
            return Stream.of(raiseHearingEventIgnored(REASON_ONLY_CROWN_COURT_HEARINGS_CAN_LOG_EVENTS, logEventCommand));
        }

        return Stream.of(
                new HearingEventDeleted(logEventCommand.getHearingEventId()),
                new HearingEventLogged(
                        logEventCommand.getLatestHearingEventId(),
                        logEventCommand.getHearingEventId(),
                        logEventCommand.getHearingId(),
                        logEventCommand.getHearingEventDefinitionId(),
                        logEventCommand.getDefenceCounselId(),
                        logEventCommand.getRecordedLabel(),
                        logEventCommand.getEventTime(),
                        logEventCommand.getLastModifiedTime(),
                        logEventCommand.getAlterable(),
                        CourtCentre.courtCentre()
                            .withId(momento.getHearing().getCourtCentre().getId())
                            .withName(momento.getHearing().getCourtCentre().getName())
                            .withRoomId(momento.getHearing().getCourtCentre().getRoomId())
                            .withRoomName(momento.getHearing().getCourtCentre().getRoomName())
                            .withWelshName(momento.getHearing().getCourtCentre().getWelshName())
                            .withWelshRoomName(momento.getHearing().getCourtCentre().getWelshRoomName())
                            .build(),
                        HearingType.hearingType()
                            .withId(momento.getHearing().getType().getId())
                            .withDescription(momento.getHearing().getType().getDescription())
                            .build(),
                        this.momento.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN() //TODO: GPE-5657 Which case URN is expected to be set?
                ));
    }

    private HearingEventIgnored raiseHearingEventIgnored(final String reason, final CorrectLogEventCommand logEventCommand) {
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

    private HearingEventIgnored raiseHearingEventIgnored(final String reason, final LogEventCommand logEventCommand) {
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

    private HearingEventIgnored raiseHearingEventIgnored(final String reason, final UUID hearingId) {
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
