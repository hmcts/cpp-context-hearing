package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.WitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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

@SuppressWarnings({"squid:S00107", "squid:S1602"})
public class NewModelHearingAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private static final String GUILTY = "GUILTY";
    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";

    private List<Case> cases;
    private Hearing hearing;

    private Map<UUID, HearingEvent> hearingHevents = new HashMap<>();

    private Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels = new HashMap<>();
    private Map<UUID, DefenceCounselUpsert> defenceCounsels = new HashMap<>();

    private Map<UUID, Plea> pleas = new HashMap<>();
    private Map<UUID, VerdictUpsert> verdicts = new HashMap<>();

    private boolean published = false;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(Initiated.class).apply(initiated -> {
                    this.cases = initiated.getCases();
                    this.hearing = initiated.getHearing();
                }),

                when(InitiateHearingOffencePlead.class).apply(inheritedPlea -> {
                    pleas.computeIfAbsent(inheritedPlea.getOffenceId(), offenceId -> Plea.plea()
                            .setOriginHearingId(inheritedPlea.getHearingId())
                            .setOffenceId(offenceId)
                            .setValue(inheritedPlea.getValue())
                            .setPleaDate(inheritedPlea.getPleaDate()));
                }),

                when(ProsecutionCounselUpsert.class).apply(prosecutionCounselUpsert ->
                        prosecutionCounsels.put(prosecutionCounselUpsert.getAttendeeId(), prosecutionCounselUpsert)
                ),

                when(DefenceCounselUpsert.class).apply(defenceCounselUpsert ->
                        defenceCounsels.put(defenceCounselUpsert.getAttendeeId(), defenceCounselUpsert)
                ),

                when(HearingEventLogged.class).apply(hearingEventLogged ->
                        this.hearingHevents.put(hearingEventLogged.getHearingEventId(), new HearingEvent(hearingEventLogged))
                ),

                when(HearingEventDeleted.class).apply(hearingEventDeleted ->
                        this.hearingHevents.get(hearingEventDeleted.getHearingEventId()).setDeleted(true)
                ),

                when(WitnessAdded.class).apply(witnessAdded -> {

                }),

                when(ResultsShared.class).apply(resultsShared -> {
                    published = true;
                }),

                when(PleaUpsert.class).apply(pleaUpsert -> {
                    pleas.put(pleaUpsert.getOffenceId(),
                            Plea.plea()
                                    .setOriginHearingId(pleaUpsert.getHearingId())
                                    .setOffenceId(pleaUpsert.getOffenceId())
                                    .setValue(pleaUpsert.getValue())
                                    .setPleaDate(pleaUpsert.getPleaDate())
                    );
                }),

                when(VerdictUpsert.class).apply(verdictUpsert -> {
                    verdicts.put(verdictUpsert.getOffenceId(), verdictUpsert);
                }),

                when(ConvictionDateAdded.class).apply(convictionDateAdded -> {
                    this.hearing.getDefendants().stream()
                            .flatMap(d -> d.getOffences().stream())
                            .filter(o -> o.getId().equals(convictionDateAdded.getOffenceId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                            .setConvictionDate(convictionDateAdded.getConvictionDate());
                }),

                when(ConvictionDateRemoved.class).apply(convictionDateRemoved -> {
                    this.hearing.getDefendants().stream()
                            .flatMap(d -> d.getOffences().stream())
                            .filter(o -> o.getId().equals(convictionDateRemoved.getOffenceId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                            .setConvictionDate(null);
                }),

                otherwiseDoNothing()
        );
    }


    public Stream<Object> addProsecutionCounsel(final AddProsecutionCounselCommand prosecutionCounselCommand) {
        return apply(Stream.of(
                ProsecutionCounselUpsert.builder()
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
                DefenceCounselUpsert.builder()
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

        final UUID caseId = this.hearing.getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> offenceId.equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("case id is not present"))
                .getCaseId();

        final List<Object> events = new ArrayList<>();
        events.add(PleaUpsert.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withPleaDate(pleaDate)
                .withValue(pleaValue)
                .build());
        events.add(GUILTY.equalsIgnoreCase(pleaValue) ?
                ConvictionDateAdded.builder()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withOffenceId(offenceId)
                        .withConvictionDate(pleaDate)
                        .build() :
                ConvictionDateRemoved.builder()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withOffenceId(offenceId)
                        .build());
        return apply(events.stream());
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

        if (this.hearing == null || hearingHevents.containsKey(logEventCommand.getHearingEventId())) {

            String reason = hearing == null ? REASON_HEARING_NOT_FOUND :
                    hearingHevents.get(logEventCommand.getHearingEventId()).isDeleted() ? REASON_ALREADY_DELETED : REASON_ALREADY_LOGGED;

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
        if (!hearingHevents.containsKey(logEventCommand.getHearingEventId())) {

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

        if (hearingHevents.get(logEventCommand.getHearingEventId()).isDeleted()) {
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

    public Stream<Object> updateVerdict(UUID hearingId, UUID caseId, UUID offenceId, Verdict verdict) {

        final List<Object> events = new ArrayList<>();

        events.add(VerdictUpsert.builder()
                .withCaseId(caseId)
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withVerdictId(verdict.getId())
                .withVerdictValueId(verdict.getValue().getId())
                .withCategory(verdict.getValue().getCategory())
                .withCode(verdict.getValue().getCode())
                .withDescription(verdict.getValue().getDescription())
                .withNumberOfJurors(verdict.getNumberOfJurors())
                .withNumberOfSplitJurors(verdict.getNumberOfSplitJurors())
                .withUnanimous(verdict.getUnanimous())
                .withVerdictDate(verdict.getVerdictDate())
                .build()
        );

        if (GUILTY.equalsIgnoreCase(verdict.getValue().getCategory())) {
            events.add(new ConvictionDateAdded(caseId, hearingId, offenceId, verdict.getVerdictDate()));
        } else {
            events.add(new ConvictionDateRemoved(caseId, hearingId, offenceId));
        }
        return apply(events.stream());
    }

    public Stream<Object> shareResults(final ShareResultsCommand command, final ZonedDateTime sharedTime) {
        return apply(Stream.of(ResultsShared.builder()
                .withHearingId(command.getHearingId())
                .withSharedTime(sharedTime)
                .withResultLines(command.getResultLines())
                .withHearing(this.hearing)
                .withCases(this.cases)
                .withProsecutionCounsels(this.prosecutionCounsels)
                .withDefenceCounsels(this.defenceCounsels)
                .withPleas(this.pleas)
                .withVerdicts(this.verdicts)
                .build()));
    }

    public Stream<Object> addWitness(UUID hearingId, UUID witnessId, String type, String classification, String title, String firstName, String lastName, List<DefendantId> defendantIdList) {
        return apply(Stream.of(new WitnessAdded(witnessId, hearingId, type, classification, title, firstName,
                lastName, defendantIdList.stream().map(DefendantId::getDefendantId)
                .collect(Collectors.toList()))));
    }

    public Stream<Object> generateNows(final NowsRequested nowsRequested) {
        return apply(Stream.of(nowsRequested));
    }

    public Stream<Object> updateDefendantDetails(CaseDefendantDetailsWithHearingCommand command) {

        if (!isPublished()) {

            final Defendant defendant = command.getDefendants();

            final Address address = defendant.getAddress();

            final Interpreter interpreter = defendant.getInterpreter();

            return apply(
                    Stream.of(
                            DefendantDetailsUpdated.builder()
                                    .withCaseId(command.getCaseId())
                                    .withHearingId(command.getHearingIds().get(0))
                                    .withDefendant(Defendant.builder()
                                            .withId(defendant.getId())
                                            .withPersonId(defendant.getPersonId())
                                            .withFirstName(defendant.getFirstName())
                                            .withLastName(defendant.getLastName())
                                            .withNationality(defendant.getNationality())
                                            .withGender(defendant.getGender())
                                            .withAddress(Address.address()
                                                    .withAddress1(address.getAddress1())
                                                    .withAddress2(address.getAddress2())
                                                    .withAddress3(address.getAddress3())
                                                    .withAddress4(address.getAddress4())
                                                    .withPostcode(address.getPostCode()))
                                            .withDateOfBirth(defendant.getDateOfBirth())
                                            .withBailStatus(defendant.getBailStatus())
                                            .withCustodyTimeLimitDate(defendant.getCustodyTimeLimitDate())
                                            .withDefenceOrganisation(defendant.getDefenceOrganisation())
                                            .withInterpreter(Interpreter.interpreter()
                                                    .withLanguage(interpreter.getLanguage())
                                                    .withNeeded(interpreter.getNeeded())))
                                    .build()));
        }

        return Stream.empty();
    }

    public boolean isPublished() {
        return published;
    }

    public static final class HearingEvent implements Serializable {

        private static final long serialVersionUID = 1L;

        private boolean deleted;
        private HearingEventLogged hearingEventLogged;

        public HearingEvent(final HearingEventLogged hearingEventLogged) {
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
