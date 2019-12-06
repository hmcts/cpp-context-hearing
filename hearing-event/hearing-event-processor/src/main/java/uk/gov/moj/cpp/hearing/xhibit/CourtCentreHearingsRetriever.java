package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;

import java.time.ZonedDateTime;
import java.util.Optional;

import javax.inject.Inject;
import javax.json.JsonObject;

public class CourtCentreHearingsRetriever {

    private static final String HEARING_QUERY_GET_HEARINGS_BY_COURT_CENTRE = "hearing.get-hearings-by-court-centre";

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public Optional<CurrentCourtStatus> getHearingData(final String courtCentreId,
                                                       final ZonedDateTime latestCourtListUploadTime,
                                                       final JsonEnvelope envelope) {
        final JsonObject queryParameters = createObjectBuilder()
                .add("courtCentreId", courtCentreId)
                .add("lastModifiedTime", latestCourtListUploadTime.toString())
                .build();

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, HEARING_QUERY_GET_HEARINGS_BY_COURT_CENTRE).apply(queryParameters);

        final JsonEnvelope jsonEnvelope = requester.requestAsAdmin(requestEnvelope);

        if (!jsonEnvelope.payloadAsJsonObject().isEmpty()) {
            return of(jsonObjectToObjectConverter.convert(jsonEnvelope.payloadAsJsonObject(), CurrentCourtStatus.class));
        }
        return empty();
    }
}
