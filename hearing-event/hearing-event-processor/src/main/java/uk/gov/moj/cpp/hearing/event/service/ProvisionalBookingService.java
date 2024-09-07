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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProvisionalBookingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisionalBookingService.class);
    private static final String SERVICE = "/provisionalBooking";

    public static final String COURTSCHEDULER_CREATE_PROVISIONAL_BOOKING = "application/vnd.courtscheduler.create.provisional.booking+json";
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
            LOGGER.info("ProvisionalBooking-bookSlots in S & L with payload '{}'", payload);
        }

        final UUID systemUserId = systemUserProvider.getContextSystemUserId().orElseThrow(() -> new IllegalStateException("contextSystemUserId missing!!!"));
        final String contextSystemUserId = systemUserId.toString();

        try {
            final HttpPost httpPost = new HttpPost(new URL(baseUri + SERVICE).toString());
            httpPost.addHeader(CONTENT_TYPE, COURTSCHEDULER_CREATE_PROVISIONAL_BOOKING);
            httpPost.addHeader(CJS_CPP_UID, contextSystemUserId);

            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString(payload));
            httpPost.setEntity(requestEntity);

            final HttpResponse httpResponse = HttpClientBuilder
                    .create()
                    .build()
                    .execute(httpPost);

            if (isOkay(httpResponse)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("create provisionalBooking completed successfully");
                }
            } else {
                LOGGER.error("create provisionalBooking failed with status code:{}", httpResponse.getStatusLine().getStatusCode());
            }

            final JSONObject responseJson = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            if (responseJson.has("bookingId")) {
                return ProvisionalBookingServiceResponse.normal(responseJson.getString("bookingId"));
            } else {
                return ProvisionalBookingServiceResponse.error(String.format("%s has no bookingId", responseJson));
            }
        } catch (IOException ex) {
            LOGGER.error("create provisionalBooking failed", ex);
            return ProvisionalBookingServiceResponse.error(ex.getMessage());
        }
    }

    private boolean isOkay(HttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode();
    }
}