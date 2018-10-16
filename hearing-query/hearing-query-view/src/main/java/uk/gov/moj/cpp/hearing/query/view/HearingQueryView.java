package uk.gov.moj.cpp.hearing.query.view;

import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ServiceComponent(Component.QUERY_VIEW)
@SuppressWarnings({"squid:S3655"})
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";

    @Inject
    private HearingService hearingService;

    @Inject
    private Enveloper enveloper;

    private static final String FIELD_QUERY = "q";

    @Handles("hearing.get.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate date = LocalDates.from(payload.getString(FIELD_DATE));
        final UUID courtCentreId = UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID));
        final UUID roomId = UUID.fromString(payload.getString(FIELD_ROOM_ID));
        final String startTime = payload.containsKey(FIELD_START_TIME) ? payload.getString(FIELD_START_TIME) : "00:00";
        final String endTime = payload.containsKey(FIELD_END_TIME) ? payload.getString(FIELD_END_TIME) : "23:59";
        final HearingListResponse hearingListResponse = hearingService.getHearingByDateV2(date, startTime, endTime, courtCentreId, roomId);
        return enveloper.withMetadataFrom(envelope, "hearing.get.hearings")
                .apply(hearingListResponse);
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingById(hearingId.get());
        return enveloper.withMetadataFrom(envelope, "hearing.get-hearing")
                .apply(hearingDetailsResponse);
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final TargetListResponse targetListResponse = hearingService.getTargets(hearingId);
        return enveloper.withMetadataFrom(envelope, "hearing.get-draft-result").apply(targetListResponse);
    }

    @Handles("hearing.get.nows")
    public JsonEnvelope findNows(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final NowsResponse nowsResponse = hearingService.getNows(hearingId.get());
        return enveloper.withMetadataFrom(envelope, "hearing.get.nows")
                .apply(nowsResponse);
    }

    @Handles("hearing.query.search-by-material-id")
    public JsonEnvelope searchByMaterialId(final JsonEnvelope envelope) {
        return enveloper.withMetadataFrom(envelope, "hearing.query.search-by-material-id")
                .apply(hearingService.getNowsRepository(envelope.payloadAsJsonObject().getString(FIELD_QUERY)));
    }

    @Handles("hearing.retrieve-subscriptions")
    public JsonEnvelope retrieveSubscriptions(final JsonEnvelope envelope) {

        final String referenceDate = envelope.payloadAsJsonObject().getString("referenceDate");

        final String nowTypeId = envelope.payloadAsJsonObject().getString("nowTypeId");

        return enveloper.withMetadataFrom(envelope, "hearing.retrieve-subscriptions")
                .apply(hearingService.getSubscriptions(referenceDate, nowTypeId));
    }
}
