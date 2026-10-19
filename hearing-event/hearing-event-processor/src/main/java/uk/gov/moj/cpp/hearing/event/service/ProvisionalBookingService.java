package uk.gov.moj.cpp.hearing.event.service;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingFailureCode;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProvisionalBookingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisionalBookingService.class);
    private static final String SERVICE = "/unconfirmedBooking";
    private static final String SESSIONS = "/sessions/";

    public static final String COURTSCHEDULER_CREATE_UNCONFIRMED_BOOKING = "application/vnd.courtscheduler.create.unconfirmed.booking+json";
    public static final String CJS_CPP_UID = "CJSCPPUID";

    /**
     * javax.ws.rs's {@code Response.Status} has no 422 constant (it was only added to JAX-RS in
     * 3.1), so both statuses {@link #failureCodeFor} switches on are spelled out here.
     */
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    /**
     * courtscheduler's {@code GlobalExceptionHandler} answers BOTH {@code NoCapacityException}
     * and {@code ConfirmedBookingExistsException} with 409, and normalises every refusal to the
     * same {@code {"error":"<message>"}} shape. The status therefore cannot tell "this session is
     * full" (re-pick and you may succeed) from "this booking is already confirmed" (re-picking
     * can never succeed, it is the amend journey).
     *
     * <p>This substring of {@code ReservationService#guardAgainstConfirmedAllocation}'s message is
     * the only discriminator available without a courtscheduler contract change. It is a genuine
     * weakness: reword that message upstream and a confirmed-booking conflict quietly becomes
     * {@link ProvisionalBookingFailureCode#NO_CAPACITY}, which is wrong but not dangerous — the
     * clerk sees the no-capacity message rather than the confirmed-booking one. A structured {@code code} on
     * courtscheduler's own error body is the proper fix and belongs in that service.
     */
    private static final String CONFIRMED_ALLOCATION_MARKER = "already has a confirmed allocation";

    @Inject
    @Value(key = "courtscheduler.base.url", defaultValue = "http://localhost:8080/listingcourtscheduler-api/rest/courtscheduler")
    protected String baseUri;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private SystemUserProvider systemUserProvider;


    public ProvisionalBookingServiceResponse bookSlots(final Object payload) {


        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("ProvisionalBooking-bookSlots in S & L");
        }

        final UUID systemUserId = systemUserProvider.getContextSystemUserId().orElseThrow(() -> new IllegalStateException("contextSystemUserId missing!!!"));
        final String contextSystemUserId = systemUserId.toString();

        try {
            final HttpPost httpPost = new HttpPost(new URL(baseUri + SERVICE).toString());
            httpPost.addHeader(CONTENT_TYPE, COURTSCHEDULER_CREATE_UNCONFIRMED_BOOKING);
            httpPost.addHeader(CJS_CPP_UID, contextSystemUserId);

            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString(payload));
            httpPost.setEntity(requestEntity);

            final HttpResponse httpResponse = HttpClientBuilder
                    .create()
                    .build()
                    .execute(httpPost);

            final int status = httpResponse.getStatusLine().getStatusCode();
            // Read the body once, before branching. Both paths need it: on OK it carries the
            // bookingId, and on a rejection it carries courtscheduler's own explanation
            // ({"error":["..."]}) - the only thing that says WHY. Logging the status alone made a
            // 400 indistinguishable from a 500 or a version mismatch, and the public event
            // downstream only ever showed "has no bookingId". It also cannot be read twice:
            // EntityUtils.toString consumes the entity stream.
            //
            // getEntity() is null for a bodiless response (a gateway 502, a 204), and
            // EntityUtils.toString(null) throws IllegalArgumentException - unchecked, so it would
            // escape the IOException catch below and take the DLQ path the JSONException guard
            // further down exists to prevent.
            final String body = httpResponse.getEntity() == null
                    ? ""
                    : EntityUtils.toString(httpResponse.getEntity());

            if (isOkay(httpResponse)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("create unconfirmedBooking completed successfully");
                }
            } else {
                LOGGER.error("create unconfirmedBooking failed with status code:{} body:{}", status, body);
            }

            // courtscheduler answers a rejection with a JSON error body, but an infrastructure
            // failure (gateway, sidecar, empty 503) can answer with HTML or nothing at all.
            // JSONObject would throw JSONException there - unchecked, so it would escape the
            // IOException catch below, roll the JMS transaction back and, after the redelivery
            // attempts, drop the command on the DLQ with no public event at all. The clerk would
            // see the spinner never resolve. Treat an unparseable body as an ordinary error.
            final JSONObject responseJson;
            try {
                responseJson = new JSONObject(body);
            } catch (JSONException ex) {
                LOGGER.error("create unconfirmedBooking returned an unparseable body, status:{} body:{}", status, body, ex);
                // COURTSCHEDULER_ERROR whatever the status says, and deliberately NOT the
                // status-derived code failureCodeFor would give. courtscheduler's own answers
                // always carry a JSON body; a response that is not JSON came from something in
                // front of it (gateway, sidecar, error page), so the status is not courtscheduler
                // speaking and must not be read as if it were. A 409 is the case that matters:
                // read as NO_CAPACITY it would tell the clerk a session is full that may be free.
                return ProvisionalBookingServiceResponse.error(
                        ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR,
                        String.format("courtscheduler returned %d with an unparseable body: %s", status, body));
            }

            if (responseJson.has("bookingId")) {
                return ProvisionalBookingServiceResponse.normal(responseJson.getString("bookingId"));
            }
            return ProvisionalBookingServiceResponse.error(
                    failureCodeFor(status, body),
                    String.format("courtscheduler returned %d: %s", status, responseJson));
        } catch (IOException ex) {
            LOGGER.error("create unconfirmedBooking failed", ex);
            // The call never got an answer at all (connection refused, read timeout, DNS), so
            // courtscheduler's logic never ran, nothing was written and nothing was held.
            //
            // getMessage() is null for several IOException subclasses (a bare SocketException,
            // for one), and the failure event's reason is a required string - fall back to the
            // exception type rather than publishing a null or an empty reason.
            return ProvisionalBookingServiceResponse.error(
                    ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR,
                    StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
        }
    }

    /**
     * Maps courtscheduler's HTTP answer onto the code set the public failure event carries. See
     * {@link ProvisionalBookingFailureCode} for what each code means and
     * {@link #CONFIRMED_ALLOCATION_MARKER} for why the 409 branch has to read the message text.
     *
     * <p>Matched against the raw body rather than a parsed {@code error} field on purpose: the
     * handler's normalisation has had more than one shape over the migration ({@code "error"} as a
     * string, as a list, {@code "errors"} for 422), and a marker search over the whole body
     * survives all of them.
     *
     * <p>Only 409 and 422 are courtscheduler refusing the booking. Every other status is
     * {@link ProvisionalBookingFailureCode#COURTSCHEDULER_ERROR}; the status itself is kept in the
     * event's {@code reason}, so the logs still tell a 400 (our request) from a 503.
     *
     * <p>Called only for a body that PARSED as JSON: an unparseable body short-circuits to
     * {@link ProvisionalBookingFailureCode#COURTSCHEDULER_ERROR} at the call site. A 2xx that
     * carried no bookingId reaches the default branch here and lands on the same code.
     */
    private static ProvisionalBookingFailureCode failureCodeFor(final int status, final String body) {
        switch (status) {
            case HTTP_CONFLICT:
                return body != null && body.contains(CONFIRMED_ALLOCATION_MARKER)
                        ? ProvisionalBookingFailureCode.CONFIRMED_BOOKING_EXISTS
                        : ProvisionalBookingFailureCode.NO_CAPACITY;
            case HTTP_UNPROCESSABLE_ENTITY:
                return ProvisionalBookingFailureCode.NO_SESSION_AVAILABLE;
            default:
                return ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR;
        }
    }

    private boolean isOkay(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode();
    }

    /**
     * Releases the hold taken at slot-pick time. courtscheduler's DELETE performs the full
     * three-step release, restoring the session's capacity, and is a no-op when the booking has
     * no reservation — which is the normal case for a pre-go-live magistrates draft.
     */
    public void releaseSlots(final String bookingId) {
        final UUID systemUserId = systemUserProvider.getContextSystemUserId()
                .orElseThrow(() -> new IllegalStateException("contextSystemUserId missing!!!"));

        try {
            final HttpDelete httpDelete = new HttpDelete(new URL(baseUri + SESSIONS + bookingId).toString());
            httpDelete.addHeader(CJS_CPP_UID, systemUserId.toString());

            final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpDelete);
            final int status = httpResponse.getStatusLine().getStatusCode();
            if (status == Response.Status.ACCEPTED.getStatusCode() || status == Response.Status.OK.getStatusCode()) {
                LOGGER.info("released provisional booking {}", bookingId);
            } else {
                LOGGER.error("release of provisional booking {} failed with status {}", bookingId, status);
            }
        } catch (IOException ex) {
            LOGGER.error("release of provisional booking {} failed", bookingId, ex);
        }
    }
}