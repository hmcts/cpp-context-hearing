package uk.gov.moj.cpp.hearing.query.view;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@ServiceComponent(Component.QUERY_VIEW)
@SuppressWarnings({"squid:S3655"})
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_IDS = "courtCentreIds";
    private static final String DATE_OF_HEARING = "dateOfHearing";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";
    private static final String FIELD_QUERY = "q";
    @Inject
    private HearingService hearingService;
    @Inject
    private Enveloper enveloper;

    @Inject
    private CourtListRepository courtListRepository;

    @Handles("hearing.get.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate date = LocalDates.from(payload.getString(FIELD_DATE));
        final UUID courtCentreId = UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID));
        final UUID roomId = UUID.fromString(payload.getString(FIELD_ROOM_ID));
        final String startTime = payload.containsKey(FIELD_START_TIME) ? payload.getString(FIELD_START_TIME) : "00:00";
        final String endTime = payload.containsKey(FIELD_END_TIME) ? payload.getString(FIELD_END_TIME) : "23:59";
        final GetHearings hearingListResponse = hearingService.getHearings(date, startTime, endTime, courtCentreId, roomId);
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

    @Handles("hearing.get-application-draft-result")
    public JsonEnvelope getApplicationDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final ApplicationTargetListResponse applicationTargetListResponse = hearingService.getApplicationTargets(hearingId);
        return enveloper.withMetadataFrom(envelope, "hearing.get-application-draft-result").apply(applicationTargetListResponse);
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
    public JsonEnvelope findNows(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final NowListResponse nowListResponse = hearingService.getNows(hearingId.get());
        return enveloper.withMetadataFrom(envelope, "hearing.get-nows")
                .apply(nowListResponse);
    }

    @Handles("hearing.get-cracked-ineffective-reason")
    public JsonEnvelope getCrackedIneffectiveTrialReason(final JsonEnvelope envelope) {

        final Optional<UUID> trialTypeId = getUUID(envelope.payloadAsJsonObject(), "trialTypeId");

        return enveloper.withMetadataFrom(envelope, "hearing.get-cracked-ineffective-reason")
                .apply(hearingService.getCrackedIneffectiveTrial(trialTypeId.get()));
    }

    @Handles("hearing.court.list.publish.status")
    public JsonEnvelope getCourtListPublishStatus(final JsonEnvelope query) {
        final String courtCentreId = query.payloadAsJsonObject().getString(FIELD_COURT_CENTRE_ID);

        final Optional<CourtListPublishStatusResult> publishCourtListStatus = courtListRepository.courtListPublishStatuses(fromString(courtCentreId));

        final JsonObjectBuilder builder = createObjectBuilder();
        if (publishCourtListStatus.isPresent()) {
            builder.add("courtCentreId", publishCourtListStatus.get().getCourtCentreId().toString())
                    .add("lastUpdated", publishCourtListStatus.get().getLastUpdated().toString())
                    .add("publishStatus", publishCourtListStatus.get().getPublishStatus().toString())
                    .add("errorMessage", defaultIfEmpty(publishCourtListStatus.get().getFailureMessage(), ""));
        }
        return enveloper.withMetadataFrom(query, "hearing.court.list.publish.status").apply(createObjectBuilder().add("publishCourtListStatus", builder.build()).build());
    }


    @Handles("hearing.latest-hearings-by-court-centres")
    public JsonEnvelope getLatestHearingsByCourtCentres(final JsonEnvelope envelope) {
        final Optional<String> courtCentreIds = getString(envelope.payloadAsJsonObject(), FIELD_COURT_CENTRE_IDS);
        final Optional<String> dateOfHearing = getString(envelope.payloadAsJsonObject(), DATE_OF_HEARING);

        final List<UUID> courtCentreList = Stream.of(courtCentreIds.get().split(",")).map(x -> fromString(x)).collect(Collectors.toList());

        final Optional<CurrentCourtStatus> currentCourtStatus = hearingService.getLatestHearings(courtCentreList,  LocalDate.parse(dateOfHearing.get()));

        return enveloper.withMetadataFrom(envelope, "hearing.get-latest-hearings-by-court-centres").apply(currentCourtStatus.isPresent() ? currentCourtStatus.get() : createObjectBuilder().build());
    }

    @SuppressWarnings({"squid:CallToDeprecatedMethod", "squid:CallToDeprecatedMethod"})
    @Handles("hearing.hearings-court-centres-for-date")
    public JsonEnvelope getHearingsForCourtCentresForDate(final JsonEnvelope envelope) {
        final Optional<String> courtCentreId = getString(envelope.payloadAsJsonObject(), FIELD_COURT_CENTRE_IDS);
        final Optional<String> dateOfHearing = getString(envelope.payloadAsJsonObject(), DATE_OF_HEARING);

        final List<UUID> courtCentreList = Stream.of(courtCentreId.get().split(",")).map(x -> fromString(x)).collect(Collectors.toList());

        final Optional<CurrentCourtStatus> currentCourtStatus = hearingService.getHearingsByDate(courtCentreList, LocalDate.parse(dateOfHearing.get()));

        return enveloper.withMetadataFrom(envelope, "hearing.hearings-court-centres-for-date").apply(currentCourtStatus.isPresent() ? currentCourtStatus.get() : createObjectBuilder().build());
    }
}

