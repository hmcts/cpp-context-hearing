package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationalUnit;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getFileForPath;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

/**
 * Verifies the xhibit court list export honours per-day court room overrides.
 *
 * The hearing has two days within the same court centre but with different
 * court rooms — top-level/day 1 in courtRoom4Id ("CourtRoom 4"), day 2 in
 * courtRoom2Id ("CourtRoom 2"). Logs an event on day 2, then publishes the
 * court list. Asserts the generated WebPage XML places the hearing under
 * "CourtRoom 2" (day 2's room) — not "CourtRoom 1" (an unrelated room that
 * must not appear in this hearing's bucket).
 *
 * <p>Uses {@code courtCentreId_2} rather than the default {@code courtCentreId}
 * so the day-level room override here does not share a SQL bucket with
 * {@code PublishLatestCourtCentreHearingEventsIT.shouldRequestToPublishCourtListOpenCaseProsecution}'s
 * hearing — {@code findLatestHearingsForThatDayByCourt} groups by
 * {@code coalesce(day.court_room_id, hearing.room_id)}, so both running in the
 * same centre with overlapping rooms would collide. The OU cache populated by
 * the abstract base's {@code @BeforeAll} ensures the publish flow's lookups for
 * {@code courtCentreId_2} resolve correctly.
 */
@NotThreadSafe
public class PublishCourtListMultidayPerDayRoomIT extends AbstractPublishLatestCourtCentreHearingIT {

    @Test
    public void publishCourtList_shouldPlaceHearingUnderPerDayRoom_whenHearingDayHasOverrideRoom() throws NoSuchAlgorithmException {
        final UUID hearingEventId = randomUUID();
        final UUID defenceCounselId = randomUUID();
        final ZonedDateTime today = now(ZoneOffset.UTC);
        final ZonedDateTime yesterday = today.minusDays(1);

        // Use a unique createdTime far in the future so the published file path is unique to this test.
        final String createdTime = "2099-12-31T23:59:59.999Z";
        final String fileNameDatePart = "20991231235959";

        stubOrganisationalUnit(fromString(courtCentreId_2), "OUCODE");

        // Build a multi-day hearing in courtCentreId_2.
        // Top-level / day 1 (yesterday) sits in courtRoom4Id ("CourtRoom 4");
        // day 2 (today) overrides to courtRoom2Id ("CourtRoom 2"). Avoids
        // courtRoom1Id at top level so the not(containsString("CourtRoom 1"))
        // assertion remains valid.
        final InitiateHearingCommand initCmd = initiateHearingTemplateWithParam(
                fromString(courtCentreId_2),
                fromString(courtRoom4Id),
                "CourtRoom 4",
                today.toLocalDate(),
                defenceCounselId,
                caseId,
                of(hearingTypeId)
        );

        final HearingDay day1 = HearingDay.hearingDay()
                .withSittingDay(yesterday)
                .withListingSequence(1)
                .withListedDurationMinutes(60)
                .withCourtCentreId(fromString(courtCentreId_2))
                .withCourtRoomId(fromString(courtRoom4Id))
                .build();
        final HearingDay day2 = HearingDay.hearingDay()
                .withSittingDay(today)
                .withListingSequence(2)
                .withListedDurationMinutes(60)
                .withCourtCentreId(fromString(courtCentreId_2))
                .withCourtRoomId(fromString(courtRoom2Id))
                .build();
        initCmd.getHearing().setHearingDays(asList(day1, day2));

        final InitiateHearingCommandHelper hearing = h(initiateHearing(getRequestSpec(), initCmd));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());

        // Log an event on today (day 2) so the publish picks up the hearing for today's date.
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, false, defenceCounselId, today, null);

        final JsonObject publishCourtListJson = Json.createObjectBuilder()
                .add("courtCentreId", courtCentreId_2)
                .add("createdTime", createdTime)
                .build();

        sendPublishCourtListCommand(publishCourtListJson, courtCentreId_2);

        new PublishCourtListSteps().verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId_2);

        // Filename format: WebPage_<crestCourtId>_<uuuuMMddHHmmss>.xml — wildcard the crest part.
        final String filePath = "/xhibit-gateway/send-to-xhibit/WebPage.*" + fileNameDatePart + ".*\\.xml";
        final String filePayload = getFileForPath(filePath);

        // Day 2 is in CourtRoom 2 — the published XML must place the hearing under that room.
        assertThat(filePayload, containsString("CourtRoom 2"));
        // Without the fix, the hearing would have been grouped under CourtRoom 1 (top-level);
        // with the fix, only CourtRoom 2 should appear as the hearing's room.
        assertThat(filePayload, not(containsString("CourtRoom 1")));
    }
}
