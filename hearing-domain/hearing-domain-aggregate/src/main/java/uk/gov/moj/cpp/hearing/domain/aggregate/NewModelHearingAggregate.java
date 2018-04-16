package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.DefendantId;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingOffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

public class NewModelHearingAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;
    
    private static final String GUILTY = "GUILTY";
    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";

    private List<Case> cases;
    private Hearing hearing;

    private Map<UUID, HearingEvent> events = new HashMap<>();

    private List<NewProsecutionCounselAdded> prosecutionCounsels = new ArrayList<>();
    private List<NewDefenceCounselAdded> newDefenceCounsels = new ArrayList<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(Initiated.class).apply(initiated -> {
                    this.cases = initiated.getCases();
                    this.hearing = initiated.getHearing();
                }),
                when(InitiateHearingOffencePlead.class).apply(initiateHearingOffencePlead -> {

                }),
                when(NewProsecutionCounselAdded.class).apply(newProsecutionCounselAdded -> {
                    prosecutionCounsels.add(newProsecutionCounselAdded);
                }),

                when(NewDefenceCounselAdded.class).apply(newDefenceCounselAdded -> {
                    newDefenceCounsels.add(newDefenceCounselAdded);
                }),

                when(HearingEventLogged.class).apply(hearingEventLogged -> {
                    this.events.put(hearingEventLogged.getHearingEventId(), new HearingEvent(hearingEventLogged));
                }),

                when(HearingEventDeleted.class).apply(hearingEventDeleted -> {
                    this.events.get(hearingEventDeleted.getHearingEventId()).setDeleted(true);
                }),

                otherwiseDoNothing()
        );
    }


    public Stream<Object> addProsecutionCounsel(final AddProsecutionCounselCommand prosecutionCounselCommand) {
        return apply(Stream.of(
                NewProsecutionCounselAdded.builder()
                        .withHearingId(prosecutionCounselCommand.getHearingId())
                        .withAttendeeId(prosecutionCounselCommand.getAttendeeId())
                        .withPersonId(prosecutionCounselCommand.getPersonId())
                        .withFirstName(prosecutionCounselCommand.getFirstName())
                        .withLastName(prosecutionCounselCommand.getLastName())
                        .withStatus(prosecutionCounselCommand.getStatus())
                        .withTitle(prosecutionCounselCommand.getTitle())
                        .build()
        ));
    }

    public Stream<Object> addDefenceCounsel(final AddDefenceCounselCommand defenceCounselCommand) {
        return apply(Stream.of(
                NewDefenceCounselAdded.builder()
                        .withHearingId(defenceCounselCommand.getHearingId())
                        .withDefendantIds(
                                defenceCounselCommand.getDefendantIds().stream()
                                        .map(DefendantId::getDefendantId)
                                        .collect(Collectors.toList())
                        )
                        .withAttendeeId(defenceCounselCommand.getAttendeeId())
                        .withPersonId(defenceCounselCommand.getPersonId())
                        .withFirstName(defenceCounselCommand.getFirstName())
                        .withLastName(defenceCounselCommand.getLastName())
                        .withStatus(defenceCounselCommand.getStatus())
                        .withTitle(defenceCounselCommand.getTitle())
                        .build()
        ));
    }

    public Stream<Object> updatePlea(final UUID hearingId, final UUID offenceId, final LocalDate pleaDate,
            final String pleaValue) {
        final List<Object> events = new ArrayList<>();
        events.add(HearingOffencePleaUpdated.builder()
                    .withHearingId(hearingId)
                    .withOffenceId(offenceId)
                    .withPleaDate(pleaDate)
                    .withValue(pleaValue)
                    .build());
        events.add(isGuilty(pleaValue) ? 
                ConvictionDateAdded.builder()
                    .withHearingId(hearingId)
                    .withOffenceId(offenceId)
                    .withConvictionDate(pleaDate)
                    .build() : 
               ConvictionDateRemoved.builder()
                    .withHearingId(hearingId)
                    .withOffenceId(offenceId)
                    .build());
        return apply(events.stream());
    }

    private boolean isGuilty(final String value) {
        return GUILTY.equalsIgnoreCase(value);
    }

    public Stream<Object> initiate(InitiateHearingCommand initiateHearingCommand) {
        return apply(Stream.of(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
    }

    public Stream<Object> initiateHearingOffencePlea(final InitiateHearingOffencePleaCommand command) {
        return apply(Stream.of(InitiateHearingOffencePlead.builder()
                .withOffenceId(command.getOffenceId())
                .withCaseId(command.getCaseId())
                .withDefendantId(command.getDefendantId())
                .withHearingId(command.getHearingId())
                .withOriginHearingId(command.getOriginHearingId())
                .withPleaDate(command.getPleaDate())
                .withValue(command.getValue())
                .build()
        ));
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
                this.cases.get(0).getCaseId(),
                logEventCommand.getWitnessId())));
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

        if (events.get(logEventCommand.getHearingEventId()).isDeleted()) {
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
                        this.cases.get(0).getCaseId(),
                        null)
        ));
    }

    public Stream<Object> updateVerdict(UUID hearingId, UUID caseId, UUID defendantId, UUID offenceId, Verdict verdict) {

        List<Object> events = new ArrayList<>();

        events.add(new OffenceVerdictUpdated(
                caseId, //TODO - offenceId is unique within case, so do we need this?
                hearingId,
                offenceId,
                verdict.getId(), //TODO - do we need verdictId
                verdict.getValue().getId(),
                verdict.getValue().getCategory(),
                verdict.getValue().getCode(),
                verdict.getValue().getDescription(),
                verdict.getNumberOfJurors(),
                verdict.getNumberOfSplitJurors(),
                verdict.getUnanimous(),
                verdict.getVerdictDate()
        ));

        if (isGuilty(verdict.getValue().getCategory())) {
            events.add(new ConvictionDateAdded(hearingId, offenceId, verdict.getVerdictDate()));
        } else {
            events.add(new ConvictionDateRemoved(hearingId, offenceId));
        }

        return apply(events.stream());
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
