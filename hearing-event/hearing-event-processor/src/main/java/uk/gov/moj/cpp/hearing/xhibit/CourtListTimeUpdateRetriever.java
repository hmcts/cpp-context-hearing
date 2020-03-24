package uk.gov.moj.cpp.hearing.xhibit;


import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.parse;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class CourtListTimeUpdateRetriever {

    private static final String COURT_LIST_LIST_PUBLISH_STATUS = "hearing.court.list.publish.status";
    private static final String CREATED_TIME = "createdTime";

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    public ZonedDateTime getLatestCourtListUploadTime(final JsonEnvelope envelope, final String courtCentreId) {

        final JsonObject queryParameters = createObjectBuilder()
                .add("courtCentreId", courtCentreId)
                .build();

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, COURT_LIST_LIST_PUBLISH_STATUS).apply(queryParameters);

        final JsonEnvelope responseEnvelope = requester.requestAsAdmin(requestEnvelope);

        final String latestUploadTime = getTime(responseEnvelope);

        if (latestUploadTime.isEmpty()) {
            return now().minusMinutes(10l);
        }
        return parse(latestUploadTime);
    }


    private String getTime(final JsonEnvelope jsonEnvelope) {
        final JsonObject jsonObject = jsonEnvelope.payloadAsJsonObject();

        final JsonObject publishCourtListStatus = jsonObject.getJsonObject("publishCourtListStatus");

        if (!publishCourtListStatus.isEmpty()) {
            return publishCourtListStatus.getString(CREATED_TIME, EMPTY);
        }
        return EMPTY;
    }
}

