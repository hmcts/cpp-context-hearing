package uk.gov.moj.cpp.hearing.event.service;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                        String.format("courtscheduler returned %d with an unparseable body: %s", status, body));
            }

            if (responseJson.has("bookingId")) {
                return ProvisionalBookingServiceResponse.normal(responseJson.getString("bookingId"));
            }
            return ProvisionalBookingServiceResponse.error(
                    String.format("courtscheduler returned %d: %s", status, responseJson));
        } catch (IOException ex) {
            LOGGER.error("create unconfirmedBooking failed", ex);
            return ProvisionalBookingServiceResponse.error(ex.getMessage());
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