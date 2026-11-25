package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.ClonedValueRule.applicationTypeCode;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.ClonedValueRule.isCloned;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.ClonedValueRule.isInheritedConvictionDate;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.ClonedValueRule.isShareRuleApplication;
import static uk.gov.moj.cpp.hearing.domain.event.result.AdjustedApplicationOffence.CONVICTION_DATE;
import static uk.gov.moj.cpp.hearing.domain.event.result.AdjustedApplicationOffence.PLEA;
import static uk.gov.moj.cpp.hearing.domain.event.result.AdjustedApplicationOffence.VERDICT;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Target2;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.event.result.AdjustedApplicationOffence;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationClonedValuesAdjusted;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationClonedValuesAdjusted.Action;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationClonedValuesAdjusted.Outcome;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationOffenceValues;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * At share, clears (granted) or restores (not granted) the plea, verdict and conviction date cloned
 * onto a STAT DEC application's offences from an earlier hearing (CHD-3361 AC2, AC5, AC6, AC14). Works on
 * the shared copy of the hearing only; aggregate state keeps the values as entered (work order design D1).
 */
class ApplicationClonedValuesAdjuster {

    private static final UUID G = fromString("2b3f7c20-8fc1-4fad-9076-df196c24b27e");
    private static final UUID STDEC = fromString("e2f6e11b-c3a2-4e76-8d29-fbede4174988");
    private static final UUID RFSD = fromString("d3902789-4cc8-4753-a15f-7e26dd39f6ae");
    private static final UUID DISM = fromString("14d66587-8fbe-424f-a369-b1144f1684e3");
    private static final UUID WDRN = fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");

    private static final Set<UUID> GRANTED_RESULTS = Set.of(G, STDEC);
    private static final Set<UUID> NOT_GRANTED_RESULTS = Set.of(RFSD, DISM, WDRN);
    private static final Comparator<ZonedDateTime> AMENDMENT_ORDER = nullsFirst(Comparator.<ZonedDateTime>naturalOrder());

    private final HearingAggregateMomento momento;

