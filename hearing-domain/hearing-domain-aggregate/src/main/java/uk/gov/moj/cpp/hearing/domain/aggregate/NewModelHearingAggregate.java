package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingOffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;

public class NewModelHearingAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";

    private List<Case> cases;
    private Hearing hearing;

    private Map<UUID, HearingEvent> events = new HashMap<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(Initiated.class).apply(this::onInitiated),
                when(InitiateHearingOffencePlead.class).apply(this::onInitiateHearingOffencePlead),
                when(HearingEventLogged.class).apply(this::onHearingEventLogged),
                when(HearingEventDeleted.class).apply(this::onHearingEventDeleted),
                otherwiseDoNothing()
        );
    }

    private void onInitiated(Initiated initiated) {
        this.cases = initiated.getCases();
        this.hearing = initiated.getHearing();
    }

    private void onInitiateHearingOffencePlead(InitiateHearingOffencePlead initiateHearingOffencePlead) {

    }
    
    public Stream<Object> updatePlea(final UUID originHearingId, final UUID offenceId, final LocalDate pleaDate,
            final String pleaValue) {
        return HearingOffencePleaUpdated.builder()
                    .withHearingId(originHearingId)
                    .withOffenceId(offenceId)
                    .withPleaDate(pleaDate)
                    .withValue(pleaValue)
                    .buildStream();
    }

    private void onHearingEventLogged(HearingEventLogged hearingEventLogged) {
        this.events.put(hearingEventLogged.getHearingEventId(), new HearingEvent(hearingEventLogged));
    }

    private void onHearingEventDeleted(HearingEventDeleted hearingEventDeleted) {
        this.events.get(hearingEventDeleted.getHearingEventId()).setDeleted(true);
    }

    public Stream<Object> initiate(InitiateHearingCommand initiateHearingCommand) {
        return apply(Stream.of(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
    }

    public Stream<Object> initiateHearingOffencePlea(InitiateHearingOffencePleaCommand initiateHearingOffencePleaCommand) {
        return apply(Stream.of(new InitiateHearingOffencePlead(
                initiateHearingOffencePleaCommand.getOffenceId(),
                initiateHearingOffencePleaCommand.getCaseId(),
                initiateHearingOffencePleaCommand.getDefendantId(),
                initiateHearingOffencePleaCommand.getHearingId(),
                initiateHearingOffencePleaCommand.getOriginHearingId(),
                initiateHearingOffencePleaCommand.getPleaDate(),
                initiateHearingOffencePleaCommand.getValue()
        )));
    }

    public Stream<Object> logHearingEvent(LogEventCommand logEventCommand) {

        if (this.hearing == null || events.containsKey(logEventCommand.getHearingEventId())) {

            String reason = hearing == null ? REASON_HEARING_NOT_FOUND :
                    events.get(logEventCommand.getHearingEventId()).isDeleted() ? REASON_ALREADY_DELETED : REASON_ALREADY_LOGGED;

            return apply(Stream.of(new HearingEventIgnored(
                    logEventCommand.getHearingEventId(),
                    logEventCommand.getHearingId(),
                    logEventCommand.getHearingEventDefinitionId(),
                    logEventCommand.getRecordedLabel(),
                    logEventCommand.getEventTime(),
                    reason,
                    logEventCommand.getAlterable()
            )));
        }

        return apply(Stream.of(new HearingEventLogged(
                logEventCommand.getHearingEventId(),
                null,
                logEventCommand.getHearingId(),
                logEventCommand.getHearingEventDefinitionId(),
                logEventCommand.getRecordedLabel(),
                logEventCommand.getEventTime(),
                logEventCommand.getLastModifiedTime(),
                logEventCommand.getAlterable(),
                this.hearing.getCourtCentreId(),
                this.hearing.getCourtCentreName(),
                this.hearing.getCourtRoomId(),
                this.hearing.getCourtRoomName(),
                this.hearing.getType(),
                this.cases.get(0).getUrn(), //TODO - doesn't support multiple cases yet.
                this.cases.get(0).getCaseId()
        )));
    }

    public Stream<Object> correctHearingEvent(CorrectLogEventCommand logEventCommand) {
        if (!events.containsKey(logEventCommand.getHearingEventId())) {

            return apply(Stream.of(new HearingEventIgnored(
                    logEventCommand.getHearingEventId(),
                    logEventCommand.getHearingId(),
                    logEventCommand.getHearingEventDefinitionId(),
                    logEventCommand.getRecordedLabel(),
                    logEventCommand.getEventTime(),
                    REASON_EVENT_NOT_FOUND,
                    logEventCommand.getAlterable()
            )));
        }

        if (events.get(logEventCommand.getHearingEventId()).isDeleted()){
            return apply(Stream.of(new HearingEventIgnored(
                    logEventCommand.getHearingEventId(),
                    logEventCommand.getHearingId(),
                    logEventCommand.getHearingEventDefinitionId(),
                    logEventCommand.getRecordedLabel(),
                    logEventCommand.getEventTime(),
                    REASON_ALREADY_DELETED,
                    logEventCommand.getAlterable()
            )));
        }

        return apply(Stream.of(
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
                        this.hearing.getCourtCentreId(),
                        this.hearing.getCourtCentreName(),
                        this.hearing.getCourtRoomId(),
                        this.hearing.getCourtRoomName(),
                        this.hearing.getType(),
                        this.cases.get(0).getUrn(), //TODO - doesn't support multiple cases yet.
                        this.cases.get(0).getCaseId()
                )
        ));
    }

    public static class HearingEvent {
        private boolean deleted;

        private HearingEventLogged hearingEventLogged;

        public HearingEvent(HearingEventLogged hearingEventLogged) {
            this.hearingEventLogged = hearingEventLogged;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public HearingEventLogged getHearingEventLogged() {
            return hearingEventLogged;
        }
    }
}
