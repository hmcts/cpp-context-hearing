package uk.gov.moj.cpp.hearing.query.view;

import static java.time.format.DateTimeFormatter.ISO_TIME;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.convertor.DefenceCounselToDefendantMapConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingOutcomesConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ProsecutionCounselListConverter;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.service.DefenceCounselService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingOutcomeService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ProsecutionCounselService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@ServiceComponent(Component.QUERY_VIEW)
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_START_DATE = "startDate";

    @Inject
    private HearingService hearingService;

    @Inject
    private HearingOutcomeService hearingOutcomeService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private HearingOutcomesConverter hearingOutcomesConverter;

    @Handles("hearing.get.hearings-by-startdate.v2")
    public JsonEnvelope findHearingsByStartDateV2(final JsonEnvelope envelope) {
        final LocalDate startDate = LocalDates.from(envelope.payloadAsJsonObject().getString(FIELD_START_DATE));
        final HearingListResponse hearingListResponse = hearingService.getHearingByStartDateV2(startDate);
        return enveloper.withMetadataFrom(envelope, "hearing.get.hearings.v2")
                .apply(hearingListResponse);
    }

    @Handles("hearing.get.hearing.v2")
    public JsonEnvelope findHearingV2(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingByIdV2(hearingId.get());
        return enveloper.withMetadataFrom(envelope, "hearing.get-hearing.v2")
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
}
