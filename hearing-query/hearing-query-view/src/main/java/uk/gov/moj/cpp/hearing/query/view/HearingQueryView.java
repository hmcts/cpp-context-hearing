package uk.gov.moj.cpp.hearing.query.view;

import static java.time.LocalDate.now;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.domain.OutstandingFinesQuery;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.NoResultException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings({"squid:S3655"})
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_IDS = "courtCentreIds";
    private static final String DATE_OF_HEARING = "dateOfHearing";
    private static final String FIELD_COURT_ROOM_IDS = "courtRoomIds";
    private static final String FIELD_HEARING_DATE = "hearingDate";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";
    private static final String FIELD_QUERY = "q";
    private static final String FIELD_ID = "id";

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingQueryView.class);

    @Inject
    private HearingService hearingService;

    @Inject
    private DefendantRepository defendantRepository;
    @Inject
    private Enveloper enveloper;
    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private ObjectMapper mapper;
    @Inject
    private ReusableInfoService reusableInfoService;

    @Inject
    private CourtListRepository courtListRepository;

    public Envelope<GetHearings> findHearings(final JsonEnvelope envelope,
                                              final List<UUID> accessibleCasesId,
                                              final boolean isDDJ) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate date = LocalDates.from(payload.getString(FIELD_DATE));
        final UUID courtCentreId = UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID));
        final UUID roomId = payload.containsKey(FIELD_ROOM_ID) ? UUID.fromString(payload.getString(FIELD_ROOM_ID)) : null;
        final String startTime = payload.containsKey(FIELD_START_TIME) ? payload.getString(FIELD_START_TIME) : "00:00";
        final String endTime = payload.containsKey(FIELD_END_TIME) ? payload.getString(FIELD_END_TIME) : "23:59";

        final GetHearings hearingListResponse = hearingService.getHearings(date, startTime, endTime, courtCentreId, roomId, accessibleCasesId, isDDJ);
        return envelop(hearingListResponse)
                .withName("hearing.get.hearings")
                .withMetadataFrom(envelope);
    }

    @SuppressWarnings({"squid:S3655"})
    public Envelope<GetHearings> findHearingsForToday(final JsonEnvelope envelope) {
        final GetHearings hearingListResponse = hearingService.getHearingsForToday(now(), fromString(envelope.metadata().userId().get()));

        return envelop(hearingListResponse)
                .withName("hearing.get.hearings-for-today")
                .withMetadataFrom(envelope);
    }

    public Envelope<HearingDetailsResponse> findHearing(final JsonEnvelope envelope,
                                                        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes,
                                                        final List<UUID> accessibleCasesId,
                                                        final boolean isDDJ) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingDetailsResponseById(hearingId.get(), crackedIneffectiveVacatedTrialTypes, accessibleCasesId, isDDJ);

        return envelop(hearingDetailsResponse)
                .withName("hearing.get-hearing")
                .withMetadataFrom(envelope);
    }


    public Envelope<TargetListResponse> getDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final TargetListResponse targetListResponse = hearingService.getTargets(hearingId);

        return envelop(targetListResponse)
                .withName("hearing.get-draft-result")
                .withMetadataFrom(envelope);
    }

    public JsonEnvelope searchByMaterialId(final JsonEnvelope envelope) {
        return enveloper.withMetadataFrom(envelope, "hearing.query.search-by-material-id")
                .apply(hearingService.getNowsRepository(envelope.payloadAsJsonObject().getString(FIELD_QUERY)));
    }


    public JsonEnvelope retrieveSubscriptions(final JsonEnvelope envelope) {

        final String referenceDate = envelope.payloadAsJsonObject().getString("referenceDate");

        final String nowTypeId = envelope.payloadAsJsonObject().getString("nowTypeId");

        return enveloper.withMetadataFrom(envelope, "hearing.retrieve-subscriptions")
                .apply(hearingService.getSubscriptions(referenceDate, nowTypeId));
    }


    public Envelope<NowListResponse> findNows(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final Optional<Hearing> optionalHearing = hearingService.getHearingById(hearingId);

        if (!optionalHearing.isPresent()) {
            return envelop((NowListResponse) null)
                    .withName("hearing.get-nows")
                    .withMetadataFrom(envelope);
        }
        final NowListResponse nowListResponse = hearingService.getNows(hearingId);

        return envelop(nowListResponse)
                .withName("hearing.get-nows")
                .withMetadataFrom(envelope);
    }


    public Envelope<CrackedIneffectiveTrial> getCrackedIneffectiveTrialReason(final JsonEnvelope envelope, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes) {


        final Optional<UUID> trialTypeId = getUUID(envelope.payloadAsJsonObject(), "trialTypeId");
        return envelop(hearingService.fetchCrackedIneffectiveTrial(trialTypeId.get(), crackedIneffectiveVacatedTrialTypes))
                .withName("hearing.get-cracked-ineffective-reason")
                .withMetadataFrom(envelope);
    }


    public Envelope<Timeline> getTimeline(final JsonEnvelope envelope, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {
        final Optional<UUID> caseId = getUUID(envelope.payloadAsJsonObject(), FIELD_ID);

        final Timeline timeline = hearingService.getTimeLineByCaseId(caseId.get(), crackedIneffectiveVacatedTrialTypes, allCourtRooms);

        return envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(envelope);
    }


    public Envelope<Timeline> getTimelineByApplicationId(final JsonEnvelope envelope, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {
        final Optional<UUID> applicationId = getUUID(envelope.payloadAsJsonObject(), FIELD_ID);

        final Timeline timeline = hearingService.getTimeLineByApplicationId(applicationId.get(), crackedIneffectiveVacatedTrialTypes, allCourtRooms);

        return envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(envelope);
    }


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


    public JsonEnvelope getLatestHearingsByCourtCentres(final JsonEnvelope envelope,
                                                        final Set<UUID> cppHearingEventIds) {
        final Optional<String> courtCentreIds = getString(envelope.payloadAsJsonObject(), FIELD_COURT_CENTRE_IDS);
        final Optional<String> dateOfHearing = getString(envelope.payloadAsJsonObject(), DATE_OF_HEARING);

        final List<UUID> courtCentreList = Stream.of(courtCentreIds.get().split(",")).map(x -> fromString(x)).collect(Collectors.toList());

        final Optional<CurrentCourtStatus> currentCourtStatus = hearingService.getHearingsForWebPage(courtCentreList, LocalDate.parse(dateOfHearing.get()), cppHearingEventIds);

        return enveloper.withMetadataFrom(envelope, "hearing.get-latest-hearings-by-court-centres").apply(currentCourtStatus.isPresent() ? currentCourtStatus.get() : createObjectBuilder().build());
    }

    @SuppressWarnings({"squid:CallToDeprecatedMethod", "squid:CallToDeprecatedMethod"})
    public JsonEnvelope getHearingsForCourtCentresForDate(final JsonEnvelope envelope, final Set<UUID> cppHearingEventIds) {
        final Optional<String> courtCentreId = getString(envelope.payloadAsJsonObject(), FIELD_COURT_CENTRE_IDS);
        final Optional<String> dateOfHearing = getString(envelope.payloadAsJsonObject(), DATE_OF_HEARING);

        final List<UUID> courtCentreList = Stream.of(courtCentreId.get().split(",")).map(x -> fromString(x)).collect(Collectors.toList());

        final Optional<CurrentCourtStatus> currentCourtStatus = hearingService.getHearingsByDate(courtCentreList, LocalDate.parse(dateOfHearing.get()), cppHearingEventIds);

        return enveloper.withMetadataFrom(envelope, "hearing.hearings-court-centres-for-date").apply(currentCourtStatus.isPresent() ? currentCourtStatus.get() : createObjectBuilder().build());
    }

    @SuppressWarnings("squid:S1166")

    public JsonEnvelope getOutstandingFromDefendantId(final JsonEnvelope envelope) {
        final Optional<UUID> defendantId = getUUID(envelope.payloadAsJsonObject(), FIELD_DEFENDANT_ID);
        final JsonEnvelope jsonEnvelopeWithoutPayload = envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        if (defendantId.isPresent()) {
            try {
                final DefendantSearch defendantSearch = defendantRepository.getDefendantDetailsForSearching(defendantId.get());
                return envelopeFrom(envelope.metadata(), objectToJsonValueConverter.convert(defendantSearch));
            } catch (final NoResultException ex) {
                LOGGER.error(String.format("No defendant found with defendantId  ='%s'", defendantId.get()), ex);
                return jsonEnvelopeWithoutPayload;
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("No defendant id found in the payload");
        }
        return jsonEnvelopeWithoutPayload;
    }

    @SuppressWarnings("squid:S1166")
    public JsonEnvelope getDefendantInfoFromCourtHouseId(final JsonEnvelope envelope) {

        final JsonObject payload = envelope.payloadAsJsonObject();

        final OutstandingFinesQuery outstandingFinesQuery = OutstandingFinesQuery.newBuilder()
                .withCourtCentreId(UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID)))
                .withCourtRoomIds(Stream.of(payload.getString(FIELD_COURT_ROOM_IDS).split(","))
                        .map(UUID::fromString)
                        .collect(toList()))
                .withHearingDate(LocalDate.parse(payload.getString(FIELD_HEARING_DATE)))
                .build();

        try {
            final DefendantInfoQueryResult result = hearingService.getHearingsByCourtRoomList(outstandingFinesQuery.getHearingDate(), outstandingFinesQuery.getCourtCentreId(), outstandingFinesQuery.getCourtRoomIds());
            return envelopeFrom(envelope.metadata(), objectToJsonValueConverter.convert(result));


        } catch (final NoResultException nre) {
            LOGGER.error("### No defendant found with courtCentreId = '{}' , courtRoomIds = '{}' and hearingDate = '{}'", payload.getString(FIELD_COURT_CENTRE_ID), payload.getString(FIELD_COURT_ROOM_IDS), payload.getString(FIELD_HEARING_DATE));
            return envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        }
    }

    public JsonEnvelope getReusableInformation(final JsonEnvelope envelope, final List<Prompt> prompts, final Map<String, String> countryCodesMap) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingId = UUID.fromString(payload.getString(FIELD_HEARING_ID));
        final Optional<uk.gov.justice.core.courts.Hearing> hearingEntity = hearingService.getHearingDomainById(hearingId);

        if (!hearingEntity.isPresent()) {
            return envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        }

        final uk.gov.justice.core.courts.Hearing hearing = hearingEntity.get();
        final Map<UUID, Defendant> defendants = hearing.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .collect(toMap(Defendant::getMasterDefendantId, defendant -> defendant, (defendant1, defendant2) -> defendant1));

        final List<JsonObject> reusableCaseDetailPrompts = reusableInfoService.getCaseDetailReusableInformation(defendants.values(), prompts, countryCodesMap);
        final JsonObject reusableViewStorePrompts = reusableInfoService.getViewStoreReusableInformation(defendants.values(), reusableCaseDetailPrompts);
        return envelopeFrom(envelope.metadata(), reusableViewStorePrompts);
    }
}
