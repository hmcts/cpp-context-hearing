package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots.Reason.DROPPED_FROM_DRAFT;
import static uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots.Reason.RELEASED_BY_USER;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ManageResultsFailed;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Release-on-save (LPT-2617): a slot hold is taken the moment a session is picked, and its
 * bookingId is then carried only by the result line's {@code bookingReference} prompt. Every draft
 * save carries the whole draft, so a reference that was in the previous save of a hearing day and
 * is on no line of the next one has been given up - whichever screen did it (attach to a related
 * hearing, change of result, delete, reset) - and its hold must be released.
 */
class HearingAggregateDraftBookingReleaseTest {

    private static final String BOOKING_A = "11111111-1111-1111-1111-111111111111";
    private static final String BOOKING_B = "22222222-2222-2222-2222-222222222222";

    private final UUID userId = randomUUID();
    private final LocalDate hearingDay = LocalDate.of(2026, 10, 24);

    // the aggregate checks each save's version against the last one accepted for that day
    private final Map<LocalDate, Integer> versions = new HashMap<>();

    private HearingAggregate hearingAggregate;
    private UUID hearingId;

    @BeforeEach
    void setUp() {
        hearingAggregate = new HearingAggregate();
        final var hearing = standardInitiateHearingTemplate().getHearing();
        hearingId = hearing.getId();
        hearingAggregate.apply(new HearingInitiated(hearing));
    }

    @Test
    void shouldReleaseABookingWhenItsLineIsAttachedToAnExistingHearing() {
        save(hearingDay, draft(line("line-1", BOOKING_A)));

        final List<Object> events = save(hearingDay, draft(attachedLine("line-1")));

        assertThat(events, hasSize(2));
        assertThat(events.get(0), instanceOf(DraftResultSavedV2.class));
        final ReleaseProvisionalHearingSlots release = (ReleaseProvisionalHearingSlots) events.get(1);
        assertThat(release.getHearingId(), is(hearingId));
        assertThat(release.getBookingIds(), contains(BOOKING_A));
        assertThat(release.getReason(), is(DROPPED_FROM_DRAFT));
    }

    @Test
    void shouldReleaseNothingWhileTheLineStillCarriesTheBooking() {
        save(hearingDay, draft(line("line-1", BOOKING_A)));

        // a re-pick reuses the id, and any other edit leaves the prompt as it was
        assertThat(releasesIn(save(hearingDay, draft(line("line-1", BOOKING_A), line("line-2", null)))), is(empty()));
    }

    @Test
    void shouldReleaseNothingOnTheFirstSaveOfADay() {
        assertThat(releasesIn(save(hearingDay, draft(line("line-1", BOOKING_A)))), is(empty()));
    }

    @Test
    void shouldKeepABookingThatAnotherLineStillCarries() {
        // a copied result keeps every prompt but sharedDate, so both lines point at one booking
        save(hearingDay, draft(line("line-1", BOOKING_A), line("line-2", BOOKING_A)));

        assertThat(releasesIn(save(hearingDay, draft(attachedLine("line-1"), line("line-2", BOOKING_A)))), is(empty()));

        final List<ReleaseProvisionalHearingSlots> releases =
                releasesIn(save(hearingDay, draft(attachedLine("line-1"), attachedLine("line-2"))));
        assertThat(releases, hasSize(1));
        assertThat(releases.get(0).getBookingIds(), contains(BOOKING_A));
    }

    @Test
    void shouldReleaseTheOldBookingWhenALineMovesToAFreshOne() {
        save(hearingDay, draft(line("line-1", BOOKING_A)));

        final List<ReleaseProvisionalHearingSlots> releases = releasesIn(save(hearingDay, draft(line("line-1", BOOKING_B))));

        assertThat(releases, hasSize(1));
        assertThat(releases.get(0).getBookingIds(), contains(BOOKING_A));
    }

