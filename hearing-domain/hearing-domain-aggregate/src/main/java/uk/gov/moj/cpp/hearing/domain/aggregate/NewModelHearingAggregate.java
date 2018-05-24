package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

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

import javax.json.JsonArray;
import javax.json.JsonObject;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
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
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.WitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

@SuppressWarnings({"squid:S00107", "squid:S1602"})
public class NewModelHearingAggregate implements Aggregate {

    private static final String HEARING_EVENTS = "hearingEvents";

    private static final long serialVersionUID = 1L;

    private static final String GUILTY = "GUILTY";
    private static final String REASON_ALREADY_LOGGED = "Already logged";
    private static final String REASON_ALREADY_DELETED = "Already deleted";
    private static final String REASON_EVENT_NOT_FOUND = "Hearing event not found";
    private static final String REASON_HEARING_NOT_FOUND = "Hearing not found";
    private static final String HEARING_EVENT_ID = "hearingEventId";
    private static final String RECORDED_LABEL = "recordedLabel";
    private static final String HEARING_ID = "hearingId";

    private List<Case> cases;
    private Hearing hearing;

    private final Map<UUID, HearingEvent> hearingHevents = new HashMap<>();

    private final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels = new HashMap<>();
    private final Map<UUID, DefenceCounselUpsert> defenceCounsels = new HashMap<>();

    private final Map<UUID, Plea> pleas = new HashMap<>();
    private final Map<UUID, VerdictUpsert> verdicts = new HashMap<>();

