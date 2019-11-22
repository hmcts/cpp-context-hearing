package uk.gov.moj.cpp.hearing.xhibit;


import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.parse;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

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

        final JsonArray publishCourtListStatuses = jsonObject.getJsonArray("publishCourtListStatuses");

        if (!publishCourtListStatuses.isEmpty()) {
            final Optional<JsonValue> successStatus = getPublishStatus(publishCourtListStatuses, EXPORT_SUCCESSFUL);
            final Optional<JsonValue> failedStatus = getPublishStatus(publishCourtListStatuses, EXPORT_FAILED);

            if (successStatus.isPresent()) {
                return ((JsonObject) successStatus.get()).getString(CREATED_TIME);
            }
            if (failedStatus.isPresent()) {
                return ((JsonObject) failedStatus.get()).getString(CREATED_TIME);
            }
        }
        return EMPTY;
    }

    //TODO: we need to make sure we retrieve the latest success/fail status, and probably compare s<-->f, but this could be fixed from the query.
    private Optional<JsonValue> getPublishStatus(final JsonArray publishCourtListStatuses, final PublishStatus publishStatus) {
        return publishCourtListStatuses
                .stream()
                .filter(status -> ((JsonObject) status).getString("publishStatus").equals(publishStatus.name()))
                .findFirst();
    }

}
