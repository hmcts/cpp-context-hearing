package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
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
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

/**
 * Verifies the xhibit court list export honours per-day court room overrides.
 *
 * Hearing has two days, both within the same court centre but with different
 * court rooms — top-level/day 1 in courtRoom1Id ("CourtRoom 1"), day 2 in
 * courtRoom2Id ("CourtRoom 2"). Logs an event on day 2, then publishes the
 * court list. Asserts the generated WebPage XML places the hearing under
 * "CourtRoom 2" (day 2's room) — not "CourtRoom 1" (the hearing's top-level).
 */
@NotThreadSafe
public class PublishCourtListMultidayPerDayRoomIT extends AbstractPublishLatestCourtCentreHearingIT {

    @Test
    public void publishCourtList_shouldPlaceHearingUnderPerDayRoom_whenHearingDayHasOverrideRoom() throws NoSuchAlgorithmException {
        final UUID hearingEventId = randomUUID();
        final UUID defenceCounselId = randomUUID();
        final ZonedDateTime today = now(ZoneOffset.UTC);
        final ZonedDateTime yesterday = today.minusDays(1);

        // Use a unique createdTime far in the future so the published file path is unique to this test
        final String createdTime = "2099-12-31T23:59:59.999Z";
        final String fileNameDatePart = "20991231235959";

        stubOrganisationalUnit(fromString(courtCentreId), "OUCODE");

        // Build a multi-day hearing where day 1 (yesterday) is in courtRoom1Id (top-level)
        // and day 2 (today) is in courtRoom2Id — distinct rooms within the same centre.
        final InitiateHearingCommand initCmd = initiateHearingTemplateWithParam(
                fromString(courtCentreId),
                fromString(courtRoom1Id),
                "CourtRoom 1",
                today.toLocalDate(),
                defenceCounselId,
                caseId,
                of(hearingTypeId)
        );

        final HearingDay day1 = HearingDay.hearingDay()
                .withSittingDay(yesterday)
                .withListingSequence(1)
                .withListedDurationMinutes(60)
                .withCourtCentreId(fromString(courtCentreId))
                .withCourtRoomId(fromString(courtRoom1Id))
                .build();
        final HearingDay day2 = HearingDay.hearingDay()
                .withSittingDay(today)
                .withListingSequence(2)
                .withListedDurationMinutes(60)
                .withCourtCentreId(fromString(courtCentreId))
                .withCourtRoomId(fromString(courtRoom2Id))
                .build();
        initCmd.getHearing().setHearingDays(asList(day1, day2));

        final InitiateHearingCommandHelper hearing = h(initiateHearing(getRequestSpec(), initCmd));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());

        // Log an event on today (day 2) so the publish picks up the hearing for today's date
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(),
                OPEN_CASE_PROSECUTION_EVENT_DEFINITION_ID, false, defenceCounselId, today, null);

        // Publish the court list for today
        final JsonObject publishCourtListJson = javax.json.Json.createObjectBuilder()
                .add("courtCentreId", courtCentreId)
                .add("createdTime", createdTime)
                .build();

        sendPublishCourtListCommand(publishCourtListJson, courtCentreId);

        // Wait for the publish flow to complete (status becomes EXPORT_SUCCESSFUL)
        new PublishCourtListSteps().verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);

        // Filename format: WebPage_<crestCourtId>_<uuuuMMddHHmmss>.xml — use a wildcard for the crest part
        final String filePath = "/xhibit-gateway/send-to-xhibit/WebPage.*" + fileNameDatePart + ".*\\.xml";
        final String filePayload = getFileForPath(filePath);

        // Day 2 is in CourtRoom 2 — the published XML must place the hearing under that room
        assertThat(filePayload, containsString("CourtRoom 2"));
        // Without the fix, the hearing would have been grouped under CourtRoom 1 (top-level);
        // with the fix, only CourtRoom 2 should appear as the hearing's room
        assertThat(filePayload, not(containsString("CourtRoom 1")));
    }
}