    private boolean published = false;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingInitiated.class).apply(initiated -> {
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

    public Stream<Object> initiate(final InitiateHearingCommand initiateHearingCommand) {
        return apply(Stream.of(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
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

    public Stream<Object> logHearingEvent(final LogEventCommand logEventCommand) {

        if (hearing == null) {
            return apply(Stream.of(generateHearingIgnoredMessage(REASON_HEARING_NOT_FOUND, logEventCommand)));
        }

        if (hearingHevents.containsKey(logEventCommand.getHearingEventId())) {
            if (hearingHevents.get(logEventCommand.getHearingEventId()).isDeleted()) {
                return apply(Stream.of(generateHearingIgnoredMessage(REASON_ALREADY_DELETED, logEventCommand)));
            }

            return apply(Stream.of(generateHearingIgnoredMessage(REASON_ALREADY_LOGGED, logEventCommand)));
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
                        logEventCommand.getWitnessId(), logEventCommand.getCounselId())));
    }

    public Stream<Object> updateHearingEvents(final JsonObject payload) {
        final UUID hearingId = fromString(payload.getString(HEARING_ID));
        if (hearing == null) {
            return apply(Stream.of(generateHearingIgnoredMessage(REASON_HEARING_NOT_FOUND,
                            hearingId)));
        }

        final List<uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent> hearingEvents =
                        new ArrayList<>();
        final JsonArray hearingEventsArray = payload.getJsonArray(HEARING_EVENTS);
        hearingEventsArray.getValuesAs(JsonObject.class).stream().forEach(hearingEvent -> {

            hearingEvents.add(new uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent(
                            fromString(hearingEvent.getString(HEARING_EVENT_ID)),
                            hearingEvent.getString(RECORDED_LABEL)));
        });
        return hearingEvents.isEmpty() ? Stream.empty()
                        : Stream.of(new HearingEventsUpdated(hearingId, hearingEvents));
    }
    public Stream<Object> correctHearingEvent(final CorrectLogEventCommand logEventCommand) {

        if (hearing == null) {
            return apply(Stream.of(generateHearingIgnoredMessage(REASON_HEARING_NOT_FOUND, logEventCommand)));
        }

        if (!hearingHevents.containsKey(logEventCommand.getHearingEventId())) {

            return apply(Stream.of(generateHearingIgnoredMessage(REASON_EVENT_NOT_FOUND, logEventCommand)));
        }

        if (hearingHevents.get(logEventCommand.getHearingEventId()).isDeleted()) {
            return apply(Stream.of(generateHearingIgnoredMessage(REASON_ALREADY_DELETED, logEventCommand)));
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
                        this.cases.get(0).getUrn(),
                        this.cases.get(0).getCaseId(),
                        logEventCommand.getWitnessId(),
                        logEventCommand.getCounselId())
        ));
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final UUID caseId, final UUID offenceId, final Verdict verdict) {

        final List<Object> events = new ArrayList<>();

        events.add(VerdictUpsert.builder()
                .withCaseId(caseId)
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withVerdictId(verdict.getId())
                .withVerdictValueId(verdict.getValue().getId())
                .withVerdictTypeId(verdict.getValue().getVerdictTypeId())
                .withCategory(verdict.getValue().getCategory())
                .withCategoryType(verdict.getValue().getCategoryType())
                .withLesserOffence(verdict.getValue().getLesserOffence())
                .withCode(verdict.getValue().getCode())
                .withDescription(verdict.getValue().getDescription())
                .withNumberOfJurors(verdict.getNumberOfJurors())
                .withNumberOfSplitJurors(verdict.getNumberOfSplitJurors())
                .withUnanimous(verdict.getUnanimous())
                .withVerdictDate(verdict.getVerdictDate())
                .build()
        );

        final String categoryType = ofNullable(verdict.getValue().getCategoryType()).orElse("");
        
        final LocalDate offenceConvictionDate = this.hearing.getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> offenceId.equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"))
                .getConvictionDate();
        
        if (categoryType.startsWith(GUILTY)) {
            if (offenceConvictionDate == null) {
                events.add(new ConvictionDateAdded(caseId, hearingId, offenceId, verdict.getVerdictDate()));
            }
        } else {
            if (offenceConvictionDate != null) {
                events.add(new ConvictionDateRemoved(caseId, hearingId, offenceId));
            }
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

    public Stream<Object> updateDefendantDetails(final CaseDefendantDetailsWithHearingCommand command) {

        if (!isPublished()) {

            return apply(
                    Stream.of(
                            DefendantDetailsUpdated.builder()
                                    .withCaseId(command.getCaseId())
                                    .withHearingId(command.getHearingIds().get(0))
                                    .withDefendant(Defendant.builder(command.getDefendant()))
                                    .build()));
        }

        return Stream.empty();
    }

    public boolean isPublished() {
        return published;
    }

    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID caseId, final UpdatedOffence offence) {

        if(!published) {
            return apply(Stream.of(OffenceAdded.builder()
                    .withId(offence.getId())
                    .withHearingId(hearingId)
                    .withDefendantId(defendantId)
                    .withCaseId(caseId)
                    .withOffenceCode(offence.getOffenceCode())
                    .withWording(offence.getWording())
                    .withStartDate(offence.getStartDate())
                    .withEndDate(offence.getEndDate())
                    .withCount(offence.getCount())
                    .withConvictionDate(offence.getConvictionDate())
                    .build()));
        }

        return apply(Stream.empty());
    }

    public Stream<Object> updateOffence(final UUID hearingId, final UpdatedOffence offence) {

        if(!published) {
            return apply(Stream.of(OffenceUpdated.builder()
                    .withHearingId(hearingId)
                    .withId(offence.getId())
                    .withOffenceCode(offence.getOffenceCode())
                    .withWording(offence.getWording())
                    .withStartDate(offence.getStartDate())
                    .withEndDate(offence.getEndDate())
                    .withCount(offence.getCount())
                    .withConvictionDate(offence.getConvictionDate())
                    .build()));
        }

        return apply(Stream.empty());
    }

    public Stream<Object> deleteOffence(final UUID offenceId, final UUID hearingId) {

        if(!published) {
            return apply(Stream.of(OffenceDeleted.builder()
                    .withId(offenceId)
                    .withHearingId(hearingId)
                    .build()));
        }

        return apply(Stream.empty());
    }

    public static final class HearingEvent implements Serializable {

        private static final long serialVersionUID = 1L;

        private boolean deleted;
        private final HearingEventLogged hearingEventLogged;

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


    public Stream<Object> addWitness(final UUID hearingId, final UUID witnessId, final String type, final String classification, final String title, final String firstName, final String lastName, final List<DefendantId> defendantIdList) {
        return apply(Stream.of(new WitnessAdded(
                witnessId,
                hearingId,
                type,
                classification,
                title,
                firstName,
                lastName,
                defendantIdList.stream()
                        .map(DefendantId::getDefendantId)
                        .collect(Collectors.toList())
        )));
    }

    public Stream<Object> generateNows(final NowsRequested nowsRequested) {
        return apply(Stream.of(nowsRequested));
    }

    public Stream<Object> nowsMaterialStatusUpdated(final NowsMaterialStatusUpdated nowsRequested) {
        return apply(Stream.of(nowsRequested));
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

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason,final UUID hearingId) {
        return new HearingEventIgnored(
                        hearingId,
                        reason
        );
    }
}