    ApplicationClonedValuesAdjuster(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    /**
     * Replaces the shared copy's court applications with adjusted copies and returns one event per
     * application whose shared values the system changed.
     */
    List<ApplicationClonedValuesAdjusted> adjust(final UUID hearingId, final LocalDate hearingDay, final ZonedDateTime sharedTime,
                                                 final Hearing sharedHearing, final List<SharedResultsCommandResultLineV2> resultLines) {
        if (isNull(sharedHearing.getCourtApplications())) {
            return emptyList();
        }

        final List<ApplicationClonedValuesAdjusted> events = new ArrayList<>();
        final List<CourtApplication> courtApplications = sharedHearing.getCourtApplications().stream()
                .map(courtApplication -> {
                    if (!isShareRuleApplication(courtApplication)) {
                        return courtApplication;
                    }
                    return outcome(courtApplication.getId(), hearingDay, resultLines)
                            .map(outcome -> adjust(hearingId, hearingDay, sharedTime, courtApplication, outcome, events))
                            .orElse(courtApplication);
                })
                .collect(toList());
        sharedHearing.setCourtApplications(courtApplications);
        return events;
    }

    private CourtApplication adjust(final UUID hearingId, final LocalDate hearingDay, final ZonedDateTime sharedTime,
                                    final CourtApplication courtApplication, final Outcome outcome,
                                    final List<ApplicationClonedValuesAdjusted> events) {
        final Map<UUID, ApplicationOffenceValues> asLoaded = momento.getApplicationOffencesAsLoaded().getOrDefault(courtApplication.getId(), emptyMap());
        final Map<UUID, ApplicationOffenceValues> lastShared = momento.getLastSharedApplicationOffences().getOrDefault(courtApplication.getId(), emptyMap());
        final Map<UUID, ApplicationOffenceValues> enteredAtLastShare = momento.getLastSharedApplicationOffencesAsEntered().getOrDefault(courtApplication.getId(), emptyMap());
        final List<AdjustedApplicationOffence> adjustedOffences = new ArrayList<>();

        final List<CourtApplicationCase> courtApplicationCases = ofNullable(courtApplication.getCourtApplicationCases()).orElse(emptyList()).stream()
                .map(courtApplicationCase -> CourtApplicationCase.courtApplicationCase()
                        .withValuesFrom(courtApplicationCase)
                        .withOffences(ofNullable(courtApplicationCase.getOffences()).map(offences -> offences.stream()
                                .map(offence -> {
                                    final ApplicationOffenceValues entered = ApplicationOffenceValues.of(offence);
                                    final ApplicationOffenceValues shared = outcome == Outcome.GRANTED
                                            ? withoutClonedValues(offence, hearingId, courtApplication)
                                            : asLoaded.getOrDefault(offence.getId(), entered);
                                    final ApplicationOffenceValues previous = lastShared.getOrDefault(offence.getId(), entered);
                                    final List<String> systemChanges = systemChanges(previous, shared, enteredAtLastShare.getOrDefault(offence.getId(), entered), entered);
                                    if (!systemChanges.isEmpty()) {
                                        adjustedOffences.add(new AdjustedApplicationOffence(offence.getId(), systemChanges, previous, shared));
                                    }
                                    return shared.equals(entered) ? offence : Offence.offence()
                                            .withValuesFrom(offence)
                                            .withPlea(shared.getPlea())
                                            .withVerdict(shared.getVerdict())
                                            .withConvictionDate(shared.getConvictionDate())
                                            .build();
                                })
                                .collect(toList())).orElse(null))
                        .build())
                .collect(toList());

        if (!adjustedOffences.isEmpty()) {
            events.add(ApplicationClonedValuesAdjusted.builder()
                    .withHearingId(hearingId)
                    .withHearingDay(hearingDay)
                    .withSharedTime(sharedTime)
                    .withApplicationId(courtApplication.getId())
                    .withApplicationTypeCode(applicationTypeCode(courtApplication))
                    .withOutcome(outcome)
                    .withAction(outcome == Outcome.GRANTED ? Action.CLEARED : Action.RESTORED)
                    .withOffences(adjustedOffences)
                    .build());
        }

        return CourtApplication.courtApplication()
                .withValuesFrom(courtApplication)
                .withCourtApplicationCases(courtApplicationCases)
                .build();
    }

    /**
     * Granted: cloned plea and verdict go, values entered at the hearing stay (CHD-3361 AC2). A conviction
     * date earlier than the application is cloned and goes too (CHD-3361 AC4): a guilty plea entered here re-dates
     * it, but a guilty verdict entered at a STAT DEC hearing does not (that rule is REOPEN only).
     */
    private static ApplicationOffenceValues withoutClonedValues(final Offence offence, final UUID hearingId, final CourtApplication courtApplication) {
        final ApplicationOffenceValues entered = ApplicationOffenceValues.of(offence);
        return new ApplicationOffenceValues(
                isCloned(entered.getPlea(), hearingId, courtApplication) ? null : entered.getPlea(),
                isCloned(entered.getVerdict(), hearingId, courtApplication) ? null : entered.getVerdict(),
                isInheritedConvictionDate(entered.getConvictionDate(), courtApplication) ? null : entered.getConvictionDate());
    }

    /**
     * Fields whose shared value differs from the last share, other than those the adviser changed since
     * then and that are now shared as entered. Compared with what was shared rather than the outcome, so
     * a hearing shared before this rule existed records its first clear or restore too.
     */
    private static List<String> systemChanges(final ApplicationOffenceValues previous, final ApplicationOffenceValues shared,
                                              final ApplicationOffenceValues enteredAtLastShare, final ApplicationOffenceValues entered) {
        final List<String> fieldsChanged = new ArrayList<>();
        if (isSystemChange(previous.getPlea(), shared.getPlea(), enteredAtLastShare.getPlea(), entered.getPlea())) {
            fieldsChanged.add(PLEA);
        }
        if (isSystemChange(previous.getVerdict(), shared.getVerdict(), enteredAtLastShare.getVerdict(), entered.getVerdict())) {
            fieldsChanged.add(VERDICT);
        }
        if (isSystemChange(previous.getConvictionDate(), shared.getConvictionDate(), enteredAtLastShare.getConvictionDate(), entered.getConvictionDate())) {
            fieldsChanged.add(CONVICTION_DATE);
        }
        return fieldsChanged;
    }

    private static boolean isSystemChange(final Object previous, final Object shared, final Object enteredAtLastShare, final Object entered) {
        final boolean sharedAsNewlyEntered = !Objects.equals(enteredAtLastShare, entered) && Objects.equals(shared, entered);
        return !Objects.equals(previous, shared) && !sharedAsNewlyEntered;
    }

    /**
     * The latest non-deleted application-level outcome result: from the lines being shared, otherwise
     * from the most recent other hearing day's saved results (work order MULTI-DAY). G and/or STDEC grant
     * (CHD-3361 AC2, AC7, AC15), RFSD/DISM/WDRN do not (AC5); any other result leaves the values alone (AC8).
     */
    private Optional<Outcome> outcome(final UUID applicationId, final LocalDate hearingDay, final List<SharedResultsCommandResultLineV2> resultLines) {
        final Optional<Outcome> sharedNow = latest(resultLines.stream()
                .filter(line -> applicationId.equals(line.getApplicationId()) && isNull(line.getOffenceId()) && !line.getIsDeleted())
                .map(line -> new OutcomeResult(line.getResultDefinitionId(), line.getAmendmentDate())));
        return sharedNow.isPresent() ? sharedNow : outcomeOnOtherDays(applicationId, hearingDay);
    }

    private Optional<Outcome> outcomeOnOtherDays(final UUID applicationId, final LocalDate hearingDay) {
        return momento.getMultiDaySavedTargets().entrySet().stream()
                .filter(day -> !day.getKey().equals(hearingDay))
                .sorted(Map.Entry.<LocalDate, Map<UUID, Target2>>comparingByKey().reversed())
                .map(day -> latest(savedApplicationResults(applicationId, day.getValue())))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Stream<OutcomeResult> savedApplicationResults(final UUID applicationId, final Map<UUID, Target2> targets) {
        return targets.values().stream()
                .map(Target2::getResultLines)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(line -> applicationId.equals(line.getApplicationId()) && isNull(line.getOffenceId()) && !Boolean.TRUE.equals(line.getIsDeleted()))
                .map(line -> new OutcomeResult(line.getResultDefinitionId(), line.getAmendmentDate()));
    }

    private static Optional<Outcome> latest(final Stream<OutcomeResult> results) {
        final List<OutcomeResult> outcomeResults = results
                .filter(result -> GRANTED_RESULTS.contains(result.resultDefinitionId) || NOT_GRANTED_RESULTS.contains(result.resultDefinitionId))
                .collect(toList());
        // stable: of equally dated results the last one wins
        return outcomeResults.stream()
                .reduce((earlier, later) -> AMENDMENT_ORDER.compare(later.amendmentDate, earlier.amendmentDate) >= 0 ? later : earlier)
                .map(result -> GRANTED_RESULTS.contains(result.resultDefinitionId) ? Outcome.GRANTED : Outcome.NOT_GRANTED);
    }

    private static final class OutcomeResult {
        private final UUID resultDefinitionId;
        private final ZonedDateTime amendmentDate;

        private OutcomeResult(final UUID resultDefinitionId, final ZonedDateTime amendmentDate) {
            this.resultDefinitionId = resultDefinitionId;
            this.amendmentDate = amendmentDate;
        }
    }
}
