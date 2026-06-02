package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubApplicationsByParentId;

import com.jayway.jsonpath.matchers.JsonPathMatchers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage for SPRDT-870 / SPRDT-892.
 *
 * Prod symptom: a second {@code hearing.events.initiated} on the same hearing stream caused
 * a JPA merge of a freshly-built {@code Hearing} entity (with empty {@code hearingApplications}
 * collection) onto the managed one. Combined with {@code @OneToMany(orphanRemoval = true)}, the
 * existing {@code ha_hearing_application} link row was DELETE'd at flush. Downstream,
 * progression's {@code linked-application-hearings-for-court-extract} query then rendered an
 * empty {@code IN ()} and threw {@code SQLGrammarException}.
 *
 * The fix changed {@code InitiateHearingEventListener.newHearingInitiated} so the entity is
 * only built/mutated/saved on the first-time path; re-delivery only reconciles guarded link rows.
 */
@SuppressWarnings("java:S2699")
public class DuplicateInitiatedEventLinkPreservedIT extends AbstractIT {

    private static final String APPLICATION_TIMELINE_MEDIA_TYPE = "application/vnd.hearing.application.timeline+json";

    @Test
    public void linkRow_isQueryable_afterSingleInit_baseline() {
        final InitiateHearingCommandHelper helper =
                h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final Hearing hearing = helper.getHearing();
        final UUID applicationId = hearing.getCourtApplications().get(0).getId();

        stubApplicationsByParentId(applicationId);

        pollApplicationTimelineForHearing(applicationId, hearing.getId());
    }

    /**
     * The fix's true regression assertion: after a duplicate {@code hearing.events.initiated} hits
     * the listener, the link row must still be present and the timeline query must still return
     * the hearing. Disabled until the IT harness has a way to re-deliver internal events to the
     * listener — internal events are routed via the framework's local subscription registry, not
     * the {@code public.event} topic, and no existing IT in this module re-emits internal events.
     *
     * Suggested approaches when this is picked up:
     * <ul>
     *   <li>Use a {@code @Subscription} test helper to push the same event into the event stream
     *       a second time, or</li>
     *   <li>Add a test-only REST hook on the hearing service that invokes the listener method
     *       directly with the original payload, or</li>
     *   <li>Reproduce the upstream path that emits two {@code hearing.events.initiated} (the
     *       non-boxwork application flow documented in the SPRDT-870 investigation).</li>
     * </ul>
     */
    @Test
    @Disabled("SPRDT-870 follow-up: requires a way to re-emit hearing.events.initiated to the local listener — see class javadoc")
    public void linkRow_survives_duplicateInitiatedEvent() {
        final InitiateHearingCommandHelper helper =
                h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final Hearing hearing = helper.getHearing();
        final UUID applicationId = hearing.getCourtApplications().get(0).getId();

        stubApplicationsByParentId(applicationId);

        // Baseline: link row in place after first init.
        pollApplicationTimelineForHearing(applicationId, hearing.getId());

        // TODO(SPRDT-870 follow-up): re-deliver hearing.events.initiated for hearing.getId()
        //   with the original payload. The pre-fix code would orphan-remove the link row here;
        //   the fix should make this a no-op for the entity and only re-attempt link insert
        //   under findBy == null guard.
        // resendInitiatedEvent(hearing);

        // Regression: link row still present, timeline query still returns the hearing.
        pollApplicationTimelineForHearing(applicationId, hearing.getId());
    }

    private void pollApplicationTimelineForHearing(final UUID applicationId, final UUID expectedHearingId) {
        final String endpoint = format(ENDPOINT_PROPERTIES.getProperty("hearing.application.timeline"), applicationId);
        final String url = getBaseUri() + "/" + endpoint;

        poll(requestParams(url, APPLICATION_TIMELINE_MEDIA_TYPE)
                .withHeader(USER_ID, getLoggedInUser())
                .build())
                .timeout(30, SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                JsonPathMatchers.withJsonPath(
                                        "$.hearingSummaries[0].hearingId",
                                        is(expectedHearingId.toString())))));
    }
}
