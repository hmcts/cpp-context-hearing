package uk.gov.moj.cpp.hearing.query.view;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingOutcomesConverter;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.query.view.service.HearingOutcomeService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

@ServiceComponent(Component.QUERY_VIEW)
@SuppressWarnings({"squid:S3655"})
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DATE = "date";

    @Inject
    private HearingService hearingService;

    @Inject
    private HearingOutcomeService hearingOutcomeService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private HearingOutcomesConverter hearingOutcomesConverter;

    private static final String FIELD_QUERY = "q";

    @Handles("hearing.get.hearings-by-date")
    public JsonEnvelope findHearingsByDateV2(final JsonEnvelope envelope) {
        final LocalDate date = LocalDates.from(envelope.payloadAsJsonObject().getString(FIELD_DATE));
        final HearingListResponse hearingListResponse = hearingService.getHearingByDateV2(date);
        return enveloper.withMetadataFrom(envelope, "hearing.get.hearings")
                .apply(hearingListResponse);
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingByIdV2(hearingId.get());
        return enveloper.withMetadataFrom(envelope, "hearing.get-hearing")
                .apply(hearingDetailsResponse);
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = UUID.fromString(envelope
                .payloadAsJsonObject()
                .getString(FIELD_HEARING_ID));
        final List<HearingOutcome> hearingOutcomes =
                hearingOutcomeService.getHearingOutcomeByHearingId(hearingId);
        return enveloper.withMetadataFrom(envelope, "hearing.get-draft-result-response").apply(hearingOutcomesConverter.convert(hearingOutcomes));
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
