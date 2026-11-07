package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.CourtSchedulerStub.releasedBookingIds;
import static uk.gov.moj.cpp.hearing.utils.CourtSchedulerStub.stubReleaseUnconfirmedBookings;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;

import java.io.IOException;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * LPT-2617, end to end: a draft save that leaves no line carrying a booking makes courtscheduler
 * release that booking's hold. Worth an IT on top of the aggregate tests because the release goes
 * through an event the aggregate builds itself, the processor's schema for it, its subscription
 * and the HTTP call - any of which can drop it without the save failing.
 */
public class ReleaseDroppedBookingOnDraftSaveIT extends AbstractIT {

    private static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";

    @BeforeEach
    public void setUp() {
        stubReleaseUnconfirmedBookings();
    }

    @Test
    public void shouldReleaseTheBookingADraftSaveNoLongerCarries() throws IOException {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final UUID hearingId = randomUUID();
        final UUID resultLineId = randomUUID();
        final String hearingDay = "2021-03-01";
        final String bookingId = randomUUID().toString();

        saveDraftResult(hearingId, hearingDay, resultLineId, 1, bookingId);
        assertThat(releasedBookingIds(), not(hasItem(bookingId)));

        // the line attached to an existing hearing: its prompts are replaced and the reference is gone
        saveDraftResult(hearingId, hearingDay, resultLineId, 2, null);

        await().timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(() -> releasedBookingIds().contains(bookingId), is(true));
    }

    private void saveDraftResult(final UUID hearingId, final String hearingDay, final UUID resultLineId,
                                 final int version, final String bookingReference) throws IOException {
        final JsonObject template = new StringToJsonObjectConverter().convert(getStringFromResource("hearing.save-draft-result-v2.json")
                .replaceAll("RESULT_LINE_ID", resultLineId.toString())
                .replaceAll("HEARING_ID", hearingId.toString())
                .replaceAll("CASE_ID", randomUUID().toString())
                .replaceAll("OFFENCE_ID", randomUUID().toString()));

        final JsonObject line = template.getJsonObject("resultLines").getJsonObject(resultLineId.toString());
        final JsonObjectBuilder lineBuilder = createObjectBuilder();
        line.forEach(lineBuilder::add);
        if (bookingReference != null) {
            final JsonArrayBuilder prompts = createArrayBuilder();
            line.getJsonArray("resultPrompts").forEach(prompts::add);
            lineBuilder.add("resultPrompts", prompts.add(createObjectBuilder()
                    .add("promptRef", "bookingReference")
                    .add("type", "TXT")
                    .add("value", bookingReference)));
        }

        final JsonObjectBuilder draft = createObjectBuilder();
        template.forEach(draft::add);
        draft.add("version", version);
        draft.add("resultLines", createObjectBuilder().add(resultLineId.toString(), lineBuilder));

        try (final EventListener draftSaved = listenFor(PUBLIC_HEARING_DRAFT_RESULT_SAVED)
                .withFilter(convertStringTo(PublicHearingDraftResultSaved.class, isBean(PublicHearingDraftResultSaved.class)
                        .with(PublicHearingDraftResultSaved::getHearingId, is(hearingId))))) {
            makeCommand(getRequestSpec(), "hearing.save-draft-result-v2")
                    .ofType("application/vnd.hearing.save-draft-result-v2+json")
                    .withArgs(hearingId, hearingDay)
                    .withPayload(draft.build().toString())
                    .executeSuccessfully();

            draftSaved.waitFor();
        }
    }
}