    @Test
    void shouldReleaseEveryDroppedBookingInOneEvent() {
        save(hearingDay, draft(line("line-1", BOOKING_A), line("line-2", BOOKING_B)));

        final List<ReleaseProvisionalHearingSlots> releases = releasesIn(save(hearingDay, draft()));

        assertThat(releases, hasSize(1));
        assertThat(releases.get(0).getBookingIds(), containsInAnyOrder(BOOKING_A, BOOKING_B));
    }

    @Test
    void shouldKeepABookingThatAnotherHearingDaysDraftStillCarries() {
        final LocalDate otherDay = hearingDay.plusDays(1);
        save(hearingDay, draft(line("line-1", BOOKING_A)));
        save(otherDay, draft(line("line-9", BOOKING_A)));

        assertThat(releasesIn(save(hearingDay, draft(attachedLine("line-1")))), is(empty()));
    }

    @Test
    void shouldReleaseNothingWhenTheSaveIsRefused() {
        save(hearingDay, draft(line("line-1", BOOKING_A)));

        final List<Object> refused = hearingAggregate.saveDraftResultV2(userId, draft(attachedLine("line-1")),
                hearingId, hearingDay, versions.get(hearingDay) + 5, Collections.emptyMap(), null).collect(toList());

        assertThat(refused, hasSize(1));
        assertThat(refused.get(0), instanceOf(ManageResultsFailed.class));

        // the refused save changed nothing, so the next accepted one still sees the booking drop
        final List<ReleaseProvisionalHearingSlots> releases = releasesIn(save(hearingDay, draft(attachedLine("line-1"))));
        assertThat(releases, hasSize(1));
        assertThat(releases.get(0).getBookingIds(), contains(BOOKING_A));
    }

    @Test
    void shouldReadBookingReferencesFromALegacyArrayOfResultLines() {
        save(hearingDay, createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("resultLines", createArrayBuilder().add(lineJson("line-1", BOOKING_A)))
                .build());

        assertThat(releasesIn(save(hearingDay, draft())).get(0).getBookingIds(), contains(BOOKING_A));
    }

    @Test
    void shouldIgnoreABlankBookingReference() {
        save(hearingDay, draft(line("line-1", "  ")));

        assertThat(releasesIn(save(hearingDay, draft())), is(empty()));
    }

    /**
     * A shared line deleted during an amendment is not removed from the draft: the UI keeps it,
     * prompts and all, flagged {@code deleted} so the re-share can withdraw it. Its booking is no
     * longer wanted - e.g. one picked by re-listing the line during the amendment - so it must be
     * released all the same. (A booking a share confirmed is untouched: courtscheduler releases
     * only unconfirmed holds.)
     */
    @Test
    void shouldReleaseTheBookingOfALineDeletedDuringAnAmendment() {
        save(hearingDay, draft(line("line-1", BOOKING_A)));

        final List<ReleaseProvisionalHearingSlots> releases =
                releasesIn(save(hearingDay, draft(deleted(line("line-1", BOOKING_A)))));

        assertThat(releases, hasSize(1));
        assertThat(releases.get(0).getBookingIds(), contains(BOOKING_A));
    }

    @Test
    void shouldKeepABookingADeletedLineSharesWithALiveOne() {
        save(hearingDay, draft(line("line-1", BOOKING_A), line("line-2", BOOKING_A)));

        assertThat(releasesIn(save(hearingDay, draft(deleted(line("line-1", BOOKING_A)), line("line-2", BOOKING_A)))),
                is(empty()));
    }

    @Test
    void shouldReleaseEveryBookingOfTheDayWhenItsDraftIsDeleted() {
        save(hearingDay, draft(line("line-1", BOOKING_A), line("line-2", BOOKING_B)));
        givenTheClerkIsAmendingTheHearing();

        final List<Object> events = hearingAggregate.deleteDraftResultV2(userId, hearingId, hearingDay).collect(toList());

        assertThat(events, hasSize(2));
        assertThat(events.get(0), instanceOf(DraftResultDeletedV2.class));
        final ReleaseProvisionalHearingSlots release = (ReleaseProvisionalHearingSlots) events.get(1);
        assertThat(release.getBookingIds(), containsInAnyOrder(BOOKING_A, BOOKING_B));
        assertThat(release.getReason(), is(DROPPED_FROM_DRAFT));

        // nothing is left to diff against
        assertThat(releasesIn(save(hearingDay, draft())), is(empty()));
    }

