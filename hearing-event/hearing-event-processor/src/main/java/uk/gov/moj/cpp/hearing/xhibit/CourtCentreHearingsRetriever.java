package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

public class CourtCentreHearingsRetriever {

    private static final String HEARING_QUERY_LATEST_HEARING_BY_COURT_CENTRES = "hearing.latest-hearings-by-court-centres";
    private static final String HEARING_QUERY_GET_HEARINGS_FOR_COURT_CENTRES_FOR_DATE = "hearing.hearings-court-centres-for-date";

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public Optional<CurrentCourtStatus> getHearingDataForWebPage(final String courtCentreIds,
                                                       final ZonedDateTime latestCourtListUploadTime,
                                                       final JsonEnvelope envelope) {
        final JsonObject queryParameters = createObjectBuilder()
                .add("courtCentreIds", String.join(",", courtCentreIds))
                .add("dateOfHearing", latestCourtListUploadTime.toLocalDate().toString())
                .build();

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, HEARING_QUERY_LATEST_HEARING_BY_COURT_CENTRES).apply(queryParameters);

        final JsonEnvelope jsonEnvelope = requester.requestAsAdmin(requestEnvelope);

        if (!jsonEnvelope.payloadAsJsonObject().isEmpty()) {
            return of(jsonObjectToObjectConverter.convert(jsonEnvelope.payloadAsJsonObject(), CurrentCourtStatus.class));
        }
        return empty();
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public Optional<CurrentCourtStatus> getHearingDataForPublicDisplay(final String courtCentreId,
                                                       final ZonedDateTime latestCourtListUploadTime,
                                                       final JsonEnvelope envelope) {



        final String crownCourtCrestId =  commonXhibitReferenceDataService.getCourtDetails(fromString(courtCentreId)).getCrestCourtId();

        final List<String> courtCentreIds = commonXhibitReferenceDataService.getCourtCentreIdsForCrestId(crownCourtCrestId)
                .stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        final JsonObject queryParameters = createObjectBuilder()
                .add("courtCentreIds", String.join(",", courtCentreIds))
                .add("dateOfHearing", latestCourtListUploadTime.toLocalDate().toString())
                .build();

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, HEARING_QUERY_GET_HEARINGS_FOR_COURT_CENTRES_FOR_DATE).apply(queryParameters);

        final JsonEnvelope jsonEnvelope = requester.requestAsAdmin(requestEnvelope);

        if (!jsonEnvelope.payloadAsJsonObject().isEmpty()) {
            return of(jsonObjectToObjectConverter.convert(jsonEnvelope.payloadAsJsonObject(), CurrentCourtStatus.class));
        }
        return empty();
    }
}
