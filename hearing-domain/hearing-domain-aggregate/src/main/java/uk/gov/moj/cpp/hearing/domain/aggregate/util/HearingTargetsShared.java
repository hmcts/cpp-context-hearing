package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

public class HearingTargetsShared {

    static final String APPLICATION_ID = "applicationId";
    static final String OFFENCE_ID = "offenceId";

    public static boolean hasAllTargetsShared(final LocalDate hearingDay, final List<SharedResultsCommandResultLineV2> resultLines,
                                              final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap, final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap) {

        final boolean isCaseResultType = resultLines.stream().anyMatch(rl -> nonNull(rl.getOffenceId()));
        final boolean isApplicationResultType = resultLines.stream().filter(rl -> isNull(rl.getOffenceId())).anyMatch(rl -> nonNull(rl.getApplicationId()));

        if (isCaseResultType && isApplicationResultType) {
            return isAllOffencesShared(hearingDay, resultLines, hearingDaySharedOffencesMap)
                    && isAllApplicationsShared(hearingDay, resultLines, hearingDaySharedApplicationsMap);
        }

        if (isCaseResultType) {
            return isAllOffencesShared(hearingDay, resultLines, hearingDaySharedOffencesMap);
        }

        if (isApplicationResultType) {
            return isAllApplicationsShared(hearingDay, resultLines, hearingDaySharedApplicationsMap);
        }

        return false;
    }

    public static boolean hasAllTargetsShared(final LocalDate hearingDay, final Map<UUID, JsonObject> resultLines,
                                              final Set<UUID> offencesSavedAsDraft,
                                              final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap,
                                              final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap) {

        final Set<UUID> saveDraftResultsRequestOffenceSet = resultLines.values().stream()
                .filter(rl -> rl.containsKey(OFFENCE_ID))
                .map(rl -> fromString(rl.getString(OFFENCE_ID))).collect(Collectors.toSet());
        final boolean isCaseResultType = resultLines.values().stream().anyMatch(rl -> rl.containsKey(OFFENCE_ID));
        final boolean isApplicationResultType = resultLines.values().stream().filter(rl -> !rl.containsKey(OFFENCE_ID)).anyMatch(rl -> rl.containsKey(APPLICATION_ID));

        if (isCaseResultType && isApplicationResultType) {
            return isAllOffencesShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, saveDraftResultsRequestOffenceSet)
                    && isAllApplicationsShared(hearingDay, resultLines, hearingDaySharedApplicationsMap);
        }

        if (isCaseResultType) {
            return isAllOffencesShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, saveDraftResultsRequestOffenceSet);
        }

        if (isApplicationResultType) {
            return isAllApplicationsShared(hearingDay, resultLines, hearingDaySharedApplicationsMap);
        }

        return false;
    }

    private static boolean isAllOffencesShared(final LocalDate hearingDay, final List<SharedResultsCommandResultLineV2> resultLines, final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap) {
        final Set<UUID> sharedOffenceIdSet = hearingDaySharedOffencesMap.get(hearingDay);
        return nonNull(sharedOffenceIdSet) && resultLines.stream()
                .filter(rl -> nonNull(rl.getOffenceId()))
                .allMatch(rl -> sharedOffenceIdSet.contains(rl.getOffenceId()));
    }

    private static boolean isAllApplicationsShared(final LocalDate hearingDay, final List<SharedResultsCommandResultLineV2> resultLines,
                                                   final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap) {
        final Set<UUID> sharedApplicationIdSet = hearingDaySharedApplicationsMap.get(hearingDay);
        return nonNull(sharedApplicationIdSet) && resultLines.stream()
                .filter(rl -> isNull(rl.getOffenceId()))
                .filter(rl -> nonNull(rl.getApplicationId()))
                .allMatch(rl -> sharedApplicationIdSet.contains(rl.getApplicationId()));
    }

    private static boolean isAllOffencesShared(final LocalDate hearingDay, final Map<UUID, JsonObject> resultLines, final Set<UUID> offencesSavedAsDraft,
                                               final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap, final Set<UUID> saveDraftResultsRequestOffenceSet) {
        final Set<UUID> sharedOffenceIdSet = hearingDaySharedOffencesMap.get(hearingDay);
        return nonNull(sharedOffenceIdSet)
                && resultLines.values().stream()
                .filter(rl -> nonNull(rl.get(OFFENCE_ID)))
                .allMatch(rl -> sharedOffenceIdSet.contains(fromString(rl.getString(OFFENCE_ID))))
                && saveDraftResultsRequestOffenceSet.equals(offencesSavedAsDraft);
    }

    private static boolean isAllApplicationsShared(final LocalDate hearingDay, final Map<UUID, JsonObject> resultLines, final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap) {
        final Set<UUID> sharedApplicationIdSet = hearingDaySharedApplicationsMap.get(hearingDay);
        return nonNull(sharedApplicationIdSet)
                && resultLines.values().stream()
                .filter(rl -> isNull(rl.get(OFFENCE_ID)))
                .filter(rl -> nonNull(rl.get(APPLICATION_ID)))
                .allMatch(rl -> sharedApplicationIdSet.contains(fromString(rl.getString(APPLICATION_ID))));
    }

}
