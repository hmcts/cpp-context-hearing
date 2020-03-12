package uk.gov.moj.cpp.hearing.query.view;

import static java.time.LocalDate.now;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(Component.QUERY_VIEW)
@SuppressWarnings({"squid:S3655"})
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";
    private static final String FIELD_QUERY = "q";
    private static final String FIELD_ID = "id";

    @Inject
    private HearingService hearingService;
    @Inject
    private Enveloper enveloper;

    @Handles("hearing.get.hearings")
    public Envelope<GetHearings> findHearings(final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate date = LocalDates.from(payload.getString(FIELD_DATE));
        final UUID courtCentreId = UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID));
        final UUID roomId = payload.containsKey(FIELD_ROOM_ID) ? UUID.fromString(payload.getString(FIELD_ROOM_ID)) : null;
        final String startTime = payload.containsKey(FIELD_START_TIME) ? payload.getString(FIELD_START_TIME) : "00:00";
        final String endTime = payload.containsKey(FIELD_END_TIME) ? payload.getString(FIELD_END_TIME) : "23:59";
        final GetHearings hearingListResponse = hearingService.getHearings(date, startTime, endTime, courtCentreId, roomId);

        return envelop(hearingListResponse)
                .withName("hearing.get.hearings")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.get.hearings-for-today")
    @SuppressWarnings({"squid:S3655"})
    public Envelope<GetHearings> findHearingsForToday(final JsonEnvelope envelope) {
        final GetHearings hearingListResponse = hearingService.getHearingsForToday(now(), fromString(envelope.metadata().userId().get()));

        return envelop(hearingListResponse)
                .withName("hearing.get.hearings-for-today")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.get.hearing")
    public Envelope<HearingDetailsResponse> findHearing(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingById(hearingId.get());

        return envelop(hearingDetailsResponse)
                .withName("hearing.get-hearing")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.get-draft-result")
    public Envelope<TargetListResponse> getDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final TargetListResponse targetListResponse = hearingService.getTargets(hearingId);

        return envelop(targetListResponse)
                .withName("hearing.get-draft-result")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.get-application-draft-result")
    public Envelope<ApplicationTargetListResponse> getApplicationDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final ApplicationTargetListResponse applicationTargetListResponse = hearingService.getApplicationTargets(hearingId);

        return envelop(applicationTargetListResponse)
                .withName("hearing.get-application-draft-result")
                .withMetadataFrom(envelope);
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

    @Handles("hearing.get.nows")
    public Envelope<NowListResponse> findNows(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final NowListResponse nowListResponse = hearingService.getNows(hearingId.get());

        return envelop(nowListResponse)
                .withName("hearing.get-nows")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.get-cracked-ineffective-reason")
    public Envelope<CrackedIneffectiveTrial> getCrackedIneffectiveTrialReason(final JsonEnvelope envelope) {


        final Optional<UUID> trialTypeId = getUUID(envelope.payloadAsJsonObject(), "trialTypeId");
        return envelop(hearingService.getCrackedIneffectiveTrial(trialTypeId.get()))
                .withName("hearing.get-cracked-ineffective-reason")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.case.timeline")
    public Envelope<Timeline> getTimeline(final JsonEnvelope envelope) {
        final Optional<UUID> caseId = getUUID(envelope.payloadAsJsonObject(), FIELD_ID);
        final Timeline timeline = hearingService.getTimeLineByCaseId(caseId.get());

        return envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(envelope);
    }

    @Handles("hearing.application.timeline")
    public Envelope<Timeline> getTimelineByApplicationId(final JsonEnvelope envelope) {
        final Optional<UUID> applicationId = getUUID(envelope.payloadAsJsonObject(), FIELD_ID);
        final Timeline timeline = hearingService.getTimeLineByApplicationId(applicationId.get());
        return envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(envelope);
    }
}
