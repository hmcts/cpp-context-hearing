package uk.gov.moj.cpp.hearing.event.service;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
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
    private static final String RELEASE_REASON_DROPPED_FROM_DRAFT = "DROPPED_FROM_DRAFT";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisionalBookingService.class);
    private static final String SERVICE = "/unconfirmedBooking";
    private static final String RELEASE = "/release";

    public static final String COURTSCHEDULER_CREATE_UNCONFIRMED_BOOKING = "application/vnd.courtscheduler.create.unconfirmed.booking+json";
    public static final String COURTSCHEDULER_RELEASE_UNCONFIRMED_BOOKINGS = "application/vnd.courtscheduler.release.unconfirmed.bookings+json";
    public static final String CJS_CPP_UID = "CJSCPPUID";

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
                return ProvisionalBookingServiceResponse.error(
                        status,
                        String.format("courtscheduler returned %d with an unparseable body: %s", status, body));
            }

            if (responseJson.has("bookingId")) {
                return ProvisionalBookingServiceResponse.normal(responseJson.getString("bookingId"));
            }
            return ProvisionalBookingServiceResponse.error(
                    status,
                    String.format("courtscheduler returned %d: %s", status, responseJson));
        } catch (IOException ex) {
            LOGGER.error("create unconfirmedBooking failed", ex);
            // The call never got an answer at all (connection refused, read timeout, DNS), so
            // courtscheduler's logic never ran, nothing was written and nothing was held. There is
            // no status to report.
            //
            // getMessage() is null for several IOException subclasses (a bare SocketException,
            // for one), and the failure event's message is a required string - fall back to the
            // exception type rather than publishing a null or an empty message.
            return ProvisionalBookingServiceResponse.error(
                    null,
                    StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
        }
    }

    private boolean isOkay(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode();
    }

    /**
     * Gives back the unconfirmed holds taken under these bookingIds, in one call and one
     * courtscheduler transaction. courtscheduler releases only holds no share has confirmed, and
     * never anything keyed by hearing_id, so a booking already listed survives whatever is named
     * here. Unknown ids are no-ops.
     *
     * <p>Best effort: a failure is logged and swallowed. Rolling the event back would only retry a
     * release the overnight purge makes anyway, and would hold up the processor in the meantime.
     */
    public void releaseUnconfirmedBookings(final List<String> bookingIds) {
        if (bookingIds.isEmpty()) {
            return;
        }
        final UUID systemUserId = systemUserProvider.getContextSystemUserId()
                .orElseThrow(() -> new IllegalStateException("contextSystemUserId missing!!!"));

        try {
            final HttpPost httpPost = new HttpPost(new URL(baseUri + SERVICE + RELEASE).toString());
            httpPost.addHeader(CONTENT_TYPE, COURTSCHEDULER_RELEASE_UNCONFIRMED_BOOKINGS);
            httpPost.addHeader(CJS_CPP_UID, systemUserId.toString());
            // LPT-2611: the only caller is hearing's release on draft save, so every booking here was
            // dropped from the draft; courtscheduler records that reason in its booking log.
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(
                    Map.of("bookingIds", bookingIds, "reason", RELEASE_REASON_DROPPED_FROM_DRAFT))));

            final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
            final int status = httpResponse.getStatusLine().getStatusCode();
            if (status == Response.Status.ACCEPTED.getStatusCode()) {
                LOGGER.info("released unconfirmed bookings {}", bookingIds);
            } else {
                final String body = httpResponse.getEntity() == null ? "" : EntityUtils.toString(httpResponse.getEntity());
                LOGGER.error("release of unconfirmed bookings {} failed with status {} body {}", bookingIds, status, body);
            }
        } catch (IOException ex) {
            LOGGER.error("release of unconfirmed bookings {} failed", bookingIds, ex);
        }
    }
}