    @Test
    void shouldReleaseNothingWhenTheDraftDeleteIsRefused() {
        save(hearingDay, draft(line("line-1", BOOKING_A)));
        ReflectionUtil.setField(hearingAggregate, "hearingState", HearingState.INITIALISED);

        final List<Object> events = hearingAggregate.deleteDraftResultV2(userId, hearingId, hearingDay).collect(toList());

        assertThat(releasesIn(events), is(empty()));
    }

    @Test
    void shouldReleaseABookingTheUserGaveUp() {
        final List<Object> events = hearingAggregate.releaseProvisionalHearingSlots(hearingId, BOOKING_A).collect(toList());

        assertThat(events, hasSize(1));
        final ReleaseProvisionalHearingSlots release = (ReleaseProvisionalHearingSlots) events.get(0);
        assertThat(release.getHearingId(), is(hearingId));
        assertThat(release.getBookingIds(), contains(BOOKING_A));
        assertThat(release.getReason(), is(RELEASED_BY_USER));
    }

    private List<Object> save(final LocalDate day, final JsonObject draftResult) {
        final Integer version = versions.merge(day, 1, Integer::sum);
        return hearingAggregate.saveDraftResultV2(userId, draftResult, hearingId, day, version, Collections.emptyMap(), null)
                .collect(toList());
    }

    /** A draft can only be deleted once the hearing has left INITIALISED, i.e. by the clerk amending it. */
    private void givenTheClerkIsAmendingTheHearing() {
        ReflectionUtil.setField(hearingAggregate, "hearingState", HearingState.SHARED_AMEND_LOCKED_USER_ERROR);
        ReflectionUtil.setField(hearingAggregate, "amendingSharedHearingUserId", userId);
    }

    private static List<ReleaseProvisionalHearingSlots> releasesIn(final List<Object> events) {
        return events.stream()
                .filter(ReleaseProvisionalHearingSlots.class::isInstance)
                .map(ReleaseProvisionalHearingSlots.class::cast)
                .collect(toList());
    }

    private JsonObject draft(final JsonObject... lines) {
        final JsonObjectBuilder resultLines = createObjectBuilder();
        for (final JsonObject line : lines) {
            resultLines.add(line.getString("resultLineId"), line);
        }
        return createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("hearingDay", hearingDay.toString())
                .add("resultLines", resultLines)
                .build();
    }

    private static JsonObject line(final String resultLineId, final String bookingReference) {
        return lineJson(resultLineId, bookingReference);
    }

    /** What omitResultLine does to a shared line: it stays in the draft, flagged deleted. */
    private static JsonObject deleted(final JsonObject line) {
        final JsonObjectBuilder builder = createObjectBuilder();
        line.forEach(builder::add);
        return builder.add("deleted", true).build();
    }

    /** What related-hearings.container.ts leaves behind: the prompts are replaced, the reference is gone. */
    private static JsonObject attachedLine(final String resultLineId) {
        return createObjectBuilder()
                .add("resultLineId", resultLineId)
                .add("resultPrompts", createArrayBuilder()
                        .add(prompt("HDATE", "2026-11-02"))
                        .add(prompt("existingHearingId", randomUUID().toString())))
                .build();
    }

    private static JsonObject lineJson(final String resultLineId, final String bookingReference) {
        final JsonArrayBuilder prompts = createArrayBuilder().add(prompt("HDATE", "2026-11-02"));
        if (bookingReference != null) {
            prompts.add(prompt("bookingReference", bookingReference));
        }
        return createObjectBuilder()
                .add("resultLineId", resultLineId)
                .add("resultPrompts", prompts)
                .build();
    }

    private static JsonObject prompt(final String promptRef, final String value) {
        return createObjectBuilder().add("promptRef", promptRef).add("value", value).build();
    }
}
