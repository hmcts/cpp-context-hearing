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

    private static final String DEFAULT_COURT_CENTRE_NAME = "Liverpool";
    private static final String DEFAULT_ROOM_NAME = "Room 1";

    private static final String RESPONSE_NAME_HEARINGS = "hearing.get.hearings";
    private static final String RESPONSE_NAME_HEARING = "hearing.get-hearing";

    private static final String FIELD_HEARINGS = "hearings";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_CASE_IDS = "caseIds";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";

    private static final String FIELD_CASE_ID = "caseId";

    @Inject
    private HearingService hearingService;

    @Inject
    private ProsecutionCounselService prosecutionCounselService;

    @Inject
    private HearingOutcomeService hearingOutcomeService;

    @Inject
    private DefenceCounselService defenceCounselService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Inject
    private ProsecutionCounselListConverter prosecutionCounselsListConverter;

    @Inject
    private HearingOutcomesConverter hearingOutcomesConverter;

    @Inject
    private DefenceCounselToDefendantMapConverter defenceCounselToDefendantMapConverter;

    @Handles("hearing.get.hearings-by-startdate")
    public JsonEnvelope findHearingsByStartDate(final JsonEnvelope query) {
        final LocalDate startDate = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_START_DATE));

        final List<Hearing> hearings = hearingRepository.findByStartDate(startDate);
        final JsonArrayBuilder hearingsJsonArrayBuilder = createArrayBuilder();

        if (!hearings.isEmpty()) {
            final List<UUID> hearingIds = hearings.stream().map(Hearing::getHearingId).collect(toList());
            final List<HearingCase> hearingCases = hearingCaseRepository.findByHearingIds(hearingIds);

            final Map<UUID, List<HearingCase>> hearingToCasesMap = hearingCases.stream().collect(groupingBy(HearingCase::getHearingId));


            hearings.forEach(hearing -> {
                final JsonArrayBuilder caseIdsJsonArrayBuilder = createArrayBuilder();
                hearingToCasesMap.get(hearing.getHearingId()).forEach(hearingCase ->
                        caseIdsJsonArrayBuilder.add(hearingCase.getCaseId().toString()));

                setHearingJsonArrayBuilder(hearingsJsonArrayBuilder, hearing, caseIdsJsonArrayBuilder);
            });
        }
        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARINGS)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARINGS, hearingsJsonArrayBuilder)
                        .build());
    }

    @Handles("hearing.get.hearings-by-startdate.v2")
    public JsonEnvelope findHearingsByStartDateV2(final JsonEnvelope envelope) {
        final LocalDate startDate = LocalDates.from(envelope.payloadAsJsonObject().getString(FIELD_START_DATE));
        final HearingListResponse hearingListResponse = hearingService.getHearingByStartDateV2(startDate);
        return enveloper.withMetadataFrom(envelope, "hearing.get.hearings.v2")
                .apply(hearingListResponse);
    }

    private void setHearingJsonArrayBuilder(final JsonArrayBuilder hearingsJsonArrayBuilder, final Hearing hearing, final JsonArrayBuilder caseIdsJsonArrayBuilder) {
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                .add(FIELD_HEARING_ID, hearing.getHearingId().toString())
                .add(FIELD_START_DATE, LocalDates.to(hearing.getStartDate()))
                .add(FIELD_START_TIME, hearing.getStartTime().format(ISO_TIME))
                .add(FIELD_HEARING_TYPE, hearing.getHearingType())
                .add(FIELD_DURATION, hearing.getDuration())
                .add(FIELD_COURT_CENTRE_NAME, hearing.getCourtCentreName() != null ? hearing.getCourtCentreName() : DEFAULT_COURT_CENTRE_NAME)
                .add(FIELD_ROOM_NAME, hearing.getRoomName() != null ? hearing.getRoomName() : DEFAULT_ROOM_NAME)
                .add(FIELD_CASE_IDS, caseIdsJsonArrayBuilder);
        if (hearing.getRoomId() != null) {
            jsonObjectBuilder.add(FIELD_ROOM_ID, hearing.getRoomId().toString());
        }
        if (hearing.getCourtCentreId() != null) {
            jsonObjectBuilder.add(FIELD_COURT_CENTRE_ID, hearing.getCourtCentreId().toString());
        }
        hearingsJsonArrayBuilder.add(jsonObjectBuilder);
    }

    @Handles("hearing.get.hearings-by-caseid")
    public JsonEnvelope findHearingsByCaseId(final JsonEnvelope query) {
        final UUID caseId = fromString(query.payloadAsJsonObject().getString(FIELD_CASE_ID));

        final List<HearingCase> hearingCases = hearingCaseRepository.findByCaseId(caseId);

        final List<UUID> hearingIds = hearingCases.stream().map(HearingCase::getHearingId).collect(toList());
        final List<Hearing> hearings = hearingRepository.findByHearingIds(hearingIds);

        final JsonArrayBuilder hearingsJsonArrayBuilder = createArrayBuilder();

        hearings.forEach(hearing -> setHearingJsonArrayBuilder(hearingsJsonArrayBuilder, hearing,
                createArrayBuilder().add(caseId.toString())));

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARINGS)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARINGS, hearingsJsonArrayBuilder)
                        .build());
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        return enveloper.withMetadataFrom(envelope, RESPONSE_NAME_HEARING)
                .apply(hearingService.getHearingById(hearingId.get()));
    }

    @Handles("hearing.get.hearing.v2")
    public JsonEnvelope findHearingV2(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingByIdV2(hearingId.get());
        return enveloper.withMetadataFrom(envelope, "hearing.get-hearing.v2")
                .apply(hearingDetailsResponse);
    }

    @Handles("hearing.get.prosecution-counsels")
    public JsonEnvelope getProsecutionCounsels(final JsonEnvelope query) {

        final UUID hearingId = fromString(query
                .payloadAsJsonObject()
                .getString(FIELD_HEARING_ID));

        final List<ProsecutionCounsel> prosecutionCounsels =
                prosecutionCounselService.getProsecutionCounselsByHearingId(hearingId);

        return enveloper.withMetadataFrom(query, "hearing.get.prosecution-counsels").apply(payloadFrom(prosecutionCounsels));
    }

    @Handles("hearing.get.defence-counsels")
    public JsonEnvelope getDefenceCounsels(final JsonEnvelope query) {

        final UUID hearingId = UUID.fromString(query
                .payloadAsJsonObject()
                .getString(FIELD_HEARING_ID));

        final List<DefenceCounsel> defenceCounsels =
                defenceCounselService.getDefenceCounselsByHearingId(hearingId);

        final Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselsAndDefendantsMap = new HashMap<>();

        for (final DefenceCounsel defenceCounsel : defenceCounsels) {
            defenceCounselsAndDefendantsMap.put(defenceCounsel,
                    defenceCounselService.getDefenceCounselDefendantsByDefenceCounselAttendeeId(defenceCounsel.getAttendeeId()));
        }

        return enveloper.withMetadataFrom(query, "hearing.get.defence-counsels").apply(payloadFrom(defenceCounselsAndDefendantsMap));
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

    private JsonObject payloadFrom(final List<ProsecutionCounsel> prosecutionCounsels) {
        return prosecutionCounselsListConverter.convert(prosecutionCounsels);
    }

    private JsonObject payloadFrom(final Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselsAndDefendantsMap) {
        return defenceCounselToDefendantMapConverter.convert(defenceCounselsAndDefendantsMap);
    }

}
