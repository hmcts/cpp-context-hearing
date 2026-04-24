package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2.sharedResultsCommandResultLine;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.HearingTargetsShared.APPLICATION_ID;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.HearingTargetsShared.OFFENCE_ID;

import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

class HearingTargetsSharedTest {

    @Test
    void givenResultLinesSharedWithOffencesOnlyAndAllOffencesAlreadySharedThenReturnTrue() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withOffenceId(offenceId1)
                        .build(),
                sharedResultsCommandResultLine()
                        .withOffenceId(offenceId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId1, offenceId2));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(true));
    }

    @Test
    void givenResultLinesSharedWithOffencesOnlyAndNotAllOffencesAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withOffenceId(offenceId1)
                        .build(),
                sharedResultsCommandResultLine()
                        .withOffenceId(offenceId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId1));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSharedWithApplicationsOnlyAndAllApplicationsAlreadySharedThenReturnTrue() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID applicationId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of();
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId, applicationId2));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(true));
    }

    @Test
    void givenResultLinesSharedWithApplicationsOnlyAndNotAllApplicationsAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID applicationId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of();
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSharedWithOffencesAndApplicationsAndAllTargetsAlreadySharedThenReturnTrue() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId, offenceId2));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(true));
    }

    @Test
    void givenResultLinesSharedWithOffencesAndApplicationAndAllOffenceTargetsAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId, offenceId2));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSharedWithOffencesAndApplicationsAndApplicationsTargetsNotAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId2)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId, offenceId2));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSharedWithOffencesAndApplicationsAndOffencesTargetsNotAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final List<SharedResultsCommandResultLineV2> resultLines = List.of(sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .build(),
                sharedResultsCommandResultLine()
                        .withApplicationIds(applicationId)
                        .withOffenceId(offenceId)
                        .build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of();
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithOffencesOnlyAndAllOffencesAlreadySharedThenReturnTrue() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(OFFENCE_ID, offenceId1.toString()).build(),
                randomUUID(), createObjectBuilder().add(OFFENCE_ID, offenceId2.toString()).build());
        final Set<UUID> offencesSavedAsDraft = Set.of(offenceId1, offenceId2);
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId1, offenceId2));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(true));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithOffencesOnlyAndNotAllOffencesAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(OFFENCE_ID, offenceId1.toString()).build(),
                randomUUID(), createObjectBuilder().add(OFFENCE_ID, offenceId2.toString()).build());
        final Set<UUID> offencesSavedAsDraft = Set.of(offenceId1, offenceId2);
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId1));
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithApplicationsOnlyAndAllApplicationsAlreadySharedThenReturnTrue() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID applicationId2 = randomUUID();
        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId2.toString()).build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of();
        final Set<UUID> offencesSavedAsDraft = Set.of();
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId, applicationId2));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(true));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithApplicationsOnlyAndNotAllApplicationsAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID applicationId2 = randomUUID();
        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId2.toString()).build());
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of();
        final Set<UUID> offencesSavedAsDraft = Set.of();
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithOffencesAndApplicationsAndAllTargetsAlreadySharedThenReturnTrue() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId2.toString()).build());

        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId, offenceId2));
        final Set<UUID> offencesSavedAsDraft = Set.of(offenceId, offenceId2);
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(true));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithOffencesAndApplicationsAndApplicationsTargetsNotAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId2.toString()).build());

        final Set<UUID> offencesSavedAsDraft = Set.of(offenceId, offenceId2);
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of();
        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId, offenceId2));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithOffencesAndApplicationsAndOffencesTargetsNotAlreadySharedThenReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId2.toString()).build());

        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of();
        final Set<UUID> offencesSavedAsDraft = Set.of(offenceId, offenceId2);
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

    @Test
    void givenResultLinesSaveDraftResultsWithOffencesAndApplications_whenHearingSharedWithApplicationAndOneOffenceResults_AndOffenceTwoSaveDraftResultsReturnFalse() {
        final LocalDate hearingDay = LocalDate.now();
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId2 = randomUUID();
        final Map<UUID, JsonObject> resultLines = Map.of(randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId.toString()).build(),
                randomUUID(), createObjectBuilder().add(APPLICATION_ID, applicationId.toString()).add(OFFENCE_ID, offenceId2.toString()).build());

        final Map<LocalDate, Set<UUID>> hearingDaySharedOffencesMap = Map.of(hearingDay, Set.of(offenceId, offenceId2));
        final Set<UUID> offencesSavedAsDraft = Set.of(offenceId);
        final Map<LocalDate, Set<UUID>> hearingDaySharedApplicationsMap = Map.of(hearingDay, Set.of(applicationId));

        final boolean allTargetsShared = HearingTargetsShared.hasAllTargetsShared(hearingDay, resultLines, offencesSavedAsDraft, hearingDaySharedOffencesMap, hearingDaySharedApplicationsMap);
        assertThat(allTargetsShared, is(false));
    }

}